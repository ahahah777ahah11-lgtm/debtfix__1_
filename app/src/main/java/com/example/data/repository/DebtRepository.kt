package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.DebtEntity
import com.example.data.local.MessageTemplateEntity
import com.example.data.local.PaymentEntity
import com.example.data.model.DebtCategory
import com.example.data.model.DebtType
import com.example.data.model.MessageTone
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class DebtRepository(private val database: AppDatabase) {

    private val debtDao = database.debtDao()
    private val paymentDao = database.paymentDao()
    private val templateDao = database.messageTemplateDao()

    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebtsFlow()
    val allTemplates: Flow<List<MessageTemplateEntity>> = templateDao.getAllTemplatesFlow()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPaymentsFlow()

    fun getPaymentsForDebt(debtId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForDebtFlow(debtId)

    suspend fun insertDebt(debt: DebtEntity): Long {
        return debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        paymentDao.deletePaymentsForDebt(debt.id)
        debtDao.deleteDebt(debt)
    }

    suspend fun recordPayment(debt: DebtEntity, paidAmountToAdd: Double, paymentNotes: String) {
        val newPaid = debt.paidAmount + paidAmountToAdd
        val isSettled = newPaid >= debt.totalAmount
        debtDao.updateDebtPayment(debt.id, newPaid, isSettled)

        val payment = PaymentEntity(
            debtId = debt.id,
            amount = paidAmountToAdd,
            dateMillis = System.currentTimeMillis(),
            notes = paymentNotes
        )
        paymentDao.insertPayment(payment)
    }

    suspend fun logReminderSent(debtId: Long) {
        debtDao.incrementReminder(debtId, System.currentTimeMillis())
    }

    suspend fun insertTemplate(template: MessageTemplateEntity): Long {
        return templateDao.insertTemplate(template)
    }

    suspend fun updateTemplate(template: MessageTemplateEntity) {
        templateDao.updateTemplate(template)
    }

    suspend fun deleteTemplate(template: MessageTemplateEntity) {
        templateDao.deleteTemplate(template)
    }

    suspend fun resetTemplatesToDefault() {
        templateDao.clearAll()
        templateDao.insertAll(AppDatabase.defaultTemplates)
    }

    suspend fun clearAll() {
        debtDao.clearAll()
        paymentDao.clearAll()
    }

    suspend fun populateSampleData() {
        val now = System.currentTimeMillis()
        val oneDay = TimeUnit.DAYS.toMillis(1)

        val sampleDebts = listOf(
            DebtEntity(
                personName = "Ahmed Al-Mansoor (أحمد المنصور)",
                phoneNumber = "+966501234567",
                type = DebtType.THEY_OWE_ME,
                totalAmount = 2500.0,
                paidAmount = 500.0,
                currency = "SAR",
                dueDateMillis = now - (3 * oneDay), // Overdue by 3 days
                category = DebtCategory.BUSINESS,
                notes = "Supply invoice #1042 - pending final installment",
                isSettled = false,
                reminderCount = 2,
                lastRemindedMillis = now - (2 * oneDay)
            ),
            DebtEntity(
                personName = "Sarah Jenkins",
                phoneNumber = "+14155552671",
                type = DebtType.THEY_OWE_ME,
                totalAmount = 450.0,
                paidAmount = 0.0,
                currency = "$",
                dueDateMillis = now + (2 * oneDay), // Due in 2 days
                category = DebtCategory.PERSONAL,
                notes = "Weekend road trip shared hotel expenses",
                isSettled = false,
                reminderCount = 0
            ),
            DebtEntity(
                personName = "Khaled Ibrahim (خالد إبراهيم)",
                phoneNumber = "+971509876543",
                type = DebtType.THEY_OWE_ME,
                totalAmount = 8000.0,
                paidAmount = 2000.0,
                currency = "AED",
                dueDateMillis = now + (14 * oneDay),
                category = DebtCategory.LOAN,
                notes = "Personal emergency loan - monthly plan",
                isSettled = false,
                reminderCount = 1,
                lastRemindedMillis = now - (5 * oneDay)
            ),
            DebtEntity(
                personName = "Apex Office Solutions",
                phoneNumber = "+18005550199",
                type = DebtType.I_OWE_THEM,
                totalAmount = 650.0,
                paidAmount = 0.0,
                currency = "$",
                dueDateMillis = now + (7 * oneDay),
                category = DebtCategory.RENT,
                notes = "Monthly co-working space invoice",
                isSettled = false
            ),
            DebtEntity(
                personName = "Omar Al-Sayed (عمر السيد)",
                phoneNumber = "+201012345678",
                type = DebtType.THEY_OWE_ME,
                totalAmount = 15000.0,
                paidAmount = 15000.0,
                currency = "EGP",
                dueDateMillis = now - (10 * oneDay),
                category = DebtCategory.INVOICE,
                notes = "Design project delivery fee",
                isSettled = true,
                reminderCount = 3
            )
        )

        debtDao.insertAll(sampleDebts)

        if (templateDao.getCount() == 0) {
            templateDao.insertAll(AppDatabase.defaultTemplates)
        }
    }
}
