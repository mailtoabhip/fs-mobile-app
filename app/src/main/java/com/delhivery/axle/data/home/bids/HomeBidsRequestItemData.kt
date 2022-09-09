package com.delhivery.axle.data.home.bids

import android.R
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.IndentHaltCenters
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.bids.TransactionBidStatus
import com.delhivery.axle.data.bids.TransactionBidStatus.Accepted
import com.delhivery.axle.data.bids.TransactionBidStatus.Cancelled
import com.delhivery.axle.data.bids.TransactionBidStatus.Open
import com.delhivery.axle.data.bids.TransactionBidStatus.Rejected
import com.delhivery.axle.utils.ColorProviderUtils
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DrawableProviderUtils
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Transaction details
 */
data class HomeBidsRequestItemData(
  @SerializedName("material_type") val materialType: String,
  @SerializedName("pickup_location") val pickupLocation: String,
  @SerializedName("destination") val destination: String,
  @SerializedName("origin_state") val originState: String,
  @SerializedName("required_on") val _requiredOn: String,
  @SerializedName("target_price") val targetPrice: Double,
  @SerializedName("uuid") val uuid: String?,
  @SerializedName("transaction_id") val transactionId: String?,
  @SerializedName("truck_type") val truckType: String?,
  @SerializedName("origin") val origin: String,
  @SerializedName("intermediary_stop1") val stop1City: String,
  @SerializedName("intermediary_stop1_state") val stop1State: String,
  @SerializedName("intermediary_stop2") val stop2City: String,
  @SerializedName("intermediary_stop2_state") val stop2State: String,
  @SerializedName("intermediary_pickup_stop1") val pickup1City: String,
  @SerializedName("intermediary_pickup_stop2") val pickup2City: String,
  @SerializedName("destination_state") val destinationState: String,
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
  @SerializedName("client_confirmed") val clientConfirmationPending: Boolean?=null,
  @SerializedName("expected_arrival_time_pickup") val expectedArrivalTimePickup:String? =  null,
  @SerializedName("indent_halt_centers") val indentHaltCenters:List<IndentHaltCenters>? =  null,
  @SerializedName("pickup_location_address") val pickupLocationAddress: String?,
  @SerializedName("drop_location_address") val dropLocationAddress: String?,
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
  @SerializedName("destination_city_code") var destinationCityCode: String? =null,
var lowestBid: Double? = 0.0,
  var numBids: Int = 0,
  var transactionBid: TransactionBid? = null,
  var showing: Boolean = false,
  var bulkTransactionBids: List<TransactionBid> = mutableListOf()
) : BaseKeyTypeModel<String>() {
  override fun key() = uuid ?: transactionId!!

  fun loadDetails() = StringUtils.capitalize(materialType) ?: "Not available"

  fun target() = if (targetPrice > 0 && loadPricePercent > 0) {
    targetPrice * loadPricePercent / 100
  } else {
    0.0
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


  fun setTruckTypeText() = truckType!!.capitalize() ?: ""

  fun setUnAllocatedText()= if (unAllocatedVolume!=null && unAllocatedVolume != 0.0 ) "Unallocated Load: ${unAllocatedVolume.toInt()} MT" else ""

  fun setUnAllocatedVol() = if (unAllocatedVolume!=null && unAllocatedVolume != 0.0 ) "Unallocated vol: ${unAllocatedVolume.toInt()} MT" else ""



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
  fun originDistrictState() = if(originDistrictName().isNotNullOrEmpty()) {
    originDistrictName() + ", " + originStateName()
  } else {
    originStateName()
  }

  /**
   * @return destination district and state name
   */
  fun destinationDistrictState() = if(destinationDistrictName().isNotNullOrEmpty()) {
    destinationDistrictName() + ", " + destinationStateName()
  } else {
    destinationStateName()
  }

  /**
   * @return formatted origin district, city, state
   */
  fun originDistrictCityState() = if(originDistrictName().isNotNullOrEmpty()) {
    originCityName() + "\n" + originDistrictName() + "\n" + originStateName()
  } else {
    originCityName() + "\n" + originStateName()
  }

  /**
   * @return formatted destination city, state
   */
  fun destinationDistrictCityState() = if(destinationDistrictName().isNotNullOrEmpty()) {
    destinationCityName() + "\n" + destinationDistrictName() + "\n" + destinationStateName()
  } else {
    destinationCityName() + "\n" + destinationStateName()
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

  /**
   * @return intermediary stops
   */
  fun tripRouteOriginDes(): String {
    val stopBuilder = StringBuilder()
    stopBuilder.append(originCityName())
      .append(" > ")
    stopBuilder.append(destinationCityName())
    return stopBuilder.toString()
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
  fun bidAmountValue() = if (transactionBid != null) {
    when (transactionBid!!.status()) {
      Accepted, Open, Rejected, Cancelled ->transactionBid!!.bidAmount.toString()
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
   * @return truck_type with placed capacity
   */
  fun truckTypeWithCapacity() = "$truckType/${StringUtils.formatAmount(placedTruckPassing?: 0.0)}MT"

  /**
   * @return truck_type with placed capacity
   */
  fun truckCapacity() = "${StringUtils.formatAmount(placedTruckPassing?: 0.0)}MT"

  /**
   * Formatted required at
   */
  fun requiredAt() = DateUtils.daysDiffWithTimeStr(_requiredOn, DatePatterns.OrionDateFormat)

  /**
   * Required at background as per designs
   */
  @DrawableRes
  fun requiredAtBg() =
    DrawableProviderUtils.daysDiffBgDrawableRes(_requiredOn, DatePatterns.OrionDateFormat)

  @DrawableRes
  fun requiredAtDraw() =
    DrawableProviderUtils.daysDiffBgDrawableResDraw(_requiredOn, DatePatterns.OrionDateFormat)

  /**
   * Required at text color as per status
   */
  @ColorRes
  fun requiredTextColor() =
    ColorProviderUtils.getStatusColor(bidStatus().status.toLowerCase())

  /**
   * Get truck details/type
   */
  fun truckDetail() = if (isDMTIndent()){
      truckType!!.capitalize() ?:""
    }else{
    truckSpecification?.let {
      it.truckDispName + "(" + StringUtils.formatAmount(requestedCapacityMg) + " MT)"
    }
  }

  /**
   * Trip display name for toolbar title
   */
  fun tripDisplayName() =
    "${originState.capitalize()} - ${destinationState.capitalize()}"
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
    return if(!isDMTIndent())
      "Bid placed for ₹ ${StringUtils.formatAmount(
              transactionBid?.bidAmount ?: 0.0
      )}" + if (isPMTIndent()) " /MT" else ""
    else{
      "Bid Placed"
    }
  }

  /**
   * @return lowest bid difference
   */
  fun lowestbidDifference() = if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
    if (isPMTIndent()) {
      " (₹ ${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - (lowestBid ?: 0.0))}" + " /MT"
    } else {
      " (₹ ${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - (lowestBid ?: 0.0))}"
    }
  } else {
    ""
  }

  /**
   * @return lowest bid text
   */
  fun lowestbidText() = if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
    if (isPMTIndent()) {
      "Lowest Bid: ₹ ${StringUtils.formatAmount(lowestBid ?: 0.0)}/MT (-${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - (lowestBid ?: 0.0))}/MT)"
    } else {
      "Lowest Bid: ₹ ${StringUtils.formatAmount(lowestBid ?: 0.0)} (-${StringUtils.formatAmount((transactionBid?.bidAmount ?: 0.0) - (lowestBid ?: 0.0))})"
    }
  } else {
    ""
  }

  /**
   * @return benchmark price text
   */
  fun benchmarkPriceText() : String {
    guidancePrice?.let {
      return if (isPMTIndent()) {
        "Benchmark Price: ₹ ${StringUtils.formatAmount(guidancePrice)}/MT (-${StringUtils.formatAmount(transactionBid!!.bidAmount - guidancePrice)}/MT)"
      } else {
        "Benchmark Price: ₹ ${StringUtils.formatAmount(guidancePrice)} (-${StringUtils.formatAmount(transactionBid!!.bidAmount - guidancePrice)})"
      }
    }
    return ""
  }

  /**
   * @return set image if supplier bid is more than lowest bid
   */
  fun setLowestBidImage() = if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * @return set close bracket text if lowest bid is present
   */
  fun setCloseText() = if ((transactionBid?.bidAmount ?: 0.0 > lowestBid ?: 0.0) && (numBids > 1) && (lowestBid ?: 0.0 > 0.0)) {
    View.VISIBLE
  } else {
    View.GONE
  }


  /**
   * @return true if indent type(pmt/ftl)
   */
  fun isPMTIndent() = biddingType?.toLowerCase() == "pmt"

  /**
   * @return true if request type(dmt)
   */
  fun isDMTIndent() = isDmt!= null && isDmt == true

  /**
   * @return true if speed is express
   */
  fun isExpress() = speed?.compareTo("EXP") == 0



  /**
   * @return expressText with tat
   */
  fun expressText(showNum: Boolean): String {
    val sb = StringBuilder()
    if(showNum)
      sb.append("1. ")
    sb.append("Delhivery Load")
    if (tatMinutes != null) {
      val tat = tatMinutes?.toDouble() ?: 0.0
      if (tat > 60) {
        sb.append("(")
            .append(tat / 60)
            .append(" hrs)")
      } else {
        sb.append("(")
            .append(tat)
            .append(" min)")
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
  fun benchmarkSuggestedAmount() : String {
    guidancePrice?.let {
      val bid: Double = transactionBid!!.bidAmount
      var diff = 500
      if (isPMTIndent()) {
        diff=25
        //diff = (diff/requestedCapacityMg).roundToInt()
      }
      val suggestedBidAmount : Double
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
  fun layoutTwoVisibility() : Boolean {
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
  fun layoutThreeVisibility() : Boolean {
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
  fun layoutFourVisibility() : Boolean {
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
      "$elapsedHours hours, $elapsedMinutes minutes"
    } else{
      "$elapsedMinutes minutes"
    }
  }

  /**
   * layout visibility one
   */
  fun oneVisibility() = if (layoutOneVisibility() && !(layoutTwoVisibility() || layoutThreeVisibility() || layoutFourVisibility())) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * layout visibility two
   */
  fun twoVisibility() = if (layoutTwoVisibility() && !(layoutOneVisibility() || layoutThreeVisibility() || layoutFourVisibility())) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * layout visibility three
   */
  fun threeVisibility() = if (layoutThreeVisibility() && !(layoutOneVisibility() || layoutTwoVisibility() || layoutFourVisibility())) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * layout visibility four
   */
  fun fourVisibility() = if (layoutFourVisibility() && !(layoutOneVisibility() || layoutTwoVisibility() || layoutThreeVisibility())) {
    View.VISIBLE
  } else {
    View.GONE
  }

  /**
   * revise bid button visibility
   *
   */
  fun reviseButtonVisibility() = if(layoutThreeVisibility() || layoutFourVisibility()){
    View.VISIBLE
  } else{
    View.GONE
  }

}

@BindingAdapter("layoutMarginStart")
fun setLayoutMarginBottom(view: View, dimen: Float) {
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

/* actions */
const val HomeBidsRequestAction_ViewDetails = "bid_details"
const val HomeBidsRequestAction_PlaceBid = "place_bid"
const val HomeBidsRequestAction_ViewOtherDetails = "bid__others_details"
const val HomeBidsRequestAction_DeleteItem = "delete_item"
