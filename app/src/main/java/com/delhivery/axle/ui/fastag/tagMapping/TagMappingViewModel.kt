package com.delhivery.axle.ui.fastag.tagMapping

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.response.BarcodeLookupResponse
import com.delhivery.axle.api.response.IssueTagResponse
import com.delhivery.axle.api.response.ProductBarcodeResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class TagMappingViewModel @Inject constructor(
    private val fastagRepository: FastagRepository
) : ViewModel() {

    val barcodeLookupData = MutableLiveData<Resource<BarcodeLookupResponse>>()
    val productBarcodeData = MutableLiveData<Resource<ProductBarcodeResponse>>()
    val generateOtpData = MutableLiveData<Resource<Any>>()
    val issueTagData = MutableLiveData<Resource<IssueTagResponse>>()

    /**
     * Lookup barcode from dispatch table.
     */
    fun fetchBarcodeLookup(orderId: String, orderItemId: Int, vehicleClass: String) {
        barcodeLookupData.value = Resource.Loading
        viewModelScope.launch {
            val result = fastagRepository.barcodeLookup(orderId, orderItemId, vehicleClass)
            barcodeLookupData.postValue(result)
        }
    }

    /**
     * Search products and barcodes from IDFC.
     */
    fun searchProductBarcode(journeyId: String, barcode: String) {
        productBarcodeData.value = Resource.Loading
        viewModelScope.launch {
            val result = fastagRepository.searchProductBarcode(journeyId, barcode)
            productBarcodeData.postValue(result)
        }
    }

    /**
     * Generate consent OTP for tag mapping.
     */
    fun generateOtp(journeyId: String, barcode: String, tagId: String) {
        generateOtpData.value = Resource.Loading
        viewModelScope.launch {
            val result = fastagRepository.generateOtp(journeyId, barcode, tagId)
            generateOtpData.postValue(result)
        }
    }

    /**
     * Issue FASTag and process payment.
     */
    fun issueTag(journeyId: String, orderId: String, orderItemId: Int, barcode: String, otp: String) {
        issueTagData.value = Resource.Loading
        viewModelScope.launch {
            val result = fastagRepository.issueTag(journeyId, orderId, orderItemId, barcode, otp)
            issueTagData.postValue(result)
        }
    }
}
