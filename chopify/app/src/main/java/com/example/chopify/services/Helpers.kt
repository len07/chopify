package com.example.chopify.services

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun getCurrentDate(): String {
    return formatDate(LocalDate.now())
}

fun addToDate(date: String, days: Int): String {
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val parsedDate = LocalDate.parse(date, formatter)
    val newDate = parsedDate.plusDays(days.toLong())

    return newDate.format(formatter)
}

fun formatDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
}
