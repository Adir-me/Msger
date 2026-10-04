package com.example.personamessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personamessenger.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun getConversationByIdFlow(id: Long): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE contactId = :contactId LIMIT 1")
    suspend fun getConversationForContact(contactId: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET isAiActive = :isActive WHERE id = :id")
    suspend fun setAiActiveForConversation(id: Long, isActive: Boolean)

    @Query("UPDATE conversations SET lastMessageSnippet = :snippet, lastMessageTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastMessage(id: Long, snippet: String, timestamp: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)
}
