package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.local.DebtEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object MessengerHelper {

    fun formatCurrency(amount: Double): String {
        return try {
            val format = NumberFormat.getNumberInstance(Locale.getDefault())
            format.minimumFractionDigits = 0
            format.maximumFractionDigits = 2
            format.format(amount)
        } catch (_: Exception) {
            "%.2f".format(amount)
        }
    }

    fun formatDate(millis: Long): String {
        if (millis <= 0) return "-"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun getDaysStatus(dueDateMillis: Long, isArabic: Boolean = false): String {
        val now = System.currentTimeMillis()
        val diffMillis = dueDateMillis - now
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

        return if (isArabic) {
            when {
                diffDays < -1 -> "متأخر منذ ${-diffDays} يوم"
                diffDays == -1 -> "متأخر منذ يوم واحد"
                diffDays == 0 -> "مستحق اليوم"
                diffDays == 1 -> "مستحق غداً"
                else -> "مستحق خلال $diffDays يوم"
            }
        } else {
            when {
                diffDays < -1 -> "overdue by ${-diffDays} days"
                diffDays == -1 -> "overdue by 1 day"
                diffDays == 0 -> "due today"
                diffDays == 1 -> "due tomorrow"
                else -> "due in $diffDays days"
            }
        }
    }

    fun formatMessage(template: String, debt: DebtEntity, isArabic: Boolean = false): String {
        val daysStatus = getDaysStatus(debt.dueDateMillis, isArabic)
        return template
            .replace("{name}", debt.personName.trim())
            .replace("{amount}", formatCurrency(debt.totalAmount))
            .replace("{paid}", formatCurrency(debt.paidAmount))
            .replace("{remaining}", formatCurrency(debt.remainingAmount))
            .replace("{currency}", debt.currency.trim())
            .replace("{due_date}", formatDate(debt.dueDateMillis))
            .replace("{days_status}", daysStatus)
            .replace("{notes}", debt.notes.trim())
    }

    fun cleanPhoneNumber(phone: String): String {
        var clean = phone.replace(Regex("[^0-9+]"), "")
        if (clean.startsWith("00")) {
            clean = "+" + clean.substring(2)
        }
        return clean
    }

    fun sendViaWhatsApp(context: Context, phone: String, message: String): Boolean {
        val cleanedPhone = cleanPhoneNumber(phone).removePrefix("+")
        return try {
            val encodedMessage = Uri.encode(message)
            val uri = if (cleanedPhone.isNotEmpty()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanedPhone&text=$encodedMessage")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun sendViaSms(context: Context, phone: String, message: String): Boolean {
        return try {
            val uri = Uri.parse("smsto:${Uri.encode(phone)}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun shareGeneric(context: Context, text: String, title: String = "Smart Debt Messenger") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_SUBJECT, title)
            }
            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Share failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Debt Reminder") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Message copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
