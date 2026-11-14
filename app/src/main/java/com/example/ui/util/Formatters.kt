package com.example.ui.util

import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localeId = Locale.forLanguageTag("id-ID")

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(localeId)
    return "Rp ${formatter.format(amount)}"
}

fun formatRupiahCompact(amount: Long): String {
    return when {
        amount >= 1_000_000_000L -> String.format(localeId, "Rp %.1f M", amount / 1_000_000_000.0)
        amount >= 1_000_000L -> String.format(localeId, "Rp %.1f Jt", amount / 1_000_000.0)
        amount >= 1_000L -> String.format(localeId, "Rp %.0f Rb", amount / 1_000.0)
        else -> formatRupiah(amount)
    }
}

fun formatKg(kg: Double): String {
    return if (kg % 1.0 == 0.0) {
        "${kg.toLong()} kg"
    } else {
        String.format(localeId, "%.1f kg", kg)
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    val date: Date = timestamp?.toDate() ?: return "Baru saja"
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", localeId)
    return sdf.format(date)
}

fun formatDateShort(timestamp: Timestamp?): String {
    val date: Date = timestamp?.toDate() ?: return "Hari ini"
    val sdf = SimpleDateFormat("dd MMM", localeId)
    return sdf.format(date)
}
