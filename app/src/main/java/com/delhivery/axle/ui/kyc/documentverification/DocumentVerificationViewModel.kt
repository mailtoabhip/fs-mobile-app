package com.delhivery.axle.ui.kyc.documentverification

import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.ApiError
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.ServiceRequirementsResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class DocumentVerificationViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository
) : BaseViewModel() {

    // ────────────────────────── ViewState ──────────────────────────────

    data class ViewState(
        val uiState: UiState<ServiceRequirementsResponse> = UiState.Idle,
        val isLoading: Boolean = false
    )

    private val _state = MutableStateFlow(ViewState())
    val state: StateFlow<ViewState> = _state.asStateFlow()

    // ────────────────────────── Public API ─────────────────────────────

    /**
     * Fetch service requirements for the given service ID.
     * Call this from Activity's onCreate.
     */
    fun fetchRequirements(serviceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, uiState = UiState.Idle) }

            loadboardRepository.getServiceRequirementsFlow(serviceId)
                .collect { resource -> handleResource(resource) }
        }
    }

    fun retry(serviceId: String) {
        fetchRequirements(serviceId)
    }

    // ────────────────────────── Resource Handler ───────────────────────

    private fun handleResource(resource: Resource<ServiceRequirementsResponse>) {
        when (resource) {
            is Resource.Loading -> Unit

            is Resource.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        uiState = if (resource.data != null)
                            UiState.Success(data = resource.data)
                        else
                            UiState.Empty("No requirements found")
                    )
                }
            }

            is Resource.Failure -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        uiState = UiState.Error(
                            apiError = resource.apiError,
                            message = getErrorMessage(resource.apiError),
                            isNetworkError = resource.isNetworkError
                        )
                    )
                }
            }
        }
    }

    // ────────────────────────── Error mapping ──────────────────────────

    private fun getErrorMessage(apiError: ApiError): String = when (apiError) {
        ApiError.Network            -> "No internet connection. Please check your network."
        ApiError.Timeout            -> "Request timed out. Please try again."
        ApiError.Unauthorized       -> "Session expired. Please log in again."
        ApiError.AccessDenied       -> "You don't have permission to access this resource."
        ApiError.NotFound           -> "Resource not found."
        ApiError.ServiceUnavailable -> "Service temporarily unavailable. Please try again later."
        ApiError.Unknown            -> "An unexpected error occurred. Please try again."
    }
}
