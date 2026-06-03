package com.delhivery.axle.api.repository

import com.delhivery.axle.api.request.ValidateSalesRequest
import com.delhivery.axle.api.response.FastagOrdersResponse
import com.delhivery.axle.api.response.ValidateSalesCodeResponse
import com.delhivery.axle.api.response.VehicleClassResponse
import com.delhivery.axle.api.response.toResource
import com.delhivery.axle.api.service.FasTAGIssuanceService
import com.delhivery.axle.injection.qualifier.IoDispatcher
import com.delhivery.axle.utils.ErrorLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesCodeRepository @Inject constructor(
    private val salesCodeService: FasTAGIssuanceService,
    private val kycService: com.delhivery.axle.api.service.FasTAGKycService,
    errorLogger: ErrorLogger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseRepository(errorLogger) {

    suspend fun validateSalesCode(salesCode: String): Resource<ValidateSalesCodeResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorId = "vendor-123"
                val request = ValidateSalesRequest(salesCode = salesCode)
                val response = salesCodeService.validateSalesCode(vendorId, request)
                response.toResource()
            }
        }

    suspend fun getVehicleClasses(): Resource<VehicleClassResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val response = salesCodeService.getVehicleClasses(vendorID)
                response.toResource()
            }
        }

    suspend fun getOrdersByVendor(salesCode: String, limit: Int = 20, offset: Int = 0): Resource<FastagOrdersResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val response = salesCodeService.getOrdersByVendor(vendorID, salesCode, limit, offset)
                response.toResource()
            }
        }

    suspend fun confirmCollection(
        orderId: String,
        request: com.delhivery.axle.api.request.ConfirmCollectionRequest
    ): Resource<com.delhivery.axle.api.response.ConfirmCollectionResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val response = salesCodeService.confirmCollection(vendorID, orderId, request)
                response.toResource()
            }
        }

    suspend fun checkVehicle(vehicleNumber: String): Resource<com.delhivery.axle.api.response.VehicleCheckResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val response = salesCodeService.checkVehicle(vendorID, vehicleNumber)
                response.toResource()
            }
        }

    suspend fun kycOnboardValidate(bankCode: String): Resource<com.delhivery.axle.api.response.KycOnboardValidateResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val response = kycService.kycOnboardValidate(vendorID, bankCode)
                response.toResource()
            }
        }

    suspend fun getKycTypes(bankCode: String): Resource<com.delhivery.axle.api.response.KycTypesResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val response = kycService.getKycTypes(vendorID, bankCode)
                response.toResource()
            }
        }

    suspend fun initiateKyc(bankCode: String, kycType: String): Resource<com.delhivery.axle.api.response.KycInitiateResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val request = com.delhivery.axle.api.request.KycInitiateRequest(bankCode = bankCode, kycType = kycType)
                val response = kycService.initiateKyc(vendorID, request)
                response.toResource()
            }
        }

    suspend fun verifyAndCreateKyc(
        journeyId: String,
        otp: String,
        bankCode: String,
        kycType: String
    ): Resource<com.delhivery.axle.api.response.KycVerifyResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val request = com.delhivery.axle.api.request.KycVerifyRequest(
                    journeyId = journeyId,
                    otp = otp,
                    bankCode = bankCode,
                    kycType = kycType
                )
                val response = kycService.verifyAndCreateKyc(vendorID, request)
                response.toResource()
            }
        }

    suspend fun getPaymentBreakup(
        request: com.delhivery.axle.api.request.PaymentBreakupRequest
    ): Resource<com.delhivery.axle.api.response.PaymentBreakupResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "a1e38d7a-7001-7073-d3e4-320e007ddaad"
                val response = salesCodeService.getPaymentBreakup(vendorID, request)
                response.toResource()
            }
        }

    suspend fun createOrder(
        request: com.delhivery.axle.api.request.CreateOrderRequest
    ): Resource<com.delhivery.axle.api.response.CreateOrderResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val vendorID = "vendor-123"
                val response = salesCodeService.createOrder(vendorID, request)
                response.toResource()
            }
        }
}
