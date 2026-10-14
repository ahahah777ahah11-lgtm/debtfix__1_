package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageTemplateDao {
    @Query("SELECT * FROM message_templates ORDER BY id ASC")
    fun getAllTemplatesFlow(): Flow<List<MessageTemplateEntity>>

    @Query("SELECT * FROM message_templates WHERE language = :lang ORDER BY id ASC")
    fun getTemplatesByLanguageFlow(lang: String): Flow<List<MessageTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: MessageTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<MessageTemplateEntity>)

    @Update
    suspend fun updateTemplate(template: MessageTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: MessageTemplateEntity)

    @Query("SELECT COUNT(*) FROM message_templates")
    suspend fun getCount(): Int

    @Query("DELETE FROM message_templates")
    suspend fun clearAll()
}
