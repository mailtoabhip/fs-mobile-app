package com.delhivery.axle.api.repository

import com.delhivery.axle.api.repository.ContractStatus.BiddingClosed
import com.delhivery.axle.api.repository.ContractStatus.Cancelled
import com.delhivery.axle.api.repository.ContractStatus.CollectingBids
import com.delhivery.axle.api.repository.ContractStatus.LiveBidding
import com.delhivery.axle.api.repository.ContractStatus.ResultDeclared
import com.delhivery.axle.api.repository.TransactionStatus.InEnquiry
import com.delhivery.axle.api.repository.TransactionStatus.Requested
import com.delhivery.axle.api.request.FuelPayoutRequest
import com.delhivery.axle.api.request.ReccomdationRequest
import com.delhivery.axle.api.response.SearchAfter
import com.delhivery.axle.api.service.RecommendationService
import com.delhivery.axle.api.service.TransactionService
import com.delhivery.axle.data.bids.TransactionBid
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
  fun fetchRecommTransactions(offset: Int, demand_type: String, vehicle_type: String?= null,excludeTruckTypes: String?= null, filterVehicleType: Boolean?= null, biddingGoingOn:Boolean = false, splitViewCount:Boolean?=null, searchAfter: SearchAfter?) =
         recommendationService.recommendationTransactions(
           ReccomdationRequest( userPrefs.parentId,UserTripsLoadLimit,offset,
             demand_type, vehicle_type, splitViewCount = splitViewCount, searchAfter = searchAfter )
          ).convertResponse()

  /**
   * Get user intracity transactions
   */
  fun fetchIntracityRecommTransactions(offset: Int, demand_type: String?=null, vehicle_type: String?= null,excludeTruckTypes: String?= null, filterVehicleType: Boolean?= null, biddingGoingOn:Boolean = false, onlyCount:Boolean?=null, searchAfter: SearchAfter?= null) =
    recommendationService.recommendationIntracityTransactions(
      ReccomdationRequest(userPrefs.parentId,UserTripsLoadLimit,offset,
        null, vehicle_type, onlyCount = onlyCount, searchAfter = searchAfter)
    ).convertResponse()

  /**
   * Get spot marketplace transactions
   */
  fun fetchSpotMarketplaceTransactions(onlyCount: Boolean = false, limit: Int = UserTripsLoadLimit, offset: Int) =
    transactionService.spotMarketplaceTransactions(
      onlyCount = onlyCount,
      limit = limit,
      offset = offset
    ).convertResponse()

  /**
   * Get contracts transactions
   */
  fun fetchContractsTransactions(offset: Int, demand_type: String, allActiveFetched:Boolean?,limit:Int,matchLanePrefOriginCities:Boolean?,isFlexible:Boolean?=null,includeFlexibleContracts:Boolean?=null, searchAfterCreationTime:String? = null, searchAfterTransactionId:String? = null) =
    transactionService.contractsTransactions(
      userPrefs.parentId, offset, limit,demand_type, allActiveFetched = allActiveFetched,matchLanePrefOriginCities,isFlexible,includeFlexibleContracts
    , searchAfterCreationTime = searchAfterCreationTime, searchAfterTransactionId = searchAfterTransactionId).
    convertResponse()


  /**
   * Get contracts summary count
   */
  fun fetchContractsSummaryCount() =
    transactionService.contractsCountSummary("yes", userRepository.userId()
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
    contractType:String?,
    limit: Int,
    isFlexible: Boolean?=null,
    includeFlexibleContracts: Boolean?=null
  ) = transactionService.transactions(
      offset, if(contractType!=ContractType.INTRACITY.type) Requested.statusId + "," + InEnquiry.statusId else null, source, destination, truckType, truckDisplayName,
    contractsMap[contractStatus]?.statusId, if(requestType==RequestType.Load.type) "yes" else null,if(requestType==RequestType.Load.type) true else null, if(requestType==RequestType.Load.type) "fixed,spot" else "contract",
    contractType,if(requestType==RequestType.Load.type) null else true,limit,isFlexible,includeFlexibleContracts
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
  //delhivery intercity
  LH_FTL("LH_FTL"),
  //non-delhivery intercity
  FRC("FRC"),
  //intracity
  INTRACITY("INTRACITY")
  //fixed/ flexible
}

enum class RequestType(val type: String) {
  Contract("contract"),
  Load("load"),
  Spot("spot"),
  SpotMarketplace("spot_marketplace"),
  Fixed("fixed")
}
enum class DemandType(val type: String) {
  Internal("Internal"),
  Others("Others"),
  Intracity("Intracity"),
  Intracity_OPS("intracity_ops"),
  Corporate("Corporate"),
  Spot_Marketplace("spot_marketplace")
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