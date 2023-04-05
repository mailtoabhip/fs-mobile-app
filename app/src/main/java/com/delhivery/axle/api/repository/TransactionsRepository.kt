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
  fun fetchContractsTransactions(offset: Int, demand_type: String, allActiveFetched:Boolean?,limit:Int,count:String?=null) =
    transactionService.contractsTransactions(
      userRepository.userId(), offset, limit,demand_type, allActiveFetched = allActiveFetched,count
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
    status: String?,
    requestType:String?,
    contractType:String?
  ) = transactionService.transactions(
      offset, Requested.statusId + "," + InEnquiry.statusId, source, destination, truckType, truckDisplayName, if(status!=null) contractsMap[status]?.statusId else null, if(requestType=="load") "yes" else null,if (requestType=="load") true else null, if(requestType=="load") "fixed,spot" else "contract",
    contractType,if(requestType=="load") null else true
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
  fun transactionDetails(id: String) = transactionService.transactionDetails(id).convertResponse()

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
  Completed("completed")
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
  ResultDeclared("result_declared")
}