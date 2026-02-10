package com.delhivery.axle.data.home.bids

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.delhivery.axle.api.repository.ContractType
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.api.repository.RequestType
import com.delhivery.axle.api.repository.TransactionStatus
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.CostDataObject
import com.delhivery.axle.data.IndentHaltCenters
import com.delhivery.axle.data.Misc
import com.delhivery.axle.data.RouteInformation
import com.delhivery.axle.data.VehicleModel
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.ui.bids.BidType
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DateUtils.formatDate
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.LoadTypeUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.StringUtils.capitalize
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Transaction details
 */
data class HomeBidsRequestItemData(
  @SerializedName("material_type") val materialType: String?,
  @SerializedName("pickup_location") val pickupLocation: String,
  @SerializedName("destination") val destination: String,
  @SerializedName("origin_state") val originState: String="",
  @SerializedName("required_on") val _requiredOn: String?,
  @SerializedName("target_price") val targetPrice: Double?=0.0,
  @SerializedName("uuid") val uuid: String?,
  @SerializedName("transaction_id") val transactionId: String?,
  @SerializedName("truck_type") val truckType: String?,
  @SerializedName("origin") val origin: String,
  @SerializedName("intermediary_stop1_city") val stop1City: String,
  @SerializedName("intermediary_stop1_state") val stop1State: String,
  @SerializedName("intermediary_stop2_city") val stop2City: String,
  @SerializedName("intermediary_stop2_state") val stop2State: String,
  @SerializedName("intermediary_pickup_stop1") val pickup1: String,
  @SerializedName("intermediary_pickup_stop2") val pickup2: String,
  @SerializedName("intermediary_stop1") val stop1: String,
  @SerializedName("intermediary_stop2") val stop2: String,
  @SerializedName("intermediary_pickup_stop1_city") val pickup1City: String,
  @SerializedName("intermediary_pickup_stop2_city") val pickup2City: String,
  @SerializedName("destination_state") val destinationState: String="",
  @SerializedName("truck_display_name") val truckDisplayName: Any?,
  @SerializedName("bidding_type") val biddingType: String? = "FTL",
  @SerializedName("load_price_percent") var loadPricePercent: Int,
  @SerializedName("is_multi_drop") val isMultidrop: Boolean? = false,
  @SerializedName("creation_time") val creationTime :String,
  @SerializedName("requested_capacity_mg") var requestedCapacityMg: Double,
  @SerializedName("pmt_rate") var pmtRate: Double,
  @SerializedName("distance") var distance: Double,
  @SerializedName("truck_specifications") var truckSpecification: TruckSpecification?,
  @SerializedName("speed") var speed: String?,
  @SerializedName("tat_minutes") var tatMinutes: String?,
  @SerializedName("origin_district") val originDistrict: String?,
  @SerializedName("destination_district") val destinationDistrict: String?,
  @SerializedName("guidance_price") val guidancePrice: Double ?= 0.0,
  @SerializedName("placed_truck_passing") val placedTruckPassing: Double? = 0.0,
  @SerializedName("request_type") val requestType: String? = "",
  @SerializedName("unallocated_volume") val unAllocatedVolume: Double? = 0.0,
  @SerializedName("allocated_volume") val allocatedVolume: Double? = 0.0,
  @SerializedName("child_transactions") val childTransactions: List<String> = mutableListOf(),
  @SerializedName("truck_uuid") val truckUUID: Any?,
  @SerializedName("is_dmt") val isDmt :Boolean? = false,
  @SerializedName("status") val transactionStatus: String?= "",
  @SerializedName("entity") val entity:String?= "",
  @SerializedName("bidding_ending_time_for_axle_app") val bidEndingTime:String? =  null,
  @SerializedName("indent_origin") val indentOrigin:String? =  null,
  @SerializedName("client_name") val clientName:String? =  null,
  @SerializedName("client_confirmed") val clientConfirmationPending: Boolean?=null,
  @SerializedName("expected_arrival_time_pickup_remark") val expectedArrivalTimePickupRemark: String?=null,
  @SerializedName("expected_arrival_time_pickup") val expectedArrivalTimePickup:String? =  null,
  @SerializedName("indent_halt_centers") val indentHaltCenters:List<IndentHaltCenters>? =  null,
  @SerializedName("pickup_location_address") val pickupLocationAddress: String?,
  @SerializedName("drop_location_address") val dropLocationAddress: String?,
  @SerializedName("pickup_location_city") val pickupLocationCity: String?,
  @SerializedName("drop_location_city") val dropLocationCity: String?,
  @SerializedName("pickup_location_pincode") val loadingLocationPincode: String?,
  @SerializedName("pickup_location_coordinates") val pickupLocationCoordinates: PickUpCoordinate?,
  @SerializedName("drop_location_pincode") val unloadingLocationPincode: String?,
  @SerializedName("intermediary_pickup_stop1_address") val pickup1Address: String?,
  @SerializedName("intermediary_pickup_stop1_pincode") val pickup1AddressPin: String?,
  @SerializedName("intermediary_pickup_stop2_address") val pickup2Address: String?,
  @SerializedName("intermediary_pickup_stop2_pincode") val pickup2AddressPin: String?,
  @SerializedName("intermediary_stop1_address") val intermediaryStop1Address: String?,
  @SerializedName("intermediary_stop1_pincode") val intermediaryStop1AddressPin: String?,
  @SerializedName("intermediary_stop2_address") val intermediaryStop2Address: String?,
  @SerializedName("intermediary_stop2_pincode") val intermediaryStop2AddressPin: String?,
  @SerializedName("res_offer") var resOffer: Triple<Pair<Boolean?,String?>?, Pair<String?, String?>?, Triple<String?, String?,String?>?>? =Triple(Pair(null, null), Pair(null, null), Triple(null, null,null)),
  @SerializedName("origin_city_code") var originCityCode: String? =null,
  @SerializedName("origin_city") var originCity: String? =null,
  @SerializedName("origin_center_name") var originCenterName: String?=null,
  @SerializedName("destination_city_code") var destinationCityCode: String? =null,
  @SerializedName("additional_remarks") var additionalRemarks: String? = null,
  @SerializedName("order_creation_remarks") var orderCreationRemarks: String? =null,
  @SerializedName("bidding_end_time") var contractBiddingEndTime: String? =null,
  @SerializedName("halt_centers") var haltCenters:List<HaltCenters>? =  null,
  @SerializedName("continuous_connection") var continuousConnection:Boolean? =  false,
  @SerializedName("contract_type") var contractType:String? =  null,
  @SerializedName("tentative_trip_count") var tentativeTripCount:Int? =  null,
  @SerializedName("bid_collection_slot") var bidCollectionSlot:String? =  null,
  @SerializedName("contract_validity") var contractValidity:String? =  null,
  @SerializedName("route_type") var routeType:String? =  null,
  @SerializedName("vehicle_count_cc_lane") var vehicleCountCCLane:Int? =  null,
  @SerializedName("vehicle_count_per_route") var vehicleCountPerRoute:Int? =  null,
  @SerializedName("operating_days") var operatingDays:Int? =  null,
  @SerializedName("slab_payout_details") var paymentSlabs:JsonObject? =  null,
  @SerializedName("reporting_time") var reportingTime:String? =  null,
  @SerializedName("intracity_days") var intracityDays:String? =  null,
  @SerializedName("intracity_hours") var intracityHours:String? =  null,
  @SerializedName("intracity_kms") var intracityKms:String? =  null,
  @SerializedName("intracity_extra_km_rate") var intracityExtraKmRate:String? =  null,
  @SerializedName("intracity_extra_hour_rate") var intracityExtraHourRate:String? =  null,
  @SerializedName("intracity_extra_day_rate") var intracityExtraDayRate:String? =  null,
  @SerializedName("nep_required") var nepRequired:Boolean? =  false,
  @SerializedName("origin_longitude")val longitude:String?,
  @SerializedName("origin_latitude")val latitude:String?,
  @SerializedName("demand_type")val demandType:String?,
  @SerializedName("intracity_slab_details")val intracitySlabDetails:List<String>?,
  @SerializedName("contract_remarks")val contractRemarks:String?,
  @SerializedName("contract_usage")val contractUsage:String?,
  @SerializedName("secondary_reporting_centers")val secondaryReportingCenters:List<SecondaryReportingCenters>?=null,
  @SerializedName("is_flexible")val isFlexible:Boolean=false,
  @SerializedName("requirement_duration")val requirementDuration:Int?=null,
  @SerializedName("sub_request_type")val subRequestType:String?=null,
  @SerializedName("nep_states")val nepState:String?=null,
  @SerializedName("fms_ticket_id")val fmsTicketId:String?=null,
  @SerializedName("intracity_lob")val intracityLob:String?=null,
  @SerializedName("payment_mode")var paymentMode:String?=null,
  @SerializedName("indent_payment_mode")var indentPaymentMode:String?=null,
  @SerializedName("advance_percentage")var advancePercentage:String?=null,
  @SerializedName("bid_suggestion")var bidSuggestion: Boolean = false,
  @SerializedName("message")var suggestedBidMessage: String? = null,
  @SerializedName("vendor_advance_percent")var vendorAdvancePercent: Int? = null,
  @SerializedName("shipper_name")var shipperName: String? = null,
  @SerializedName("shipper_phone_number")var shipperPhoneNumber: String? = null,
  @SerializedName("shipper_ucid")var shipperUcid: String? = null,
  @SerializedName("shipper_price") var shipperPrice: Int? = null,
  @SerializedName("marketplace_payment_type") var marketplacePaymentType: String? = null,
  @SerializedName("marketplace_advance_percentage") var marketplaceAdvancePercentage: Int? = null,
  @SerializedName("poc_name") var pocName: String? = null,
  @SerializedName("poc_contact_no") var pocContactNo: String? = null,


  //Added New Placement Details API Response keys
  @SerializedName("contract_id")val contractId:String?=null,
  @SerializedName("contract_code")val contractCode:String?=null,
  @SerializedName("vehicle_info")val vehicleInfo: VehicleModel?=null,
  @SerializedName("route_info")val routeInfo: RouteInformation?=null,
  @SerializedName("cost_data")val costData: CostDataObject?=null,
  @SerializedName("misc")val misc: Misc?=null,

  //for Placement details
  var loadType: String?=null,
  //
  var lowestBid: Double? = 0.0,
  var numBids: Int = 0,
  var transactionBid: TransactionBid? = null,
  var showing: Boolean = false,
  var bulkTransactionBids: List<TransactionBid> = mutableListOf(),
  //
  var bidType : BidType? = null
) : BaseKeyTypeModel<String>() {
  override fun key() = uuid ?: transactionId ?: kotlin.random.Random.nextInt().toString()

  fun getBidAmount() = "₹${transactionBid?.bidAmount?.toString()?:""}"

  /**
   * Returns formatted bid amount with commas and proper decimal formatting
   * Example: ₹1,25,000.50
   */
  fun getFormattedBidAmount(): String {
    val bidAmount = transactionBid?.bidAmount ?: 0.0
    return if (bidAmount > 0.0) {
      "₹${StringUtils.formatDecimalAmount(bidAmount)}"
    } else {
      "₹0"
    }
  }

  /**
   * Returns bid suggestion availability
   * Returns true if bid suggestion data is available
   */
  fun getBidSuggestionValue() : Boolean {
    return bidSuggestion
  }

  /**
   * Returns the suggested message if there is any
   * Example: "Competition is tough! Lower your price by ₹20 to win the load"
   */
  fun getSuggestedBidMessageValue() : String {
    return suggestedBidMessage ?: ""
  }

  /**
   * Returns the suggested bid amount if there is any
   */
  fun getSuggestedBidAmount() : Int? {
    return transactionBid?.suggestion?.suggestedAmount
  }

  /**
   * Returns formatted suggested bid amount with currency
   * Example: "₹1,25,000"
   */
  fun getFormattedSuggestedBidAmount(): String {
    val suggestedAmount = transactionBid?.suggestion?.suggestedAmount
    return if (suggestedAmount != null && suggestedAmount > 0) {
      "₹${StringUtils.formatAmount(suggestedAmount.toDouble())}"
    } else {
      ""
    }
  }

  /****
   *
   * get Ticket Id
   */
  fun getTicketId(): String = "Ticket Id: "+fmsTicketId

  /**
   * get formatted payment mode display text
   */
  fun getPaymentModeDisplay(): String {
    // Use indentPaymentMode first, fall back to paymentMode for backward compatibility
    val mode = indentPaymentMode
    return when (mode?.lowercase()) {
      "credit" -> "Credit Payment"
      "advance" -> "Advance Payment"
      else -> mode?.replaceFirstChar { it.uppercase() } ?: "N/A"
    }
  }

  /**
   * get formatted marketplace payment mode display text
   * Returns formatted payment type like "Advance payment" or "To Pay"
   */
  fun getMarketplacePaymentModeDisplay(): String {
    // First check new marketplace_payment_type field
    if (!marketplacePaymentType.isNullOrEmpty()) {
      return marketplacePaymentType!!.replace("_", " ").capitalize()
    }
    // Fallback to vendorAdvancePercent for backward compatibility
    return if (vendorAdvancePercent == null || vendorAdvancePercent == 0) {
      "To Pay"
    } else {
      "Advance Payment"
    }
  }

  /**
   * Check if marketplace payment mode should be displayed
   */
  fun shouldShowMarketplacePaymentMode(): Boolean {
    return !marketplacePaymentType.isNullOrEmpty() || vendorAdvancePercent != null
  }

  /**
   * Check if marketplace advance payment percentage should be displayed
   */
  fun shouldShowMarketplaceAdvancePercentage(): Boolean {
    // First check new marketplace_advance_percentage field
    if (marketplaceAdvancePercentage != null && marketplaceAdvancePercentage!! > 0) {
      return true
    }
    // Fallback to vendorAdvancePercent
    return vendorAdvancePercent != null && vendorAdvancePercent!! > 0
  }

  /**
   * get formatted marketplace advance payment percentage
   */
  fun getMarketplaceAdvancePaymentPercentage(): String {
    // First check new marketplace_advance_percentage field
    if (marketplaceAdvancePercentage != null && marketplaceAdvancePercentage!! > 0) {
      return "${marketplaceAdvancePercentage}%"
    }
    // Fallback to vendorAdvancePercent
    return if (vendorAdvancePercent != null && vendorAdvancePercent!! > 0) {
      "(${vendorAdvancePercent}%)"
    } else {
      ""
    }
  }

  /**
   * Get formatted truck type and display name for marketplace
   * Format: "Closed 32FTMXL"
   */
  fun getMarketplaceTruckInfo(): String {
    val type = truckType?.let { capitalize(it) } ?: ""
    val displayName = when (truckDisplayName) {
      is String -> truckDisplayName as String
      else -> truckDisplayName?.toString() ?: ""
    }
    return if (type.isNotEmpty() && displayName.isNotEmpty()) {
      "$type $displayName"
    } else if (displayName.isNotEmpty()) {
      displayName
    } else if (type.isNotEmpty()) {
      type
    } else {
      ""
    }
  }

  /**
   * Get formatted shipper price for marketplace listings
   * Returns formatted price with rupee symbol and commas
   * Example: ₹45,000
   */
  fun getFormattedShipperPrice(): String {
    return if (shipperPrice != null && shipperPrice!! > 0) {
      "₹${StringUtils.formatAmount(shipperPrice!!.toDouble())}"
    } else {
      "₹0"
    }
  }

    /**
     * Check if this is a marketplace load
     * Returns true if demand_type/request_type is spot_marketplace OR if subRequestType is marketplace OR shipper data is present
     */
    fun isMarketplaceLoad(): Boolean {
        // Primary check: demand_type is spot_marketplace
        if (demandType?.equals(DemandType.Spot_Marketplace.type, ignoreCase = true) == true) {
            return true
        }

        // Secondary check: request_type is spot_marketplace
        if (requestType?.equals(RequestType.SpotMarketplace.type, ignoreCase = true) == true) {
            return true
        }

        // Tertiary check: sub_request_type is marketplace
        if (subRequestType?.equals("marketplace", ignoreCase = true) == true) {
            return true
        }

        // Fallback: Check if shipper data is present (for backward compatibility)
        return !shipperName.isNullOrEmpty() ||
                !shipperPhoneNumber.isNullOrEmpty() ||
                shipperPrice != null ||
                !shipperUcid.isNullOrEmpty()
    }

  /**
   * Get initials from shipper/POC name for avatar
   * Returns first letter of first two words (e.g., "ABC Logistics" -> "AL", "John Doe" -> "JD")
   * Uses shipperName if available, falls back to pocName
   */
  fun getShipperInitials(): String {
    // Use shipperName if available, otherwise use pocName
    val name = shipperName ?: pocName
    if (name.isNullOrEmpty()) return ""
    
    val words = name.trim().split(" ").filter { it.isNotEmpty() }
    return when {
      words.isEmpty() -> ""
      words.size == 1 -> words[0].take(2).uppercase()
      else -> "${words[0].first()}${words[1].first()}".uppercase()
    }
  }

  /**
   * Get display name for shipper/POC
   * Uses shipperName if available, falls back to pocName
   */
  fun   getContactDisplayName(): String {
    return shipperName ?: pocName ?: ""
  }

  /**
   * Get phone number for shipper/POC
   * Uses shipperPhoneNumber if available, falls back to pocContactNo
   */
  fun getContactPhoneNumber(): String {
    return shipperPhoneNumber ?: pocContactNo ?: ""
  }

  /**
   * Check if payment mode should be displayed
   * Returns true only for valid payment modes (Credit or Advance)
   */
  fun shouldShowPaymentMode(): Boolean {
    if (indentPaymentMode.isNullOrEmpty()) return false
    val mode = indentPaymentMode?.lowercase()
    return mode.equals("credit",true)  || mode.equals("advance",true)
  }

  /**
   * Check if advance payment percentage should be displayed
   * Returns true only for advance payment mode
   */
  fun shouldShowAdvancePercentage(): Boolean {
    if (indentPaymentMode.isNullOrEmpty()) return false
    val mode = indentPaymentMode?.lowercase()
    return mode.equals("advance",true)
  }

  /**
   * get formatted advance payment percentage
   */
  fun getAdvancePaymentPercentage(): String {
    return if (advancePercentage != null) {
      "(${advancePercentage}%)"
    } else {
      ""
    }
  }

  fun getDemandTypeByLoad() : String {
    Log.d("Demand Type ","$demandType")
    if (demandType?.equals("Internal", ignoreCase = true) == true || 
        demandType?.equals("Intracity", ignoreCase = true) == true) {
      return "Delhivery Load"
    } else return "Client Load"
  }
  /**
   * get time in ist
   */

  fun getTimeOfContracts(): String {
    val ist = TimeZone.getTimeZone("Asia/Kolkata")

    val inputFormat = SimpleDateFormat("HH:mm:ss")
    inputFormat.timeZone = ist

    val outputFormat = SimpleDateFormat("hh:mm a")
    outputFormat.timeZone = ist

    val date = reportingTime?.let { inputFormat.parse(it) }
    val formattedTime = date?.let { outputFormat.format(it) }
    return formattedTime?:"NA"
  }

    fun getTimeOfContractsForPlacements(time: String): String {
        val ist = TimeZone.getTimeZone("Asia/Kolkata")

        val inputFormat = SimpleDateFormat("HH:mm:ss")
        inputFormat.timeZone = ist

        val outputFormat = SimpleDateFormat("hh:mm a")
        outputFormat.timeZone = ist

        val date = time.let { inputFormat.parse(it) }
        val formattedTime = date?.let { outputFormat.format(it) }
        return formattedTime?:"na"
    }

    fun placementsOnlyFormatReportingTime(time: String):String?= time.let { DateUtils.daysDiffWithDateTimeStr(it, "HH:mm") }

  /**
   * Returns formatted bid amount with commas but without decimal places
   * Example: ₹1,25,000
   */
  fun getFormattedBidAmountWhole(): String {
    val bidAmount = transactionBid?.bidAmount ?: 0.0
    return if (bidAmount > 0.0) {
      "₹${StringUtils.formatAmount(bidAmount)}"
    } else {
      "₹0"
    }
  }

  fun getLowestBidAmount() = if((numBids >= 1) && ((lowestBid ?: 0.0) > 0.0)) {
    "Lowest bid is ₹${StringUtils.formatAmount(lowestBid?:0.0)}"
  }else{
    ""
  }

//  fun isStrongBid(){
//      if(isBidOpen() && bidStatus().statusKey.lowercase() == "open"){
//          if (((transactionBid?.bidAmount ?: 0.0) <= (lowestBid ?: 0.0)) && (numBids >= 1) && ((lowestBid ?: 0.0) > 0.0))
//              View.VISIBLE
//          else
//              View.GONE
//      }
//  }

    fun isStrongBid(): Int {
        val isBidOpen = isBidOpen()
        val isStatusOpen = bidStatus().statusKey.equals("open", ignoreCase = true)
        val bidAmount = transactionBid?.bidAmount ?: 0.0
        val minBid = lowestBid ?: 0.0

        return if (isBidOpen && isStatusOpen && bidAmount <= minBid && numBids >= 1 && minBid > 0.0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


//  fun isWeakBid() =
//    if (((transactionBid?.bidAmount ?: 0.0) > (lowestBid ?: 0.0)) && (numBids >= 1) && ((lowestBid ?: 0.0) > 0.0))
//      View.VISIBLE
//    else
//      View.GONE


    fun isWeakBid(): Int {
        val isBidOpen = isBidOpen()
        val isStatusOpen = bidStatus().statusKey.equals("open", ignoreCase = true)
        val bidAmount = transactionBid?.bidAmount ?: 0.0
        val minBid = lowestBid ?: 0.0

        return if (isBidOpen && isStatusOpen && bidAmount > minBid && numBids >= 1 && minBid > 0.0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

  fun isActiveBid() = bidType == BidType.ActiveBid

  fun loadDetails() = StringUtils.capitalize(materialType) ?: "Not available"

  fun target() = if (targetPrice?:0.0 > 0 && loadPricePercent > 0) {
    targetPrice?:0.0 * loadPricePercent / 100
  } else {
    0.0
  }
  fun formatedTargetPrice() = if (targetPrice?:0.0 > 0) {
    "₹${targetPrice!!.toInt()}"
  } else {
    "₹0"
  }

  fun formattedFmsTicketId() = if (fmsTicketId!=null) {
    "Ticket ID: ${fmsTicketId}"
  } else {
    null
  }

  fun formattedVehicleLob() = if (intracityLob!=null) {
    when (intracityLob) {
        "Pickup", "B2B FM" -> {
          "First-mile pickups"
        }
        "Delivery" -> {
          "Last-mile delivery"
        }
        "Returns" -> {
          "Seller returns"
        }
        "Carting" -> {
          "Carting"
        }
        else -> {
          null
        }
    }
  } else {
    null
  }

  /**
   * if trip is Multidrop
   */
  fun setMutidrop() = if (isMultidrop != null && isMultidrop == true) {
    View.VISIBLE
  } else {
    View.GONE
  }
  /**
   * if trip is DMT
   */
  fun setDMTType() = if (isDMTIndent()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun delLoadVisibility() = if (indentOrigin.equals("LH")) {
    View.GONE
  } else {
    View.GONE
  }

  fun isMaterialTypeAvailable() = if (misc?.materialType.isNotNullOrEmpty()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isClientNameAvailable() = if (misc?.clientName.isNotNullOrEmpty()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isTruckNameAvailable() = if (vehicleInfo?.truckDisplayName.isNotNullOrEmpty()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isTruckTypeAvailable() = if (vehicleInfo?.truckType.isNotNullOrEmpty()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun nonDelLoadVisibility() = if (!demandType.equals(DemandType.Internal.type)) {
    View.VISIBLE
  } else {
    View.GONE
  }
  /**
   * if trip is DMT
   */
  fun setDMTTypeForBid(visibility: Boolean) = if (isDMTIndent() && !visibility) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun bidInfoVisibility() = if (isDMTIndent()) {
    View.GONE
  } else {
    View.VISIBLE
  }

  fun bidInfoLayoutVisibility()= if( transactionBid!=null || (bulkTransactionBids!= null &&bulkTransactionBids.isNotEmpty() ))
    View.VISIBLE
  else
    View.INVISIBLE

  fun bidPlaceLayoutVisibility()= if(  (bulkTransactionBids==null || bulkTransactionBids.isEmpty()) && transactionBid == null)
    View.VISIBLE
  else
    View.INVISIBLE

  fun timerPlaceLayoutVisibility()= if(!isDMTIndent() && (bulkTransactionBids==null || bulkTransactionBids.isEmpty()) && transactionBid == null )
    View.VISIBLE
  else
    View.GONE


  fun requestedCapacityVisibility() = if(isPMTIndent() && !isDMTIndent())
    View.VISIBLE
  else
    View.GONE


  fun setMoreBidsVisibility() = if (isDMTIndent()) {
    if(bulkTransactionBids!=null && bulkTransactionBids.isNotEmpty() && bulkTransactionBids.size> 1) {
      View.VISIBLE
    }else{
      View.GONE
    }
  } else {
    View.GONE
  }

  fun setDmtText() = "Bulk Load: ${requestedCapacityMg.toInt()} MT"
  fun setDmtValue() = "${requestedCapacityMg.toInt()} MT"


  fun setTruckTypeText() = capitalize(truckType) ?: ""

  fun setUnAllocatedText()= if (unAllocatedVolume!=null && unAllocatedVolume != 0.0 ) "Unallocated Load: ${unAllocatedVolume.toInt()} MT" else ""

  fun setUnAllocatedVol() = if (unAllocatedVolume!=null && unAllocatedVolume != 0.0 ) "Unallocated vol: ${unAllocatedVolume.toInt()} MT" else ""


  fun clientName() = StringUtils.capitalize(clientName) ?: ""

  /**
   * @return formatted origin city name
   */
  fun originCityName() = StringUtils.capitalize(origin) ?: ""

  /**
   * @return formatted destination city name
   */
  fun destinationCityName() = StringUtils.capitalize(destination) ?: ""

  /**
   * @return formatted origin state name
   */
  fun originStateName() = StringUtils.capitalize(originState) ?: ""

  /**
   * @return formatted origin pincode and state
   */
  fun originPincodeState(): String {
    val pincode = loadingLocationPincode
    val state = originStateName()
    return when {
      !pincode.isNullOrEmpty() && state.isNotEmpty() -> "$pincode, $state"
      !pincode.isNullOrEmpty() -> pincode
      state.isNotEmpty() -> state
      else -> ""
    }
  }

  /**
   * @return formatted destination state name
   */
  fun destinationStateName() = StringUtils.capitalize(destinationState) ?: ""

  /**
   * @return formatted origin district name
   */
  fun originDistrictName() = originDistrict?.let { StringUtils.capitalize(it) } ?: ""

  /**
   * @return formatted destination district name
   */
  fun destinationDistrictName() = destinationDistrict?.let { StringUtils.capitalize(it) } ?: ""

  /**
   * @return origin district and state name
   */
  fun originDistrictState() = if (originDistrictName().isNotNullOrEmpty()) {
    originDistrictName() + "\n" + originStateName()
  } else {
    originStateName()
  }
  /**
   * @return destination district and state name
   */
  fun destinationDistrictState() = if(destinationDistrictName().isNotNullOrEmpty()) {
    destinationDistrictName() + "\n" + destinationStateName()
  } else {
    destinationStateName()
  }

  fun originCityState() = "${originCityName()} \n(${originStateName()})"

  fun destinationCityState() = "${destinationCityName()} \n(${destinationStateName()})"

  /**
   * @return formatted origin district, city, state
   */
  fun originDistrictCityState() = if(originDistrictName().isNotNullOrEmpty()) {
    originDistrictName() + "\n" + originStateName()
  } else {
    originStateName()
  }

  /**
   * @return formatted destination city, state
   */
  fun destinationDistrictCityState() = if(destinationDistrictName().isNotNullOrEmpty()) {
    destinationDistrictName() + "\n" + destinationStateName()
  } else {
    destinationStateName()
  }

  /**
   * @return intermediary stops
   */
  fun tripRoute(): String {
    val stopBuilder = StringBuilder()
    stopBuilder.append(originCityName())
        .append(" - ")
    if (!TextUtils.isEmpty(stop1City)) {
      stopBuilder.append(StringUtils.capitalize(stop1City))
          .append(" - ")
    }
    if (!TextUtils.isEmpty(stop2City)) {
      stopBuilder.append(StringUtils.capitalize(stop2City))
          .append(" - ")
    }
    stopBuilder.append(destinationCityName())
    return stopBuilder.toString()
  }

  fun tripContractRoute(): String {
    if(contractType==ContractType.INTRACITY.type){
      return StringUtils.capitalize(origin)?:""
    }else{
      val stopBuilder = StringBuilder()
      stopBuilder.append(originCityName())
        .append(" -> ")
      if (!TextUtils.isEmpty(stop1City)) {
        stopBuilder.append(StringUtils.capitalize(stop1City))
          .append(" -> ")
      }
      if (!TextUtils.isEmpty(stop2City)) {
        stopBuilder.append(StringUtils.capitalize(stop2City))
          .append(" -> ")
      }
      stopBuilder.append(destinationCityName())
      return stopBuilder.toString()
    }

  }

  /**
   * @return intermediary stops
   */
  fun tripRouteOriginDes(): String {
    return if(isItIntraCityContract()&& isFlexible){
      reportingCenters()
    }else if(isItIntraCityContract()){
      reportingCenters()
    }else{
      val stopBuilder = StringBuilder()
      stopBuilder.append(originCityName())
        .append(" -> ")
      stopBuilder.append(destinationCityName())
      stopBuilder.toString()
    }
  }

  fun isIntracity() : Boolean{
    return if(loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.intracityAdhoc.name), ignoreCase = true) == true
      || loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.intracityRegular.name), ignoreCase = true) == true){
      true
    } else {
      false
    }
  }

  fun isIntracityVisibility() : Int{
    return if(loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.intracityAdhoc.name), ignoreCase = true) == true
      || loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.intracityRegular.name), ignoreCase = true) == true){
      View.VISIBLE
    } else {
      View.GONE
    }
  }

  fun isPlacementsIntracityVisibility() : Int{
    return if(loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.intracityAdhoc.name), ignoreCase = true) == true
      || loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.intracityRegular.name), ignoreCase = true) == true){
      View.VISIBLE
    } else {
      View.GONE
    }
  }

  fun isPlacementsIntercityVisibility() : Int{
    return if(loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.ftlRegular.name), ignoreCase = true) == true){
      View.VISIBLE
    } else {
      View.GONE
    }
  }


  fun isIntracityRegular() : Boolean{
    return loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.intracityRegular.name), ignoreCase = true) == true
  }

  /**
   * Placement route details
   */
//  fun getOriginAndDestinationName(): String {
//    val stopBuilder = StringBuilder()
//    stopBuilder.append(capitalize(routeInfo?.origin?.centerName)).append(" -> ")
//    stopBuilder.append(capitalize(routeInfo?.destination?.centerName))
//    return stopBuilder.toString()
//  }

  /**
   * Placement route details
   */
  fun getOriginAndDestinationName(): String {
    return if(isIntracity()){
      capitalize(routeInfo?.origin?.centerName) ?: ""
    }else{
      val stopBuilder = StringBuilder()
      stopBuilder.append(capitalize(routeInfo?.origin?.centerName)).append(" -> ")
      stopBuilder.append(capitalize(routeInfo?.destination?.centerName))
      stopBuilder.toString()
    }
  }

  /**
   * @return is trips is multi drop
   */
  fun isMultiDrop() = (stop1City.isNotNullOrEmpty() || stop2City.isNotNullOrEmpty())

  /**
   * @return requested capacity
   */
  fun requestedCapacityMg() = "Guarantee: $requestedCapacityMg MT"

  /**
   * @return intermediary Stops string
   */
  fun intermediaryStops(): String {
    val stopBuilder = StringBuilder()
    if (!TextUtils.isEmpty(stop1City)) {
      stopBuilder.append(StringUtils.capitalize(stop1City))
          .append("(")
          .append(StringUtils.capitalize(stop1State))
          .append(")")
    }
    if (!TextUtils.isEmpty(stop2City)) {
      stopBuilder.append(", ")
          .append(StringUtils.capitalize(stop2City))
          .append("(")
          .append(StringUtils.capitalize(stop2State))
          .append(")")
    }
    return stopBuilder.toString()
  }

  /**
   * @return formatted bid amount
   */
  fun bidAmount() = if (transactionBid != null) {
    when (transactionBid!!.status()) {
      Accepted, Open, Rejected, Cancelled -> "₹ ${StringUtils.formatAmount(transactionBid!!.bidAmount)}"
      else -> ""
    }
  } else {
    ""
  }

  /**
   * @return formatted bid amount
   */
  fun bidContractAmount() = if (transactionBid != null) {
    when (transactionBid!!.status()) {
      Accepted, Open, Rejected -> "₹ ${StringUtils.formatAmount(transactionBid!!.bidAmount)}"
      else -> ""
    }
  } else {
    ""
  }

  fun bidAmountValue() = if (transactionBid != null) {
    when (transactionBid!!.status()) {
      Accepted, Open, Rejected, Cancelled -> transactionBid!!.bidAmount.toString()
      else -> ""
    }
  } else {
    ""
  }

  /**
   * @return bid label
   */
  fun amountLabel() = when (bidStatus()) {
    Open, Rejected, Cancelled -> "Your Bid"
    Accepted -> "Confirmed Price"
    else -> ""
  }

  /**
   * @return truckTypeDrawable basis [truckType]
   */
  @DrawableRes
  fun truckTypeDrawableRes() = DrawableProviderUtils.truckTypeDrawableRes(truckType)

  /**
   * @return vehicleCloseOpenDrawable basis[indent tyoe]
   */
  @DrawableRes
  fun vehicleCloseOpenDrawable() = DrawableProviderUtils.vehicleOpenCancelDrawableRes(if (transactionStatus=="cancelled"){"cancel"}else{"open"})

  /**
   * @return vehicleRateDrawable basis[indent tyoe]
   */
  @DrawableRes
  fun vehicleRateDrawable() = DrawableProviderUtils.vehicleRateDrawableRes(if (transactionStatus=="cancelled"){"cancel"}else{"open"})

  /**
   * @return vehicleCloseOpenOperatingPerMonthDrawable basis[indent tyoe]
   */
  @DrawableRes
  fun  vehicleCloseOpenOperatingPerMonthDrawable() = DrawableProviderUtils.vehicleCloseOpenOperatingPerMonthDrawable(if (transactionStatus=="cancelled"){"cancel"}else{"open"} ,contractType )

  /**
   * @return vehicleOperationDrawableKmPerMonth basis[indent tyoe]
   */
  @DrawableRes
  fun vehicleOperationDrawableKmPerMonth() = DrawableProviderUtils.vehicleOperationDrawableKmPerMonth(if (transactionStatus=="cancelled"){"cancel"}else{"open"}, contractType)

  /**
   * @return vehicleUsageDrawable basis[indent tyoe]
   */
  @DrawableRes
  fun vehicleUsageDrawable() = DrawableProviderUtils.vehicleUsageDrawable(if (transactionStatus==TransactionStatus.Cancelled.statusId){"cancel"}else{"open"})

  /**
   * @return nepDrawable basis[indent tyoe]
   */
  @DrawableRes
  fun nepDrawable() = DrawableProviderUtils.nepDrawable(if (transactionStatus==TransactionStatus.Cancelled.statusId){"cancel"}else{"open"})

  /**
   * @return vehicleOperationDrawablePerHrs basis[indent tyoe]
   */
  @DrawableRes
  fun vehicleOperationDrawablePerHrs() = DrawableProviderUtils.vehicleOperationDrawablePerHrs(if (transactionStatus=="cancelled"){"cancel"}else{"open"})

  /**
   * @return tripCloseOpenDrawable basis[indent tyoe]
   */
  @DrawableRes
  fun tripCloseOpenDrawable() = DrawableProviderUtils.tripOpenCancelDrawableRes(if (transactionStatus=="cancelled"){"cancel"}else{"open"})

  /**
   * @return intracityContractType basis[contract type]
   */
  @DrawableRes
  fun intracityContractTypeDrawable() = DrawableProviderUtils.intracityContractType(contractType,isFlexible)

  /**
   * @return truck_type with placed capacity
   */
  fun truckTypeWithCapacity() =
    "$truckType/${StringUtils.formatAmount(placedTruckPassing ?: 0.0)}MT"

  /**
   * @return truck_type with placed capacity
   */
  fun truckCapacity() = "${StringUtils.formatAmount(placedTruckPassing ?: 0.0)}MT"

  /**
   * Formatted required at
   */
  fun requiredAt() = _requiredOn?.let { DateUtils.daysDiffWithTimeStr(it, DatePatterns.OrionDateFormat) }

  fun requiredAtWithTime() = "${_requiredOn?.let { DateUtils.daysDiffWithTimeStr(it, DatePatterns.OrionDateFormat)}}, ${DateUtils.getUtcToIstFormatTimeOnly(_requiredOn)}"

  fun isBidEndingTimeExist() : Int{
    if(requestType == RequestType.Contract.type){
      return if(contractBiddingEndTime.isNullOrBlank() || contractBiddingEndTime.equals("null", ignoreCase = true)) View.GONE else View.VISIBLE
    }else{
      return if(bidEndingTime.isNullOrBlank() || bidEndingTime.equals("null", ignoreCase = true)) View.GONE else View.VISIBLE
    }
  }

  fun isBidOpen(): Boolean {

    var endTime : String = ""
    if(requestType == RequestType.Contract.type){
      //contract
      endTime = contractBiddingEndTime?.takeIf {
        it.isNotBlank() && !it.equals("null", ignoreCase = true)
      } ?: return true //assume if contractBiddingEndTime is null -> bid is open

    }else{
      //Load
      endTime = bidEndingTime?.takeIf {
        it.isNotBlank() && !it.equals("null", ignoreCase = true)
      } ?: return true //assume if contractBiddingEndTime is null -> bid is open
    }

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
      timeZone = TimeZone.getTimeZone("IST")
    }

    return try {
      val now = Date()
      val endDate = format.parse(endTime)
      endDate?.after(now) ?: true
    } catch (e: Exception) {
      true // If parsing fails, assume bid is open
    }
  }

  fun isMarketplaceBidOpen(): Boolean {

    var endTime : String = ""
      endTime = contractBiddingEndTime?.takeIf {
        it.isNotBlank() && !it.equals("null", ignoreCase = true)
      } ?: return true

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
      timeZone = TimeZone.getTimeZone("IST")
    }

    return try {
      val now = Date()
      val endDate = format.parse(endTime)
      endDate?.after(now) ?: true
    } catch (e: Exception) {
      true // If parsing fails, assume bid is open
    }
  }


  fun formattedLoadAndContractBidEndingTime() : String{
    if(requestType == RequestType.Contract.type){
      return formattedContractBiddingEndTime()
    }else{
      return formattedBiddingEndTime()
    }
  }

  fun bidAmountLabel()=
    if(isItFRContract()){
      "Bid Amount Per Trip"
    } else if(isItIntraCityContract()){
      "Bid Amount Per Month"
    } else if(isItLHContract()){
        "Bid Amount Per Trip"
    } else{
      "Bid Amount"
    }

  fun bidAmountLabelEditHint()=
    if(isItFRContract()){
      "Enter bid amount per trip"
    } else if(isItIntraCityContract()){
      "Enter bid amount for entire contract"
    } else if(isItLHContract()){
        "Enter bid amount per trip"
    } else{
      "Enter bid amount"
    }

  fun formattedContractBiddingEndTime()= if (contractBiddingEndTime.isNullOrBlank() || contractBiddingEndTime.equals("null", ignoreCase = true) ){
    ""}
  else{
    try{
      val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      format.setTimeZone(TimeZone.getTimeZone("IST"))
      val date1: Date = format.parse(format.format(Date()))
      val date2: Date = format.parse(contractBiddingEndTime)
      if (date2.compareTo(date1) > 0) {
        "Closes in ${DateUtils.timeDiff(date1.time,date2.time)}"
      } else {
        "Closed ${DateUtils.daysDiffWithTimeStr(contractBiddingEndTime?:"", DatePatterns.OrionDateFormat) }, ${DateUtils.getUtcToIstFormatTimeOnly(contractBiddingEndTime)}"
      }
    }catch(e:Exception){
      ""
    }
  }

  fun formattedBiddingEndTime()= if (bidEndingTime.isNullOrBlank() || bidEndingTime.equals("null", ignoreCase = true) ){
    ""}
    else{
      try{
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        format.setTimeZone(TimeZone.getTimeZone("IST"));
        val date1: Date = format.parse(format.format(Date()))
        val date2: Date = format.parse(bidEndingTime)
          Log.d("Time-Remaing", "${DateUtils.timeDiff(date1.time,date2.time)}")
        if (date2.compareTo(date1) > 0) {
          "Closes in ${DateUtils.timeDiff(date1.time,date2.time)}"
        } else {
          "Closed ${ DateUtils.daysDiffWithTimeStr(bidEndingTime, DatePatterns.OrionDateFormat) }, ${DateUtils.getUtcToIstFormatTimeOnly(bidEndingTime)}"
        }
      }catch(e:Exception){
        ""
        //Log.d("formattedBiddingEndTime::catch",""+e.printStackTrace())
      }
    }
  /**
   * Required at background as per designs
   */
  @DrawableRes
  fun requiredAtBg() =
    _requiredOn?.let { DrawableProviderUtils.daysDiffBgDrawableRes(it, DatePatterns.OrionDateFormat) }

  @DrawableRes
  fun requiredAtDraw() =
    _requiredOn?.let { DrawableProviderUtils.daysDiffBgDrawableResDraw(it, DatePatterns.OrionDateFormat) }

  /**
   * Required at text color as per status
   */
  @ColorRes
  fun requiredTextColor() =
    ColorProviderUtils.getStatusColor(bidStatus().status.lowercase())

  fun getTruckTypeAndNo() = truckSpecification?.let {
    capitalize(truckType?:"")+ " "+ it.truckDispName
  }

  fun formattedTruckType() = capitalize(truckType?:"")+ " Truck"
  /**
   * Get truck details/type
   * Don't show MT for demand_type = Intracity for Load and LH/Intracity contracts
   */
  fun truckDetail() = if (isDMTIndent()) {
    capitalize(truckType!!) ?: ""
  } else {
      truckSpecification?.let {
        if(requestType==RequestType.Contract.type){
          if(contractType==ContractType.FRC.type){
            it.truckDispName + "(" + StringUtils.formatAmount(requestedCapacityMg) + " MT)"
          }else{
            it.truckDispName
          }
        } else if (demandType==DemandType.Intracity.type){
          it.truckDispName
        }else{
          it.truckDispName + "(" + StringUtils.formatAmount(requestedCapacityMg) + " MT)"
        }
      }
  }



  fun truckTypeWithMT()= truckSpecification?.let { it.truckDispName + "(" + StringUtils.formatAmount(requestedCapacityMg) + " MT)"}

  //fetch truck name
  fun getTruckName() = vehicleInfo?.truckDisplayName?:""

  //fetch truck type
  fun getFormattedTruckType() = capitalize(vehicleInfo?.truckType?:"")+ " Truck"

  fun originCentreName()= StringUtils.capitalize(originCenterName)?:" "
  fun additionalKMsRates()= "${intracityExtraKmRate()} per extra KM | ${intracityExtraHourRate()} per extra hour"

  fun reportingCenters(): String{
    val reportingCenters = StringBuilder()
    reportingCenters.append(origin)
    if(secondaryReportingCenters?.isNotEmpty() == true){
      reportingCenters.append(", ")
      reportingCenters.append(secondaryReportingCenters[0].originCenterName)
  }
    return reportingCenters.toString()
  }

  /**
   * Trip display name for toolbar title
   */
  fun tripDisplayName() =
    "${capitalize(originState)} - ${capitalize(destinationState)}"

  fun tripIntracityDisplayName() =
    "${capitalize(origin)}|${distance.toInt()} KM | ${requirementDuration} Hr"

//    "${StateModel.idFromName(originState)} - ${StateModel.idFromName(destinationState)}"
//    when (tripType) {
//      AdvancePending -> "${StateModel.idFromName(originState)} - ${StateModel.idFromName(
//          destinationState
//      )} (${DateUtils.daysDiffStr(_requiredOn, DatePatterns.OrionDateFormat)})".toUpperCase()
//      else -> "${StateModel.idFromName(originState)} - ${StateModel.idFromName(destinationState)}"
//    }

  /**
   * @return bid difference
   */
  fun bidDifference(): String {
    if (bidStatus() == Cancelled) {
      return "(The load request itself is cancelled)"
    }
    if (numBids > 1 && lowestBid != null && lowestBid!! > 0) {
      return transactionBid?.diffFromLowestBid(lowestBid!!, isPMTIndent()) ?: ""
    }
    return ""
  }

  /**
   * @return bid status
   */
  fun bidStatus() = TransactionBidStatus.byStatusKey(transactionBid?._status ?: "na")

  override fun filter(query: String) =
    origin.contains(query, true) || destination.contains(query, true)
        || originState.contains(query, true) || destinationState.contains(query, true)

  /**
   * @return bid text
   */
  fun bidText(): String {
    return if (!isDMTIndent())
      "Bid placed for ₹ ${
        StringUtils.formatAmount(
          transactionBid?.bidAmount ?: 0.0
        )
      }" + if (isPMTIndent()) " /MT" else ""
    else {
      "Bid Placed"
    }
  }

  /**
   * @return lowest bid difference
   */
  fun lowestbidDifference() =
    if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
      if (isPMTIndent()) {
        " (₹ ${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - (lowestBid ?: 0.0))}" + " /MT"
      } else {
        " (₹ ${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - (lowestBid ?: 0.0))}"
      }
    } else {
      ""
    }

  fun contractLowestbidDifference():String {
    return if ((transactionBid?.bidAmount ?: 0.0 > lowestBid?:0.0) && (numBids > 0)) {
      if (isPMTIndent()) {
        " ₹ ${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - ( lowestBid?:0.0))}" + " /MT"
      } else {
        " ₹ ${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - ( lowestBid?:0.0))}"
      }
    } else {
      ""
    }
  }

  /**
   * @return lowest percentage diff
   */
  fun userBidLessThanFivePercentage(): Boolean =
    if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
      if (transactionBid?.bidAmount != null && lowestBid != null) {
        val perc = ((transactionBid?.bidAmount!! - lowestBid!!) / transactionBid?.bidAmount!!) * 100
        perc < 5
      } else {
        false
      }

    } else {
      false
    }

  /**
   * @return lowest bid text
   */
  fun lowestbidText() =
    if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
      if (isPMTIndent()) {
        "Lowest Bid: ₹ ${StringUtils.formatAmount(lowestBid ?: 0.0)}/MT (-${
          StringUtils.formatAmount(
            (transactionBid?.bidAmount ?: 0.0) - (lowestBid ?: 0.0)
          )
        }/MT)"
      } else {
        "Lowest Bid: ₹ ${StringUtils.formatAmount(lowestBid ?: 0.0)} (-${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - (lowestBid ?: 0.0))})"
      }
    } else {
      ""
    }

  /**
   * @return benchmark price text
   */
  fun benchmarkPriceText(): String {
    guidancePrice?.let {
      return if (isPMTIndent()) {
        "Benchmark Price: ₹ ${StringUtils.formatAmount(guidancePrice)}/MT (-${
          StringUtils.formatAmount(
            transactionBid!!.bidAmount - guidancePrice
          )
        }/MT)"
      } else {
        "Benchmark Price: ₹ ${StringUtils.formatAmount(guidancePrice)} (-${
          StringUtils.formatAmount(
            transactionBid!!.bidAmount - guidancePrice
          )
        })"
      }
    }
    return ""
  }

  /**
   * @return set image if supplier bid is more than lowest bid
   */
  fun setLowestBidImage() =
    if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
      View.VISIBLE
    } else {
      View.GONE
    }

  /**
   * @return set close bracket text if lowest bid is present
   */
  fun setCloseText() =
    if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
      View.VISIBLE
    } else {
      View.GONE
    }


  /**
   * @return true if indent type(pmt/ftl)
   */
  fun isPMTIndent() = biddingType?.lowercase() == "pmt"

  /**
   * @return true if request type(dmt)
   */
  fun isDMTIndent() = isDmt != null && isDmt == true

  /**
   * @return true if speed is express
   */
  fun isExpress() = speed == "EXP"

  /**
   * @return expressText with tat
   */
  fun expressText(showNum: Boolean): String {
    val sb = StringBuilder()
    if (showNum)
      sb.append("2.")
    sb.append("Exp. loading time: ")
    if (tatMinutes != null) {
      val tat = tatMinutes?.toDouble() ?: 0.0
      if (tat > 60) {
        sb.append(tat / 60)
          .append(" hrs")
      } else {
        sb.append(tat)
          .append(" min")
      }
    }
    return sb.toString()
  }

  /**
   * bid amount text
   */
  fun bidAmountText() = if (isPMTIndent()) {
    "Bid placed: ₹${StringUtils.formatAmount(transactionBid!!.bidAmount)}/MT"
  } else {
    "Bid placed: ₹${StringUtils.formatAmount(transactionBid!!.bidAmount)}"
  }

  /**
   * Suggested price w.r.t lowest bid price text
   */
  fun benchmarkSuggestedAmount(): String {
    guidancePrice?.let {
      val bid: Double = transactionBid!!.bidAmount
      var diff = 500
      if (isPMTIndent()) {
        diff = 25
        //diff = (diff/requestedCapacityMg).roundToInt()
      }
      val suggestedBidAmount: Double
      suggestedBidAmount = if ((bid - guidancePrice) > diff) {
        guidancePrice
      } else {
        guidancePrice - (diff - (bid - guidancePrice))
      }
      if (suggestedBidAmount <= 0) {
        return ""
      }
      return if (isPMTIndent()) {
        "Suggested Bid: ₹${StringUtils.formatAmount(suggestedBidAmount)}/MT"
      } else {
        "Suggested Bid: ₹${StringUtils.formatAmount(suggestedBidAmount)}"
      }
    }
    return ""
  }

  /**
   * benchmark suggest bid visibility
   */
  fun benchmarkSuggestedBidVisibility() = if (benchmarkSuggestedAmount().isNotNullOrEmpty()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * suggested price w.r.t benchmark price
   */
  fun lowestSuggestedAmount() : String {
    lowestBid?.let {
      val bid: Double = transactionBid!!.bidAmount
      var diff = 500
      if (isPMTIndent()) {
        //diff = (diff/requestedCapacityMg).roundToInt()
        diff=25
      }
      val suggestedBidAmount : Double
      suggestedBidAmount = if ((bid - lowestBid!!) > diff) {
        lowestBid!!
      } else {
        lowestBid!! - (diff - (bid - lowestBid!!))
      }
      if (suggestedBidAmount <= 0) {
        return ""
      }
      return if (isPMTIndent()) {
        "Suggested Bid: ₹${StringUtils.formatAmount(suggestedBidAmount)}/MT"
      } else {
        "Suggested Bid: ₹${StringUtils.formatAmount(suggestedBidAmount)}"
      }
    }
    return ""
  }

  /**
   * lowest suggest bid visibility
   */
  fun lowestSuggestedBidVisibility() = if (lowestSuggestedAmount().isNotNullOrEmpty()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * Condition-1 check
   */
  fun layoutOneVisibility() : Boolean {
    val bid: Double = transactionBid!!.bidAmount
    var condition1 = false
    var condition2 = true
    guidancePrice?.let {
      if ((bid >= guidancePrice * 0.9) && (bid <= guidancePrice)) {
        condition1 = true
      }
    }
    lowestBid?.let {
      if (bid > lowestBid!! && numBids > 1) {
        condition2 = false
      }
    }
    return condition1 && condition2
  }

  /**
   * Condition-2 check
   */
  fun layoutTwoVisibility(): Boolean {
    val bid: Double = transactionBid!!.bidAmount
    var condition1 = true
    var condition2 = true
    guidancePrice?.let {
      if ((bid >= guidancePrice * 0.9) && (bid <= guidancePrice * 1.2)) {
        condition1 = false
      }
    }
    lowestBid?.let {
      if (bid > lowestBid!! && numBids > 1) {
        condition2 = false
      }
    }
    return condition1 && condition2
  }

  /**
   * Condition-3 check
   */
  fun layoutThreeVisibility(): Boolean {
    val bid: Double = transactionBid!!.bidAmount
    var condition1 = false
    var condition2 = true
    guidancePrice?.let {
      if ((bid >= guidancePrice * 0.9) && (bid <= guidancePrice * 1.2)) {
        lowestBid?.let {
          if ((lowestBid!! < guidancePrice) && (bid > lowestBid!!)) {
            condition1 = true
          }
        }
      }
      condition2 = (bid < guidancePrice * 0.9) || (bid > guidancePrice * 1.2)
    }
    lowestBid?.let {
      condition2 = condition2 && (bid > lowestBid!! && numBids > 1)
    }
    return condition1 || condition2
  }

  /**
   * Condition-4 check
   */
  fun layoutFourVisibility(): Boolean {
    val bid: Double = transactionBid!!.bidAmount
    var condition1 = false
    var condition2 = false
    guidancePrice?.let {
      if ((bid >= guidancePrice) && (bid <= guidancePrice * 1.2)) {
        condition2 = true
        lowestBid?.let {
          condition2 = false
          if (lowestBid!! > guidancePrice) {
            condition1 = true
          }
        }
      }
    }
    return condition1 || condition2
  }

  /**return time lapse between bid creation and indent creation
   *
   */
  fun timeLapse() :String{
    val sdf : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val creationTime = sdf.parse(creationTime)
    var time = Date().time - creationTime.time
    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60

    val elapsedHours : Long  = time / hoursInMilli
    time%=hoursInMilli

    val elapsedMinutes :Long = time /minutesInMilli
    time%=minutesInMilli

    return if (elapsedHours.toInt() !=0) {
      "$elapsedHours hrs, $elapsedMinutes min"
    } else{
      "$elapsedMinutes min"
    }
  }

  fun isNEPrequired() = if (nepState != null) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isLobVisibility(isDialog:Boolean) = if (formattedVehicleLob()!=null && isDialog) {
    View.VISIBLE
  } else {
    View.GONE
  }

    fun isRemarkAvailable(isDialog:Boolean) = if (additionalRemarks.isNotNullOrEmpty() && isDialog) {
        View.VISIBLE
    } else {
        View.GONE
    }

    fun getRemarks() = if(additionalRemarks.isNotNullOrEmpty()) "Remarks: ${additionalRemarks}" else ""


  fun isTicketVisibility(isDialog:Boolean) = if (fmsTicketId!=null && isDialog) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * layout visibility one
   */
  fun oneVisibility() =
    if (layoutOneVisibility() && !(layoutTwoVisibility() || layoutThreeVisibility() || layoutFourVisibility())) {
      View.VISIBLE
    } else {
      View.GONE
    }

  /**
   * layout visibility two
   */
  fun twoVisibility() =
    if (layoutTwoVisibility() && !(layoutOneVisibility() || layoutThreeVisibility() || layoutFourVisibility())) {
      View.VISIBLE
    } else {
      View.GONE
    }

  /**
   * layout visibility three
   */
  fun threeVisibility() =
    if (layoutThreeVisibility() && !(layoutOneVisibility() || layoutTwoVisibility() || layoutFourVisibility())) {
      View.VISIBLE
    } else {
      View.GONE
    }

  /**
   * layout visibility four
   */
  fun fourVisibility() =
    if (layoutFourVisibility() && !(layoutOneVisibility() || layoutTwoVisibility() || layoutThreeVisibility())) {
      View.VISIBLE
    } else {
      View.GONE
    }

  /**
   * revise bid button visibility
   *
   */
  fun reviseButtonVisibility() = if (layoutThreeVisibility() || layoutFourVisibility()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  // Contract end bid time
  fun bidEndTime(): String {
    if(transactionStatus=="cancelled"){
      return ""
    }else if (contractBiddingEndTime != null) {
      val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      format.setTimeZone(TimeZone.getTimeZone("IST"));
      val date1: Date = format.parse(format.format(Date()))
      val date2: Date = format.parse(contractBiddingEndTime)
      if (date2.compareTo(date1) < 0) {
        return "Bidding Closed on " + formatDate(
          DateUtils.parseDate(
            contractBiddingEndTime!!,
            DatePatterns.OrionDateFormat
          ), "dd MMM h:mm a"
        )
      } else {
        return "Bidding Closes on " + formatDate(
          DateUtils.parseDate(
            contractBiddingEndTime!!,
            DatePatterns.OrionDateFormat
          ), "dd MMM h:mm a"
        )
      }

    } else {
      return ""
    }

  }

  // Contract end bid time
  fun bidEndDate(): String {
    if(transactionStatus=="cancelled"){
      return ""
    }else if (contractBiddingEndTime != null) {
      val format = SimpleDateFormat("yyyy-MM-dd")
      format.setTimeZone(TimeZone.getTimeZone("IST"))
      val date1: Date = format.parse(format.format(Date()))
      val date2: Date = format.parse(contractBiddingEndTime)
      val istDate = DateUtils.getUtcToIstFormatTime(contractBiddingEndTime)
      val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
      val timeFormat = SimpleDateFormat("h a")
      val date3: Date = sdf.parse(istDate)
      val biddingDateTime = date3
      val c = Calendar.getInstance()
      c.time = biddingDateTime
      val endTime = timeFormat.format(biddingDateTime)
      c.add(Calendar.HOUR, -1)
      val timeMinusHrs = c.time
      val startTime = timeFormat.format(timeMinusHrs)
      if (date2.compareTo(date1) ==0) {
        return "Live bidding at $startTime to $endTime"

      } else if(date2.compareTo(date1) >0){
        return "Closes on " + formatDate(
          DateUtils.parseDate(
            contractBiddingEndTime!!,
            DatePatterns.OrionDateFormat
          ), "dd MMM h:mm a"
        )
      }else{
          ""
      }

    } else {
      return ""
    }
    return ""
  }

  fun bidDifferenceContract(): String {
    if (bidStatus() == Cancelled) {
      return ""
    }
    if (bidStatus() == Accepted) {
      return ""
    }
    if (bidStatus() == Open) {
      return ""
    }
  if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)&&numBids > 1 && lowestBid != null && lowestBid!! > 0) {
      return "You lost the bid by "+contractLowestbidDifference()
    }
    return ""
  }

  fun isContractBiddingOpen(): Boolean {
    if (contractBiddingEndTime != null) {
      val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      format.setTimeZone(TimeZone.getTimeZone("IST"));
      val date1: Date = format.parse(format.format(Date()))
      val date2: Date = format.parse(contractBiddingEndTime)
      return date2.compareTo(date1) >= 0
    }
    return false
  }

  fun isLoadBiddingOpen(): Boolean {
    if (bidEndingTime != null) {
      val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
      format.setTimeZone(TimeZone.getTimeZone("IST"));
      val date1: Date = format.parse(format.format(Date()))
      val date2: Date = format.parse(bidEndingTime)
      return date2.compareTo(date1) >= 0
    }
    return false
  }

  fun isUnderOneHour(): Boolean {
    if (contractBiddingEndTime != null && isContractBiddingOpen()) {
      try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        format.setTimeZone(TimeZone.getTimeZone("IST"));
        val date1: Date = format.parse(format.format(Date()))
        val date2: Date = format.parse(contractBiddingEndTime)
        if (date2.compareTo(date1) > 0) {
          val mills: Long = date2.getTime() - date1.getTime()
          val hours = (mills / (1000 * 60 * 60)).toInt()
          val mins = (mills / (1000 * 60)).toInt() % 60
          val secs = ((mills / 1000).toInt() % 60).toLong()
          if (hours < 1 && (mins > 0 || secs > 0)) {
            return true
          }

        }
      } catch (e: Exception) {
        Log.i("exception", e.toString())
      }
    }
    return false
  }

    fun isUnderTwoHourLoadAndContract() : Boolean {
        if (requestType == RequestType.Contract.type) {
            if (contractBiddingEndTime != null && isBidOpen()) {
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    format.setTimeZone(TimeZone.getTimeZone("IST"));
                    val date1: Date = format.parse(format.format(Date()))
                    val date2: Date = format.parse(contractBiddingEndTime)
                    if (date2.compareTo(date1) > 0) {
                        val mills: Long = date2.getTime() - date1.getTime()
                        val hours = (mills / (1000 * 60 * 60)).toInt()
                        val mins = (mills / (1000 * 60)).toInt() % 60
                        val secs = ((mills / 1000).toInt() % 60).toLong()
                        if (hours < 2 && (mins > 0 || secs > 0)) {
                            return true
                        }

                    }
                } catch (e: Exception) {
                    Log.i("exception", e.toString())
                    return false
                }
            }
        } else {
            if (bidEndingTime != null && isBidOpen()) {
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    format.setTimeZone(TimeZone.getTimeZone("IST"));
                    val date1: Date = format.parse(format.format(Date()))
                    val date2: Date = format.parse(bidEndingTime)
                    if (date2.compareTo(date1) > 0) {
                        val mills: Long = date2.getTime() - date1.getTime()
                        val hours = (mills / (1000 * 60 * 60)).toInt()
                        val mins = (mills / (1000 * 60)).toInt() % 60
                        val secs = ((mills / 1000).toInt() % 60).toLong()
                        if (hours < 2 && (mins > 0 || secs > 0)) {
                            return true
                        }

                    }
                } catch (e: Exception) {
                    Log.i("exception", e.toString())
                    return false
                }
            }
        }
        return false
    }

  fun isUnderOneHourLoadAndContract() : Boolean {
    if (requestType == RequestType.Contract.type) {
      if (contractBiddingEndTime != null && isBidOpen()) {
        try {
          val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
          format.setTimeZone(TimeZone.getTimeZone("IST"));
          val date1: Date = format.parse(format.format(Date()))
          val date2: Date = format.parse(contractBiddingEndTime)
          if (date2.compareTo(date1) > 0) {
            val mills: Long = date2.getTime() - date1.getTime()
            val hours = (mills / (1000 * 60 * 60)).toInt()
            val mins = (mills / (1000 * 60)).toInt() % 60
            val secs = ((mills / 1000).toInt() % 60).toLong()
            if (hours < 1 && (mins > 0 || secs > 0)) {
              return true
            }

          }
        } catch (e: Exception) {
          Log.i("exception", e.toString())
          return false
        }
      }
    } else {
      if (bidEndingTime != null && isBidOpen()) {
        try {
          val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
          format.setTimeZone(TimeZone.getTimeZone("IST"));
          val date1: Date = format.parse(format.format(Date()))
          val date2: Date = format.parse(bidEndingTime)
          if (date2.compareTo(date1) > 0) {
            val mills: Long = date2.getTime() - date1.getTime()
            val hours = (mills / (1000 * 60 * 60)).toInt()
            val mins = (mills / (1000 * 60)).toInt() % 60
            val secs = ((mills / 1000).toInt() % 60).toLong()
            if (hours < 1 && (mins > 0 || secs > 0)) {
              return true
            }

          }
        } catch (e: Exception) {
          Log.i("exception", e.toString())
          return false
        }
      }
    }
    return false
  }


  fun contractBidStatusText(): String =
    if(transactionStatus=="cancelled"){
      "Cancelled"
    }
    else if (bidStatus() == Accepted || bidStatus() == Cancelled || bidStatus() == Rejected || transactionStatus=="allocated") {
      "Result Declared"
    } else {
      if (isContractBiddingOpen()) {
        if (isUnderOneHour()) {
          "Live Bidding"
        } else {
          "Collecting Bids"
        }

      } else {
        "Bidding Closed"
      }

    }

  fun contractEventStatusText(): String =
    if(transactionStatus=="cancelled"){
      "cancelled"
    }
    else if (bidStatus() == Accepted || bidStatus() == Cancelled || bidStatus() == Rejected) {
      "closed_order"
    } else {
      if (isContractBiddingOpen()) {
        if (isUnderOneHour()) {
          "active_bidding"
        } else {
          "active_order"
        }

      } else {
        "closed_order"
      }

    }

  fun contractBidsTimerText(): String =
    if (bidStatus() == Accepted || bidStatus() == Cancelled || bidStatus() == Rejected) {
      ""
    } else {
      if (isContractBiddingOpen()) {
        if (isUnderOneHour()) {
          "Closes in"
        } else {
          "Live Bidding at"
        }

      } else {
        "Awaiting Result"
      }

    }

  fun contractBidsTimerTextVisibility() =
     if (transactionStatus=="cancelled"|| transactionStatus=="allocated"||bidStatus() == Accepted || bidStatus() == Cancelled || bidStatus() == Rejected) {
      View.GONE
    } else {
      View.VISIBLE
    }

  fun contractBidsStatusTextVisibility() =
    if(transactionStatus==TransactionStatus.Cancelled.statusId){
      View.GONE
    }
    else {
      View.VISIBLE
    }

  fun isLHContract() = if (contractType == ContractType.LH_FTL.type) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isFtlRegularVisibility() = if(loadType?.equals(LoadTypeUtils.getLoadType(LoadTypes.ftlRegular.name), ignoreCase = true) == true){
    View.VISIBLE
  } else {
    View.GONE
  }



  //use this function to identify the bid type
  fun isItContract() = requestType == RequestType.Contract.type

  fun isItLHContract() = contractType == ContractType.LH_FTL.type
  fun isItFRContract() = contractType == ContractType.FRC.type
  fun isItIntraCityContract() = contractType == ContractType.INTRACITY.type

  fun isItContractOrLoadV2()= if (requestType == RequestType.Contract.type || requestType != RequestType.Contract.type) {
    true
  } else {
    false
  }

  fun isItContractOrLoad()= if (requestType == RequestType.Contract.type) {
    "Contract"
  } else {
    "Load"
  }

  /**
   * Show live bidding condition
   * 1. Contract type
   * 2. Bid status == Open
   * 3. Close time under one hour
   */

  fun isItContractAndLiveBidding() = if (requestType == RequestType.Contract.type && bidStatus().statusKey == TransactionBidStatus.Open.statusKey  && isLiveBidding()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isLiveBidding(): Boolean {
    return isUnderOneHourLoadAndContract()
  }



  fun isFRCContractV2() = if (contractType == ContractType.FRC.type) {
    true
  } else {
    false
  }
  //non-delhivery
  fun isFRCContract() = if ((requestType==RequestType.Contract.type) && contractType == ContractType.FRC.type) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isFRCLHContract() = if (contractType == ContractType.FRC.type || contractType==ContractType.LH_FTL.type) {
    View.VISIBLE
  } else {
    View.GONE
  }

  //delhivery intracity
  fun isLHIntraCityContract() = if (contractType == ContractType.LH_FTL.type || contractType==ContractType.INTRACITY.type) {
    View.VISIBLE
  } else {
    View.GONE
  }


  fun isIntraCityContract() =  if ((requestType==RequestType.Contract.type) && contractType == ContractType.INTRACITY.type) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isIntraCityFlexibleContract() = if ((requestType==RequestType.Contract.type)&&contractType == ContractType.INTRACITY.type && isFlexible==true) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isIntraCityFixedContract() = if ((requestType==RequestType.Contract.type) && contractType == ContractType.INTRACITY.type && isFlexible==false) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isIntraCityContractWithRemarks() = if (contractType == ContractType.INTRACITY.type && contractRemarks.isNotNullOrEmpty()) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isIntraCityContractWithBid() = if (contractType == ContractType.INTRACITY.type && transactionBid!=null) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun getDaysPerMonth() =
    if(costData?.dpm?.toInt() == 1){
        "${costData.dpm.toInt()} day"
    }else{
      "${costData?.dpm?.toInt()} days"
    }


  fun isPerMonthVisible() = if (costData?.dpm ==null || costData.dpm.toInt() == 0) {
    View.GONE
  } else {
    View.VISIBLE
  }

  fun isNoPermitVisible() = if (costData?.nep == null) {
    View.GONE
  } else {
    View.VISIBLE
  }

  fun isHoursPerDayVisible() = if (costData?.hpd ==null || costData.hpd.toInt() == 0) {
    View.GONE
  } else {
    View.VISIBLE
  }

  fun isLHContinousContract() = if (continuousConnection == true) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isLHVehicleVisible() = if (contractType == ContractType.LH_FTL.type && vehicleCountPerRoute != null) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isLHIntraCityVehicleVisible() = if ((contractType == ContractType.LH_FTL.type && vehicleCountPerRoute != null) || contractType==ContractType.INTRACITY.type) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isLHVehicleRouteVisible() = if (contractType == ContractType.LH_FTL.type && operatingDays!=null) {
    View.VISIBLE
  } else {
    View.GONE
  }

  fun isLHIntraCityVehicleRouteVisible() = if ((contractType == ContractType.LH_FTL.type && operatingDays!=null)|| contractType==ContractType.INTRACITY.type) {
    View.VISIBLE
  } else {
    View.GONE
  }



  fun totalVehicleCountOperationDays():String=
    if(contractType==ContractType.INTRACITY.type){
      vehicleOperatingDaysPerMonth()
    }else{
      if(vehicleCountCCLane!=null&& vehicleCountPerRoute!=null){
        (vehicleCountCCLane!! * vehicleCountPerRoute!!).toString() + " Vehicle"
      }else if(vehicleCountCCLane==null && vehicleCountPerRoute!=null){
        (1*vehicleCountPerRoute!!).toString() + " Vehicle"
      }else {
        ""
      }
    }

  fun vehicleOperatingDays():String=if(isItIntraCityContract()){"~"+intracityKms+ " Kms"} else "$operatingDays days a week"

  fun vehicleOperatingDaysLabel():String=if(isItIntraCityContract()){"Per Month"} else "Operating Days"


  fun placementsVehicleOperatingDaysLabel():String=if(isIntracity()){"Per Month"} else "Operating Days"

  fun vehicleOperatingDaysPerMonth()= intracityDays?.toString()+" days"
  fun vehicleOperatingDaysPerMonthLabel():String=if(isItIntraCityContract()){"Per Month"} else "Tentative Total vehicles"

  fun placementsVehicleOperatingHrsPerDays()="${costData?.hpd?.toInt()} h"

  fun vehicleOperatingHrsPerDays()="$intracityHours h"

  fun adhocIntracityKms()= "${distance.toInt()} Kms"
  fun adhocIntracityhrs()= " - $requirementDuration hrs"

  fun intracityHours() = intracityHours+"h/day"
  fun intracityDays() = "$intracityDays days/month"
  fun intracityKms() = "~$intracityKms Kms/month"

  fun intracityExtraKmRate()="₹ "+ intracityExtraKmRate
  fun intracityExtraHourRate()="₹ "+ intracityExtraHourRate
  fun intracityExtraDayRate()="₹ "+ intracityExtraDayRate

  /**
   * rpk: Rate per extra kilometer
   * rpd: Rate per extra day
   * rph: Rate per extra hour
   */
  //placements flow specific function
  fun isPlacementsExtraKmRateAvailable() = if(costData?.rpk == null || costData.rpk.toInt() == 0)
    View.GONE
  else
    View.VISIBLE

  fun isPlacementsExtraHourRateAvailable() = if(costData?.rph == null || costData.rph.toInt() == 0)
    View.GONE
  else
    View.VISIBLE

  fun isPlacementsExtraDayRateAvailable() = if(costData?.rpd == null || costData.rpd.toInt() == 0)
    View.GONE
  else
    View.VISIBLE

  fun isPlacementsPaymentInfoAvailable() : Int{
    return if((costData?.rpk == null || costData.rpk.toInt() == 0)
      && (costData?.rph == null || costData.rph.toInt() == 0)
      && (costData?.rpd == null || costData.rpd.toInt() == 0))
      View.GONE
    else
      View.VISIBLE
  }


  fun placementsExtraKmRate() : String{
    return if(costData?.rpk != null){
      "₹ "+ costData.rpk.toInt()
    }else{
      ""
    }
  }

  fun placementsExtraHourRate() : String{
    return if(costData?.rph != null){
      "₹ "+ costData.rph.toInt()
    }else{
      ""
    }
  }

  fun placementsExtraDayRate() : String{
    return if(costData?.rpd != null){
      "₹ "+ costData.rpd.toInt()
    }else{
      ""
    }
  }


  fun intracityContractType()=if(contractType==ContractType.INTRACITY.type && isFlexible==false){"Fixed"}else if(contractType==ContractType.INTRACITY.type && isFlexible==true){"Flexible"} else{""}

  fun paymentSlabsVisibility() = if (contractType==ContractType.INTRACITY.type) {
    if(transactionStatus==TransactionStatus.Cancelled.statusId){
      View.GONE
    }else{
      if (transactionBid != null) {
        View.VISIBLE
      } else {
        View.GONE
      }
    }
  } else {
    View.GONE
  }

  fun isPaymentSlabsVisible():Boolean = if (contractType==ContractType.INTRACITY.type) {
    if(transactionStatus==TransactionStatus.Cancelled.statusId){
     false
    }else{
      transactionBid != null
    }
  } else {
    false
  }

  fun routeVehicleMarginVisibility()=if (contractType==ContractType.INTRACITY.type) {
    if(transactionStatus==TransactionStatus.Cancelled.statusId){
      View.VISIBLE
    }else{
      if (transactionBid != null) {
        View.GONE
      } else {
        View.VISIBLE
      }
    }
  } else {
    View.VISIBLE
  }

  fun vehiclePermitRequiredText():String=
   if(nepRequired!=null) {
     if (nepRequired!!) "Required " else "Not required "
   }else{
     ""
   }

  fun placementVehiclePermitRequiredText():String=
    if(costData != null) {
      if (costData.nep) "Not required" else "Required"
    }else{
      ""
    }


  fun totalVehicleCountOnList():String=
    if(vehicleCountCCLane!=null&& vehicleCountPerRoute!=null){
      "(X "+(vehicleCountCCLane!! * vehicleCountPerRoute!!).toString()+" Veh.)"
    }else if(vehicleCountCCLane==null && vehicleCountPerRoute!=null){
      "(x "+(1*vehicleCountPerRoute!!).toString()+" Veh.)"
    }else {
      ""
    }

  fun vehicleCCCount():String=
    if(vehicleCountCCLane!=null){
      (vehicleCountCCLane).toString()
    }else if(vehicleCountCCLane==null){
      "1"
    }else {
      "1"
    }
  fun vehicleDepText():String=
    if(vehicleCountCCLane!=null){
     if(vehicleCountCCLane!!.toInt()>1){
       "Daily Departure(s)"
     }else{
       "Daily Departure"
     }
    }else{
      "Daily Departure"
    }

  fun vehicleRouteCount():String=
    if(vehicleCountPerRoute!=null){
     vehicleCountPerRoute!!.toString()
    }else {
      ""
    }

//    fun getIntermediaryStops():String{
//        if(requestType == RequestType.Contract.type){
//
//        }
//    }

  fun getIntermediateStops(): String {
    return try {
      if (requestType == RequestType.Contract.type) {
        getContractStops()
      } else {
        getLoadStops()
      }
    } catch (e: Exception) {
      "No Stops"
    }
  }

  fun getContractStops(): String {
    val centers = haltCenters ?: return "No Stops"
    if (centers.size < 2) return "No Stops"

    var numStops = 0
    for (i in 1 until centers.size - 1) {
      if (centers[i].name != centers[i + 1].name) {
        numStops++
      }
    }

    return if (numStops > 0) "$numStops Stops" else "No Stops"
  }

   fun getLoadStops(): String {
    return if (indentOrigin == "LH") {
      val stops = indentHaltCenters?.size ?: 0
      if (stops > 0) "$stops Stops" else "No Stops"
    } else {
      val stops = listOf(stop1City, stop2City, pickup1City, pickup2City)
        .count { !it.isNullOrBlank() }

      if (stops > 0) "$stops Stops" else "No Stops"
    }
  }


  fun haltStops():String=
    if(haltCenters!=null){
      if(haltCenters!!.size>=2){
        var numStops = 0
        var i =1
        while (i< haltCenters!!.size-1) {
          if(haltCenters!![i].name != haltCenters!![i+1].name){
           numStops++
          }
          i++
        }
        if(numStops>0) (numStops).toString()+" stops" else ""
      }else{
        ""
      }
    }else  {
      ""
    }

  fun biddingTypeText()=
    if(biddingType=="FTL"){
      "/FTL per trip"
    }else{
      "/MT per trip"
    }

  fun tentativeTripCount()=
    if(!isItLHContract()&& tentativeTripCount!=null){
      tentativeTripCount.toString()+" Trips"
    }else{
      ""
    }

  fun contractValidity()=
    if(!isItLHContract()&& contractValidity!=null){
     "for "+ contractValidity.toString()+" weeks"
    }else{
      ""
    }

  fun tripLHWays()=
    if(isItLHContract() && routeType!=null){
      if (routeType == "one_way") {
        if (continuousConnection == true) {
         "1 Way . Continuous"
        } else {
         "1 Way"
        }
      } else {
        if (continuousConnection == true) {
           "2 Way . Continuous"
        } else {
           "2 Way"
        }
      }
    }else{
      ""
    }

  fun operatingDistancePerMoth()= "~"+intracityKms+ " Kms/Month"
  fun operatingDurations()= intracityDays+ " days/Month"+ " \u2022 "+ intracityHours+ "h/day"


  fun tripWays()=
    if(isItLHContract() && routeType!=null){
      if (routeType == "one_way") {
          "1 Way"
      } else {
          "2 Way"
      }
    }else{
      ""
    }

    fun tripWaysNew() = if(isItLHContract() && routeType!=null){
      if (routeType == "one_way") {
        "One Way"
      } else {
        "Two Way"
      }
    } else ""
  }






@BindingAdapter("layoutMarginStart")
fun setLayoutMarginBottom(view: View, dimen: Int) {
  val layoutParams = view.layoutParams as MarginLayoutParams
  layoutParams.marginStart = dimen.toInt()
  view.layoutParams = layoutParams
}

/**
 * Truck specification detail
 */
data class TruckSpecification(
  @SerializedName("default_MG") val defaultMG: Double?,
  @SerializedName("truck_display_name") val truckDispName: String?,
  @SerializedName("truck_type")val truckType:String?
)

/**
 * Truck specification detail
 */
data class HaltCenters(
  @SerializedName("rel_etd") val relEtd: String?,
  @SerializedName("rel_eta") val relEta: String?,
  @SerializedName("name")val name:String?,
  @SerializedName("state")val state:String?,
  @SerializedName("past_travel_hrs")val pastTravelHrs:String?,
  @SerializedName("halt_hrs")val haltHrs:String?,
  @SerializedName("longitude")val longitude:String?,
  @SerializedName("latitude")val latitude:String?

)

/**
 * Secondary reporting centers
 */
data class SecondaryReportingCenters(
  @SerializedName("origin_center_name") val originCenterName: String?,
  @SerializedName("origin_city")val originCity:String?,
  @SerializedName("origin_state")val originState:String?,
  @SerializedName("longitude")val longitude:String?,
  @SerializedName("latitude")val latitude:String?

)

/**
 * Monthly Payout detail
 */
data class PaymentSlabs(
  @SerializedName("distance") val distance: String?,
  @SerializedName("payout") val monthlyPayout: String?,

)

data class PickUpCoordinate(
  @SerializedName("lat") val lat: String?,
  @SerializedName("lon") val lon: String?,

  )

const val SUB_REQUEST_TYPE_INTRACITY = "intracity"


/* actions */
const val HomeBidsRequestAction_ViewDetails = "bid_details"
const val HomeBidsRequestAction_PlaceBid = "place_bid"
const val HomeBidsRequestAction_ReviseBid = "revise_bid"
const val HomeBidsRequestAction_ViewOtherDetails = "bid__others_details"
const val HomeBidsRequestAction_DeleteItem = "delete_item"
const val HomeBidsRequestAction_AcceptBid = "accept_bid"
const val HomeBidsRequestAction_NavigationMap = "navigate_map"
const val HomeBidsRequestAction_InitiateCall = "initiate_call"




