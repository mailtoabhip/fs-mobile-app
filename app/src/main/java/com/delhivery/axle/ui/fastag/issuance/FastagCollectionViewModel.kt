package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.SalesCodeRepository
import com.delhivery.axle.api.request.ConfirmCollectionRequest
import com.delhivery.axle.api.response.ConfirmCollectionResponse
import com.delhivery.axle.api.response.FastagOrdersResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastagCollectionViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : ViewModel() {

    private val _ordersState = MutableLiveData<Resource<FastagOrdersResponse>>()
    val ordersState: LiveData<Resource<FastagOrdersResponse>> = _ordersState

    private val _confirmState = MutableLiveData<Resource<ConfirmCollectionResponse>>()
    val confirmState: LiveData<Resource<ConfirmCollectionResponse>> = _confirmState

    fun fetchOrders(salesCode: String) {
        viewModelScope.launch {
            _ordersState.value = Resource.Loading
            val result = salesCodeRepository.getOrdersByVendor(salesCode)
            _ordersState.value = result
        }
    }

    fun confirmCollection(orderId: String, request: ConfirmCollectionRequest) {
        viewModelScope.launch {
            _confirmState.value = Resource.Loading
            val result = salesCodeRepository.confirmCollection(orderId, request)
            _confirmState.value = result
        }
    }
}
