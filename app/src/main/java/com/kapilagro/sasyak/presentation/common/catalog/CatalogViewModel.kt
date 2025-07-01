package com.kapilagro.sasyak.presentation.common.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.models.responses.CategoryResponce
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _categoriesStates = MutableStateFlow<Map<String, CategoriesState>>(emptyMap())
    val categoriesStates: StateFlow<Map<String, CategoriesState>> = _categoriesStates

    private val fetchingStates = mutableMapOf<String, Boolean>() // Track fetching per categoryType

    fun fetchCategories(categoryType: String) {
        if (fetchingStates[categoryType] == true) return
        viewModelScope.launch {
            fetchingStates[categoryType] = true
            _categoriesStates.value = _categoriesStates.value.toMutableMap().apply {
                this[categoryType] = CategoriesState.Loading
            }
            try {
                val categories = apiService.categoryService(categoryType)
                _categoriesStates.value = _categoriesStates.value.toMutableMap().apply {
                    this[categoryType] = CategoriesState.Success(categories, categoryType)
                }
            } catch (e: HttpException) {
                val errorMessage = if (e.code() == 403 || e.code() == 401) {
                    "Authentication failed: Invalid or missing access token"
                } else {
                    "HTTP error: ${e.code()} - ${e.message()}"
                }
                _categoriesStates.value = _categoriesStates.value.toMutableMap().apply {
                    this[categoryType] = CategoriesState.Error(errorMessage, categoryType)
                }
            } catch (e: Exception) {
                _categoriesStates.value = _categoriesStates.value.toMutableMap().apply {
                    this[categoryType] = CategoriesState.Error("Network error: ${e.message ?: "Unknown error"}", categoryType)
                }
            } finally {
                fetchingStates[categoryType] = false
            }
        }
    }
}

sealed class CategoriesState {
    object Idle : CategoriesState()
    object Loading : CategoriesState()
    data class Success(val categories: List<CategoryResponce>, val categoryType: String) : CategoriesState()
    data class Error(val message: String, val categoryType: String) : CategoriesState()
}