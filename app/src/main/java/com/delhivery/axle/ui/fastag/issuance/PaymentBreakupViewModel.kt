package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.ui.base.BaseViewModel
import javax.inject.Inject

class PaymentBreakupViewModel @Inject constructor() : BaseViewModel() {

    private val _issuanceFee = MutableLiveData(0)
    val issuanceFee: LiveData<Int> = _issuanceFee

    private val _securityDeposit = MutableLiveData(0)
    val securityDeposit: LiveData<Int> = _securityDeposit

    private val _minimumRecharge = MutableLiveData(0)
    val minimumRecharge: LiveData<Int> = _minimumRecharge

    private val _platformFee = MutableLiveData(0)
    val platformFee: LiveData<Int> = _platformFee

    private val _grandTotal = MutableLiveData(0)
    val grandTotal: LiveData<Int> = _grandTotal

    private val _walletBalance = MutableLiveData(0)
    val walletBalance: LiveData<Int> = _walletBalance

    private val _isWalletSufficient = MutableLiveData(false)
    val isWalletSufficient: LiveData<Boolean> = _isWalletSufficient

    fun setBreakupData(
        issuanceFee: Int,
        securityDeposit: Int,
        minimumRecharge: Int,
        platformFee: Int,
        grandTotal: Int,
        walletBalance: Int
    ) {
        _issuanceFee.value = issuanceFee
        _securityDeposit.value = securityDeposit
        _minimumRecharge.value = minimumRecharge
        _platformFee.value = platformFee
        _grandTotal.value = grandTotal
        _walletBalance.value = walletBalance
        _isWalletSufficient.value = walletBalance >= grandTotal
    }

    // TODO: Call this from API response once endpoint is ready
    fun fetchPaymentBreakup() {
        // Placeholder - will be replaced with actual API call
        setBreakupData(
            issuanceFee = 100,
            securityDeposit = 200,
            minimumRecharge = 200,
            platformFee = 200,
            grandTotal = 400,
            walletBalance = 0
        )
    }
}
