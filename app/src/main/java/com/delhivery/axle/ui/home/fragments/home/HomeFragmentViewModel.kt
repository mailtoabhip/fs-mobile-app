package com.delhivery.axle.ui.home.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(
) : BaseViewModel() {

    // ────────────────────────── KYC (existing) ─────────────────────────

    private val _kycUiModel = MutableLiveData<KycUiModel>()
    val kycUiModel: LiveData<KycUiModel> = _kycUiModel

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
