package com.kapilagro.sasyak.data.api.models.responses

import com.google.gson.annotations.SerializedName

data class DailyTaskCount(
    @SerializedName("date") val date: String,
    val count: Int
)