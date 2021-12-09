package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
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
    private val transactionBid: TransactionBid? = null, /* transaction bid null for create new bid */
    private val dialogInterface: BidDetailsCreateEditDialogInterface,
    private val position: Int = 0,
    private val analyticsUtil: AnalyticsUtil,
    private var userPrefs: UserPrefs,
    private var fromPage :String,
    private var pageTitle :String

):AlertDialog(context), DmtBidsAdapterInterface {

    private lateinit var binding: DialogBulkBidCreateEditBinding
    private val listDmtBidSummaryItemData : ArrayList<DmtBidSummaryItemData> = ArrayList()
    val dmtBidSummaryItemList:ArrayList<Pair<BaseDmtBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>? = ArrayList()

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
                val dmtBidSummaryItemDataList: ArrayList<DmtBidSummaryItemData>? = ArrayList()
                //test data to be replaced with api data
                val bids: ArrayList<TransactionBid>?=ArrayList()
                bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"1","","","","","6_TYRE"))
                bids?.add(TransactionBid("","confirmed",false,"","","","",6000.0,4444.0,"2","","","","","6_TYRE"))
                bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"3","","","","","6_TYRE"))
                bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"4","","","","","7_TYRE"))
                bids?.add(TransactionBid("","rejected",false,"","","","",6000.0,5555.0,"5","","","","","7_TYRE"))
//


                val map1: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
                for (bid in bids!!) {
                    val key: String = bid.status().toString()!!
                    if (map1.containsKey(key)) {
                            val list: MutableList<TransactionBid>? = map1[key]
                            list!!.add(bid)

                    } else {
                        val list: MutableList<TransactionBid> = ArrayList<TransactionBid>()
                        list.add(bid)
                        map1[key] = list
                    }
                }


                for(key in map1.keys){
                    var openStat: String?=null
                    var lostStat: String?=null
                    var confirmedStat: String?=null
                    val truckCount:Int?=map1[key]?.size
                    var openStatus:Int=0
                    var lostStatus:Int=0
                    var confirmedStatus:Int=0
                    for(bid in map1[key]!!){
                        when (bid._status) {
                            "open" -> {
                                openStatus+=1
                            }
                            "confirmed" -> {
                                confirmedStatus+=1
                            }
                            "rejected" -> {
                                lostStatus+=1
                            }
                        }
                    }
                    if(openStatus>0){
                        openStat=("$openStatus Open:")
                    }
                    Log.i("openstat", openStat.toString())
                    val dmtBidsItem = DmtBidSummaryItemData(key,map1[key]!!.get(0).pmtRate!!,truckCount!!,"1 Open",false)
                    dmtBidSummaryItemDataList?.add(dmtBidsItem)
                  //  dmtBidSummaryItemList?.add(Pair(DmtBidSummaryItem(dmtBidsItem), DataRVAdapterOperationType.Add))
                    (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(dmtBidsItem), DataRVAdapterOperationType.Add)))


                }

                val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()

                //       map same vehicle type with bids
                for (bid in bids!!) {
                    val key: String = bid.vehicleType!!
                    if (map.containsKey(key)) {
                        val list: MutableList<TransactionBid>? = map[key]
                        if(bid._status != "confirmed") {
                            list!!.add(bid)
                        }
                    } else {
                        val list: MutableList<TransactionBid> = ArrayList<TransactionBid>()
                        list.add(bid)
                        map[key] = list
                    }
                }
              //  get count of status
                for(key in map.keys){
                    var openStat: String?=null
                    var lostStat: String?=null
                    var confirmedStat: String?=null
                    val truckCount:Int?=map[key]?.size
                    var openStatus:Int=0
                    var lostStatus:Int=0
                    var confirmedStatus:Int=0
                    for(bid in map[key]!!){
                        if(bid._status == "open"){
                            openStatus+=1
                        }else if(bid._status == "confirmed"){
                            confirmedStatus+=1
                        }else if(bid._status == "rejected"){
                            lostStatus+=1
                        }
                    }
                    if(openStatus>0){
                        openStat=("$openStatus Open:")
                    }

                    val dmtBidsItem = DmtBidSummaryItemData(key,map[key]!!.get(0).pmtRate!!,truckCount!!,openStat!!,false)
                    dmtBidSummaryItemDataList?.add(dmtBidsItem)
                    (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(dmtBidsItem), DataRVAdapterOperationType.Add)))
                }
            }
            adapter.notifyDataSetChanged()

        }else{
            binding.rvBids.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@BulkBidDetailsCreateEditDialog.adapter

                /**To be changed after integrating API*/
                (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData("",0.0,0,"open",true)), DataRVAdapterOperationType.Add)
                ))

            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
           // Log.i("dataClicked",adapter.itemsList().get(0).data.toString())
            for(item in adapter.itemsList()){
                val bidData = item.data as DmtBidSummaryItemData

            }
        }

        binding.tvAdd.setOnClickListener {
            adapter.operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData("",0.0,0,"open",true)), DataRVAdapterOperationType.Add)))
            adapter.notifyDataSetChanged()
        }
    }

    private val adapter: DmtBidsRVAdapter by lazy { DmtBidsRVAdapter(this) }

    override fun handleAction(actionId: String, position: Int, item: BaseDmtBidSummaryRVAdapterItem<*>) {
        when(actionId){
            EXPAND_CARD -> {
                val bidData = item.data as DmtBidSummaryItemData
                bidData.expanded = !bidData.expanded
                adapter.notifyItemChanged(position)
            }
            DELETE_ITEM ->{
                val bidData = item.data as DmtBidSummaryItemData
                    adapter.operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData(bidData.vehicleType,bidData.pmtRate,bidData.truckCount,bidData.status,bidData.expanded)), DataRVAdapterOperationType.Remove)))
                    adapter.notifyDataSetChanged()

            }
        }
    }

}