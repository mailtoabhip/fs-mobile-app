package com.dfd.delfin.api.service

import com.dfd.delfin.api.response.*
import com.dfd.delfin.data.CityModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Handle network calls to User Service
 */
interface UserService {

  /**
   * Near by cities
   */
  @GET("/cities/suggestions/{cityId}")
  fun nearByLocations(
    @Path("cityId") cityId: String
  ): Single<BaseResponse<List<CityModel>>>

  @GET("/users/supplypartners")
  fun getOMCs(
    @Query("offset") offset: Int,
    @Query("limit") limit: Int,
    @Query("payee_type") payeeType: String
  ): Single<BaseResponse<OMCDetails>>

}