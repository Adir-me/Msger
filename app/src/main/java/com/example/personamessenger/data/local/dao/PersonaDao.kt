package com.example.personamessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personamessenger.data.local.entity.PersonaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {
    @Query("SELECT * FROM personas ORDER BY isDefault DESC, updatedAt DESC")
    fun getAllPersonas(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    fun getPersonaByIdFlow(id: Long): Flow<PersonaEntity?>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    suspend fun getPersonaById(id: Long): PersonaEntity?

    @Query("SELECT * FROM personas WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultPersona(): PersonaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaEntity): Long

    @Update
    suspend fun updatePersona(persona: PersonaEntity)

    @Query("DELETE FROM personas WHERE id = :id")
    suspend fun deletePersona(id: Long)
}
