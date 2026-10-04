package com.example.personamessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personamessenger.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE personaId = :personaId ORDER BY isPinned DESC, importance DESC, updatedAt DESC")
    fun getMemoriesForPersona(personaId: Long): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE personaId = :personaId AND (expirationTimestamp IS NULL OR expirationTimestamp > :now) ORDER BY isPinned DESC, importance DESC")
    suspend fun getActiveMemoriesForPersona(personaId: Long, now: Long = System.currentTimeMillis()): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE personaId = :personaId AND category = :category ORDER BY isPinned DESC, importance DESC")
    fun getMemoriesByCategory(personaId: Long, category: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE personaId = :personaId AND (content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY isPinned DESC, importance DESC")
    fun searchMemories(personaId: Long, query: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryEntity>)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM memories WHERE expirationTimestamp IS NOT NULL AND expirationTimestamp <= :now")
    suspend fun purgeExpiredMemories(now: Long = System.currentTimeMillis()): Int
}
