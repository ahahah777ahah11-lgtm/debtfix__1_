package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DebtCategory
import com.example.data.model.DebtType
import com.example.data.model.MessageTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DebtEntity::class, PaymentEntity::class, MessageTemplateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun debtDao(): DebtDao
    abstract fun paymentDao(): PaymentDao
    abstract fun messageTemplateDao(): MessageTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_debt_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val defaultTemplates = listOf(
            // English templates
            MessageTemplateEntity(
                title = "Friendly Reminder",
                tone = MessageTone.FRIENDLY,
                language = "en",
                templateContent = "Hi {name}, hope you're having a great day! Just a gentle reminder regarding the balance of {remaining} {currency} ({days_status}). Thank you!",
                isDefault = true
            ),
            MessageTemplateEntity(
                title = "Formal / Polite Notice",
                tone = MessageTone.POLITE,
                language = "en",
                templateContent = "Dear {name}, this is a courtesy notice regarding your pending balance of {remaining} {currency} ({days_status}). Please arrange for payment at your convenience. Kind regards.",
                isDefault = true
            ),
            MessageTemplateEntity(
                title = "Urgent Overdue Notice",
                tone = MessageTone.URGENT,
                language = "en",
                templateContent = "Urgent Reminder for {name}: Your payment of {remaining} {currency} was due on {due_date} ({days_status}). Please settle this balance promptly to keep your account clear.",
                isDefault = true
            ),
            MessageTemplateEntity(
                title = "Final Notice",
                tone = MessageTone.FINAL_NOTICE,
                language = "en",
                templateContent = "FINAL NOTICE: {name}, your outstanding debt of {remaining} {currency} is overdue. Immediate payment is required.",
                isDefault = true
            ),
            MessageTemplateEntity(
                title = "Payment Receipt & Thanks",
                tone = MessageTone.RECEIPT,
                language = "en",
                templateContent = "Dear {name}, thank you for your payment. Your updated remaining balance is {remaining} {currency}.",
                isDefault = true
            ),

            // Arabic templates (مرسال الديون الذكي)
            MessageTemplateEntity(
                title = "تذكير لطيف وودي",
                tone = MessageTone.FRIENDLY,
                language = "ar",
                templateContent = "مرحباً {name}، أتمنى أن تكون بأفضل حال. تذكير لطيف بخصوص المبلغ المتبقي {remaining} {currency} ({days_status}). شاكر ومقدر لك!",
                isDefault = true
            ),
            MessageTemplateEntity(
                title = "إشعار رسمي ومهذب",
                tone = MessageTone.POLITE,
                language = "ar",
                templateContent = "السلام عليكم {name}، نود تذكيركم بأن المبلغ المستحق وقدره {remaining} {currency} ({days_status}). يُرجى التكرم بالسداد في أقرب وقت. خالص التحيات.",
                isDefault = true
            ),
            MessageTemplateEntity(
                title = "تنبيه عاجل",
                tone = MessageTone.URGENT,
                language = "ar",
                templateContent = "تنبيه عاجل للأخ {name}: نلفت انتباهكم بأن موعد سداد {remaining} {currency} قد حلّ ({days_status}). نرجو التكرم بالتحويل وتسوية الحساب.",
                isDefault = true
            ),
            MessageTemplateEntity(
                title = "إنذار أخير",
                tone = MessageTone.FINAL_NOTICE,
                language = "ar",
                templateContent = "إشعار نهائي: {name}، نرجو المبادرة بسداد المبلغ المتبقي وقدره {remaining} {currency} على وجه السرعة لإنهاء المديونية.",
                isDefault = true
            ),
            MessageTemplateEntity(
                title = "إيصال سداد وشكر",
                tone = MessageTone.RECEIPT,
                language = "ar",
                templateContent = "أهلاً {name}، شكراً جزيلاً على سداد الدفعة. رصيدك المتبقي الحالي هو: {remaining} {currency}.",
                isDefault = true
            )
        )
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    database.messageTemplateDao().insertAll(defaultTemplates)
                }
            }
        }
    }
}
