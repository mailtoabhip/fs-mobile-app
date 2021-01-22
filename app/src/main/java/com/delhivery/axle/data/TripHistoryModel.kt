package com.delhivery.axle.data

import android.content.Context
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trips.TripDriverDetails
import com.delhivery.axle.data.home.trips.TripStatus
import com.delhivery.axle.data.home.trips.TripStatus.Unknown
import com.delhivery.axle.data.home.trips.TripVehicleDetails
import com.delhivery.axle.utils.DateUtils
import com.google.gson.annotations.SerializedName

/**
 * Data class for Trip history item
 */
data class TripHistoryModel(
  @SerializedName("action_time") val actionTime: String,
  @SerializedName("trip_status") private val _tripStatus: String?,
  @SerializedName("user_name") val userName: String,
  @SerializedName("details") val details: TripHistoryDetail?
) {

  /**
   * Trip Status [TripStatus]
   */
  fun status() = _tripStatus?.let { TripStatus.byKey(it) } ?: Unknown

  /**
   * Compute epoch to action time
   */
  fun timeStamp() = DateUtils.convertToRelativeTimeStamp(actionTime)

}

/**
 * Data class for Trip history Detail
 */
data class TripHistoryDetail(
  @SerializedName("arrival_time") val arrivalTime: String,
  @SerializedName("auto_advance_transfer") val autoAdvanceTransfer: Boolean,
  @SerializedName("driver") val driverDetails: TripDriverDetails,
  @SerializedName("vehicle") val vehicleDetails: TripVehicleDetails,
  @SerializedName("loading_advice") val loadingAdvice: String,
  @SerializedName("current_location") val currentLocation: String,
  @SerializedName("reached_time") val reachedTime: String,
  @SerializedName("unloaded_time") val unloadedTime: String,
  @SerializedName("pod_url") val podUrl: String,
  @SerializedName("unloading_location") val unloadingLocation: String
) {

  /**
   * @return convert arrivalTime to relative stamp
   */
  fun getArrivalEpoch() = DateUtils.convertToRelativeTimeStamp(arrivalTime)

  /**
   * @return convert reachedTime to relative stamp
   */
  fun getReachedEpoch() = DateUtils.convertToRelativeTimeStamp(reachedTime)

  /**
   * @return convert unloadedTime to relative stamp
   */
  fun getUnloadedEpoch() = DateUtils.convertToRelativeTimeStamp(unloadedTime)
}

/**
 * Data class for Trip History Item
 */
data class TripHistoryItem(
  val id: Int,
  val heading: String,
  val subHeading: String,
  val actionTime: String = "",
  val podUrl: String = "",
  val invoiceUrl: String = ""
) {

  /**
   * @return background basis status
   */
  fun getBackground(): Int {
    return when (id) {
      BalancePending, AdvancePending -> R.drawable.bg_gradient_orange
      AwaitingUnloading, AwaitingPODUpload, InTransit, InTransitLocation -> R.drawable.bg_gradient_blue
      BalancePaid, PODUploaded -> R.drawable.bg_gradient_green
      else -> R.color.white
    }
  }

  /**
   * @return heading text color
   */
  fun getHeadingTextColor(
    focused: Boolean,
    context: Context
  ) = when (focused) {
    true -> ContextCompat.getColor(context, R.color.white)
    false -> ContextCompat.getColor(context, R.color.heading_black)
  }

  /**
   * @return subheading text color
   */
  fun getSubHeadingTextColor(
    focused: Boolean,
    context: Context
  ) = when (focused) {
    true -> ContextCompat.getColor(context, R.color.white)
    false -> ContextCompat.getColor(context, R.color.sub_heading_black)
  }
}

const val TruckPlaced = 14
const val ReachedPickupPoint = 13
const val TruckLoaded = 12
const val InTransit = 11
const val InTransitLocation = 10
const val AdvancePending = 9
const val AdvancePaid = 8
const val ReachedDestination = 7
const val AwaitingUnloading = 6
const val TruckUnloaded = 5
const val AwaitingPODUpload = 4
const val PODUploaded = 3
const val BalancePending = 2
const val BalancePaid = 1