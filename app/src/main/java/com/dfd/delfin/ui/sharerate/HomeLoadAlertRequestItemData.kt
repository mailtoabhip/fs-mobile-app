package com.dfd.delfin.ui.loadAlert

import com.dfd.delfin.api.request.TruckSpecifications
import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.utils.StringUtils
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HomeLoadAlertRequestItemData(
  @SerializedName("supplier_id") val supplierId: String?=null,
  @SerializedName("supplier_name") val supplierName: String?= null,
  @SerializedName("current_city") val currentCity: String?= null,
  @SerializedName("current_city_code") val currentCityCode: String?= null,
  @SerializedName("origin_cluster_id") val currentClusterId: String?= null,
  @SerializedName("destination_city") val destinationCity: String?= null,
  @SerializedName("destination_city_code") val destinationCityCode: String?= null,
  @SerializedName("destination_cluster_id") val destinationClusterId: String?= null,
  @SerializedName("inventory_type") val inventoryType: String?= null,
  @SerializedName("demand_type") val demandType: List<String>?= null,
  @SerializedName("truck_specifications") val truckSpecifications: List<TruckSpecifications>?= null,
  @SerializedName("vehicle_number") var vehicleNumber: String? = null,
  @SerializedName("unloading_destination_amount") var unloadingAmount: Double? = null,
  @SerializedName("unloading_destination_rate") var unloadingRate: Double? = null,
  @SerializedName("open_matching_load") val openMatchingLoad: List<String>?=mutableListOf(),
  @SerializedName("created_at") val createdAt: String?= null,
  @SerializedName("inventory_uuid") val inventoryUuid: String,
  @SerializedName("truck_types") val truckType: List<String>?=mutableListOf(),
  @SerializedName("truck_uuid") val truckUuid: String?=null,
  @SerializedName("ownership") var ownership :String?=null,
  @SerializedName("valid_upto_dt") val validUptoDate: String?=null,
  @SerializedName("status") var status: String?=null,
  @SerializedName("origin_cluster_name"          ) var originClusterName          : String?= null,
  @SerializedName("validity_duration"            ) var validityDuration           : Int?= null,
  @SerializedName("created_by"                   ) var createdBy                  : String?= null,
  @SerializedName("updated_at"                   ) var updatedAt                  : String?= null,
  @SerializedName("destination_cluster_name"     ) var destinationClusterName     : String?= null,
  @SerializedName("updated_by"                   ) var updatedBy                  : String?= null,
  @SerializedName("destination_state") val destinationState: String?=null,
  @SerializedName("destination_state_code") val destinationStateCode: String?=null
): Serializable,BaseKeyTypeModel<String>(){
  override fun key() = inventoryUuid

  fun truckTypeAndLoads() = if(inventoryType=="alert"){
    if(vehicleNumber.isNotNullOrEmpty()) {
      vehicleNumber+" - "+(truckSpecifications?.get(0)?.truckType?:"") +" "+"\u2022"+" "+(openMatchingLoad?.size?:0)+" Loads"
    } else {
      (truckSpecifications?.get(0)?.truckType?:"") +" "+"\u2022"+" "+(openMatchingLoad?.size?:0)+" Loads"
    }
  }else{
    if(truckType!=null && truckType.size>0){
      truckType?.joinToString(separator = ", ") {it}+" "+"\u2022"+" "+(openMatchingLoad?.size?:0)+" Loads"
    }else{
      "0 Loads"
    }
  }

  fun tripRoute(): String {
    val stopBuilder = StringBuilder()
    stopBuilder.append(originCityName())
      .append(" - ")
    stopBuilder.append(destinationCityName())
    return stopBuilder.toString()
  }
  /**
   * @return formatted origin city name
   */
  fun originCityName() =  StringUtils.capitalize(currentCity) ?: ""

  /**
   * @return formatted destination city name
   */
  fun destinationCityName() = if(destinationState!=null){
    StringUtils.capitalize(destinationState) ?: ""
  }else{
    StringUtils.capitalize(destinationCity) ?: ""
  }

}
const val HomeLoadsAlertRequestAction_moreOption = "load_alert_more_option"
const val HomeLoadsAlertCardRequestAction_moreOption = "load_alert_card_more_option"
const val HomeLoadsAlertCardRequestAction_reward = "load_alert_card_reward"

const val EDIT_DELETE = "edit_delete"
