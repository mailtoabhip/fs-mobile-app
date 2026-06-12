package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.ConfirmCollectionRequest
import com.dfd.delfin.api.request.CreateOrderRequest
import com.dfd.delfin.api.request.IssueTagRequest
import com.dfd.delfin.api.request.PaymentBreakupRequest
import com.dfd.delfin.api.request.PaymentCheckoutRequest
import com.dfd.delfin.api.request.ValidateSalesRequest
import com.dfd.delfin.api.response.IssueTagResponse
import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.ConfirmCollectionResponse
import com.dfd.delfin.api.response.CreateOrderResponse
import com.dfd.delfin.api.response.FastagOrdersResponse
import com.dfd.delfin.api.response.PaymentBreakupResponse
import com.dfd.delfin.api.response.PaymentCheckoutResponse
import com.dfd.delfin.api.response.ValidateSalesCodeResponse
import com.dfd.delfin.api.response.VehicleCheckResponse
import com.dfd.delfin.api.response.VehicleClassResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
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

    @POST("/fastag/v1/payment/breakup")
    suspend fun getPaymentBreakup(
        @Body request: PaymentBreakupRequest
    ): BaseResponse<PaymentBreakupResponse>

    @POST("/fastag/v1/order/create")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): BaseResponse<CreateOrderResponse>

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