package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private val transactionBid: TransactionBid? = null,/* transaction bid null for create new bid */
    private val truckTypes: List<TruckResponseArray>,
    private val dialogInterface: BulkBidsCreateEditInterface,
    private val position: Int = 0,
    private val analyticsUtil: AnalyticsUtil,
    private var userPrefs: UserPrefs,
    private var fromPage :String,
    private var pageTitle :String

):AlertDialog(context), DmtBidsAdapterInterface {

    private lateinit var binding: DialogBulkBidCreateEditBinding
    val dmtBidSummaryItemDataList = mutableListOf<DmtBidSummaryItemData>()
    val dmtBidSummaryItemOperationList = mutableListOf<Pair<BaseDmtBidSummaryRVAdapterItem<*>,DataRVAdapterOperationType>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* dialog binding */
        binding = DialogBulkBidCreateEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.route = transaction.tripRouteOriginDes()
        binding.page=pageTitle
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
                //test data to be replaced with api data
                val bids: ArrayList<TransactionBid> =ArrayList()
                bids.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"1","","","","","6_TYRE(19FT)"))
                bids.add(TransactionBid("","confirmed",false,"","","","",6000.0,4444.0,"2","","","","","6_TYRE(19FT)"))
                bids.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"3","","","","","6_TYRE(19FT)"))
                bids.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"4","","","","","10_TYRE"))
                bids.add(TransactionBid("","rejected",false,"","","","",6000.0,5555.0,"5","","","","","10_TYRE"))

                val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
                val confirmedBidsMap: MutableMap<String, MutableList<TransactionBid>?> = HashMap()

                // map same vehicle type with bids for open and confirmed bids
                for (bid in bids) {
                    val key: String = bid.vehicleType!!
                    if(bid._status == "confirmed"){
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
                    for ( i in truckTypes){
                        if(i.truckUuid == key){
                            vehicleCapacity = i.defaultMG!!
                        }
                    }
                    val dmtBidsItem = DmtBidSummaryItemData(key,vehicleCapacity, confirmedBidsMap[key]!![0].pmtRate!!,truckCount!!,"confirmed",false,truckTypes,added = true)
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
                    for ( i in truckTypes){
                        if(i.truckUuid == key){
                            vehicleCapacity = i.defaultMG!!
                        }
                    }

                    val dmtBidsItem = DmtBidSummaryItemData(key,vehicleCapacity, map[key]!![0].pmtRate!!,truckCount!!,"open",false,truckTypes,bidIdsList,true)
                    dmtBidSummaryItemDataList.add(dmtBidsItem)
                    dmtBidSummaryItemOperationList.add(Pair(DmtBidSummaryItem(dmtBidsItem.copy()), DataRVAdapterOperationType.Add))

                }
                (adapter as DmtBidsRVAdapter).operation(dmtBidSummaryItemOperationList)

            }

        }else{
            binding.rvBids.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@BulkBidDetailsCreateEditDialog.adapter

                (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData(truckTypes[0].truckUuid!!,truckTypes[0].defaultMG!!,0.0,0,"open",true,truckTypes)), DataRVAdapterOperationType.Add)
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
            if( duplicate ){
                Toast.makeText(context, "Duplicate Vehicle types Found", Toast.LENGTH_SHORT).show()
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
                                    if (bidData.pmtRate != i.pmtRate) {
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
                                    createPayload.add(VehicleBidData(bidData.pmtRate, bidData.vehicleCapacity, bidData.truckCount, bidData.vehicleType))
                                }
                            }

                        }
                    }
                    for (i in dmtBidSummaryItemDataList) {
                        removedBids.addAll(i.bidIds)
                    }

                    dialogInterface.editBids(transaction.key(), position, createPayload, modifyPayload, removedBids, 200.0)
                } else {
                    for (item in adapter.itemsList()) {
                        val bidData = item.data as DmtBidSummaryItemData
                        if (bidData.truckCount != 0 && bidData.pmtRate != 0.0) {
                            createPayload.add(VehicleBidData(bidData.pmtRate, bidData.vehicleCapacity, bidData.truckCount, bidData.vehicleType))
                        }
                    }
                    dialogInterface.createBids(transaction.key(), position, createPayload, 200.0)
                }
                dismiss()
            }
        }

        binding.tvAdd.setOnClickListener {
            adapter.operation(listOf(Pair(DmtBidSummaryItem(
                    DmtBidSummaryItemData("6_TYRE(19FT)",7.5,0.0,0,"open",true,truckTypes)),
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
                }
                adapter.notifyItemChanged(position)
            }
            DELETE_ITEM ->{
                val bidData = item.data as DmtBidSummaryItemData
                adapter.operation(listOf(Pair(DmtBidSummaryItem(bidData), DataRVAdapterOperationType.Remove)))
                adapter.notifyDataSetChanged()

            }
        }
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