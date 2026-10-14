package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DebtEntity
import com.example.data.model.DebtCategory
import com.example.data.model.DebtType
import com.example.util.MessengerHelper
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditDebtSheet(
    debtToEdit: DebtEntity?,
    onDismiss: () -> Unit,
    onSave: (DebtEntity) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val isEdit = debtToEdit != null

    var personName by remember { mutableStateOf(debtToEdit?.personName ?: "") }
    var phoneNumber by remember { mutableStateOf(debtToEdit?.phoneNumber ?: "") }
    var type by remember { mutableStateOf(debtToEdit?.type ?: DebtType.THEY_OWE_ME) }
    var amountText by remember { mutableStateOf(debtToEdit?.totalAmount?.toString() ?: "") }
    var currency by remember { mutableStateOf(debtToEdit?.currency ?: "$") }
    var category by remember { mutableStateOf(debtToEdit?.category ?: DebtCategory.PERSONAL) }
    var notes by remember { mutableStateOf(debtToEdit?.notes ?: "") }

    val defaultDueDate = remember {
        debtToEdit?.dueDateMillis ?: (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7))
    }
    var dueDateMillis by remember { mutableLongStateOf(defaultDueDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)

    val currencies = listOf("$", "SAR", "AED", "EGP", "EUR", "KWD", "QAR", "GBP")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Edit Debt Record" else "Add New Debt",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Debt Direction Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = type == DebtType.THEY_OWE_ME,
                    onClick = { type = DebtType.THEY_OWE_ME },
                    label = { Text("They Owe Me (مديون لي)") },
                    modifier = Modifier.weight(1f).testTag("chip_they_owe_me")
                )
                FilterChip(
                    selected = type == DebtType.I_OWE_THEM,
                    onClick = { type = DebtType.I_OWE_THEM },
                    label = { Text("I Owe (عليّ دين)") },
                    modifier = Modifier.weight(1f).testTag("chip_i_owe_them")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Person Name
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person or Business Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_debt_name"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number (For WhatsApp / SMS)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number (with country code)") },
                placeholder = { Text("+966... or +1...") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_debt_phone"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount and Currency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total Amount *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_debt_amount"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    Text(
                        text = "Currency",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.width(140.dp)
                    ) {
                        currencies.take(4).forEach { cur ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currency == cur) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .clickable { currency = cur }
                            ) {
                                Text(
                                    text = cur,
                                    fontSize = 12.sp,
                                    fontWeight = if (currency == cur) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currency == cur) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date & Quick selector
            Text(
                text = "Due Date",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = MessengerHelper.formatDate(dueDateMillis),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick date buttons (+3d, +7d, +14d, +30d)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3 to "+3 Days", 7 to "+1 Week", 14 to "+2 Weeks", 30 to "+1 Month").forEach { (days, label) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                dueDateMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days.toLong())
                            }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DebtCategory.values().forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes or Invoice Reference") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            val amountParsed = amountText.toDoubleOrNull() ?: 0.0
            val isValid = personName.isNotBlank() && amountParsed > 0.0

            Button(
                onClick = {
                    if (isValid) {
                        val result = debtToEdit?.copy(
                            personName = personName.trim(),
                            phoneNumber = phoneNumber.trim(),
                            type = type,
                            totalAmount = amountParsed,
                            currency = currency,
                            dueDateMillis = dueDateMillis,
                            category = category,
                            notes = notes.trim()
                        ) ?: DebtEntity(
                            personName = personName.trim(),
                            phoneNumber = phoneNumber.trim(),
                            type = type,
                            totalAmount = amountParsed,
                            currency = currency,
                            dueDateMillis = dueDateMillis,
                            category = category,
                            notes = notes.trim()
                        )
                        onSave(result)
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_debt_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isEdit) "Save Changes" else "Add Debt Record",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dueDateMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
