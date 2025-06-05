package com.kapilagro.sasyak.data.api.models.responses

import com.google.gson.annotations.SerializedName

data class DailyTaskCount(
    @SerializedName("day") val days: String,
    val count: Int
)