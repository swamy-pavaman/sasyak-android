package com.kapilagro.sasyak.domain.models

data class DailyTaskCount(
    val date: String,
    val count: Int,
    val color: String? = null // Optional color field, hex string like "#26C6DA"
)