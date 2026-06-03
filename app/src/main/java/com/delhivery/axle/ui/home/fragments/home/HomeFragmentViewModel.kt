package com.delhivery.axle.ui.home.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.ServiceGroup
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.common.UiEvent
import com.delhivery.axle.ui.common.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    // ────────────────────────── KYC (existing) ─────────────────────────

    private val _kycUiModel = MutableLiveData<KycUiModel>()
    val kycUiModel: LiveData<KycUiModel> = _kycUiModel

    // ────────────────────────── FASTag Pending Actions ─────────────────

    private val _fastagPendingCount = MutableLiveData<Int>(0)
    val fastagPendingCount: LiveData<Int> = _fastagPendingCount

    /**
     * Fetch FASTag pending actions count from API.
     */
    fun fetchFastagPendingCount() {
        viewModelScope.launch {
            when (val result = fastagRepository.getPendingActions()) {
                is Resource.Success -> {
                    _fastagPendingCount.value = result.data?.count ?: 0
                }
                is Resource.Failure -> {
                    _fastagPendingCount.value = 0
                }
                Resource.Loading -> { /* no-op */ }
            }
        }
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

    // ────────────────────────── KYC Logic (existing) ───────────────────


}
