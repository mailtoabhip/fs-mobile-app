package com.dfd.delfin.api.repository

import com.dfd.delfin.api.request.ValidateSalesRequest
import com.dfd.delfin.api.response.FastagOrdersResponse
import com.dfd.delfin.api.response.ValidateSalesCodeResponse
import com.dfd.delfin.api.response.VehicleClassResponse
import com.dfd.delfin.api.response.toResource
import com.dfd.delfin.api.service.FasTAGIssuanceService
import com.dfd.delfin.injection.qualifier.IoDispatcher
import com.dfd.delfin.utils.ErrorLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesCodeRepository @Inject constructor(
    private val salesCodeService: FasTAGIssuanceService,
    private val kycService: com.dfd.delfin.api.service.FasTAGKycService,
    errorLogger: ErrorLogger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseRepository(errorLogger) {

    suspend fun validateSalesCode(salesCode: String): Resource<ValidateSalesCodeResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val request = ValidateSalesRequest(salesCode = salesCode)
                val response = salesCodeService.validateSalesCode(request)
                response.toResource()
            }
        }

    suspend fun getVehicleClasses(): Resource<VehicleClassResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.getVehicleClasses()
                response.toResource()
            }
        }

    suspend fun getOrdersByVendor(salesCode: String, orderId: String): Resource<FastagOrdersResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.getOrdersByVendor(salesCode, orderId)
                response.toResource()
            }
        }

    suspend fun confirmCollection(
        request: com.dfd.delfin.api.request.ConfirmCollectionRequest
    ): Resource<com.dfd.delfin.api.response.ConfirmCollectionResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.confirmCollection(request)
                response.toResource()
            }
        }

    suspend fun checkVehicle(vehicleNumber: String): Resource<com.dfd.delfin.api.response.VehicleCheckResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.checkVehicle(vehicleNumber)
                response.toResource()
            }
        }

    suspend fun kycOnboardValidate(bankCode: String): Resource<com.dfd.delfin.api.response.KycOnboardValidateResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = kycService.kycOnboardValidate(bankCode)
                response.toResource()
            }
        }

    suspend fun getKycTypes(bankCode: String): Resource<com.dfd.delfin.api.response.KycTypesResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = kycService.getKycTypes(bankCode)
                response.toResource()
            }
        }

    suspend fun initiateKyc(bankCode: String, kycType: String): Resource<com.dfd.delfin.api.response.KycInitiateResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val request = com.dfd.delfin.api.request.KycInitiateRequest(bankCode = bankCode, kycType = kycType)
                val response = kycService.initiateKyc(request)
                response.toResource()
            }
        }

    suspend fun verifyAndCreateKyc(
        journeyId: String,
        otp: String,
        bankCode: String,
        kycType: String
    ): Resource<com.dfd.delfin.api.response.KycVerifyResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val request = com.dfd.delfin.api.request.KycVerifyRequest(
                    journeyId = journeyId,
                    otp = otp,
                    bankCode = bankCode,
                    kycType = kycType
                )
                val response = kycService.verifyAndCreateKyc(request)
                response.toResource()
            }
        }

    suspend fun getPaymentBreakup(
        request: com.dfd.delfin.api.request.PaymentBreakupRequest
    ): Resource<com.dfd.delfin.api.response.PaymentBreakupResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.getPaymentBreakup(request)
                response.toResource()
            }
        }

    suspend fun createOrder(
        request: com.dfd.delfin.api.request.CreateOrderRequest
    ): Resource<com.dfd.delfin.api.response.CreateOrderResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.createOrder(request)
                response.toResource()
            }
        }

    suspend fun paymentCheckout(
        request: com.dfd.delfin.api.request.PaymentCheckoutRequest
    ): Resource<com.dfd.delfin.api.response.PaymentCheckoutResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.paymentCheckout(request)
                response.toResource()
            }
        }
}
