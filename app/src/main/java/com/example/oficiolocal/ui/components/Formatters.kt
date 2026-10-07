package com.example.oficiolocal.ui.components


import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

fun formatDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))

fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)