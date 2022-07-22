package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.OfferObjectResponse
import com.delhivery.axle.api.request.OfferRequest
import com.delhivery.axle.api.request.OfferResponse
import com.delhivery.axle.api.request.OfferState
import com.delhivery.axle.api.request.PriceDetailRequest
import com.delhivery.axle.api.request.UpdatePriceRequest
import com.delhivery.axle.api.response.BaseMessageResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.GetPricingDataResponse
import com.delhivery.axle.api.response.GetSupplierRewardsResponse
import com.delhivery.axle.api.response.PricingResponse
import com.delhivery.axle.api.response.SearchTripsResponse
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface  PriceService {

  /**
   * update pricing
   */
  @POST("/update_pricing_data")
  fun updatePricingData(
    @Body request: UpdatePriceRequest
  ): Single<BaseResponse<PricingResponse>>

  /**
   * get offers
   */
  @POST("/get_odvt_offers")
  fun getOffers(@Body offerRequest: OfferRequest): Single<BaseResponse<OfferObjectResponse>>

  /**
   * get offers state
   */
  @GET("/get_odvt_offers_updated_flag")
  fun getOffersFlag(): Single<BaseResponse<OfferState>>

  /**
   * get pricing data
   */
  @POST("/get_pricing_data")
  fun getPricingData(@Body offerRequest: PriceDetailRequest): Single<BaseMessageResponse>


}