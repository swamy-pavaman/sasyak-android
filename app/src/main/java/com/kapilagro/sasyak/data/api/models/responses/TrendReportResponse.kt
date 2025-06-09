package com.kapilagro.sasyak.data.api.models.responses

import com.google.gson.annotations.SerializedName
import com.kapilagro.sasyak.domain.models.DailyTaskCount

data class TrendReportResponse(
    @SerializedName("tasksCompleted") val tasksCompleted: List<DailyTaskCount>,
    @SerializedName("tasksCreated") val tasksCreated: List<DailyTaskCount>
)