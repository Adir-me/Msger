package com.example.personamessenger.data.repository

import com.example.personamessenger.data.local.dao.ContextDao
import com.example.personamessenger.data.local.dao.MemoryDao
import com.example.personamessenger.data.local.dao.PersonaDao
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.PersonaEntity
import kotlinx.coroutines.flow.Flow

class PersonaRepository(private val personaDao: PersonaDao) {
    val allPersonas: Flow<List<PersonaEntity>> = personaDao.getAllPersonas()

    fun getPersonaFlow(id: Long): Flow<PersonaEntity?> = personaDao.getPersonaByIdFlow(id)
    suspend fun getPersona(id: Long): PersonaEntity? = personaDao.getPersonaById(id)
    suspend fun getDefaultPersona(): PersonaEntity? = personaDao.getDefaultPersona()
    suspend fun savePersona(persona: PersonaEntity): Long = personaDao.insertPersona(persona)
    suspend fun updatePersona(persona: PersonaEntity) = personaDao.updatePersona(persona)
    suspend fun deletePersona(id: Long) = personaDao.deletePersona(id)
}

class MemoryRepository(private val memoryDao: MemoryDao) {
    fun getMemoriesForPersona(personaId: Long): Flow<List<MemoryEntity>> =
        memoryDao.getMemoriesForPersona(personaId)

    suspend fun getActiveMemories(personaId: Long): List<MemoryEntity> =
        memoryDao.getActiveMemoriesForPersona(personaId)

    fun searchMemories(personaId: Long, query: String): Flow<List<MemoryEntity>> =
        memoryDao.searchMemories(personaId, query)

    fun getMemoriesByCategory(personaId: Long, category: String): Flow<List<MemoryEntity>> =
        memoryDao.getMemoriesByCategory(personaId, category)

    suspend fun insertMemory(memory: MemoryEntity): Long = memoryDao.insertMemory(memory)
    suspend fun insertMemories(memories: List<MemoryEntity>) = memoryDao.insertMemories(memories)
    suspend fun updateMemory(memory: MemoryEntity) = memoryDao.updateMemory(memory)
    suspend fun deleteMemory(id: Long) = memoryDao.deleteMemory(id)
    suspend fun purgeExpired(): Int = memoryDao.purgeExpiredMemories()
}

class ContextRepository(private val contextDao: ContextDao) {
    fun getContextsForPersona(personaId: Long): Flow<List<ContextEntity>> =
        contextDao.getContextsForPersona(personaId)

    fun getActiveContextFlow(personaId: Long): Flow<ContextEntity?> =
        contextDao.getActiveContextFlow(personaId)

    suspend fun getActiveContext(personaId: Long): ContextEntity? =
        contextDao.getActiveContext(personaId)

    suspend fun saveContext(context: ContextEntity): Long = contextDao.insertContext(context)
    suspend fun updateContext(context: ContextEntity) = contextDao.updateContext(context)
    suspend fun deleteContext(id: Long) = contextDao.deleteContext(id)
}
