package com.dfd.delfin.ui.fastag.issuance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.api.repository.SalesCodeRepository
import com.dfd.delfin.api.response.KycInitiateResponse
import com.dfd.delfin.api.response.KycTypesResponse
import com.dfd.delfin.api.response.KycVerifyResponse
import com.dfd.delfin.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class FastagKycViewModel @Inject constructor(
    private val salesCodeRepository: SalesCodeRepository
) : BaseViewModel() {

    private val _kycTypesState = MutableLiveData<Resource<KycTypesResponse>>()
    val kycTypesState: LiveData<Resource<KycTypesResponse>> = _kycTypesState

    private val _kycInitiateState = MutableLiveData<Resource<KycInitiateResponse>>()
    val kycInitiateState: LiveData<Resource<KycInitiateResponse>> = _kycInitiateState

    private val _kycVerifyState = MutableLiveData<Resource<KycVerifyResponse>>()
    val kycVerifyState: LiveData<Resource<KycVerifyResponse>> = _kycVerifyState

    fun fetchKycTypes(bankCode: String) {
        viewModelScope.launch {
            showProgress()
            _kycTypesState.value = Resource.Loading
            val result = salesCodeRepository.getKycTypes(bankCode)
            _kycTypesState.value = result
            showProgress(false)
        }
    }

    fun initiateKyc(bankCode: String, kycType: String) {
        viewModelScope.launch {
            showProgress()
            _kycInitiateState.value = Resource.Loading
            val result = salesCodeRepository.initiateKyc(bankCode, kycType)
            _kycInitiateState.value = result
            showProgress(false)
        }
    }

    fun verifyAndCreateKyc(journeyId: String, otp: String, bankCode: String, kycType: String) {
        viewModelScope.launch {
            showProgress()
            _kycVerifyState.value = Resource.Loading
            val result = salesCodeRepository.verifyAndCreateKyc(journeyId, otp, bankCode, kycType)
            _kycVerifyState.value = result
            showProgress(false)
        }
    }
}
