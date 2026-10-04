package com.example.personamessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personamessenger.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY lastActiveTimestamp DESC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isAiEnabled = 1")
    fun getAiEnabledContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    fun getContactByIdFlow(id: Long): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE username = :username LIMIT 1")
    suspend fun getContactByUsername(username: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isAiEnabled = :enabled WHERE id = :id")
    suspend fun setAiEnabledForContact(id: Long, enabled: Boolean)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: Long)
}
