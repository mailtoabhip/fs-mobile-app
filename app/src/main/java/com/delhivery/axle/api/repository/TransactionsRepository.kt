package com.delhivery.axle.api.repository

import android.text.BoringLayout
import com.delhivery.axle.api.repository.ContractStatus.BiddingClosed
import com.delhivery.axle.api.repository.ContractStatus.Cancelled
import com.delhivery.axle.api.repository.ContractStatus.CollectingBids
import com.delhivery.axle.api.repository.ContractStatus.LiveBidding
import com.delhivery.axle.api.repository.ContractStatus.ResultDeclared
import com.delhivery.axle.api.repository.TransactionStatus.InEnquiry
import com.delhivery.axle.api.repository.TransactionStatus.Requested
import com.delhivery.axle.api.request.FuelPayoutRequest
import com.delhivery.axle.api.request.ReccomdationRequest
import com.delhivery.axle.api.service.RecommendationService
import com.delhivery.axle.api.service.TransactionService
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.utils.extensions.convertMessageResponse
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsRepository @Inject constructor(
  private val transactionService: TransactionService,
  private val userRepository: UserRepository,
  private val userPrefs: UserPrefs,
  private val recommendationService: RecommendationService
) : BaseRepository() {

  /**
   * Get user transactions
   */
  fun fetchLoadBoardTransactions(offset: Int, demand_type: String, vehicle_type: String?= null,excludeTruckTypes: String?= null, filterVehicleType: Boolean?= null, biddingGoingOn:Boolean = false, txnIds:String? = null) =
    transactionService.loadBoardTransactions(
      userRepository.userId(), offset, UserTripsLoadLimit, demand_type, vehicle_type,
      "yes", excludeTruckTypes, filterVehicleType, biddingGoingOn, txnIds
  ).convertResponse()

  /**
   * Get user transactions
   */
  fun fetchRecommTransactions(offset: Int, demand_type: String, vehicle_type: String?= null,excludeTruckTypes: String?= null, filterVehicleType: Boolean?= null, biddingGoingOn:Boolean = false) =
         recommendationService.recommendationTransactions(
           ReccomdationRequest( userRepository.userId(),UserTripsLoadLimit,offset,
             demand_type, vehicle_type)
          ).convertResponse()

  /**
   * Get contracts transactions
   */
  fun fetchContractsTransactions(offset: Int, demand_type: String, allActiveFetched:Boolean?,limit:Int,originCityList:String?) =
    transactionService.contractsTransactions(
      userRepository.userId(), offset, limit,demand_type, allActiveFetched = allActiveFetched,originCityList
    ).convertResponse()


  /**
   * Get contracts summary count
   */
  fun fetchContractsSummaryCount(originCityList:String?) =
    transactionService.contractsCountSummary("yes",originCityList
    ).convertResponse()
  /**
   * Search [TransactionStatus.Requested] transactions
   */
  fun searchTransactions(
    offset: Int,
    source: String?,
    destination: String?,
    truckType: String?,
    truckDisplayName: String?,
    contractStatus: String?,
    requestType:String?,
    contractType:String?
  ) = transactionService.transactions(
      offset, if(contractType!=ContractType.INTRACITY.type) Requested.statusId + "," + InEnquiry.statusId else null, source, destination, truckType, truckDisplayName,
    contractsMap[contractStatus]?.statusId, if(requestType=="load") "yes" else null,if(contractType==ContractType.INTRACITY.type) true else null, if(requestType=="load") "fixed,spot" else "contract",
    contractType,if(requestType=="load") null else true,
  ).convertResponse()

  /**
   * Get bulk transactions using ids
   */
  fun bulkTransactions(bids: List<TransactionBid>) =
    bids.joinToString(separator = ",") { it.transactionId }
        .let { transactionService.bulkTransactions(it) }
        .convertResponse()
        .map { Pair(bids, it) }

  /**
   * Transaction details
   */
  fun transactionDetails(id: String, spId:String?=null) = transactionService.transactionDetails(id,spId).convertResponse()

  /**
   * Transaction trip meter
   */
  fun transactionTripMeter() =
    transactionService.transactionsTripMeter(userRepository.userId()).convertResponse()


  fun updateTripWithFuelCardUser(
    transactionId: String,
    fuelPayoutRequest: FuelPayoutRequest
  ) = transactionService.updateTripForFuelPayout(transactionId, fuelPayoutRequest).convertResponse()
}

enum class TransactionStatus(val statusId: String) {
  Requested("requested"),
  InEnquiry("in_enquiry"),
  TruckConfirmed("truck_confirmed"),
  Completed("completed"),
  Cancelled("cancelled")
}

enum class ContractType(val type: String) {
  LH_FTL("LH_FTL"),
  FRC("FRC"),
  INTRACITY("INTRACITY")
}

enum class RequestType(val type: String) {
  Contract("contract"),
  Spot("spot"),
  Fixed("fixed")
}
enum class DemandType(val type: String) {
  Internal("Internal"),
  Others("Others"),
  Intracity("Intracity"),
  Corporate("Corporate")
}
val contractsMap= mapOf(
  Pair("Live Bidding", LiveBidding),
  Pair("Collecting Bids",CollectingBids),
  Pair("Bidding Closed",BiddingClosed),
  Pair("Cancelled",Cancelled),
  Pair("Result Declared",ResultDeclared)
)

/**
 * Map the status values for contracts to their corresponding values used in the api
 */
enum class ContractStatus(val statusId:String){
  LiveBidding("live_bidding"),
  CollectingBids("active_bidding"),
  BiddingClosed("closed_bidding"),
  Cancelled("cancelled"),
  ResultDeclared("allocated")
}