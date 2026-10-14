package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DebtEntity
import com.example.data.model.DebtCategory
import com.example.data.model.DebtType
import com.example.util.MessengerHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Debt Messenger", appName)
    }

    @Test
    fun `test messenger token replacement`() {
        val debt = DebtEntity(
            id = 1,
            personName = "Ali",
            phoneNumber = "+966500000000",
            type = DebtType.THEY_OWE_ME,
            totalAmount = 1000.0,
            paidAmount = 250.0,
            currency = "SAR",
            dueDateMillis = System.currentTimeMillis() + 86400000L,
            category = DebtCategory.BUSINESS,
            notes = "Invoice 12"
        )
        val template = "Hi {name}, remaining is {remaining} {currency}."
        val result = MessengerHelper.formatMessage(template, debt)
        assertTrue(result.contains("Ali"))
        assertTrue(result.contains("750"))
        assertTrue(result.contains("SAR"))
    }
}
