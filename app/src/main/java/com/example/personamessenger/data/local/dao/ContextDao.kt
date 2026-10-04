package com.example.personamessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personamessenger.data.local.entity.ContextEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContextDao {
    @Query("SELECT * FROM contexts WHERE personaId = :personaId ORDER BY updatedAt DESC")
    fun getContextsForPersona(personaId: Long): Flow<List<ContextEntity>>

    @Query("SELECT * FROM contexts WHERE personaId = :personaId AND isActive = 1 LIMIT 1")
    fun getActiveContextFlow(personaId: Long): Flow<ContextEntity?>

    @Query("SELECT * FROM contexts WHERE personaId = :personaId AND isActive = 1 LIMIT 1")
    suspend fun getActiveContext(personaId: Long): ContextEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: ContextEntity): Long

    @Update
    suspend fun updateContext(context: ContextEntity)

    @Query("DELETE FROM contexts WHERE id = :id")
    suspend fun deleteContext(id: Long)
}
