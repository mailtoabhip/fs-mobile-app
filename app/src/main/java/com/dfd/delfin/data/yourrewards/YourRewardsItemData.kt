package com.dfd.delfin.data.yourrewards

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.dfd.delfin.R
import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.utils.DateUtils
import com.dfd.delfin.utils.DrawableProviderUtils
import com.dfd.delfin.utils.StringUtils
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class YourRewardsItemData(
  @SerializedName("origin_city_code") val originCityCode: String?=null,
  @SerializedName("origin_city") val originCity: String?=null,
  @SerializedName("origin_state_id") val originStateId: String?=null,
  @SerializedName("origin_state_name") val originStateName: String?=null,
  @SerializedName("origin_cluster_id") val originClusterId: String?=null,
  @SerializedName("destination_city_code") val destinationCityCode: String?=null,
  @SerializedName("destination_city") val destinationCity: String?=null,
  @SerializedName("destination_state_id") val destinationStateId: String?=null,
  @SerializedName("destination_state_name") val destinationStateName: String?=null,
  @SerializedName("destination_cluster_id") val destinationClusterId: String?=null,
  @SerializedName("truck_display_name") val truckDisplayName: String?=null,
  @SerializedName("truck_capacity") val truckCapacity: String?=null,
  @SerializedName("vehicle_number") val vehicleNumber: String?=null,
  @SerializedName("rate") val rate: Int?=0,
  @SerializedName("trip_date") val tripDate: String?=null,
  @SerializedName("sp_id") val spId: String?=null,
  @SerializedName("sp_name") val spName: String?=null,
  @SerializedName("child_sp_id") val childSpId: String?=null,
  @SerializedName("child_sp_name") val childSpName: String?=null,
  @SerializedName("rate_type") val rateType: String?=null,
  @SerializedName("proof_type") val proofType: String?=null,
  @SerializedName("proof_url") val proofUrl: List<String>?=null,
  @SerializedName("phone_number") val phoneNumber: String?=null,
  @SerializedName("submitted_date") val submittedDate: String?=null,
  @SerializedName("verification_state") val verificationState: String?=null,
  @SerializedName("rejection_reason") val rejectionReason: String?=null,
  @SerializedName("pricing_id") val pricingId: String,
  @SerializedName("amount_paid") val amountPaid: Int?=null,
  @SerializedName("utr_number") val utr: String?=null,
  @SerializedName("payment_date") val paymentDate: String?=null,
  @SerializedName("sort_key") val sortKey: String?=null,
  @SerializedName("advertised_rate_message") val advertisedRateMessage: String?=null,
  @SerializedName("update_required") val updateRequired: Boolean?=false,
  var isFullDetailsEnabled:Boolean = false

): BaseKeyTypeModel<String>() {
  override fun key() = pricingId

  fun detailsVisibility() = if(isFullDetailsEnabled)
    View.VISIBLE
  else
    View.GONE

  @DrawableRes
  fun toggleButton() = DrawableProviderUtils.rewardsFullDetailsRes(isFullDetailsEnabled)


  fun statusText()= if(verificationState == "pending")
    "Pending"
  else if(verificationState == "verified")
    "Approved"
  else "Rejected"

  @ColorRes
  fun statusTextColor() = if(verificationState == "pending")
    R.color.dark_orange
  else if(verificationState == "verified")
    R.color.reward_status_confirmed
  else R.color.reward_status_lost

  @ColorRes
  fun rewardsValueTextColor() = if(amountPaid != null)
    R.color.reward_confirmed
  else R.color.heading_black

  /**
   * @return formatted origin city name
   */
  fun originCityName() = StringUtils.capitalize(originCity) ?: ""

  /**
   * @return formatted destination city name
   */
  fun destinationCityName() = StringUtils.capitalize(destinationCity) ?: ""

  fun routes(): String {
    val stopBuilder = StringBuilder()
    stopBuilder.append(originCityName())
      .append(" - ")
    stopBuilder.append(destinationCityName())
    return stopBuilder.toString()
  }

  fun truckDetails():String = truckDisplayName+" ("+truckCapacity+"MT)"+" / "+vehicleNumber?.uppercase()

  fun isApproved(): Int = if(verificationState=="verified") View.VISIBLE else View.GONE

  fun isApprovedAndUpdated(): Int = if(verificationState=="verified" && updateRequired == true) View.VISIBLE else View.GONE


  fun rewardText(): String = if(verificationState=="rejected") "Reason of Rejection" else "Reward Earned"
  fun rewardsValue(): String =if(verificationState=="pending"){
    "-------------"
  }else if(verificationState=="verified") {
    if(amountPaid==null){
      "-------------"
    }else{
      "₹ "+ amountPaid.toString()
    }
  }else{
    StringUtils.capitalize(rejectionReason)?:"-------------"
  }
  fun utrValue():String =  utr?: "-------------"
  fun submittedDate():String =if(submittedDate!=null){
    DateUtils.getUtcToIstFormatDateWithSuffix(submittedDate)
  }else{
    "-------------"
  }
  fun paymentDate():String =if(paymentDate!=null){
    DateUtils.getUtcToIstFormatDateWithSuffix(paymentDate)
  }else{
    "-------------"
  }
}

data class RangeCondition(
  @Expose
  @SerializedName("column")
  var column: String?=null,

  @Expose
  @SerializedName("operator")
  var operator: String?=null,

  @Expose
  @SerializedName("value")
  var value: String?=null
)

const val YourRewardsItemDataAction_ViewDetails = "your_rewards"
const val YourRewardsItemDataAction_DownloadProof = "download_proof"