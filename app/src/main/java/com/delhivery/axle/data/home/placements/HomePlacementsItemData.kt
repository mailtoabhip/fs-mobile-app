package com.delhivery.axle.data.home.placements

import android.view.View
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.home.fragments.placements.LoadTypes
import com.delhivery.axle.ui.home.fragments.placements.PlacementTypes
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.StringUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

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
    @SerializedName("origin") val origin: String?,
    @SerializedName("origin_state") val originState: String?,
    @SerializedName("destination") val destination: String?,
    @SerializedName("destination_state") val destinationState: String?,
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
    @SerializedName("ticket_flexible_contract_id") var ticketFlexibleContractId: String?=null,
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
                if(numStops==1){
                    (numStops).toString()+" stop"
                }else if (numStops>1){
                    (numStops).toString()+" stops"
                }else{
                    ""
                }

            }else{
                ""
            }
        }else  {
            ""
        }

    fun detailMissingText()  = if(vehicleNumber==null && (driverName==null || driverPhone==null)){
        "Vehicle/driver details missing"
    }else if(vehicleNumber==null){
        "Vehicle details missing"
    }else{
        "Driver details missing"
    }
    fun missingVehicleVisibility()= if ((vehicleNumber==null || driverName==null || driverPhone==null)&& !detailVisible) View.VISIBLE else View.GONE

    fun fillDetailsVisibility()= if ((vehicleNumber==null || driverName==null || driverPhone==null)) View.VISIBLE else View.GONE

    fun missingDriverDetails()= if ( driverName==null || driverPhone==null) View.GONE else View.VISIBLE

    fun editIconVisibility()= if (status=="Marked-in" ||((vehicleNumber==null || driverName==null || driverPhone==null))) View.GONE else View.VISIBLE

    fun callVisibility()= if (driverPhone!=null && vehicleNumber!=null && driverName!=null) View.VISIBLE else View.GONE

    fun formattedConfirmedPrice()= "₹" + confirmedPrice?.let { StringUtils.formatAmount(it) }

    fun confirmedPriceVisibility():Int {
        return if(!isFRCAdhoc())View.VISIBLE else View.GONE
    }
    fun distance() = "$distance KM - "
    fun duration() = "$duration Hr"
    fun detailsVisible(): Int {
        return if(detailVisible)View.VISIBLE else View.GONE
    }
    fun detailsNotVisible(): Int {
        return if(detailVisible)View.GONE else View.VISIBLE
    }

    fun formatReportingTime():String?= "Report "+reportingTime?.let { DateUtils.daysDiffWithDateTimeStr(it, "yyyy-MM-dd'T'HH:mm") }
    fun onlyFormatReportingTime():String?= reportingTime?.let { DateUtils.daysDiffWithDateTimeStr(it, "yyyy-MM-dd'T'HH:mm") }

    fun formatReportingTimeWithDiv():String= "| "+ reportingTime?.let { DateUtils.daysDiffWithDateTimeStr(it, "yyyy-MM-dd'T'HH:mm") }

    fun driverName():String? = driverName?.let { StringUtils.capitalize(driverName)}

    fun formattedOriginCity()= if(loadType ==LoadTypes.orionSpot.name || loadType== LoadTypes.orionFixed.name){
        StringUtils.capitalize(origin)+", "+StringUtils.capitalize(originState)
    }else{
        StringUtils.capitalize(originCenterName)+", "+StringUtils.capitalize(originCenterState)
    }
    fun formattedDestinationCity() = if(loadType ==LoadTypes.orionSpot.name || loadType== LoadTypes.orionFixed.name){
        StringUtils.capitalize(destination)+", "+StringUtils.capitalize(destinationState)
    }else{
        StringUtils.capitalize(destinationCenterName)+", "+StringUtils.capitalize(destinationCenterState)

    }
    fun relativeDurationStatus():String{
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        val date1: Date = format.parse(format.format(Date()))
        val date2: Date = format.parse(reportingTime)
        val statusDuration = if(status==PlacementTypes.Delayed.name) {"Delayed by ${DateUtils.timeDiff(date2.time,date1.time)}"} else if (status==PlacementTypes.Expected.name){"Expected in ${DateUtils.timeDiff(
          date1.time,date2.time)}"} else if(status=="Marked-in"){"Marked-in"} else ""
        return statusDuration
    }
    fun loadType():String? = loadType?.let {when(loadType){
        LoadTypes.ftlAdhoc.name->  "Delhivery Load"
        LoadTypes.ftlRegular.name->  "Delhivery Contract"
        LoadTypes.intracityRegular.name->  "Delhivery Contract"
        LoadTypes.intracityAdhoc.name->  "Delhivery Load"
        LoadTypes.orionFixed.name->  "Client Contract"
        LoadTypes.orionSpot.name->  "Client Load"
        else -> {null}
    } }

    fun isFRCAdhoc():Boolean{
        if(loadType==LoadTypes.intracityAdhoc.name && ticketFlexibleContractId!=null)
            return true
        return false
    }
    fun adhocOrFrcLabel():String{
        if(isFRCAdhoc()){
            return "FRC Contract"
        }
        return "Intracity Adhoc"
    }
}


data class HaltCenters(
        @SerializedName("halt_center_code") val haltCenterCode: String?,
        @SerializedName("halt_center_name") val haltCenterName: String?,
        @SerializedName("latitude") val latitude: String?,
        @SerializedName("longitude") val longitude: String?,

        ):Serializable







const val HomePlacementRequested_ViewDetails = "placement_details"
const val HOME_PLACEMENT_ITEM_DATA = "home_placement_item_data"

