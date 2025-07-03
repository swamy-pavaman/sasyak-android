package com.kapilagro.sasyak.presentation.reports

import android.os.Build
import android.util.Log
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
import java.util.Locale
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

    private var tasksCompleted: List<DailyTaskCount> = emptyList()
    private var tasksCreated: List<DailyTaskCount> = emptyList()

    // Define color constants
    companion object {
        private const val COLOR_COMPLETED = "#26C6DA" // Teal for Completed
        private const val COLOR_CREATED = "#EF5350"   // Red for Created
    }

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
                    tasksCompleted = response.data.tasksCompleted?.map {
                        it.copy(color = COLOR_COMPLETED) // Assign Teal to completed tasks
                    } ?: emptyList()
                    tasksCreated = response.data.tasksCreated?.map {
                        it.copy(color = COLOR_CREATED) // Assign Red to created tasks
                    } ?: emptyList()
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

    fun setChartTab(tab: ChartTab) {
        _chartTab.value = tab
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeeklyTaskCounts(): Pair<List<DailyTaskCount>, List<DailyTaskCount>> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

        // Define day-of-week order for sorting
        val dayOrder = mapOf(
            "MON" to 1, "TUE" to 2, "WED" to 3, "THU" to 4,
            "FRI" to 5, "SAT" to 6, "SUN" to 7
        )

        val completed = tasksCompleted
            .filter {
                val taskDate = LocalDate.parse(it.date, formatter)
                taskDate >= startOfWeek && taskDate <= today
            }
            .map {
                DailyTaskCount(
                    date = LocalDate.parse(it.date, formatter).format(dayOfWeekFormatter),
                    count = it.count,
                    color = COLOR_COMPLETED
                )
            }
            .sortedBy { dayOrder[it.date.uppercase()] ?: Int.MAX_VALUE }

        val created = tasksCreated
            .filter {
                val taskDate = LocalDate.parse(it.date, formatter)
                taskDate >= startOfWeek && taskDate <= today
            }
            .map {
                DailyTaskCount(
                    date = LocalDate.parse(it.date, formatter).format(dayOfWeekFormatter),
                    count = it.count,
                    color = COLOR_CREATED
                )
            }
            .sortedBy { dayOrder[it.date.uppercase()] ?: Int.MAX_VALUE }

        Log.d("ReportViewModel", "Weekly Task Counts - Completed: ${completed.size}, Created: ${created.size}")
        return Pair(completed, created)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthlyTaskCounts(): Pair<List<DailyTaskCount>, List<DailyTaskCount>> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val completedWeeks = mutableListOf<DailyTaskCount>()
        val createdWeeks = mutableListOf<DailyTaskCount>()
        var currentWeekStart = startOfMonth
        var weekNumber = 1

        while (currentWeekStart <= today) {
            val weekEnd = currentWeekStart.plusDays(6).coerceAtMost(today)

            val weekCompletedCount = tasksCompleted
                .filter {
                    val taskDate = LocalDate.parse(it.date, formatter)
                    taskDate >= currentWeekStart && taskDate <= weekEnd
                }
                .sumOf { it.count }
            completedWeeks.add(
                DailyTaskCount(
                    date = "Week $weekNumber",
                    count = weekCompletedCount,
                    color = COLOR_COMPLETED // Assign Teal to completed tasks
                )
            )

            val weekCreatedCount = tasksCreated
                .filter {
                    val taskDate = LocalDate.parse(it.date, formatter)
                    taskDate >= currentWeekStart && taskDate <= weekEnd
                }
                .sumOf { it.count }
            createdWeeks.add(
                DailyTaskCount(
                    date = "Week $weekNumber",
                    count = weekCreatedCount,
                    color = COLOR_CREATED // Assign Red to created tasks
                )
            )

            currentWeekStart = weekEnd.plusDays(1)
            weekNumber++
        }

        Log.d("ReportViewModel", "Monthly Task Counts - Completed Weeks: ${completedWeeks.size}, Created Weeks: ${createdWeeks.size}")
        return Pair(completedWeeks, createdWeeks)
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