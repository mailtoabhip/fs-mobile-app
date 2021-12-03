package com.delhivery.axle.ui.bids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.bids.DELETE_ITEM
import com.delhivery.axle.data.bids.DmtBidSummaryItemData
import com.delhivery.axle.data.bids.EXPAND_CARD
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.ActivityBidsDmtBinding
import com.delhivery.axle.databinding.DialogBidCreateEditBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.biddetails.BaseBulkBidSummaryRVAdapterItem
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
import com.delhivery.axle.ui.biddetails.BulkBidSummaryItem
import com.delhivery.axle.ui.biddetails.BulkBidsRVAdapter
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.activity_bids_dmt.*
import javax.inject.Inject

class DmtBidsActivity @Inject constructor(
    context: Context,
    private val transaction: HomeBidsRequestItemData,
    private val transactionBid: TransactionBid? = null, /* transaction bid null for create new bid */
    private val dialogInterface: BidDetailsCreateEditDialogInterface,
    private val position: Int = 0,
    private val analyticsUtil: AnalyticsUtil,
    private var userPrefs: UserPrefs,
    private var fromPage :String,
    private var pageTitle :String

):AlertDialog(context),DmtBidsAdapterInterface{
    private lateinit var binding: ActivityBidsDmtBinding


    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)

        /* dialog binding */
        binding = ActivityBidsDmtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvOrigin.setText(transaction.origin);
        binding.tvDestination.setText(transaction.destination)
        binding.tvLoadAmount.setText(""+transaction.allocatedVolume+transaction.unAllocatedVolume)

            binding.tvTitle.setText(pageTitle)



        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        setCancelable(false)


        if(pageTitle=="EDIT BIDS"){

            rvBids.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@DmtBidsActivity.adapter
                (adapter as DmtBidsRVAdapter).clearItems()

                val dmtBidSummaryItemDataList: ArrayList<DmtBidSummaryItemData>? = ArrayList()
                val dmtBidSummaryItemList:ArrayList<Pair<BaseDmtBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>? = ArrayList()
                //test data to be replaced with api data
                val bids: ArrayList<TransactionBid>?=ArrayList()
                bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"1","","","","","6_TYRE"))
                bids?.add(TransactionBid("","confirmed",false,"","","","",6000.0,4444.0,"2","","","","","6_TYRE"))
                bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"3","","","","","6_TYRE"))
                bids?.add(TransactionBid("","open",false,"","","","",6000.0,9000.0,"4","","","","","7_TYRE"))
                bids?.add(TransactionBid("","rejected",false,"","","","",6000.0,5555.0,"5","","","","","7_TYRE"))

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
                    val dmtBidsItem = DmtBidSummaryItemData(key,map1[key]!!.get(0).pmtRate!!,truckCount!!,openStat!!,false)
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
                  //  dmtBidSummaryItemList?.add(Pair(DmtBidSummaryItem(dmtBidsItem), DataRVAdapterOperationType.Add))
                    (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(dmtBidsItem), DataRVAdapterOperationType.Add)))

                }


//                (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData("6_Tyre (7.5 MT)",0.0,0,"open",true)), DataRVAdapterOperationType.Add)))

            }
            adapter.notifyDataSetChanged()

        }else{
            rvBids.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@DmtBidsActivity.adapter

                /**To be changed after integrating API*/
                (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData("6_Tyre (7.5 MT)",0.0,0,"open",false)), DataRVAdapterOperationType.Add)


                ))
            }

        }






        binding.btnCancel.setOnClickListener { dismiss() }

        tvAdd.setOnClickListener {
            rvBids.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter = this@DmtBidsActivity.adapter
                (

                (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData("6_Tyre (7.5 MT)",0.0,0,"open",true)), DataRVAdapterOperationType.Add)

                )))
            }
            adapter.notifyDataSetChanged()
        }


      //  setContentView(R.layout.activity_bids_dmt)
    }
    private val adapter: DmtBidsRVAdapter by lazy { DmtBidsRVAdapter(this) }

//    override fun getViewModelClass() = DmtBidsViewModel::class.java
//
//    override fun layoutId() = R.layout.activity_bids_dmt
//    override fun requireConnection() = true




    override fun handleAction(actionId: String, position: Int, item: BaseDmtBidSummaryRVAdapterItem<*>) {
        when(actionId){
            EXPAND_CARD -> {
                val bidData = item.data as DmtBidSummaryItemData
                bidData.expanded = !bidData.expanded
                adapter.notifyItemChanged(position)
            }
            DELETE_ITEM ->{
                val bidData = item.data as DmtBidSummaryItemData

                rvBids.apply {
                    layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                    adapter = this@DmtBidsActivity.adapter
                    (

                    (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData(bidData.vehicleType,bidData.pmtRate,bidData.truckCount,bidData.status,bidData.expanded)), DataRVAdapterOperationType.Remove))))
                }
                     System.out.println("truckCountAs"+ bidData.truckCount)


            }
        }
    }








    fun dmtBidsIntent(
        context: Context
    ) = Intent(context, DmtBidsActivity::class.java).apply {

    }

}