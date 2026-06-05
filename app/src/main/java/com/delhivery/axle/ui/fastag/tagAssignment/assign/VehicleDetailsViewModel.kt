package com.delhivery.axle.ui.fastag.tagAssignment.assign

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.OrderItem
import com.delhivery.axle.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class VehicleDetailsViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : BaseViewModel() {

    private val _orderItemsState = MutableLiveData<Resource<List<OrderItem>>>()
    val orderItemsState: LiveData<Resource<List<OrderItem>>> = _orderItemsState

    fun fetchOrderItems(orderId: String) {
        viewModelScope.launch {
            _orderItemsState.value = Resource.Loading
            val result = fastagRepository.getOrderItems(orderId)
            _orderItemsState.value = result
        }
    }
}
