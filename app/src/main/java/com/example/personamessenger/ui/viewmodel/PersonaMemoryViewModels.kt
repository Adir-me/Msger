package com.example.personamessenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personamessenger.data.local.entity.ContextEntity
import com.example.personamessenger.data.local.entity.MemoryEntity
import com.example.personamessenger.data.local.entity.PersonaEntity
import com.example.personamessenger.data.repository.ContextRepository
import com.example.personamessenger.data.repository.MemoryRepository
import com.example.personamessenger.data.repository.PersonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PersonaViewModel(
    private val personaRepository: PersonaRepository
) : ViewModel() {

    val allPersonas: StateFlow<List<PersonaEntity>> = personaRepository.allPersonas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPersonaId = MutableStateFlow<Long>(1)
    val selectedPersonaId: StateFlow<Long> = _selectedPersonaId.asStateFlow()

    val currentPersona: StateFlow<PersonaEntity?> = _selectedPersonaId
        .flatMapLatest { id -> personaRepository.getPersonaFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectPersona(id: Long) {
        _selectedPersonaId.value = id
    }

    fun savePersona(persona: PersonaEntity) {
        viewModelScope.launch {
            if (persona.id == 0L) {
                val newId = personaRepository.savePersona(persona)
                _selectedPersonaId.value = newId
            } else {
                personaRepository.updatePersona(persona)
            }
        }
    }

    fun deletePersona(id: Long) {
        viewModelScope.launch {
            personaRepository.deletePersona(id)
            _selectedPersonaId.value = 1
        }
    }
}

class MemoryViewModel(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _selectedPersonaId = MutableStateFlow<Long>(1)
    private val _selectedCategory = MutableStateFlow<String>("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val memories: StateFlow<List<MemoryEntity>> = _selectedPersonaId
        .flatMapLatest { pid ->
            if (_searchQuery.value.isNotBlank()) {
                memoryRepository.searchMemories(pid, _searchQuery.value)
            } else if (_selectedCategory.value != "all") {
                memoryRepository.getMemoriesByCategory(pid, _selectedCategory.value)
            } else {
                memoryRepository.getMemoriesForPersona(pid)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setPersonaId(personaId: Long) {
        _selectedPersonaId.value = personaId
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            if (memory.id == 0L) {
                memoryRepository.insertMemory(memory)
            } else {
                memoryRepository.updateMemory(memory)
            }
        }
    }

    fun togglePin(memory: MemoryEntity) {
        viewModelScope.launch {
            memoryRepository.updateMemory(memory.copy(isPinned = !memory.isPinned))
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(id)
        }
    }
}

class ContextViewModel(
    private val contextRepository: ContextRepository
) : ViewModel() {

    private val _selectedPersonaId = MutableStateFlow<Long>(1)

    val activeContext: StateFlow<ContextEntity?> = _selectedPersonaId
        .flatMapLatest { pid -> contextRepository.getActiveContextFlow(pid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setPersonaId(personaId: Long) {
        _selectedPersonaId.value = personaId
    }

    fun saveContext(context: ContextEntity) {
        viewModelScope.launch {
            if (context.id == 0L) {
                contextRepository.saveContext(context)
            } else {
                contextRepository.updateContext(context)
            }
        }
    }
}
