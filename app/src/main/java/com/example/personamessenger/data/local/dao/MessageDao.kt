package com.example.personamessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personamessenger.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessagesForConversation(conversationId: Long, limit: Int = 20): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE eventId = :eventId LIMIT 1")
    suspend fun getMessageByEventId(eventId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE deliveryStatus = 'scheduled' ORDER BY scheduledSendTimeEpoch ASC")
    fun getScheduledMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE deliveryStatus = 'scheduled' AND scheduledSendTimeEpoch <= :now ORDER BY scheduledSendTimeEpoch ASC")
    suspend fun getPendingScheduledMessages(now: Long = System.currentTimeMillis()): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET deliveryStatus = :status WHERE id = :id")
    suspend fun updateDeliveryStatus(id: String, status: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearMessagesForConversation(conversationId: Long)
}
