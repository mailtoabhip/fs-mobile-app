package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.SalesCodeRepository
import com.delhivery.axle.api.request.PaymentBreakupItem
import com.delhivery.axle.api.request.PaymentBreakupRequest
import com.delhivery.axle.api.response.PaymentBreakupResponse
import com.delhivery.axle.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class PaymentBreakupViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : BaseViewModel() {

    private val _breakupState = MutableLiveData<Resource<PaymentBreakupResponse>>()
    val breakupState: LiveData<Resource<PaymentBreakupResponse>> = _breakupState

    // Derived fields for data binding
    private val _grandTotal = MutableLiveData("")
    val grandTotal: LiveData<String> = _grandTotal

    private val _walletBalance = MutableLiveData("")
    val walletBalance: LiveData<String> = _walletBalance

    private val _isWalletSufficient = MutableLiveData(false)
    val isWalletSufficient: LiveData<Boolean> = _isWalletSufficient

    private val _warningMessage = MutableLiveData<String?>()
    val warningMessage: LiveData<String?> = _warningMessage

    fun fetchPaymentBreakup(salesCode: String, paymentMethod: String, items: List<PaymentBreakupItem>) {
        viewModelScope.launch {
            _breakupState.value = Resource.Loading
            val request = PaymentBreakupRequest(
                salesCode = salesCode,
                paymentMethod = paymentMethod,
                items = items
            )
            val result = salesCodeRepository.getPaymentBreakup(request)
            _breakupState.value = result

            if (result is Resource.Success) {
                result.data?.let { data ->
                    _grandTotal.value = data.grandTotal
                    _walletBalance.value = data.wallet.currentBalance
                    _isWalletSufficient.value = data.wallet.sufficient
                    _warningMessage.value = if (!data.wallet.sufficient) data.message else null
                }
            }
        }
    }
}
