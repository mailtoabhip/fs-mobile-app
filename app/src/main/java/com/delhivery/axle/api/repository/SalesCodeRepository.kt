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

    suspend fun getOrdersByVendor(limit: Int = 20, offset: Int = 0): Resource<FastagOrdersResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.getOrdersByVendor(limit, offset)
                response.toResource()
            }
        }

    suspend fun confirmCollection(
        orderId: String,
        request: com.delhivery.axle.api.request.ConfirmCollectionRequest
    ): Resource<com.delhivery.axle.api.response.ConfirmCollectionResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.confirmCollection(orderId, request)
                response.toResource()
            }
        }

    suspend fun checkVehicle(vehicleNumber: String): Resource<com.delhivery.axle.api.response.VehicleCheckResponse> =
        withContext(ioDispatcher) {
            safeApiCall {
                val response = salesCodeService.checkVehicle(vehicleNumber)
                response.toResource()
            }
        }
}
