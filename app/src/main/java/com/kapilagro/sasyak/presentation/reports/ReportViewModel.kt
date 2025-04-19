package com.kapilagro.sasyak.presentation.reports

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

    init {
        loadTaskReport()
    }

    fun loadTaskReport() {
        _reportState.value = ReportState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.getTaskReport()) {
                is ApiResponse.Success -> {
                    _reportState.value = ReportState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    // Since we don't have a real task report API, we'll simulate the data
                    _reportState.value = ReportState.Success(generateMockTaskReport())
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

    private fun generateMockTaskReport(): TaskReport {
        // Generate mock data that matches what's shown in the screenshot
        val tasksByType = mapOf(
            "Scouting" to 42,
            "Spraying" to 28,
            "Sowing" to 16,
            "Fuel" to 9,
            "Yield" to 5
        )

        val tasksByStatus = mapOf(
            "pending" to 18,
            "approved" to 45,
            "rejected" to 12,
            "implemented" to 25
        )

        val tasksByUser = mapOf(
            "John Doe" to 20,
            "Jane Smith" to 18,
            "Bob Johnson" to 15
        )

        val avgCompletionTimeByType = mapOf(
            "Scouting" to 2.5,
            "Spraying" to 3.2,
            "Sowing" to 4.1,
            "Fuel" to 1.5,
            "Yield" to 2.0
        )

        return TaskReport(
            totalTasks = 100,
            tasksByType = tasksByType,
            tasksByStatus = tasksByStatus,
            tasksByUser = tasksByUser,
            avgCompletionTimeByType = avgCompletionTimeByType
        )
    }

    fun getWeeklyTaskCounts(): List<DailyTaskCount> {
        // Generate mock data that matches what's shown in the screenshot
        return listOf(
            DailyTaskCount("Mon", 4),
            DailyTaskCount("Tue", 6),
            DailyTaskCount("Wed", 3),
            DailyTaskCount("Thu", 8),
            DailyTaskCount("Fri", 5),
            DailyTaskCount("Sat", 7),
            DailyTaskCount("Sun", 2)
        )
    }

    fun getMonthlyTaskCounts(): List<DailyTaskCount> {
        // Generate mock monthly data
        return listOf(
            DailyTaskCount("Week 1", 18),
            DailyTaskCount("Week 2", 24),
            DailyTaskCount("Week 3", 15),
            DailyTaskCount("Week 4", 22)
        )
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
