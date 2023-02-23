package com.delhivery.axle.api.service

import com.delhivery.axle.api.request.FuelPayoutRequest
import com.delhivery.axle.api.request.FuelPayoutResponse
import com.delhivery.axle.api.response.BaseMessageResponse
import com.delhivery.axle.api.response.BaseResponse
import com.delhivery.axle.api.response.TransactionsResponse
import com.delhivery.axle.api.response.TripMeterResponse
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import io.reactivex.Single
import retrofit2.http.*

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
    @Query("truck_types") truckType: String? = null,
    @Query("axle_current_week_loads") currWeekLoads: String?,
    @Query("apply_100km_logic") nearby100kmcities: Boolean?,
    @Query("request_types") requestType:String?,
    @Query("contract_type") contractType:String?,
    @Query("active_contract") activeContract:Boolean?
  ): Single<BaseResponse<TransactionsResponse>>

  /**
   * Loadboard transactions
   */
  @GET("/transactions/loadboard/")
  fun loadBoardTransactions(
    @Query("sp_id") userId: String,
    @Query("offset") offset: Int,
    @Query("limit") limit: Int,
    @Query("demand_types") vendorType: String ? = "orion",
    @Query("truck_types") vehicleType: String? = null,
    @Query("valid_loads_only") validLoads: String = "yes",
    @Query("exclude_truck_types") excludeTruckTypes: String? = null,
    @Query("filter_vehicle_type") filterVehicleType: Boolean?= null,
    @Query("bidding_going_on") biddingGoingOn: Boolean?= false,
    @Query("exclude_trip_ids") excludeTripIds: String? = null
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

  @PATCH("/admin/transactions/{transactionId}/")
  fun updateTripForFuelPayout(
      @Path("transactionId") transactionId: String,
      @Body request: FuelPayoutRequest
  ): Single<BaseResponse<FuelPayoutResponse>>

  /**
   * Loadboard transactions
   */
  @GET("/transactions/loadboard/contracts")
  fun contractsTransactions(
    @Query("sp_id") userId: String,
    @Query("offset") offset: Int,
    @Query("limit") limit: Int,
    @Query("demand_types") vendorType: String ?,
    @Query("new_limit") new_limit: Boolean= true
  ): Single<BaseResponse<TransactionsResponse>>

}