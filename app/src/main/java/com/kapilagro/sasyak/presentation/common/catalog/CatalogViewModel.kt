package com.kapilagro.sasyak.presentation.common.catalog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.models.responses.CategoryResponce
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import com.kapilagro.sasyak.presentation.scouting.CropDetails
import com.kapilagro.sasyak.utils.LocationService
import com.kapilagro.sasyak.utils.LocationService.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kapilagro.sasyak.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.collections.mapValues

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val locationService: LocationService,
) : ViewModel() {
    private val _categoriesStates = MutableStateFlow<Map<String, CategoriesState>>(emptyMap())
    val categoriesStates: StateFlow<Map<String, CategoriesState>> = _categoriesStates

    private val _location = MutableStateFlow<LocationResult?>(LocationResult(0.0, 0.0))
    val location: StateFlow<LocationResult?> = _location.asStateFlow()

    private val fetchingStates = mutableMapOf<String, Boolean>()

    init {
        loadLocation()
    }

    fun fetchCategories(categoryType: String) {
        if (fetchingStates[categoryType] == true) return
        viewModelScope.launch {
            fetchingStates[categoryType] = true
            _categoriesStates.value = _categoriesStates.value.toMutableMap().apply {
                this[categoryType] = CategoriesState.Loading
            }
            try {
                val categories = apiService.categoryService(categoryType)
                Log.d("CategoryViewModel", "Fetched categories: $categories")

                val valveDetails = categories.associate { category ->
                    val detailsJson = category.details
                    Log.d("CategoryViewModel", "Parsing details for valve ${category.value}: $detailsJson")

                    val details = try {
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        }.decodeFromString<Map<String, JsonElement>>(detailsJson)
                    } catch (e: Exception) {
                        Log.e("CategoryViewModel", "Failed to parse details: ${e.message}")
                        emptyMap()
                    }

                    val cropDetailsMap = details.mapValues { (_, value) ->
                        if (value is JsonObject) {
                            CropDetails(rawData = value.toMap())
                        } else {
                            CropDetails()
                        }
                    }

                    Log.d("CategoryViewModel", "Parsed valveDetails for ${category.value}:")
                    category.value to cropDetailsMap
                }
                Log.d("CategoryViewModel", "Full valveDetails: $valveDetails")
                _categoriesStates.value = _categoriesStates.value.toMutableMap().apply {
                    this[categoryType] = CategoriesState.Success(categories, categoryType, valveDetails)
                }
            }  catch (e: HttpException) {
                val errorMessage = if (e.code() == 403 || e.code() == 401) {
                    "Authentication failed: Invalid or missing access token"
                } else {
                    "HTTP error: ${e.code()} - ${e.message()}"
                }
                Log.e("CategoryViewModel", "HTTP error: $errorMessage")
                _categoriesStates.value = _categoriesStates.value.toMutableMap().apply {
                    this[categoryType] = CategoriesState.Error(errorMessage, categoryType)
                }
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Network error: ${e.message ?: "Unknown error"}")
                _categoriesStates.value = _categoriesStates.value.toMutableMap().apply {
                    this[categoryType] = CategoriesState.Error("Network error: ${e.message ?: "Unknown error"}", categoryType)
                }
            } finally {
                fetchingStates[categoryType] = false
            }
        }
    }
    fun loadLocation(){
        viewModelScope.launch(ioDispatcher) {
            val location = locationService.getLocation()
            _location.value = location.getOrNull()
        }
    }
}

sealed class CategoriesState {
    object Idle : CategoriesState()
    object Loading : CategoriesState()
    data class Success(
        val categories: List<CategoryResponce>,
        val categoryType: String,
        val valveDetails: Map<String, Map<String, CropDetails>>
    ) : CategoriesState()
    data class Error(val message: String, val categoryType: String) : CategoriesState()
}