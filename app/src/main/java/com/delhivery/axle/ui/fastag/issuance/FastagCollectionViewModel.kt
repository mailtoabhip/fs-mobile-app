package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.SalesCodeRepository
import com.delhivery.axle.api.request.ConfirmCollectionRequest
import com.delhivery.axle.api.response.ConfirmCollectionResponse
import com.delhivery.axle.api.response.FastagOrdersResponse
import com.delhivery.axle.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastagCollectionViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : BaseViewModel() {

    private val _ordersState = MutableLiveData<Resource<FastagOrdersResponse>>()
    val ordersState: LiveData<Resource<FastagOrdersResponse>> = _ordersState

    private val _confirmState = MutableLiveData<Resource<ConfirmCollectionResponse>>()
    val confirmState: LiveData<Resource<ConfirmCollectionResponse>> = _confirmState

    fun fetchOrders(salesCode: String, orderId: String) {
        viewModelScope.launch {
            showProgress()
            _ordersState.value = Resource.Loading
            val result = salesCodeRepository.getOrdersByVendor(salesCode, orderId)
            _ordersState.value = result
            showProgress(false)
        }
    }

    fun confirmCollection(request: ConfirmCollectionRequest) {
        viewModelScope.launch {
            showProgress()
            _confirmState.value = Resource.Loading
            val result = salesCodeRepository.confirmCollection(request)
            _confirmState.value = result
            showProgress(false)
        }
    }
}
