package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.SalesCodeRepository
import com.delhivery.axle.api.response.VehicleCheckResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddVehicleViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : ViewModel() {

    private val _vehicleCheckState = MutableLiveData<Resource<VehicleCheckResponse>>()
    val vehicleCheckState: LiveData<Resource<VehicleCheckResponse>> = _vehicleCheckState

    fun checkVehicle(vehicleNumber: String) {
        viewModelScope.launch {
            _vehicleCheckState.value = Resource.Loading
            val result = salesCodeRepository.checkVehicle(vehicleNumber)
            _vehicleCheckState.value = result
        }
    }
}
