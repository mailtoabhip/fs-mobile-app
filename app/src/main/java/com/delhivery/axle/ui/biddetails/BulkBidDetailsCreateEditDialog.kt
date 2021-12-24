package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.bids.*
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBulkBidCreateEditBinding
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.bids.BaseDmtBidSummaryRVAdapterItem
import com.delhivery.axle.ui.bids.DmtBidSummaryItem
import com.delhivery.axle.ui.bids.DmtBidsRVAdapter
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.view_bid_create_edit_item.view.*
import javax.inject.Inject

class BulkBidDetailsCreateEditDialog @Inject constructor(
    context: Context,
    private val transaction: HomeBidsRequestItemData,
    private val transactionBids: List<TransactionBid>? = mutableListOf(),/* transaction bid null for create new bid */
    private val truckTypes: List<TruckResponseArray>,
    private val dialogInterface: BulkBidsCreateEditInterface,
    private val unAllocatedLoad: Double,
    private val position: Int = 0,
    private val analyticsUtil: AnalyticsUtil,
    private var userPrefs: UserPrefs,
    private var fromPage :String,
    private var pageTitle :String

):AlertDialog(context), DmtBidsAdapterInterface {

    private lateinit var binding: DialogBulkBidCreateEditBinding
    val dmtBidSummaryItemDataList = mutableListOf<DmtBidSummaryItemData>()
    val dmtBidSummaryItemOperationList = mutableListOf<Pair<BaseDmtBidSummaryRVAdapterItem<*>,DataRVAdapterOperationType>>()
    var volumePlaced =0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* dialog binding */
        binding = DialogBulkBidCreateEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.route = transaction.tripRouteOriginDes()
        binding.request = transaction
        binding.page=pageTitle
        var availableTrucks: List<String>? = null
        availableTrucks = if( transaction.truckUUID is String) {
            listOf<String>(*((transaction.truckUUID as String).split(",")).toTypedArray())
        } else {
            transaction.truckUUID as List<String>
        }
        val truckTypesFiltered = mutableListOf<TruckResponseArray>()
        for(truck in truckTypes){
            for (item in availableTrucks){
                if(item == truck.truckUuid){
                    truckTypesFiltered.add(truck)
                    break
                }
            }
        }
        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )
        setCancelable(false)

        if(pageTitle=="EDIT BIDS"){

            binding.rvBids.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@BulkBidDetailsCreateEditDialog.adapter
                (adapter as DmtBidsRVAdapter).clearItems()
                var listVehicleData:ArrayList<VehicleBidData>?=ArrayList()
                for(item in (adapter as DmtBidsRVAdapter).itemsList()){
                    item.data
                }

                val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
                val confirmedBidsMap: MutableMap<String, MutableList<TransactionBid>?> = HashMap()

                // map same vehicle type with bids for open and confirmed bids
                for (bid in transactionBids!!) {
                    val key: String = bid.vehicleType!!
                    if(bid._status == "accepted"){
                        if (confirmedBidsMap.containsKey(key)) {
                            val list: MutableList<TransactionBid>? = confirmedBidsMap[key]
                            list!!.add(bid)
                        } else {
                            val list= mutableListOf<TransactionBid>()
                            list.add(bid)
                            confirmedBidsMap[key] = list
                        }
                    }
                    else if(bid._status == "open"){
                        if (map.containsKey(key)) {
                            val list: MutableList<TransactionBid>? = map[key]
                            list!!.add(bid)
                        } else {
                            val list= mutableListOf<TransactionBid>()
                            list.add(bid)
                            map[key] = list
                        }
                    }
                }

                // add map items to dmt data
                for (key in confirmedBidsMap.keys){
                    val truckCount:Int?=confirmedBidsMap[key]?.size
                    var vehicleCapacity = 0.0
                    for ( i in truckTypesFiltered){
                        if(i.truckUuid == key){
                            vehicleCapacity = i.defaultMG!!
                        }
                    }
                    volumePlaced += (truckCount!! * vehicleCapacity)
                    val dmtBidsItem = DmtBidSummaryItemData(key,vehicleCapacity, confirmedBidsMap[key]!![0].bidAmount,truckCount!!,"confirmed",
                            false, truckTypesFiltered,added = true)
                    dmtBidSummaryItemOperationList.add(Pair(DmtBidSummaryItem(dmtBidsItem), DataRVAdapterOperationType.Add))
                }

              //  get count of status
                for(key in map.keys){
                    val truckCount:Int?=map[key]?.size
                    val bidIdsList = mutableListOf<String>()

                    for(bid in map[key]!!){
                        bidIdsList.add(bid.id)
                    }
                    var vehicleCapacity = 0.0
                    for ( i in truckTypesFiltered){
                        if(i.truckUuid == key){
                            vehicleCapacity = i.defaultMG!!
                        }
                    }
                    volumePlaced += (truckCount!! * vehicleCapacity)
                    val dmtBidsItem = DmtBidSummaryItemData(key,vehicleCapacity, map[key]!![0].bidAmount,truckCount!!,"open",false,
                            truckTypesFiltered,bidIdsList,true)
                    dmtBidSummaryItemDataList.add(dmtBidsItem)
                    dmtBidSummaryItemOperationList.add(Pair(DmtBidSummaryItem(dmtBidsItem.copy()), DataRVAdapterOperationType.Add))

                }
                (adapter as DmtBidsRVAdapter).operation(dmtBidSummaryItemOperationList)

            }

        }else{
            binding.rvBids.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@BulkBidDetailsCreateEditDialog.adapter

                (adapter as DmtBidsRVAdapter).operation(listOf(
                        Pair(DmtBidSummaryItem(DmtBidSummaryItemData(truckTypesFiltered[0].truckUuid!!,truckTypesFiltered[0].defaultMG!!,0.0,0,
                                "open",true,truckTypesFiltered)), DataRVAdapterOperationType.Add)
                ))

            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            val createPayload = mutableListOf<VehicleBidData>()
            val modifyPayload = mutableListOf<ModifyVehicleData>()
            val removedBids = mutableListOf<String>()

            //check if vehicle types are not duplicate
            var duplicate = false
            val mapDuplicacy: MutableMap<String,Int> = HashMap()
            for(item in adapter.itemsList()) {
                val bidData = item.data as DmtBidSummaryItemData
                if( bidData.status != "confirmed"){
                    val key: String = bidData.vehicleType
                    if (mapDuplicacy.containsKey(key)) {
                        duplicate = true
                        break
                    } else {
                        mapDuplicacy[key] = 1
                    }
                }
            }
            var isZero = false
            for(item in adapter.itemsList()) {
                val bidData = item.data as DmtBidSummaryItemData
                if( bidData.status != "confirmed") {
                    if(bidData.pmtRate== 0.0 || bidData.truckCount ==0){
                        isZero = true
                        break
                    }
                }
            }
            if( duplicate  ){
                Toast.makeText(context, "Duplicate Vehicle types Found", Toast.LENGTH_SHORT).show()
            }
            else if(isZero){
                Toast.makeText(context, "Truck Count or PMT rate can't be zero", Toast.LENGTH_SHORT).show()
            }
            else {
                if (pageTitle == "EDIT BIDS") {
                    for (item in adapter.itemsList()) {
                        val bidData = item.data as DmtBidSummaryItemData
                        if (bidData.status != "confirmed") {
                            var match = false
                            for (i in dmtBidSummaryItemDataList) {
                                if (i.vehicleType == bidData.vehicleType) {
                                    var diff = 0
                                    var pmtFlag = false
                                    if (bidData.truckCount != i.truckCount) {
                                        diff = bidData.truckCount - i.truckCount
                                    }
                                    if (bidData.pmtRate !=0.0 && bidData.pmtRate != i.pmtRate) {
                                        pmtFlag = true
                                    }

                                    if (diff != 0 || pmtFlag) {
                                        modifyPayload.add(ModifyVehicleData(bidData.pmtRate, bidData.vehicleCapacity, diff, bidData.vehicleType, "modify", bidData.bidIds, pmtFlag))

                                    }
                                    dmtBidSummaryItemDataList.remove(i)
                                    match = true
                                    break
                                }
                            }
                            if (!match) {
                                if (bidData.truckCount != 0 && bidData.pmtRate != 0.0) {
                                    val freightCost = bidData.pmtRate * bidData.vehicleCapacity
                                    createPayload.add(VehicleBidData(bidData.pmtRate, bidData.vehicleCapacity, bidData.truckCount, bidData.vehicleType, freightCost))
                                }
                            }

                        }
                    }
                    for (i in dmtBidSummaryItemDataList) {
                        removedBids.addAll(i.bidIds)
                    }

                    dialogInterface.editBids(transaction.key(), position, createPayload, modifyPayload, removedBids, unAllocatedLoad)
                } else {
                    for (item in adapter.itemsList()) {
                        val bidData = item.data as DmtBidSummaryItemData
                        if (bidData.truckCount != 0 && bidData.pmtRate != 0.0) {
                            val freightCost = bidData.pmtRate * bidData.vehicleCapacity
                            createPayload.add(VehicleBidData(bidData.pmtRate, bidData.vehicleCapacity, bidData.truckCount, bidData.vehicleType, freightCost))
                        }
                    }
                    dialogInterface.createBids(transaction.key(), position, createPayload, unAllocatedLoad)
                }
                dismiss()
            }
        }

        binding.tvAdd.setOnClickListener {
            adapter.operation(listOf(Pair(DmtBidSummaryItem(
                    DmtBidSummaryItemData(truckTypesFiltered[0].truckUuid!!,truckTypesFiltered[0].defaultMG!!,0.0,0,"open",true,truckTypesFiltered)),
                    DataRVAdapterOperationType.Add)))
        }
    }

    private val adapter: DmtBidsRVAdapter by lazy { DmtBidsRVAdapter(this) }

    override fun handleAction(actionId: String, position: Int, item: BaseDmtBidSummaryRVAdapterItem<*>) {
        when(actionId){
            EXPAND_CARD -> {
                val bidData = item.data as DmtBidSummaryItemData
                bidData.expanded = !bidData.expanded
                if(currentFocus!=null) {
                    currentFocus?.clearFocus()
                    binding.root.clearFocus()
                }
                adapter.notifyItemChanged(position)
            }
            DELETE_ITEM ->{
                val bidData = item.data as DmtBidSummaryItemData
                volumePlaced -= (bidData.truckCount * bidData.vehicleCapacity)
                binding.volumePlaced.text = String.format(context.getString(R.string.msg_volume_of_bids),volumePlaced)
                adapter.operation(listOf(Pair(DmtBidSummaryItem(bidData), DataRVAdapterOperationType.Remove)))
                adapter.notifyDataSetChanged()

            }
        }
    }

    override fun itemCapacity(capacity: Double) {
        volumePlaced+=capacity
        binding.volumePlaced.text = String.format(context.getString(R.string.msg_volume_of_bids),volumePlaced)
    }

}

interface BulkBidsCreateEditInterface {

    fun createBids(
        transactionId : String,
        position: Int,
        createPayload: List<VehicleBidData>,
        unAllocatedLoad: Double
    )

    fun editBids(
        transactionId : String,
        position: Int,
        createPayload: List<VehicleBidData>,
        modifyPayload : List<ModifyVehicleData>,
        removedBids: List<String>,
        unAllocatedLoad: Double
    )
}