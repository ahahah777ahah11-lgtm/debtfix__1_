package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DebtEntity
import com.example.data.local.MessageTemplateEntity
import com.example.data.model.DebtType
import com.example.data.repository.DebtRepository
import com.example.util.MessengerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DebtFilter(val labelEn: String, val labelAr: String) {
    ALL("All", "الكل"),
    THEY_OWE_ME("They Owe Me", "مدينون لي"),
    I_OWE_THEM("I Owe", "عليّ دين"),
    OVERDUE("Overdue", "متأخرة"),
    SETTLED("Settled", "مسددة")
}

data class FinancialStats(
    val totalReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val netBalance: Double = 0.0,
    val totalCollected: Double = 0.0,
    val overdueCount: Int = 0,
    val activeCount: Int = 0,
    val settledCount: Int = 0
)

class DebtViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DebtRepository

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(DebtFilter.ALL)
    val defaultCurrency = MutableStateFlow("$")

    // Messenger active drafting state
    val selectedDebtForReminder = MutableStateFlow<DebtEntity?>(null)
    val selectedTemplate = MutableStateFlow<MessageTemplateEntity?>(null)
    val previewMessage = MutableStateFlow("")

    val allDebts: StateFlow<List<DebtEntity>>
    val allTemplates: StateFlow<List<MessageTemplateEntity>>

    val filteredDebts: StateFlow<List<DebtEntity>>
    val financialStats: StateFlow<FinancialStats>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = DebtRepository(database)

        allDebts = repository.allDebts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allTemplates = repository.allTemplates.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        filteredDebts = combine(allDebts, searchQuery, selectedFilter) { debts, query, filter ->
            debts.filter { debt ->
                val matchesQuery = query.isBlank() ||
                        debt.personName.contains(query, ignoreCase = true) ||
                        debt.phoneNumber.contains(query, ignoreCase = true) ||
                        debt.notes.contains(query, ignoreCase = true)

                val matchesFilter = when (filter) {
                    DebtFilter.ALL -> true
                    DebtFilter.THEY_OWE_ME -> debt.type == DebtType.THEY_OWE_ME && !debt.isSettled
                    DebtFilter.I_OWE_THEM -> debt.type == DebtType.I_OWE_THEM && !debt.isSettled
                    DebtFilter.OVERDUE -> debt.isOverdue
                    DebtFilter.SETTLED -> debt.isSettled
                }

                matchesQuery && matchesFilter
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        financialStats = allDebts.combine(selectedFilter) { debts, _ ->
            var receivable = 0.0
            var payable = 0.0
            var collected = 0.0
            var overdue = 0
            var active = 0
            var settled = 0

            for (debt in debts) {
                if (debt.type == DebtType.THEY_OWE_ME) {
                    receivable += debt.remainingAmount
                    collected += debt.paidAmount
                } else {
                    payable += debt.remainingAmount
                }

                if (debt.isSettled) {
                    settled++
                } else {
                    active++
                    if (debt.isOverdue) overdue++
                }
            }

            FinancialStats(
                totalReceivable = receivable,
                totalPayable = payable,
                netBalance = receivable - payable,
                totalCollected = collected,
                overdueCount = overdue,
                activeCount = active,
                settledCount = settled
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            FinancialStats()
        )

        // Prepopulate demo data on first launch if empty
        viewModelScope.launch {
            allDebts.collect { list ->
                if (list.isEmpty()) {
                    repository.populateSampleData()
                }
            }
        }
    }

    fun selectDebtForReminder(debt: DebtEntity?) {
        selectedDebtForReminder.value = debt
        regenerateMessage()
    }

    fun selectTemplate(template: MessageTemplateEntity?) {
        selectedTemplate.value = template
        regenerateMessage()
    }

    fun updatePreviewMessage(customText: String) {
        previewMessage.value = customText
    }

    private fun regenerateMessage() {
        val debt = selectedDebtForReminder.value
        val template = selectedTemplate.value
        if (debt != null && template != null) {
            val isArabic = template.language == "ar"
            previewMessage.value = MessengerHelper.formatMessage(
                template = template.templateContent,
                debt = debt,
                isArabic = isArabic
            )
        } else if (debt != null) {
            val templateText = "Hi {name}, just a friendly reminder regarding your outstanding balance of {remaining} {currency} ({days_status}). Thank you!"
            previewMessage.value = MessengerHelper.formatMessage(templateText, debt)
        } else {
            previewMessage.value = ""
        }
    }

    fun saveDebt(debt: DebtEntity) {
        viewModelScope.launch {
            if (debt.id == 0L) {
                repository.insertDebt(debt)
            } else {
                repository.updateDebt(debt)
            }
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
            if (selectedDebtForReminder.value?.id == debt.id) {
                selectedDebtForReminder.value = null
                previewMessage.value = ""
            }
        }
    }

    fun recordPayment(debt: DebtEntity, paidAmount: Double, notes: String) {
        viewModelScope.launch {
            repository.recordPayment(debt, paidAmount, notes)
            // If this debt is currently selected in messenger, update it
            if (selectedDebtForReminder.value?.id == debt.id) {
                val updated = debt.copy(
                    paidAmount = debt.paidAmount + paidAmount,
                    isSettled = (debt.paidAmount + paidAmount) >= debt.totalAmount
                )
                selectedDebtForReminder.value = updated
                regenerateMessage()
            }
        }
    }

    fun logReminderSent(debt: DebtEntity) {
        viewModelScope.launch {
            repository.logReminderSent(debt.id)
        }
    }

    fun saveTemplate(template: MessageTemplateEntity) {
        viewModelScope.launch {
            if (template.id == 0L) {
                repository.insertTemplate(template)
            } else {
                repository.updateTemplate(template)
            }
        }
    }

    fun deleteTemplate(template: MessageTemplateEntity) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
            if (selectedTemplate.value?.id == template.id) {
                selectedTemplate.value = null
                regenerateMessage()
            }
        }
    }

    fun resetTemplatesToDefault() {
        viewModelScope.launch {
            repository.resetTemplatesToDefault()
        }
    }

    fun populateSampleData() {
        viewModelScope.launch {
            repository.populateSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            selectedDebtForReminder.value = null
            previewMessage.value = ""
        }
    }
}
