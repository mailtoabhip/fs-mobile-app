package com.delhivery.axle.ui.bids

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.data.bids.DELETE_ITEM
import com.delhivery.axle.data.bids.DmtBidSummaryItemData
import com.delhivery.axle.data.bids.EXPAND_CARD
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.ActivityBidsDmtBinding
import com.delhivery.axle.databinding.DialogBidCreateEditBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialogInterface
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
    private var fromPage :String
):AlertDialog(context),DmtBidsAdapterInterface{
    private lateinit var binding: ActivityBidsDmtBinding


    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        binding = ActivityBidsDmtBinding.inflate(layoutInflater)
        setContentView(binding.root)


        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        setCancelable(false)

        /* dialog binding */



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

        rvBids.apply {
             layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@DmtBidsActivity.adapter

            /**To be changed after integrating API*/
            (adapter as DmtBidsRVAdapter).operation(listOf(Pair(DmtBidSummaryItem(DmtBidSummaryItemData("6_Tyre (7.5 MT)",0.0,0,"open",false)), DataRVAdapterOperationType.Add)


            ))
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