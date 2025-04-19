package com.kapilagro.sasyak.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    fun formatDateTime(date: Date): String {
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return format.format(date)
    }

    fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun formatTime(date: Date): String {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(date)
    }

    fun formatTimeAgo(date: Date): String {
        val now = Date()
        val diffInMillies = now.time - date.time
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillies)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillies)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies)
        val diffInWeeks = diffInDays / 7

        return when {
            diffInMinutes < 1 -> "Just now"
            diffInMinutes < 60 -> "$diffInMinutes minutes ago"
            diffInHours < 24 -> "${diffInHours}h ago"
            diffInDays < 7 -> "${diffInDays}d ago"
            diffInWeeks < 4 -> "${diffInWeeks}w ago"
            else -> formatDate(date)
        }
    }

    fun isToday(date: Date): Boolean {
        val todayCal = Calendar.getInstance()
        val dateCal = Calendar.getInstance().apply { time = date }

        return todayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                todayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
    }

    fun isTomorrow(date: Date): Boolean {
        val tomorrowCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val dateCal = Calendar.getInstance().apply { time = date }

        return tomorrowCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                tomorrowCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(date: Date): Boolean {
        val yesterdayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val dateCal = Calendar.getInstance().apply { time = date }

        return yesterdayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                yesterdayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
    }

    fun formatForDisplay(date: Date): String {
        return when {
            isToday(date) -> "Today, ${formatTime(date)}"
            isYesterday(date) -> "Yesterday, ${formatTime(date)}"
            isTomorrow(date) -> "Tomorrow, ${formatTime(date)}"
            else -> formatDateTime(date)
        }
    }

    fun getCurrentWeekDays(): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        // Set to start of current week (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val days = mutableListOf<Date>()

        // Add all days of the week
        for (i in 0..6) {
            days.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return days
    }

    fun getCurrentMonthWeeks(): List<Pair<Date, Date>> {
        val calendar = Calendar.getInstance()

        // Set to first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val weeks = mutableListOf<Pair<Date, Date>>()
        val monthNumber = calendar.get(Calendar.MONTH)

        while (calendar.get(Calendar.MONTH) == monthNumber) {
            val weekStart = calendar.clone() as Calendar

            // Move to the end of the week
            calendar.add(Calendar.DAY_OF_WEEK, 6)

            // If we've moved to the next month, adjust to the last day of current month
            if (calendar.get(Calendar.MONTH) != monthNumber) {
                calendar.add(Calendar.DAY_OF_MONTH, -calendar.get(Calendar.DAY_OF_MONTH))
            }

            val weekEnd = calendar.clone() as Calendar

            weeks.add(Pair(weekStart.time, weekEnd.time))

            // Move to the start of the next week
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weeks
    }
}

