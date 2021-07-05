package com.delhivery.axle.api.service

import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.api.response.TripMeterResponse
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Handle network calls to Transaction Service
 */
interface TransactionService {

  /**
   * List all transactions basis [status_list] [source] [destination] [truckType]
   */
  @GET("/transactions/list/")
  fun transactions(
    @Query("offset") offset: Int,
    @Query("status_list") status_list: String?,
    @Query("origin_city_code") source: String? = null,
    @Query("destination_city_code") destination: String? = null,
    @Query("truck_type") truckType: String? = null,
    @Query("axle_current_week_loads") currWeekLoads: String = "yes",
    @Query("apply_100km_logic") nearby100kmcities: Boolean = true
  ): Single<BaseResponse<TransactionsResponse>>

  /**
   * Loadboard transactions
   */
  @GET("/transactions/loadboard/")
  fun loadBoardTransactions(
    @Query("sp_id") userId: String,
    @Query("offset") offset: Int,
    @Query("limit") limit: Int,
    @Query("vendor_type") vendorType: String ? = "orion",
    @Query("vehicle_type") vehicleType: String,
    @Query("valid_loads_only") validLoads: String = "yes",
    @Query("speed") speed: String
  ): Single<BaseResponse<TransactionsResponse>>

  /**
   * Transaction details
   */
  @GET("/transactions/")
  fun transactionDetails(
    @Query("uuid") transactionId: String
  ): Single<BaseResponse<HomeBidsRequestItemData>>

  /**
   * Bulk transaction ids
   *
   * @param transactionIds Comma separated ids
   */
  @GET("/transactions/list/")
  fun bulkTransactions(
    @Query("transactions_ids") transactionIds: String
  ): Single<BaseResponse<TransactionsResponse>>

  /**
   * Fetch trip meter details
   */
  @GET("/transactions/tripmeter/{sp_id}/")
  fun transactionsTripMeter(
    @Path("sp_id") userId: String
  ): Single<BaseResponse<TripMeterResponse>>
}