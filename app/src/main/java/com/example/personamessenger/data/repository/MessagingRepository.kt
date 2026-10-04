package com.example.personamessenger.data.repository

import com.example.personamessenger.data.local.dao.AuditLogDao
import com.example.personamessenger.data.local.dao.ContactDao
import com.example.personamessenger.data.local.dao.ContextDao
import com.example.personamessenger.data.local.dao.ConversationDao
import com.example.personamessenger.data.local.dao.MemoryDao
import com.example.personamessenger.data.local.dao.MessageDao
import com.example.personamessenger.data.local.dao.ModelConfigDao
import com.example.personamessenger.data.local.dao.PersonaDao
import com.example.personamessenger.data.local.dao.ScheduledActionDao
import com.example.personamessenger.data.local.entity.AuditLogEntity
import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.ConversationEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.data.local.entity.ModelConfigEntity
import com.example.personamessenger.data.local.entity.PersonaEntity
import com.example.personamessenger.data.local.entity.ScheduledActionEntity
import com.example.personamessenger.domain.ai.AIProvider
import com.example.personamessenger.domain.ai.AIRequest
import com.example.personamessenger.domain.ai.AIResponse
import com.example.personamessenger.domain.ai.OperationalDebugInfo
import com.example.personamessenger.domain.engine.MemoryRetrievalEngine
import com.example.personamessenger.domain.engine.ProactiveScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MessagingRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val contactDao: ContactDao,
    private val personaDao: PersonaDao,
    private val memoryDao: MemoryDao,
    private val contextDao: ContextDao,
    private val modelConfigDao: ModelConfigDao,
    private val scheduledActionDao: ScheduledActionDao,
    private val auditLogDao: AuditLogDao,
    private val aiProvider: AIProvider,
    private val memoryRetrievalEngine: MemoryRetrievalEngine = MemoryRetrievalEngine(),
    private val proactiveScheduler: ProactiveScheduler = ProactiveScheduler(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    val scheduledActions: Flow<List<ScheduledActionEntity>> = scheduledActionDao.getAllScheduledActions()
    val auditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs()

    // Typing state flow for UI animation
    private val _isTypingMap = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val isTypingMap = _isTypingMap.asStateFlow()

    // Last debug info for developer/inspect panel
    private val _lastDebugInfo = MutableStateFlow<OperationalDebugInfo?>(null)
    val lastDebugInfo = _lastDebugInfo.asStateFlow()

    fun getConversationMessages(conversationId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    fun getConversationFlow(conversationId: Long): Flow<ConversationEntity?> =
        conversationDao.getConversationByIdFlow(conversationId)

    fun getAllContacts(): Flow<List<ContactEntity>> = contactDao.getAllContacts()

    fun getContactFlow(contactId: Long): Flow<ContactEntity?> = contactDao.getContactByIdFlow(contactId)

    suspend fun createConversation(title: String, platform: String): Long {
        val contact = ContactEntity(
            name = title,
            username = title.lowercase().replace(" ", "_"),
            platform = platform,
            isAiEnabled = true,
            isAuthorizedAccount = true
        )
        val contactId = contactDao.insertContact(contact)
        val conversation = ConversationEntity(
            contactId = contactId,
            platform = platform,
            title = title,
            isAiActive = true,
            lastMessageSnippet = "Chat created",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        return conversationDao.insertConversation(conversation)
    }

    suspend fun toggleAiForConversation(conversationId: Long, isActive: Boolean) {
        conversationDao.setAiActiveForConversation(conversationId, isActive)
        auditLogDao.insertLog(
            AuditLogEntity(
                actionCategory = "CONVERSATION_CONTROL",
                contactName = "Conv #$conversationId",
                details = "AI automation set to $isActive for conversation",
                safetyVerified = true
            )
        )
    }

    suspend fun toggleAiForContact(contactId: Long, enabled: Boolean) {
        contactDao.setAiEnabledForContact(contactId, enabled)
    }

    suspend fun updateContact(contact: ContactEntity) {
        contactDao.updateContact(contact)
    }

    /**
     * Handles manual human response sent by user (Human Override).
     * The AI recognizes this as part of conversation history.
     */
    suspend fun sendManualMessage(conversationId: Long, text: String) {
        val message = MessageEntity(
            conversationId = conversationId,
            senderId = "me",
            senderName = "You",
            isFromMe = true,
            isAiGenerated = false,
            isManualOverride = true,
            eventType = "text",
            contentText = text,
            deliveryStatus = "delivered",
            operationalReason = "Manual Human Override Response",
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, text, System.currentTimeMillis())

        auditLogDao.insertLog(
            AuditLogEntity(
                actionCategory = "MANUAL_OVERRIDE",
                contactName = "Conv #$conversationId",
                details = "User took over chat and sent manual message: '$text'",
                safetyVerified = true
            )
        )
    }

    /**
     * Processes an incoming event (e.g. from Simulator or social platform webhook).
     * Runs full pipeline:
     * Event Normalizer -> State -> Memory Retrieval -> AI Decision -> Timing -> Scheduling / Delivery.
     */
    suspend fun processIncomingEvent(incoming: MessageEntity): AIResponse? {
        // 1. Duplicate Prevention / Idempotency
        val existing = messageDao.getMessageByEventId(incoming.eventId)
        if (existing != null && existing.id != incoming.id) {
            return null // Already processed
        }

        // 2. Insert incoming event
        messageDao.insertMessage(incoming)

        val conversation = conversationDao.getConversationById(incoming.conversationId) ?: return null
        val contact = contactDao.getContactById(conversation.contactId) ?: return null
        val modelConfig = modelConfigDao.getPrimaryConfig() ?: ModelConfigEntity()

        // Update conversation snippet
        val snippet = when (incoming.eventType) {
            "reaction" -> "Reacted ${incoming.reactionEmoji} to message"
            "reel" -> "Sent a Reel"
            "voice" -> "Sent a voice note (${incoming.durationSeconds}s)"
            "image" -> "Sent a photo"
            "sticker" -> "Sent a sticker"
            "gif" -> "Sent a GIF"
            else -> incoming.contentText
        }
        conversationDao.updateLastMessage(conversation.id, snippet, incoming.timestamp)

        // 3. Safety & Authorization validation
        val validation = proactiveScheduler.validateMessageAttempt(
            contact = contact,
            modelConfig = modelConfig,
            isProactive = false,
            recentMessagesInLastHourCount = 2
        )

        if (!validation.isAllowed || !conversation.isAiActive) {
            auditLogDao.insertLog(
                AuditLogEntity(
                    actionCategory = "AI_RESPONSE_SUPPRESSED",
                    contactName = contact.name,
                    details = "AI response withheld: ${validation.reason} (Conv AI Active: ${conversation.isAiActive})",
                    safetyVerified = true
                )
            )
            return null
        }

        // 4. Retrieve Persona & High-Priority Context
        val persona = personaDao.getPersonaById(contact.assignedPersonaId)
            ?: personaDao.getDefaultPersona()
            ?: PersonaEntity(name = "Default Persona", personalityDescription = "Helpful friend")

        val currentContext = contextDao.getActiveContext(persona.id)

        // 5. Retrieve Relevant Memories (Filtered & Scored)
        val allMemories = memoryDao.getActiveMemoriesForPersona(persona.id)
        val relevantMemories = memoryRetrievalEngine.retrieveRelevantMemories(
            incomingMessage = incoming,
            allMemories = allMemories,
            currentContext = currentContext,
            limit = 6
        )

        // 6. Retrieve Recent History Window
        val history = messageDao.getRecentMessagesForConversation(
            conversation.id,
            limit = conversation.recentMemoryWindowSize
        ).reversed()

        // 7. Request AI Decision & Multiple Messages Generation
        val aiRequest = AIRequest(
            persona = persona,
            relevantMemories = relevantMemories,
            currentContext = currentContext,
            recentHistory = history,
            incomingMessage = incoming,
            contact = contact,
            modelConfig = modelConfig
        )

        val aiResponse = aiProvider.generateResponse(aiRequest)
        _lastDebugInfo.value = aiResponse.debugInfo

        // 8. Handle Extracted Memories
        if (aiResponse.extractedMemories.isNotEmpty()) {
            val newMemories = aiResponse.extractedMemories.map { mem ->
                MemoryEntity(
                    personaId = persona.id,
                    category = mem.category,
                    content = mem.content,
                    importance = mem.importance,
                    source = "extracted",
                    tags = mem.tags,
                    expirationTimestamp = if (mem.isTemporary && mem.ttlMinutes != null)
                        System.currentTimeMillis() + (mem.ttlMinutes * 60 * 1000L) else null
                )
            }
            memoryDao.insertMemories(newMemories)
        }

        // 9. If AI decided to respond, schedule and deliver with typing simulation
        if (aiResponse.shouldRespond && aiResponse.messages.isNotEmpty()) {
            scope.launch {
                deliverScheduledMessagesWithTyping(
                    conversationId = conversation.id,
                    contactName = contact.name,
                    personaName = persona.name,
                    messages = aiResponse.messages,
                    operationalReason = aiResponse.operationalReason,
                    modelName = aiResponse.debugInfo?.selectedModel ?: modelConfig.defaultModel
                )
            }
        }

        return aiResponse
    }

    private suspend fun deliverScheduledMessagesWithTyping(
        conversationId: Long,
        contactName: String,
        personaName: String,
        messages: List<com.example.personamessenger.domain.ai.ScheduledMessage>,
        operationalReason: String,
        modelName: String
    ) {
        messages.forEachIndexed { index, scheduledMsg ->
            // Indicate typing
            _isTypingMap.value = _isTypingMap.value + (conversationId to true)
            delay(scheduledMsg.typingDurationMs)

            // Stop typing and deliver
            _isTypingMap.value = _isTypingMap.value + (conversationId to false)

            val outMessage = MessageEntity(
                conversationId = conversationId,
                senderId = "me",
                senderName = personaName,
                isFromMe = true,
                isAiGenerated = true,
                isManualOverride = false,
                eventType = "text",
                contentText = scheduledMsg.text,
                deliveryStatus = "delivered",
                operationalReason = operationalReason,
                confidence = 0.95f,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(outMessage)
            conversationDao.updateLastMessage(conversationId, scheduledMsg.text, System.currentTimeMillis())

            auditLogDao.insertLog(
                AuditLogEntity(
                    actionCategory = "AI_RESPONSE",
                    contactName = contactName,
                    details = "Sent AI message (${index + 1}/${messages.size}): '${scheduledMsg.text}'",
                    modelUsed = modelName,
                    safetyVerified = true
                )
            )

            // Short pause before next message in multi-message sequence
            if (index < messages.size - 1) {
                delay(scheduledMsg.delayMs)
            }
        }
    }

    /**
     * Executes proactive authorized check-in.
     */
    suspend fun scheduleProactiveMessage(
        conversationId: Long,
        contactId: Long,
        promptObjective: String,
        delayMinutes: Int = 15
    ) {
        val contact = contactDao.getContactById(contactId) ?: return
        val persona = personaDao.getPersonaById(contact.assignedPersonaId) ?: return
        val context = contextDao.getActiveContext(persona.id)
        val memories = memoryDao.getActiveMemoriesForPersona(persona.id)

        val proposal = aiProvider.generateProactiveMessage(contact, persona, context, memories)

        val action = ScheduledActionEntity(
            conversationId = conversationId,
            contactId = contactId,
            actionType = "follow_up",
            promptObjective = promptObjective,
            proposedText = proposal.proposedText,
            targetSendTimeEpoch = System.currentTimeMillis() + (delayMinutes * 60 * 1000L),
            status = "pending"
        )
        scheduledActionDao.insertAction(action)

        auditLogDao.insertLog(
            AuditLogEntity(
                actionCategory = "PROACTIVE_SCHEDULED",
                contactName = contact.name,
                details = "Scheduled proactive check-in in $delayMinutes min: '${proposal.proposedText}'",
                safetyVerified = true
            )
        )
    }

    suspend fun cancelScheduledAction(actionId: String) {
        scheduledActionDao.cancelAction(actionId, "User manually cancelled via Dashboard")
    }

    suspend fun executePendingActionNow(action: ScheduledActionEntity) {
        val outMessage = MessageEntity(
            conversationId = action.conversationId,
            senderId = "me",
            senderName = "AI Assistant",
            isFromMe = true,
            isAiGenerated = true,
            eventType = "text",
            contentText = action.proposedText,
            deliveryStatus = "delivered",
            operationalReason = "Executed scheduled proactive message: ${action.promptObjective}",
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(outMessage)
        conversationDao.updateLastMessage(action.conversationId, action.proposedText, System.currentTimeMillis())
        scheduledActionDao.updateAction(action.copy(status = "executed"))
    }
}
