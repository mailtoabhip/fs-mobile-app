package com.delhivery.axle.api.repository

import com.delhivery.axle.api.repository.TransactionStatus.InEnquiry
import com.delhivery.axle.api.repository.TransactionStatus.Requested
import com.delhivery.axle.api.request.FuelPayoutRequest
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
  private val userPrefs: UserPrefs
) : BaseRepository() {

  /**
   * Get user transactions
   */
  fun fetchLoadBoardTransactions(offset: Int, demand_type: String, vehicle_type: String?= null,excludeTruckTypes: String?= null, filterVehicleType: Boolean?= null) =
    transactionService.loadBoardTransactions(
      userRepository.userId(), offset, UserTripsLoadLimit, demand_type, vehicle_type,
      "yes", excludeTruckTypes, filterVehicleType
  ).convertResponse()

  /**
   * Search [TransactionStatus.Requested] transactions
   */
  fun searchTransactions(
    offset: Int,
    source: String?,
    destination: String?,
    truckType: String?
  ) = transactionService.transactions(
      offset, Requested.statusId + "," + InEnquiry.statusId, source, destination, truckType
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