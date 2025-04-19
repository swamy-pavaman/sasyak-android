package com.kapilagro.sasyak.data.local

import com.kapilagro.sasyak.data.db.dao.NotificationDao
import com.kapilagro.sasyak.data.db.dao.TaskDao
import com.kapilagro.sasyak.data.db.dao.UserDao
import com.kapilagro.sasyak.data.db.entities.NotificationEntity
import com.kapilagro.sasyak.data.db.entities.TaskEntity
import com.kapilagro.sasyak.data.db.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val userDao: UserDao,
    private val taskDao: TaskDao,
    private val notificationDao: NotificationDao
) {
    // User related operations
    fun getUserById(userId: Int): Flow<UserEntity?> = userDao.getUserById(userId)

    fun getUserByEmail(email: String): Flow<UserEntity?> = userDao.getUserByEmail(email)

    fun getUsersByRole(role: String): Flow<List<UserEntity>> = userDao.getUsersByRole(role)

    fun getUsersByManager(managerId: Int): Flow<List<UserEntity>> = userDao.getUsersByManager(managerId)

    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun insertUsers(users: List<UserEntity>) = userDao.insertUsers(users)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser(userId: Int) = userDao.deleteUser(userId)

    suspend fun deleteAllUsers() = userDao.deleteAllUsers()

    // Task related operations
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTasksByStatus(status: String): Flow<List<TaskEntity>> = taskDao.getTasksByStatus(status)

    fun getTasksAssignedToUser(userId: String): Flow<List<TaskEntity>> = taskDao.getTasksAssignedToUser(userId)

    fun getTasksCreatedByUser(userId: String): Flow<List<TaskEntity>> = taskDao.getTasksCreatedByUser(userId)

    fun getTaskById(taskId: Int): Flow<TaskEntity?> = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun insertTasks(tasks: List<TaskEntity>) = taskDao.insertTasks(tasks)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(taskId: Int) = taskDao.deleteTask(taskId)

    suspend fun deleteAllTasks() = taskDao.deleteAllTasks()

    // Notification related operations
    fun getAllNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun getUnreadNotifications(): Flow<List<NotificationEntity>> = notificationDao.getUnreadNotifications()

    fun getUnreadNotificationCount(): Flow<Int> = notificationDao.getUnreadNotificationCount()

    fun getNotificationById(notificationId: Int): Flow<NotificationEntity?> = notificationDao.getNotificationById(notificationId)

    suspend fun insertNotification(notification: NotificationEntity) = notificationDao.insertNotification(notification)

    suspend fun insertNotifications(notifications: List<NotificationEntity>) = notificationDao.insertNotifications(notifications)

    suspend fun updateNotification(notification: NotificationEntity) = notificationDao.updateNotification(notification)

    suspend fun markNotificationAsRead(notificationId: Int) = notificationDao.markNotificationAsRead(notificationId)

    suspend fun markAllNotificationsAsRead() = notificationDao.markAllNotificationsAsRead()

    suspend fun deleteNotification(notificationId: Int) = notificationDao.deleteNotification(notificationId)

    suspend fun deleteAllNotifications() = notificationDao.deleteAllNotifications()
}
