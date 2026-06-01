package com.delhivery.axle.ui.fastag.pending

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class PendingActionsViewModel @Inject constructor() : BaseViewModel() {

    private val _pendingOrders = MutableLiveData<List<PendingOrder>>()
    val pendingOrders: LiveData<List<PendingOrder>> = _pendingOrders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchPendingOrders() {
        _isLoading.value = true
        _error.value = null

        // TODO: Replace with real API call
        _isLoading.value = false
        _pendingOrders.value = getMockPendingOrders()
    }

    private fun getMockPendingOrders(): List<PendingOrder> {
        return listOf(
            PendingOrder(
                orderId = "#41641",
                date = "26 May 2026",
                pendingCount = 4,
                vehicles = listOf(
                    PendingVehicle("Vehicle Class 5", "#607318-001-0000343", null, PendingActionType.ASSIGNMENT, "RED"),
                    PendingVehicle("Vehicle Class 6", "#607318-001-0000343", null, PendingActionType.ACTIVATION, "YELLOW"),
                    PendingVehicle("Vehicle Class 7", null, null, PendingActionType.KYC, "GREEN"),
                    PendingVehicle("Vehicle Class 5", "#607318-001-0000343", "DL01CA1234", PendingActionType.HANDOVER, "RED")
                ),
                isExpanded = true
            ),
            PendingOrder(
                orderId = "#42612",
                date = "23 April 2026",
                pendingCount = 2,
                vehicles = listOf(
                    PendingVehicle("Vehicle Class 7", null, null, PendingActionType.KYC, "GREEN"),
                    PendingVehicle("Vehicle Class 5", "#607318-001-0000343", "DL01CA1234", PendingActionType.HANDOVER, "RED")
                )
            )
        )
    }
}
