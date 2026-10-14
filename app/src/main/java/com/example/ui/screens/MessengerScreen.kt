package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DebtEntity
import com.example.data.local.MessageTemplateEntity
import com.example.data.model.DebtType
import com.example.data.model.MessageTone
import com.example.ui.theme.DebtRed
import com.example.ui.theme.PaidGreen
import com.example.util.MessengerHelper
import com.example.viewmodel.DebtViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(
    viewModel: DebtViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allDebts by viewModel.allDebts.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()
    val selectedDebt by viewModel.selectedDebtForReminder.collectAsState()
    val selectedTemplate by viewModel.selectedTemplate.collectAsState()
    val messageText by viewModel.previewMessage.collectAsState()

    // Filter debtors that owe money (receivable)
    val eligibleDebtors = remember(allDebts) {
        allDebts.filter { it.type == DebtType.THEY_OWE_ME && !it.isSettled }
    }

    var selectedLanguage by remember { mutableStateOf("en") }
    var showDebtorDropdown by remember { mutableStateOf(false) }

    val templatesForLanguage = remember(allTemplates, selectedLanguage) {
        allTemplates.filter { it.language == selectedLanguage }
    }

    // Auto-select first debtor if none selected and debtors exist
    if (selectedDebt == null && eligibleDebtors.isNotEmpty()) {
        viewModel.selectDebtForReminder(eligibleDebtors.first())
    }

    // Auto-select first template if none selected
    if (selectedTemplate == null && templatesForLanguage.isNotEmpty()) {
        viewModel.selectTemplate(templatesForLanguage.first())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 72.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Smart Debt Messenger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "مرسال التذكيرات الذكي بضغطة زر",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Language Switcher Chip
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        selectedLanguage = "en"
                        val defaultEn = allTemplates.firstOrNull { it.language == "en" }
                        viewModel.selectTemplate(defaultEn)
                    }
                ) {
                    Text(
                        text = "EN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedLanguage == "en") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedLanguage == "ar") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        selectedLanguage = "ar"
                        val defaultAr = allTemplates.firstOrNull { it.language == "ar" }
                        viewModel.selectTemplate(defaultAr)
                    }
                ) {
                    Text(
                        text = "عربي",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedLanguage == "ar") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Select Debtor Card
        Text(
            text = "1. Select Debtor (المستلم)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDebtorDropdown = true }
                .testTag("debtor_selector_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (selectedDebt != null) {
                        Text(
                            text = selectedDebt!!.personName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Due: ${MessengerHelper.formatCurrency(selectedDebt!!.remainingAmount)} ${selectedDebt!!.currency} • ${selectedDebt!!.phoneNumber.ifBlank { "No phone" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Select a debtor from list",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = showDebtorDropdown,
                onDismissRequest = { showDebtorDropdown = false }
            ) {
                if (eligibleDebtors.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No active debtors found") },
                        onClick = { showDebtorDropdown = false }
                    )
                } else {
                    eligibleDebtors.forEach { debtor ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(debtor.personName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${MessengerHelper.formatCurrency(debtor.remainingAmount)} ${debtor.currency} • ${debtor.phoneNumber}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                viewModel.selectDebtForReminder(debtor)
                                showDebtorDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Tone & Template Selection
        Text(
            text = "2. Select Message Tone (نبرة التذكير)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            templatesForLanguage.forEach { template ->
                FilterChip(
                    selected = selectedTemplate?.id == template.id,
                    onClick = { viewModel.selectTemplate(template) },
                    label = { Text(template.title) },
                    modifier = Modifier.testTag("template_chip_${template.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 3: Live Preview & Editor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3. Live Message Preview (معاينة الرسالة)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${messageText.length} chars",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = messageText,
            onValueChange = { viewModel.updatePreviewMessage(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .testTag("preview_message_input"),
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Select a debtor and template to preview formatted message...") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Token Insert Chips
        Text(
            text = "Insert variable tag:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("{name}", "{remaining}", "{currency}", "{due_date}", "{days_status}").forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        viewModel.updatePreviewMessage(messageText + " " + tag)
                    }
                ) {
                    Text(
                        text = tag,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step 4: Dispatch Action Buttons
        val phone = selectedDebt?.phoneNumber ?: ""
        val hasMessage = messageText.isNotBlank()

        // Primary: WhatsApp Send Button
        Button(
            onClick = {
                if (hasMessage) {
                    val success = MessengerHelper.sendViaWhatsApp(context, phone, messageText)
                    if (success && selectedDebt != null) {
                        viewModel.logReminderSent(selectedDebt!!)
                    }
                }
            },
            enabled = hasMessage,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("send_whatsapp_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF25D366),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Send via WhatsApp (إرسال واتساب)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary: Send SMS & Copy Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SMS Button
            FilledTonalButton(
                onClick = {
                    if (hasMessage) {
                        val success = MessengerHelper.sendViaSms(context, phone, messageText)
                        if (success && selectedDebt != null) {
                            viewModel.logReminderSent(selectedDebt!!)
                        }
                    }
                },
                enabled = hasMessage,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("send_sms_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send SMS")
            }

            // Copy to Clipboard Button
            OutlinedButton(
                onClick = {
                    MessengerHelper.copyToClipboard(context, messageText)
                    if (selectedDebt != null) {
                        viewModel.logReminderSent(selectedDebt!!)
                    }
                },
                enabled = hasMessage,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("copy_message_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Text")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Share via System Sheet
        OutlinedButton(
            onClick = {
                MessengerHelper.shareGeneric(context, messageText)
                if (selectedDebt != null) {
                    viewModel.logReminderSent(selectedDebt!!)
                }
            },
            enabled = hasMessage,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("share_message_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share to Other Apps (مشاركة)")
        }

        // Debtor reminder info note
        selectedDebt?.let { debt ->
            if (debt.reminderCount > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Last reminded: ${MessengerHelper.formatDate(debt.lastRemindedMillis)} • Reminded ${debt.reminderCount} times so far",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
