package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.ConfirmCollectionRequest
import com.delhivery.axle.api.request.CreateOrderRequest
import com.delhivery.axle.api.request.IssueTagRequest
import com.delhivery.axle.api.request.PaymentBreakupRequest
import com.delhivery.axle.api.request.PaymentCheckoutRequest
import com.delhivery.axle.api.request.ValidateSalesRequest
import com.delhivery.axle.api.response.IssueTagResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.ConfirmCollectionResponse
import com.delhivery.axle.api.response.CreateOrderResponse
import com.delhivery.axle.api.response.FastagOrdersResponse
import com.delhivery.axle.api.response.PaymentBreakupResponse
import com.delhivery.axle.api.response.PaymentCheckoutResponse
import com.delhivery.axle.api.response.ValidateSalesCodeResponse
import com.delhivery.axle.api.response.VehicleCheckResponse
import com.delhivery.axle.api.response.VehicleClassResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FasTAGIssuanceService {
    @POST("/fastag/v1/sales-code/validate")
    suspend fun validateSalesCode(
        @Body validateSalesRequest: ValidateSalesRequest
    ): BaseResponse<ValidateSalesCodeResponse>

    @GET("/fastag/v1/vehicle-classes")
    suspend fun getVehicleClasses(
    ): BaseResponse<VehicleClassResponse>

    @GET("/fastag/v1/orders/by-vendor")
    suspend fun getOrdersByVendor(
        @Query("sales_code") salesCode: String,
        @Query("order_id") orderId: String
    ): BaseResponse<FastagOrdersResponse>

    @POST("/fastag/v1/order/confirm-collection")
    suspend fun confirmCollection(
        @Body request: ConfirmCollectionRequest
    ): BaseResponse<ConfirmCollectionResponse>

    @GET("/fastag/v1/vehicle/verify")
    suspend fun checkVehicle(
        @Query("vrn") vrn: String
    ): BaseResponse<VehicleCheckResponse>

    @Headers("No-Request-Id: true")
    @POST("/fastag/v1/payment/breakup")
    suspend fun getPaymentBreakup(
        @Body request: PaymentBreakupRequest
    ): BaseResponse<PaymentBreakupResponse>

    @POST("/fastag/v1/order/create")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): BaseResponse<CreateOrderResponse>

    @Headers("No-Request-Id: true")
    @POST("/fastag/v1/payment/checkout")
    suspend fun paymentCheckout(
        @Body request: PaymentCheckoutRequest
    ): BaseResponse<PaymentCheckoutResponse>

    /**
     * Issue FASTag and process payment.
     */
    @POST("/fastag/v1/issuance/issue-tag")
    suspend fun issueTag(
        @Body request: IssueTagRequest
    ): BaseResponse<IssueTagResponse>
}