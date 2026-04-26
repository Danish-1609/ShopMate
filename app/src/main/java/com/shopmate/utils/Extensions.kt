package com.shopmate.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Double.toCurrency(): String {
    return "₹${String.format("%,.2f", this)}"
}

fun Double.toPercent(): String = String.format("%.1f%%", this)

fun Int.toStockLabel(): String = "$this units"

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun EditText.onTextChanged(action: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            action(s?.toString() ?: "")
        }
        override fun afterTextChanged(s: Editable?) {}
    })
}

fun String.toLocalDate(): LocalDate? = try {
    LocalDateTime.parse(this).toLocalDate()
} catch (e: Exception) { null }

fun LocalDate.toDisplayString(): String =
    format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))

fun LocalDateTime.toDisplayString(): String =
    format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault()))

fun getTodayString(): String = LocalDate.now().toString()

fun getStartOfWeek(): String = LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() - 1).toString()

fun getStartOfMonth(): String = LocalDate.now().withDayOfMonth(1).toString()

fun getEndOfDay(): String = LocalDate.now().toString() + "T23:59:59"

fun getDateRange(days: Int): Pair<String, String> {
    val end = LocalDate.now()
    val start = end.minusDays(days.toLong())
    return Pair(start.toString(), end.toString() + "T23:59:59")
}

object DateUtils {
    fun todayRange(): Pair<String, String> {
        val today = LocalDate.now().toString()
        return Pair(today, "${today}T23:59:59.999")
    }

    fun weekRange(): Pair<String, String> {
        val end = LocalDate.now()
        val start = end.minusDays(6)
        return Pair(start.toString(), "${end}T23:59:59.999")
    }

    fun monthRange(): Pair<String, String> {
        val end = LocalDate.now()
        val start = end.withDayOfMonth(1)
        return Pair(start.toString(), "${end}T23:59:59.999")
    }

    fun last30DaysRange(): Pair<String, String> {
        val end = LocalDate.now()
        val start = end.minusDays(29)
        return Pair(start.toString(), "${end}T23:59:59.999")
    }

    fun formatDate(dateStr: String): String {
        return try {
            val date = LocalDateTime.parse(dateStr).toLocalDate()
            date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))
        } catch (e: Exception) { dateStr.take(10) }
    }

    fun formatDateTime(dateStr: String): String {
        return try {
            LocalDateTime.parse(dateStr).toDisplayString()
        } catch (e: Exception) { dateStr }
    }
}
