package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DebtCategory
import com.example.data.model.DebtType

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val phoneNumber: String = "",
    val type: DebtType = DebtType.THEY_OWE_ME,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val currency: String = "$",
    val dueDateMillis: Long,
    val createdDateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val category: DebtCategory = DebtCategory.PERSONAL,
    val isSettled: Boolean = false,
    val lastRemindedMillis: Long = 0L,
    val reminderCount: Int = 0
) {
    val remainingAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)

    val isOverdue: Boolean
        get() = !isSettled && dueDateMillis < System.currentTimeMillis()
}
