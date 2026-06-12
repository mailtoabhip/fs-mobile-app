package com.dfd.delfin.ui.fastag.tagAssignment.pendingActions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.ui.base.BaseViewModel
import javax.inject.Inject

class AssignVehicleViewModel @Inject constructor() : BaseViewModel() {

    private val _availableVehicles = MutableLiveData<List<AvailableVehicle>>()
    val availableVehicles: LiveData<List<AvailableVehicle>> = _availableVehicles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchAvailableVehicles(vrn: String? = null) {
        _isLoading.value = true
        _error.value = null

        _isLoading.value = false
        if (!vrn.isNullOrEmpty()) {
            // Show only the VRN passed from pending actions
            _availableVehicles.value = listOf(AvailableVehicle(vehicleNumber = vrn, chassisNumber = ""))
        } else {
            _availableVehicles.value = emptyList()
        }
    }
}

/**
 * Represents an available vehicle that can be assigned.
 */
data class AvailableVehicle(
    val vehicleNumber: String,
    val chassisNumber: String
)
