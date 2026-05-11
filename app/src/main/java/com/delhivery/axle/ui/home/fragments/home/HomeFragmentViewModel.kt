package com.delhivery.axle.ui.home.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.ApiError
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.ServiceGroup
import com.delhivery.axle.api.response.ServiceGroupsResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.common.UiEvent
import com.delhivery.axle.ui.common.UiState
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(
    private val userPrefs: UserPrefs,
    private val loadboardRepository: LoadboardRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    // ────────────────────────── KYC (existing) ─────────────────────────

    private val _kycUiModel = MutableLiveData<KycUiModel>()
    val kycUiModel: LiveData<KycUiModel> = _kycUiModel

    fun refreshKycStatus() {
        _kycUiModel.value = calculateKycUiModel()
    }

    // ────────────────────────── Service Groups (new) ───────────────────

    /**
     * State container for service groups section.
     *
     * [isLoading]  — true while shimmer/loading indicator should be shown
     * [uiState]    — the actual data state (Idle / Success / Empty / Error)
     */
    data class ServiceGroupsViewState(
        val uiState: UiState<List<ServiceGroup>> = UiState.Idle,
        val isLoading: Boolean = false
    )

    private val _serviceGroupsState = MutableStateFlow(ServiceGroupsViewState())

    /** Fragment collects this for service groups UI. */
    val serviceGroupsState: StateFlow<ServiceGroupsViewState> = _serviceGroupsState.asStateFlow()

    /** One-time events (snackbars, navigation). */
    private val _events = MutableSharedFlow<UiEvent>(replay = 0)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ────────────────────────── Service Groups: Public API ─────────────

    /**
     * Fetch service groups from the API.
     * Call this from Fragment's onViewCreated or when a refresh is needed.
     */
    fun fetchServiceGroups() {
        viewModelScope.launch {
            // Set loading state
            _serviceGroupsState.update { it.copy(isLoading = true, uiState = UiState.Idle) }

            loadboardRepository.getServiceGroupsFlow(userRepository.userId())
                .collect { resource -> handleServiceGroupsResource(resource) }
        }
    }

    /**
     * Retry fetching service groups after an error.
     */
    fun retryServiceGroups() {
        fetchServiceGroups()
    }

    // ────────────────────────── Service Groups: Resource Handler ───────

    private suspend fun handleServiceGroupsResource(resource: Resource<ServiceGroupsResponse>) {
        when (resource) {
            // Ignored: loading flag was set in fetchServiceGroups()
            is Resource.Loading -> Unit

            is Resource.Success -> {
                val groups = resource.data?.groups
                    ?.sortedBy { it.displayOrder }
                    ?: emptyList()

                _serviceGroupsState.update {
                    it.copy(
                        isLoading = false,
                        uiState = if (groups.isEmpty())
                            UiState.Empty("No service groups available")
                        else
                            UiState.Success(data = groups)
                    )
                }
            }

            is Resource.Failure -> {
                _serviceGroupsState.update {
                    it.copy(
                        isLoading = false,
                        uiState = UiState.Error(
                            apiError = resource.apiError,
                            message = getErrorMessage(resource.apiError),
                            isNetworkError = resource.isNetworkError
                        )
                    )
                }
                _events.emit(
                    UiEvent.ShowSnackbar(
                        message = "Failed to load service groups: ${getErrorMessage(resource.apiError)}",
                        action = "Retry",
                        onActionClick = { retryServiceGroups() }
                    )
                )
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

    // ────────────────────────── KYC Logic (existing) ───────────────────

    private fun calculateKycUiModel(): KycUiModel {
        val isLoadBoardUser = userPrefs.isLoadBoardClient && userPrefs.isLoadBoardSupplier

        if (!isLoadBoardUser || userPrefs.isUserVerfied) {
            return KycUiModel(uiState = KycUiState.COMPLETED, progress = 100)
        }

        val steps = listOf(
            userPrefs.pancard.isNotEmpty(),
            userPrefs.aadhaarNumber.isNotNullOrEmpty() ||
                    userPrefs.gstNumber.isNotNullOrEmpty() ||
                    userPrefs.cinNumber.isNotNullOrEmpty() ||
                    userPrefs.shopNumber.isNotNullOrEmpty() ||
                    userPrefs.udyogNumber.isNotNullOrEmpty(),
            userPrefs.businessAddress.isNotEmpty(),
            if (userPrefs.userMode.equals("post_load", true))
                true
            else
                userPrefs.rcNumber.isNotEmpty() || userPrefs.isTruckingDocumentUploaded,
            userPrefs.ifscCode.isNotEmpty() &&
                    userPrefs.accNumber.isNotEmpty() &&
                    !userPrefs.accNumber.equals("Not Available", true),
            userPrefs.vendorPolicyAccepted
        )

        val totalSteps = steps.size
        val completedSteps = steps.count { it }

        val uiState = when (completedSteps) {
            0 -> KycUiState.START
            totalSteps -> {
                when (userPrefs.verificationStatus) {
                    "pending" -> KycUiState.VERIFICATION_PENDING
                    "success" -> KycUiState.COMPLETED
                    "failed" -> KycUiState.REJECTED
                    else -> KycUiState.CONTINUE
                }
            }
            else -> KycUiState.CONTINUE
        }

        val progress = when (uiState) {
            KycUiState.START -> 0
            KycUiState.CONTINUE -> 50
            else -> 100
        }

        return KycUiModel(uiState = uiState, progress = progress)
    }
}
