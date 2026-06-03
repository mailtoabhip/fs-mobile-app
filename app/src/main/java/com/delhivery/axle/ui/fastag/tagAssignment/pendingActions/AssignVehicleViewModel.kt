package com.delhivery.axle.ui.fastag.tagAssignment.pendingActions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class AssignVehicleViewModel @Inject constructor() : BaseViewModel() {

    private val _availableVehicles = MutableLiveData<List<AvailableVehicle>>()
    val availableVehicles: LiveData<List<AvailableVehicle>> = _availableVehicles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchAvailableVehicles() {
        _isLoading.value = true
        _error.value = null

        // TODO: Replace with real API call
        _isLoading.value = false
        _availableVehicles.value = getMockVehicles()
    }

    private fun getMockVehicles(): List<AvailableVehicle> {
        return listOf(
            AvailableVehicle("MH01CA1234", "MEXKRCPA5PG019"),
            AvailableVehicle("DL01CA5678", "MEXKRCPA5PG020"),
            AvailableVehicle("KA01AB9012", "MEXKRCPA5PG021")
        )
    }
}

/**
 * Represents an available vehicle that can be assigned.
 */
data class AvailableVehicle(
    val vehicleNumber: String,
    val chassisNumber: String
)
