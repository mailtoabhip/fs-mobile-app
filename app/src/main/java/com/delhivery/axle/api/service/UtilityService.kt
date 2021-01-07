package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.ChargesMapping
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Handle network calls to Utility Service
 */
interface UtilityService {

  /**
   * Get charges
   */
  @POST("all_charges")
  fun fetchCharges(@Body payload: JsonObject): Single<BaseResponse<Map<String, ChargesMapping>>>

}