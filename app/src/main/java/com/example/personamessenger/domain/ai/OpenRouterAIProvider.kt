package com.example.personamessenger.domain.ai

import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.MessageEntity
import com.example.personamessenger.data.local.entity.PersonaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenRouterAIProvider(
    private val localFallbackEngine: EngineAIProvider = EngineAIProvider()
) : AIProvider {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun generateResponse(request: AIRequest): AIResponse {
        val apiKey = request.modelConfig.openRouterApiKey.trim()

        // If no API key is set, use the intelligent local engine
        if (apiKey.isBlank()) {
            return localFallbackEngine.generateResponse(request)
        }

        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val selectedModel = if (request.modelConfig.isManualModelActive && request.modelConfig.manualModelId.isNotBlank()) {
                request.modelConfig.manualModelId.trim()
            } else {
                request.modelConfig.defaultModel.trim()
            }

            try {
                val promptPayload = buildPromptPayload(request, selectedModel)
                val responseJson = callOpenRouterApi(apiKey, promptPayload)

                val parsedResponse = parseModelResponse(responseJson, request, selectedModel, startTime)
                parsedResponse ?: localFallbackEngine.generateResponse(request)
            } catch (e: Exception) {
                // Try fallback model if available
                if (request.modelConfig.fallbackModel.isNotBlank() && request.modelConfig.fallbackModel != selectedModel) {
                    try {
                        val fallbackPayload = buildPromptPayload(request, request.modelConfig.fallbackModel.trim())
                        val fallbackResponseJson = callOpenRouterApi(apiKey, fallbackPayload)
                        val parsed = parseModelResponse(fallbackResponseJson, request, request.modelConfig.fallbackModel, startTime)
                        if (parsed != null) return@withContext parsed
                    } catch (fallbackError: Exception) {
                        // Fall through to local engine
                    }
                }
                localFallbackEngine.generateResponse(request)
            }
        }
    }

    override suspend fun generateMultipleMessages(request: AIRequest): List<ScheduledMessage> {
        val response = generateResponse(request)
        return response.messages
    }

    override suspend fun analyzeIncomingEvent(message: MessageEntity): EventClassification {
        return localFallbackEngine.analyzeIncomingEvent(message)
    }

    override suspend fun summarizeConversation(messages: List<MessageEntity>): String {
        return localFallbackEngine.summarizeConversation(messages)
    }

    override suspend fun generateProactiveMessage(
        contact: ContactEntity,
        persona: PersonaEntity,
        context: ContextEntity?,
        memories: List<MemoryEntity>
    ): ProactiveProposal {
        return localFallbackEngine.generateProactiveMessage(contact, persona, context, memories)
    }

    override suspend fun analyzeVoiceContext(
        transcript: String,
        durationSeconds: Int,
        availableAudios: List<AudioAssetEntity>
    ): VoiceInterpretation {
        return localFallbackEngine.analyzeVoiceContext(transcript, durationSeconds, availableAudios)
    }

    private fun buildPromptPayload(request: AIRequest, modelName: String): String {
        val p = request.persona
        val c = request.currentContext
        val mems = request.relevantMemories
        val history = request.recentHistory

        val systemPrompt = """
You are simulating a natural, authorized persona in a private chat:
- Name: ${p.name}
- Age: ${p.ageRange}
- Personality: ${p.personalityDescription}
- Interests: ${p.interests}
- Communication Style: ${p.communicationStyle}
- Humor: ${p.humorStyle}
- Vocabulary: ${p.vocabulary}
- Favorite Expressions: ${p.favoriteExpressions}
- Language: ${p.languageStyle} (Code-switching: ${p.codeSwitchingBehavior})
- Emoji Frequency: ${p.emojiFrequency} (Preferred: ${p.preferredEmojis}, Avoided: ${p.avoidedEmojis})
- Punctuation Habits: ${p.punctuationHabits}
- Known Topics: ${p.thingsKnown}
- Unknown Topics: ${p.thingsUnknown}

[CURRENT CONTEXT - HIGHEST PRIORITY OVER OLD MEMORIES]:
- Situation: ${c?.whatHappenedToday ?: "Normal day"}
- Plans: ${c?.currentPlans ?: "None"}
- Emotional State: ${c?.temporaryEmotionalState ?: "Good"}
- Things to know: ${c?.immediateThingsToKnow ?: "None"}

[RELEVANT RETRIEVED MEMORIES]:
${if (mems.isEmpty()) "None retrieved." else mems.joinToString("\n") { "- [${it.category}] ${it.content}" }}

[SAFETY & RULES]:
1. Be genuine to this persona. Never say "As an AI language model".
2. Split responses into 1-3 natural short messages when appropriate.
3. Distinguish between actual text and social events (e.g. reels, reactions, voice notes).

OUTPUT STRICT JSON ONLY:
{
  "shouldRespond": true,
  "messages": [
    {"text": "...", "delayMs": 1800, "typingDurationMs": 1200}
  ],
  "operationalReason": "Short reason for reply style",
  "newTopic": "...",
  "extractedMemories": [
    {"category": "preference", "content": "...", "importance": 3}
  ]
}
""".trimIndent()

        val messagesArray = JSONArray()

        val systemMsg = JSONObject()
        systemMsg.put("role", "system")
        systemMsg.put("content", systemPrompt)
        messagesArray.put(systemMsg)

        // Recent conversation history
        history.takeLast(request.conversationWindowSize()).forEach { m ->
            val msgObj = JSONObject()
            msgObj.put("role", if (m.isFromMe) "assistant" else "user")
            val content = if (m.eventType == "text") {
                m.contentText
            } else {
                "[Event: ${m.eventType}] ${m.contentText} ${m.mediaDescription} ${m.transcript} ${m.reactionEmoji}"
            }
            msgObj.put("content", content)
            messagesArray.put(msgObj)
        }

        // Current incoming message
        val currentEventObj = JSONObject()
        currentEventObj.put("role", "user")
        val eventContent = if (request.incomingMessage.eventType == "text") {
            request.incomingMessage.contentText
        } else {
            "[Event: ${request.incomingMessage.eventType}] ${request.incomingMessage.contentText} ${request.incomingMessage.mediaDescription} ${request.incomingMessage.transcript} ${request.incomingMessage.reactionEmoji}"
        }
        currentEventObj.put("content", eventContent)
        messagesArray.put(currentEventObj)

        val root = JSONObject()
        root.put("model", modelName)
        root.put("messages", messagesArray)
        root.put("temperature", request.modelConfig.temperature.toDouble())
        root.put("max_tokens", request.modelConfig.maxTokens)

        val formatObj = JSONObject()
        formatObj.put("type", "json_object")
        root.put("response_format", formatObj)

        return root.toString()
    }

    private fun callOpenRouterApi(apiKey: String, payloadJson: String): String {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payloadJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://personamessenger.aistudio.app")
            .addHeader("X-Title", "Persona Messenger")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw RuntimeException("OpenRouter API error code: ${response.code}, body: $responseBody")
        }

        return responseBody
    }

    private fun parseModelResponse(
        responseJson: String,
        request: AIRequest,
        modelUsed: String,
        startTime: Long
    ): AIResponse? {
        try {
            val root = JSONObject(responseJson)
            val choices = root.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null

            val firstChoice = choices.getJSONObject(0)
            val messageObj = firstChoice.optJSONObject("message") ?: return null
            val rawContent = messageObj.optString("content", "")

            val jsonContent = JSONObject(rawContent)
            val shouldRespond = jsonContent.optBoolean("shouldRespond", true)
            val operationalReason = jsonContent.optString("operationalReason", "AI response generated via OpenRouter")
            val newTopic = jsonContent.optString("newTopic", "")

            val messagesList = mutableListOf<ScheduledMessage>()
            val msgsArray = jsonContent.optJSONArray("messages")
            if (msgsArray != null) {
                for (i in 0 until msgsArray.length()) {
                    val m = msgsArray.getJSONObject(i)
                    val text = m.optString("text", "")
                    val delay = m.optLong("delayMs", 1800L)
                    val typing = m.optLong("typingDurationMs", 1200L)
                    if (text.isNotBlank()) {
                        messagesList.add(ScheduledMessage(text, delay, typing))
                    }
                }
            }

            val extractedMems = mutableListOf<MemoryToSave>()
            val memArray = jsonContent.optJSONArray("extractedMemories")
            if (memArray != null) {
                for (i in 0 until memArray.length()) {
                    val mem = memArray.getJSONObject(i)
                    extractedMems.add(
                        MemoryToSave(
                            category = mem.optString("category", "fact"),
                            content = mem.optString("content", ""),
                            importance = mem.optInt("importance", 3)
                        )
                    )
                }
            }

            val latency = System.currentTimeMillis() - startTime

            val debugInfo = OperationalDebugInfo(
                eventType = request.incomingMessage.eventType,
                normalizedEvent = request.incomingMessage.contentText,
                selectedModel = modelUsed,
                retrievedMemoriesCount = request.relevantMemories.size,
                retrievedMemoriesSummary = request.relevantMemories.map { it.content },
                contextPriorityState = request.currentContext?.title ?: "Default",
                responseDecision = operationalReason,
                generatedMessages = messagesList.map { it.text },
                timingDelays = messagesList.map { it.delayMs },
                memoryUpdates = extractedMems.map { it.content },
                latencyMs = latency,
                validationPassed = true
            )

            return AIResponse(
                shouldRespond = shouldRespond,
                messages = messagesList,
                confidence = 0.95f,
                operationalReason = operationalReason,
                extractedMemories = extractedMems,
                newTopic = newTopic.ifBlank { null },
                newSentiment = "Natural",
                newMomentum = "Active",
                isRecipientWaitingForAnswer = false,
                debugInfo = debugInfo
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun AIRequest.conversationWindowSize(): Int {
        return 20
    }
}
