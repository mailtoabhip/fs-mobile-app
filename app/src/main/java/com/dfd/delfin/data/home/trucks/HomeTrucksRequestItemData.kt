package com.dfd.delfin.data.home.trucks

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.dfd.delfin.R
import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.utils.StringUtils.capitalize
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HomeTrucksRequestItemData(
    @SerializedName("latest_inventory_uuid")
    val inventoryId: String?,

    @SerializedName("vehicle_number")
    val vehicleNumber: String,

    @SerializedName("truck_type")
    val truckType: String,

    @SerializedName("truck_display_name")
    val truckSize: String,

    @SerializedName("truck_uuid")
    val truckUuid: String,

    @SerializedName("capacity")
    val capacity: Double,

    @SerializedName("current_city")
    var currentCityName: String,

    @SerializedName("current_city_code")
    var currentCityCode: String,

    @SerializedName("destination_city")
    var unloadingDestination: String,

    @SerializedName("destination_city_code")
    var unloadingDestinationCode: String,

    @SerializedName("ownership")
    var ownership: String?=null,

    @SerializedName("latest_inventory_status")
    var latestStatus: String,

    @SerializedName("uuid")
    var latestUUID: String,

    @SerializedName("origin_cluster_id")
    var originClusterId: String,

    @SerializedName("destination_cluster_id")
    var destinationClusterId: String,

    @SerializedName("unloading_destination_amount")
    var unloadingDestinationAmount: Double = 0.0,

    @SerializedName("unloading_destination_rate")
    var unloadingDestinationRate: Double = 0.0,

    @SerializedName("sourced_as")
    val sourcedAs: String,

    @SerializedName("demand_type")
    val demandType: List<String>? = null,

    @SerializedName("fastag_id")
    val fastagTagId: String? = null,

    @SerializedName("fastag_vrn")
    val fastagVrn: String? = null,

    @SerializedName("fastag_balance")
    var fastagBalance: String? = null,

    @SerializedName("fastag_issued_by")
    val fastagIssuedBy: String? = null,

    @SerializedName("fastag_linked_bank")
    val fastagLinkedBank: String? = null,

    @SerializedName("fastag_tag_status")
    val fastagTagStatus: String? = null,

    @SerializedName("fastag_last_balance_updated")
    val fastagLastBalanceUpdated: String? = null,

    @SerializedName("fastag_last_transaction")
    val fastagLastTransaction: FastagLastTransaction? = null
)

    : BaseKeyTypeModel<String>(){

    override fun key() = inventoryId ?: ""

    @DrawableRes
    fun truckImage() : Int{
        return if(truckType =="closed")
            R.drawable.ic_closed
        else if( truckType== "open")
            R.drawable.ic_open
        else
            R.drawable.ic_trailer
    }

    fun truckNumber() = vehicleNumber

    fun ownership() = when(ownership) {
        "owns_truck" -> "Own Truck"
        "market_truck" -> "Market Truck"
        else -> capitalize((((ownership?.split("_"))?.toTypedArray())?.joinToString(" ")))
    }

    fun truckSizeAndCap() = truckSize() + " | " + truckCapacity()

    fun originCity() = capitalize(currentCityName)

    fun destinationCity() = capitalize(unloadingDestination)


    fun truckName(): String {
        return if (truckType == "closed")
            "Container"
        else if (truckType == "open")
           "Open Body"
        else
            "Trailer"
    }

    fun truckCapacity():String  = "$capacity MT"

    fun truckSize(): String = truckSize

    @ColorRes
    fun statusColor() = if(latestStatus == "Free")
        R.color.bid_placed_green
      else if(latestStatus == "Active")
        R.color.bid_placed_green
    else R.color.bid_placed_red

    fun statusText()= if(latestStatus == "Free")
        "Available"
    else ""

    fun statusVisibilty() = if(latestStatus == "not_available")
        View.VISIBLE
    else
        View.GONE

    fun locationVisibility() =  if(latestStatus == "Free")
        View.VISIBLE
    else
        View.GONE

    // FASTag helper methods
    fun verifiedIconVisibility() = if(fastagTagStatus.equals("Active", ignoreCase = true)) View.VISIBLE else View.GONE
    
    fun hasFastagInfo() = fastagTagId != null
    
    fun fastagSectionVisibility() = if(hasFastagInfo()) View.VISIBLE else View.GONE
    
    fun noFastagSectionVisibility() = if(!hasFastagInfo()) View.VISIBLE else View.GONE
    
//    fun lowBalanceWarningVisibility(): Int {
//        val balance = fastagBalance?.toDoubleOrNull() ?: 0.0
//        return if(hasFastagInfo() && balance < 100) View.VISIBLE else View.GONE
//    }

    fun fastagBalanceText() = "₹${fastagBalance ?: "0"}"
    
    fun fastagProviderText() = "${fastagIssuedBy ?: "IDFC"} FASTag by Delhivery"

    fun fastagTransactionInfo(): String {
        val transaction = fastagLastTransaction
        return transaction?.let {
            val sign = when (it.txnType) {
                "Debit" -> "-"
                "Credit" -> "+"
                else -> ""
            }
            
            val formattedDate = com.dfd.delfin.utils.DateUtils.formatFastagTransactionDateShort(it.datetime)
            
            "$sign ₹${it.amount} on $formattedDate"
        } ?: ""
    }

    
    fun fastagTransactionLocation() = fastagLastTransaction?.txnEvent ?: ""
    
    fun fastagLowBalanceWarning() = "Your FASTag is hotlisted due to low balance. Recharge to avoid blacklisting."

}

const val HomeTrucksRequestAction_ViewDetails = "truck_details"

const val HomeTrucksRequestAction_EditTruck = "edit_truck"

const val HomeTrucksRequestAction_ActivateTruck = "activate_truck"

const val HomeTrucksRequestAction_BuyFastag = "buy_fastag"

data class TruckFrequentItem(
    val truckType: String,
    val truckSize: String,
    val capacity: Double,
    val minCap: Double,
    val maxCap: Double,
    val sourcedAs: String
    ) {


    @DrawableRes
    fun truckImage(): Int {
        return if (truckType == "closed")
            R.drawable.ic_closed
        else if (truckType == "open")
            R.drawable.ic_open
        else
            R.drawable.ic_trailer
    }


    fun truckName(): String {
        return if (truckType == "closed")
            "Container"
        else if (truckType == "open")
            "Open Body"
        else
            "Trailer"
    }

    fun truckCapacity(): String = "$capacity MT"

    fun truckSize(): String = truckSize

}

data class FastagLastTransaction(
    @SerializedName("txn_id")
    val id: String,

    @SerializedName("txn_time")
    val datetime: String,

    @SerializedName("txn_amount")
    val amount: Double,

    @SerializedName("txn_type")
    val txnType: String?,

    @SerializedName("txn_details")
    val txnDetails: String?,
    
    @SerializedName("txn_event")
    val txnEvent: String?
) : Serializable

fun List<HomeTrucksRequestItemData>.names() =
        mapIndexed { _, truckModel ->
            return@mapIndexed truckModel.vehicleNumber
        }