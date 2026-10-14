package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DebtEntity
import com.example.ui.components.AddEditDebtSheet
import com.example.ui.components.RecordPaymentDialog
import com.example.viewmodel.DebtViewModel

enum class AppTab(val label: String, val icon: ImageVector) {
    DEBTS("Ledger", Icons.Default.AccountBalanceWallet),
    MESSENGER("Messenger", Icons.Default.Chat),
    TEMPLATES("Templates", Icons.Default.Description),
    ANALYTICS("Analytics", Icons.Default.BarChart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: DebtViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppTab.DEBTS) }

    // Dialog & Sheet States
    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingDebt by remember { mutableStateOf<DebtEntity?>(null) }
    var paymentDebt by remember { mutableStateOf<DebtEntity?>(null) }

    val defaultCurrency by viewModel.defaultCurrency.collectAsState()
    var showCurrencyMenu by remember { mutableStateOf(false) }

    // BackHandler: if not on DEBTS, go back to DEBTS
    BackHandler(enabled = currentTab != AppTab.DEBTS) {
        currentTab = AppTab.DEBTS
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Smart Debt Messenger",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { showCurrencyMenu = true }
                        ) {
                            Text(
                                text = "Currency: $defaultCurrency ▼",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showCurrencyMenu,
                            onDismissRequest = { showCurrencyMenu = false }
                        ) {
                            listOf("$", "SAR", "AED", "EGP", "EUR", "KWD", "QAR", "GBP").forEach { cur ->
                                DropdownMenuItem(
                                    text = { Text(cur, fontWeight = if (cur == defaultCurrency) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.defaultCurrency.value = cur
                                        showCurrencyMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 12.sp, fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DEBTS -> DebtsScreen(
                    viewModel = viewModel,
                    onAddNewDebt = {
                        editingDebt = null
                        showAddEditSheet = true
                    },
                    onEditDebt = { debt ->
                        editingDebt = debt
                        showAddEditSheet = true
                    },
                    onSendReminder = { debt ->
                        viewModel.selectDebtForReminder(debt)
                        currentTab = AppTab.MESSENGER
                    },
                    onRecordPayment = { debt ->
                        paymentDebt = debt
                    }
                )

                AppTab.MESSENGER -> MessengerScreen(
                    viewModel = viewModel
                )

                AppTab.TEMPLATES -> TemplatesScreen(
                    viewModel = viewModel
                )

                AppTab.ANALYTICS -> AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateToMessengerForDebt = { debt ->
                        viewModel.selectDebtForReminder(debt)
                        currentTab = AppTab.MESSENGER
                    }
                )
            }
        }
    }

    // Add / Edit Debt BottomSheet
    if (showAddEditSheet) {
        AddEditDebtSheet(
            debtToEdit = editingDebt,
            onDismiss = {
                showAddEditSheet = false
                editingDebt = null
            },
            onSave = { debt ->
                viewModel.saveDebt(debt)
                showAddEditSheet = false
                editingDebt = null
            }
        )
    }

    // Record Payment Dialog
    paymentDebt?.let { debt ->
        RecordPaymentDialog(
            debt = debt,
            onDismiss = { paymentDebt = null },
            onConfirm = { amount, notes ->
                viewModel.recordPayment(debt, amount, notes)
                paymentDebt = null
            }
        )
    }
}
