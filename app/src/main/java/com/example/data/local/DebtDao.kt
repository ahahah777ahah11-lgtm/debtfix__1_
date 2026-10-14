package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DebtType
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, dueDateMillis ASC")
    fun getAllDebtsFlow(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Query("SELECT * FROM debts WHERE type = :type ORDER BY isSettled ASC, dueDateMillis ASC")
    fun getDebtsByTypeFlow(type: DebtType): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(debts: List<DebtEntity>)

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("UPDATE debts SET paidAmount = :newPaid, isSettled = :settled WHERE id = :debtId")
    suspend fun updateDebtPayment(debtId: Long, newPaid: Double, settled: Boolean)

    @Query("UPDATE debts SET lastRemindedMillis = :time, reminderCount = reminderCount + 1 WHERE id = :debtId")
    suspend fun incrementReminder(debtId: Long, time: Long)

    @Query("DELETE FROM debts")
    suspend fun clearAll()
}
