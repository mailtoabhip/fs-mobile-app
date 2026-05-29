package com.delhivery.axle.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.SalesCodeRepository
import com.delhivery.axle.api.response.KycInitiateResponse
import com.delhivery.axle.api.response.KycTypesResponse
import com.delhivery.axle.api.response.KycVerifyResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastagKycViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : ViewModel() {

    private val _kycTypesState = MutableLiveData<Resource<KycTypesResponse>>()
    val kycTypesState: LiveData<Resource<KycTypesResponse>> = _kycTypesState

    private val _kycInitiateState = MutableLiveData<Resource<KycInitiateResponse>>()
    val kycInitiateState: LiveData<Resource<KycInitiateResponse>> = _kycInitiateState

    private val _kycVerifyState = MutableLiveData<Resource<KycVerifyResponse>>()
    val kycVerifyState: LiveData<Resource<KycVerifyResponse>> = _kycVerifyState

    fun fetchKycTypes(bankCode: String) {
        viewModelScope.launch {
            _kycTypesState.value = Resource.Loading
            val result = salesCodeRepository.getKycTypes(bankCode)
            _kycTypesState.value = result
        }
    }

    fun initiateKyc(bankCode: String, kycType: String) {
        viewModelScope.launch {
            _kycInitiateState.value = Resource.Loading
            val result = salesCodeRepository.initiateKyc(bankCode, kycType)
            _kycInitiateState.value = result
        }
    }

    fun verifyAndCreateKyc(journeyId: String, otp: String, bankCode: String, kycType: String) {
        viewModelScope.launch {
            _kycVerifyState.value = Resource.Loading
            val result = salesCodeRepository.verifyAndCreateKyc(journeyId, otp, bankCode, kycType)
            _kycVerifyState.value = result
        }
    }
}
