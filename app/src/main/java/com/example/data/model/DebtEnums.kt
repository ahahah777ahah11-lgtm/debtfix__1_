package com.example.data.model

enum class DebtType(val labelEn: String, val labelAr: String) {
    THEY_OWE_ME("They Owe Me", "مديون لي"),
    I_OWE_THEM("I Owe Them", "عليّ دين")
}

enum class DebtCategory(val label: String, val iconName: String) {
    PERSONAL("Personal", "person"),
    BUSINESS("Business", "business_center"),
    LOAN("Loan", "payments"),
    RENT("Rent", "home"),
    INVOICE("Invoice", "receipt_long"),
    OTHER("Other", "more_horiz")
}

enum class MessageTone(val labelEn: String, val labelAr: String) {
    FRIENDLY("Friendly", "ودي ولطيف"),
    POLITE("Formal / Polite", "رسمي ومهذب"),
    URGENT("Urgent Notice", "تنبيه عاجل"),
    FINAL_NOTICE("Final Notice", "إنذار أخير"),
    RECEIPT("Receipt / Thanks", "إيصال وشكر"),
    CUSTOM("Custom", "مخصص")
}
