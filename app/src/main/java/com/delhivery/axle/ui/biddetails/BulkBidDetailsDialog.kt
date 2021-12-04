package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.biddetail.EXPAND_CARD
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBidDetailsBinding
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.abs
/**
 * Show related bids dialog
 */
class BulkBidDetailsDialog  @Inject constructor(
    context: Context,
    private val transaction: HomeBidsRequestItemData,
    private val transactionBids: List<TransactionBid>? = null, /* transaction bid null for create new bid */
    private val position: Int = 0,
    private val analyticsUtil: AnalyticsUtil,
    private var userPrefs: UserPrefs,
    private var fromPage :String?
) : AlertDialog(context),BulkBidsRVAdapterInterface {

    /* dialog binding */
    private lateinit var binding: DialogBidDetailsBinding
    private var amount = 0
    private var pmtRate = 0
    private var isChecked = false
    private val adapter: BulkBidsRVAdapter by lazy { BulkBidsRVAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        setCancelable(true)

        /* dialog binding */
        binding = DialogBidDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* set binding params */
        binding.apply {
            request = transaction
            route = transaction.tripRoute()

        }
        binding.rvBidSummary.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@BulkBidDetailsDialog.adapter
            (adapter as BulkBidsRVAdapter).clearItems()
            Log.i("dataBids", transactionBids?.size.toString())
            /**To be changed after integrating API*/
           /* (adapter as BulkBidsRVAdapter).operation(listOf(Pair(BulkBidSummaryItem(BulkBidSummaryItemData("6_Tyre (7.5 MT)",1050.0,4,"open",false)), DataRVAdapterOperationType.Add),
                Pair(BulkBidSummaryItem(BulkBidSummaryItemData("12_Tyre (21 MT)",1060.0,2,"open",false)), DataRVAdapterOperationType.Add),
                Pair(BulkBidSummaryItem(BulkBidSummaryItemData("18_Tyre (32 MT)",1070.0,1,"open",false)), DataRVAdapterOperationType.Add)))*/

            val bulkBidSummaryItemDataList: ArrayList<BulkBidSummaryItemData>? = ArrayList()
            val bulkBidSummaryItemList:ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>? = ArrayList()
            //Test data
            val bids: ArrayList<TransactionBid>?=ArrayList()
            bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"1","","","","","6_TYRE"))
            bids?.add(TransactionBid("","confirmed",false,"","","","",6000.0,4444.0,"2","","","","","6_TYRE"))
            bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"3","","","","","6_TYRE"))
            bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"4","","","","","7_TYRE"))
            bids?.add(TransactionBid("","rejected",false,"","","","",6000.0,5555.0,"5","","","","","7_TYRE"))

            //map same vehicle type with bids
            val map: MutableMap<String, MutableList<TransactionBid>?> = HashMap()
            for (bid in bids!!) {
                val key: String = bid.vehicleType!!
                if (map.containsKey(key)) {
                    val list: MutableList<TransactionBid>? = map[key]
                    list!!.add(bid)
                } else {
                    val list: MutableList<TransactionBid> = ArrayList<TransactionBid>()
                    list.add(bid)
                    map[key] = list
                }
            }
            //get count of status
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
                if(lostStatus>0){
                    lostStat=("$lostStatus Lost:")
                }
                if(confirmedStatus>0){
                    confirmedStat=("$confirmedStatus Confirmed")
                }
                val bulkBidsItem = BulkBidSummaryItemData(key,map[key]!!.get(0).pmtRate!!,truckCount!!,openStat!!,false,confirmedStat,lostStat)
                bulkBidSummaryItemDataList?.add(bulkBidsItem)
                bulkBidSummaryItemList?.add(Pair(BulkBidSummaryItem(bulkBidsItem), DataRVAdapterOperationType.Add))
            }
            (adapter as BulkBidsRVAdapter).operation(bulkBidSummaryItemList!!)
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun handleAction(
        actionId: String,
        position: Int,
        item: BaseBulkBidSummaryRVAdapterItem<*>
    ) {
        when(actionId){
            EXPAND_CARD -> {
                val bidData = item.data as BulkBidSummaryItemData
                bidData.expanded = !bidData.expanded
                adapter.notifyItemChanged(position)
            }
        }
    }

}