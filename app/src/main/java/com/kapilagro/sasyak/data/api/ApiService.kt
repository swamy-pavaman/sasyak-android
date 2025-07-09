// ApiService.kt
package com.kapilagro.sasyak.data.api

import com.kapilagro.sasyak.data.api.models.requests.*
import com.kapilagro.sasyak.data.api.models.responses.*
import com.kapilagro.sasyak.domain.models.LoginRequest
import com.kapilagro.sasyak.domain.models.User
import retrofit2.Response
import retrofit2.http.*

interface ApiService {


    // File: app/src/main/java/com/kapilagro/sasyak/data/api/ApiService.kt
// Add to existing interface
    @POST("api/minio/presigned-url/upload")
    suspend fun getPresignedUrls(@Body request: PresignedUrlRequest): Response<PresignedUrlResponse>


    @GET("/api/manager/users/supervisor-list")
    suspend fun getSupervisorsList(): Response<List<SupervisorListResponse>>

    @GET("api/tasks/by-supervisors")
    suspend fun getTasksBySupervisors(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<TaskListResponse>


    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<AuthResponse>
    // User Endpoints
    @GET("api/user/profile")
    suspend fun getCurrentUser(): Response<UserResponse>

//    @PUT("api/supervisor/profile")
//    suspend fun updateSupervisorProfile(@Body updateProfileRequest: UpdateProfileRequest): Response<UserResponse>
    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body updateProfileRequest: UpdateProfileRequest): Response<UserResponse>

    // Task Endpoints
    @GET("api/tasks/assigned")
    suspend fun getAssignedTasks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<TaskListResponse>

    @GET("api/tasks/status/{status}")
    suspend fun getTasksByStatus(
        @Path("status") status: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<TaskListResponse>


    @GET("api/tasks/created")
    suspend fun getCreatedTasks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<TaskListResponse>

    @GET("api/tasks/{taskId}")
    suspend fun getTaskById(@Path("taskId") taskId: Int): Response<TaskDetailResponse>

    @POST("api/tasks")
    suspend fun createTask(@Body createTaskRequest: CreateTaskRequest): Response<TaskResponse>

    @PUT("api/tasks/{taskId}/status")
    suspend fun updateTaskStatus(
        @Path("taskId") taskId: Int,
        @Body updateStatusRequest: UpdateTaskStatusRequest
    ): Response<TaskResponse>

    @PUT("api/tasks/{taskId}/implementation")
    suspend fun updateTaskImplementation(
        @Path("taskId") taskId: Int,
        @Body implementationRequest: UpdateImplementationRequest
    ): Response<TaskResponse>

    // Task Advice Endpoints
    @POST("api/task-advices")
    suspend fun createTaskAdvice(@Body createAdviceRequest: CreateAdviceRequest): Response<TaskAdviceResponse>

    @GET("api/task-advices/task/{taskId}")
    suspend fun getAdviceForTask(@Path("taskId") taskId: Int): Response<TaskAdviceListResponse>

    @GET("api/task-advices/provided")
    suspend fun getAdviceProvidedByCurrentManager(): Response<TaskAdviceListResponse>

    // Notification Endpoints
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("onlyUnread") onlyUnread: Boolean = false,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<NotificationListResponse>

    @GET("api/notifications/unread/count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountResponse>

    @PUT("api/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: Int): Response<SuccessResponse>

    @PUT("api/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<SuccessResponse>

    // For Manager Only
    @GET("api/manager/users/team")
    suspend fun getTeamMembers(): Response<UserListResponse>

    @GET("api/manager/users/supervisors")
    suspend fun getAllSupervisors(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PagedUserListResponse>

    @PUT("api/manager/users/team/{id}")
    suspend fun updateTeamMember(
        @Path("id") userId: Int,
        @Body updateTeamMemberRequest: UpdateTeamMemberRequest
    ): Response<UserResponse>

    @PUT("api/manager/users/assign/{supervisorId}")
    suspend fun assignSupervisorToTeam(@Path("supervisorId") supervisorId: Int): Response<UserResponse>

    @PUT("api/manager/users/unassign/{supervisorId}")
    suspend fun unassignSupervisorFromTeam(@Path("supervisorId") supervisorId: Int): Response<UserResponse>

    // For Supervisor Only
    @GET("api/supervisor/manager")
    suspend fun getSupervisorManager(): Response<UserResponse>

    @GET("api/supervisor/profile")
    suspend fun getSupervisorProfile(): Response<UserResponse>

    // Global search endpoint
    @GET("api/search")
    suspend fun searchGlobal(@Query("query") query: String): Response<SearchResultsResponse>

    @GET("api/tasks/type/{taskType}")
    suspend fun getTasksByType(
        @Path("taskType") taskType: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<TaskListResponse>


    @GET("/api/tasks/report")
    suspend fun getTaskReport(): TaskReportResponse

//    @GET("/api/tasks/report/trend")
//    suspend fun getTrendReport(): TrendReportResponse
@GET("/api/tasks/report/trend")
suspend fun getTrendReport(): Response<TrendReportResponse>

@GET("api/catalog/Crop")
suspend fun cropService() : List<CropsResponce>

@GET("api/catalog/{categoryType}")
suspend fun categoryService(
    @Path("categoryType") categoryType: String
) : List<CategoryResponce>

    @GET("api/admin/users")
    suspend fun getAdminTeam(): Response<UserListResponse>

    @GET("api/admin/users/{userId}")
    suspend fun getAdminUserById(@Path("userId") userId: Int): Response<UserResponse>

    @GET("api/tasks/by-user-id/{user_id}")
    suspend fun getTasksByUserId(
         @Path("user_id") userId: Int,
         @Query("page") page: Int = 0,
         @Query("size") size: Int = 10
    ): Response<TaskListResponse>

    @GET("api/admin/users/by-role/{role}")
    suspend fun getUsersByRole(@Path("role") role: String): Response<TeamMemberListResponse>
}