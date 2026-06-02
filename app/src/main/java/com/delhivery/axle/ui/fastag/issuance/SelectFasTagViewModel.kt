package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.SalesCodeRepository
import com.delhivery.axle.api.response.VehicleClassResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectFasTagViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : ViewModel() {

    private val _vehicleClassesState = MutableLiveData<Resource<VehicleClassResponse>>()
    val vehicleClassesState: LiveData<Resource<VehicleClassResponse>> = _vehicleClassesState

    fun fetchVehicleClasses() {
        viewModelScope.launch {
            _vehicleClassesState.value = Resource.Loading
            val result = salesCodeRepository.getVehicleClasses()
            _vehicleClassesState.value = result
        }
    }
}
