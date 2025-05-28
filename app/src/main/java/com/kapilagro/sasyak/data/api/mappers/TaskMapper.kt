package com.kapilagro.sasyak.data.api.mappers

import com.kapilagro.sasyak.data.api.models.responses.DailyTaskCount as ApiDailyTaskCount
import com.kapilagro.sasyak.data.api.models.responses.TaskReportResponse
import com.kapilagro.sasyak.data.api.models.responses.TrendReportResponse
import com.kapilagro.sasyak.data.db.entities.TaskEntity
import com.kapilagro.sasyak.domain.models.DailyTaskCount as DomainDailyTaskCount
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskReport
import java.text.SimpleDateFormat
import java.util.*

fun TaskEntity.toDomainModel(): Task {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    return Task(
        id = id,
        title = title,
        description = description,
        status = status,
        taskType = taskType,
        createdBy = createdBy,
        assignedTo = assignedTo,
        createdAt = dateFormat.format(createdAt),
        updatedAt = dateFormat.format(updatedAt),
        detailsJson = detailsJson,
        imagesJson = imagesJson,
        implementationJson = implementationJson
    )
}

fun Task.toEntityModel(): TaskEntity {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    return TaskEntity(
        id = id,
        title = title,
        description = description,
        status = status,
        taskType = taskType,
        createdBy = createdBy,
        assignedTo = assignedTo,
        createdAt = try {
            dateFormat.parse(createdAt) ?: Date()
        } catch (e: Exception) {
            Date()
        },
        updatedAt = try {
            dateFormat.parse(updatedAt) ?: Date()
        } catch (e: Exception) {
            Date()
        },
        detailsJson = detailsJson,
        imagesJson = imagesJson,
        implementationJson = implementationJson
    )
}

object TaskMapper {
    fun toTaskReport(response: TaskReportResponse): TaskReport {
        return TaskReport(
            totalTasks = response.totalTasks,
            tasksByType = response.tasksByType,
            tasksByStatus = response.tasksByStatus,
            tasksByUser = response.tasksByUser,
            avgCompletionTimeByType = response.avgCompletionTimeByType
        )
    }
}

// Mapping for DailyTaskCount
fun ApiDailyTaskCount.toDomainModel(): DomainDailyTaskCount {
    return DomainDailyTaskCount(
        data = "",  // Default value since the API doesn't provide this field
        count = this.count,
        days = this.days
    )
}

fun List<ApiDailyTaskCount>.toDomainModel(): List<DomainDailyTaskCount> {
    return this.map { it.toDomainModel() }
}