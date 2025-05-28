package com.kapilagro.sasyak.presentation.reports

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.DailyTaskCount
import com.kapilagro.sasyak.domain.models.TaskReport
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Loading)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    private val _chartTab = MutableStateFlow(ChartTab.WEEKLY)
    val chartTab: StateFlow<ChartTab> = _chartTab.asStateFlow()

    private var trendData: List<DailyTaskCount> = emptyList()

    init {
        loadTaskReport()
        loadTrendReport()
    }

    fun loadTaskReport() {
        _reportState.value = ReportState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.getTaskReport()) {
                is ApiResponse.Success -> {
                    _reportState.value = ReportState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _reportState.value = ReportState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _reportState.value = ReportState.Loading
                }
            }
        }
    }

    private fun loadTrendReport() {
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.getTrendReport()) {
                is ApiResponse.Success -> {
                    trendData = response.data  // Fixed from 'response.day' to 'response.data'
                }
                is ApiResponse.Error -> {
                    _reportState.value = ReportState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _reportState.value = ReportState.Loading
                    // Handle loading state if needed
                }
            }
        }
    }

    fun setChartTab(tab: ChartTab) {
        _chartTab.value = tab
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeeklyTaskCounts(): List<DailyTaskCount> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        return trendData
            .filter {
                val taskDate = LocalDate.parse(it.days, formatter)
                taskDate >= startOfWeek && taskDate <= today
            }
            .map {
                DailyTaskCount(
                    data = it.data,
                    count = it.count,
                    days = LocalDate.parse(it.days, formatter).dayOfWeek.toString().substring(0, 3)
                )
            }
            .sortedBy { LocalDate.parse(it.days, formatter) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthlyTaskCounts(): List<DailyTaskCount> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val weeks = mutableListOf<DailyTaskCount>()
        var currentWeekStart = startOfMonth
        var weekNumber = 1

        while (currentWeekStart <= today) {
            val weekEnd = currentWeekStart.plusDays(6).coerceAtMost(today)
            val weekCount = trendData
                .filter {
                    val taskDate = LocalDate.parse(it.days, formatter)
                    taskDate >= currentWeekStart && taskDate <= weekEnd
                }
                .sumOf { it.count }
            weeks.add(DailyTaskCount("Week $weekNumber", weekCount, "Week $weekNumber"))
            currentWeekStart = weekEnd.plusDays(1)
            weekNumber++
        }
        return weeks
    }

    sealed class ReportState {
        object Loading : ReportState()
        data class Success(val report: TaskReport) : ReportState()
        data class Error(val message: String) : ReportState()
    }

    enum class ChartTab {
        WEEKLY, MONTHLY
    }
}