package com.delhivery.axle.data.home.placements

import android.view.View
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsIntercityAdhocRequestItem
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsIntercityContractsRequestItem
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsIntracityAdhocRequestItem
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsIntracityContractsRequestItem
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HomePlacementsItemData(
    @SerializedName("contract_code") val contractCode: String?,
    @SerializedName("contract_id") val contractId: String?,
    @SerializedName("destination_center_code") val destinationCenterCode: String?,
    @SerializedName("destination_center_lat") val destinationCenterLat: Float?,
    @SerializedName("destination_center_long") val destinationCenterLong: Float?,
    @SerializedName("destination_center_name") val destinationCenterName: String?,
    @SerializedName("destination_center_state") val destinationCenterState: String?,
    @SerializedName("driver_name") val driverName: String?=null,
    @SerializedName("driver_phone") val driverPhone: String?=null,
    @SerializedName("origin_center_code") val originCenterCode: String?,
    @SerializedName("origin_center_lat") val originCenterLat: Float?,
    @SerializedName("origin_center_long") val originCenterLong: Float?,
    @SerializedName("origin_center_name") val originCenterName: String?,
    @SerializedName("origin_center_state") val originCenterState: String?,
    @SerializedName("reporting_time") val reportingTime: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("vehicle_id") val vehicleId: String?,
    @SerializedName("vehicle_number") val vehicleNumber: String?=null,
    @SerializedName("vehicle_type") val vehicleType: String?,
    @SerializedName("transporter_id") val transporterId: Int?,
    @SerializedName("transporter_supplier_id") val transporterSupplierId: String?,
    @SerializedName("halt_centers") var haltCenters:List<HaltCenters>? =  null,
    @SerializedName("confirmed_price") var confirmedPrice:Double?=null,
    @SerializedName("distance") var distance:Float?=null,
    @SerializedName("duration") var duration:Float?=null,
    @SerializedName("transaction_id") var transactionId: String?,

    var loadType:String?=null,
    var detailVisible:Boolean= false

): BaseKeyTypeModel<String>() {
    override fun key() = contractId?:""

    fun haltStops():String=
        if(haltCenters!=null){
            if(haltCenters!!.size>=2){
                var numStops = 0
                var i =1
                while (i< haltCenters!!.size-1) {
                    if(haltCenters!![i].haltCenterName != haltCenters!![i+1].haltCenterName){
                        numStops++
                    }
                    i++
                }
                (numStops).toString()+" stops"
            }else{
                ""
            }
        }else  {
            "0 stop"
        }

    fun missingVehicleVisibility()= if ((vehicleNumber==null || driverName==null || driverPhone==null)&& !detailVisible) View.VISIBLE else View.GONE

    fun missingDriverDetails()= if ( driverName==null || driverPhone==null) View.GONE else View.VISIBLE

    fun formattedConfirmedPrice()= "₹" + confirmedPrice?.let { StringUtils.formatAmount(it) }

    fun distance() = "$distance KM - "
    fun duration() = "$duration Hr"
    fun detailsVisible(): Int {
        return if(detailVisible)View.VISIBLE else View.GONE
    }
    fun detailsNotVisible(): Int {
        return if(detailVisible)View.GONE else View.VISIBLE
    }

    fun formatReportingTime():String?= reportingTime?.let { DateUtils.daysDiffWithDateTimeStr(it, "yyyy-MM-dd'T'HH:mm") }

    fun formatReportingTimeWithDiv():String= "| "+ reportingTime?.let { DateUtils.daysDiffWithDateTimeStr(it, "yyyy-MM-dd'T'HH:mm") }

    fun driverName():String? = driverName?.let { StringUtils.capitalize(driverName)}

    fun loadType():String? = loadType?.let {when(loadType){
        LoadTypes.ftlAdhoc.name->  "FTL Adhoc"
        LoadTypes.ftlRegular.name->  "FTL Contract"
        LoadTypes.intracityRegular.name->  "Intracity Contract"
        LoadTypes.intracityAdhoc.name->  "Intracity Adhoc"

        else -> {null}
    } }
}


data class HaltCenters(
        @SerializedName("halt_center_code") val haltCenterCode: String?,
        @SerializedName("halt_center_name") val haltCenterName: String?,
):Serializable







const val HomePlacementRequested_ViewDetails = "placement_details"
const val HOME_PLACEMENT_ITEM_DATA = "home_placement_item_data"

