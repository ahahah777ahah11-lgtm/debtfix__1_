package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MessageTone

@Entity(tableName = "message_templates")
data class MessageTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val tone: MessageTone = MessageTone.FRIENDLY,
    val language: String = "en", // "en" or "ar"
    val templateContent: String,
    val isDefault: Boolean = false
)
