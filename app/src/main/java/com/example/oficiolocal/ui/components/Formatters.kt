package com.example.oficiolocal.ui.components

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun formatTime(hour: Int, minute: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

/** Número más alto de un texto como "$200 - $500" (devuelve null si no hay números). */
fun maxPriceOf(priceRange: String): Int? =
    Regex("\\d+").findAll(priceRange).map { it.value.toInt() }.maxOrNull()

/**
 * Dice si un horario como "Lun - Sáb: 8:00 AM - 7:00 PM" incluye el día de hoy.
 * Si no logra leer los días, regresa true para no ocultar al prestador.
 */
fun isAvailableToday(
    schedule: String,
    today: Int = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0 = lunes ... 6 = domingo
): Boolean {
    val abbreviations = listOf("lun", "mar", "mi", "jue", "vie", "sa", "dom")
    val head = schedule.substringBefore(":").lowercase().replace("á", "a").replace("é", "e")
    val days = Regex("lun|mar|mi|jue|vie|sa|dom").findAll(head).map { abbreviations.indexOf(it.value) }.toList()
    return when (days.size) {
        0 -> true
        1 -> days[0] == today
        else -> today in days.first()..days.last()
    }
}
