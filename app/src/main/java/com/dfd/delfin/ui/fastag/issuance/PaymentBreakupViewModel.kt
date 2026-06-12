package com.dfd.delfin.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.api.repository.SalesCodeRepository
import com.dfd.delfin.api.request.PaymentBreakupItem
import com.dfd.delfin.api.request.PaymentBreakupRequest
import com.dfd.delfin.api.request.PaymentCheckoutRequest
import com.dfd.delfin.api.response.PaymentBreakupResponse
import com.dfd.delfin.api.response.PaymentCheckoutResponse
import com.dfd.delfin.ui.base.BaseViewModel
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

    private val _isDataLoaded = MutableLiveData(false)
    val isDataLoaded: LiveData<Boolean> = _isDataLoaded

    private val _warningMessage = MutableLiveData<String?>()
    val warningMessage: LiveData<String?> = _warningMessage

    private val _checkoutState = MutableLiveData<Resource<PaymentCheckoutResponse>>()
    val checkoutState: LiveData<Resource<PaymentCheckoutResponse>> = _checkoutState

    fun fetchPaymentBreakup(salesCode: String, paymentMethod: String, items: List<PaymentBreakupItem>) {
        viewModelScope.launch {
            showProgress()
            _breakupState.value = Resource.Loading
            val request = PaymentBreakupRequest(
                salesCode = salesCode,
                paymentMethod = paymentMethod,
                items = items
            )
            val result = salesCodeRepository.getPaymentBreakup(request)
            _breakupState.value = result
            showProgress(false)

            if (result is Resource.Success) {
                result.data?.let { data ->
                    _grandTotal.value = data.grandTotal
                    _walletBalance.value = data.wallet.currentBalance
                    _isWalletSufficient.value = data.wallet.sufficient
                    _warningMessage.value = if (!data.wallet.sufficient) data.message else null
                    _isDataLoaded.value = true
                }
            }
        }
    }

    fun paymentCheckout(orderId: String, totalAmount: String) {
        viewModelScope.launch {
            showProgress()
            _checkoutState.value = Resource.Loading
            val request = PaymentCheckoutRequest(
                orderId = orderId,
                totalAmount = totalAmount,
                idempotencyKey = "CHK-${java.util.UUID.randomUUID()}"
            )
            val result = salesCodeRepository.paymentCheckout(request)
            _checkoutState.value = result
            showProgress(false)
        }
    }
}
