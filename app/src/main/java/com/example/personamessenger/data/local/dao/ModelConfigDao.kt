package com.example.personamessenger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personamessenger.data.local.entity.ModelConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelConfigDao {
    @Query("SELECT * FROM model_configs WHERE id = 'primary_config' LIMIT 1")
    fun getPrimaryConfigFlow(): Flow<ModelConfigEntity?>

    @Query("SELECT * FROM model_configs WHERE id = 'primary_config' LIMIT 1")
    suspend fun getPrimaryConfig(): ModelConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ModelConfigEntity)

    @Update
    suspend fun updateConfig(config: ModelConfigEntity)

    @Query("UPDATE model_configs SET isGlobalAutomationActive = :isActive WHERE id = 'primary_config'")
    suspend fun setGlobalAutomationState(isActive: Boolean)
}
