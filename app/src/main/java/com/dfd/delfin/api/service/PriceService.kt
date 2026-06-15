package com.dfd.delfin.api.service

import com.dfd.delfin.api.request.OfferObjectResponse
import com.dfd.delfin.api.request.OfferRequest
import com.dfd.delfin.api.request.OfferState
import com.dfd.delfin.api.request.PriceDetailRequest
import com.dfd.delfin.api.request.UpdatePriceRequest
import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.GetPricingDataResponse
import com.dfd.delfin.api.response.PricingResponse
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
  fun getPricingData(@Body offerRequest: PriceDetailRequest): Single<BaseResponse<GetPricingDataResponse>>


}