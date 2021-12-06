package com.delhivery.axle.ui.bids

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.data.biddetail.BulkBidSummaryItemData
import com.delhivery.axle.data.biddetail.EXPAND_CARD
import com.delhivery.axle.data.bids.TransactionBid
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBidDetailsBinding
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.biddetails.BaseBulkBidSummaryRVAdapterItem
import com.delhivery.axle.ui.biddetails.BulkBidsRVAdapter
import com.delhivery.axle.ui.biddetails.BulkBidsRVAdapterInterface
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Show related bids dialog
 */
class BulkBidDetailsDialog  @Inject constructor(
    context: Context,
    private val transaction: HomeBidsRequestItemData,
    private val transactionBids: List<TransactionBid>? = null, /* transaction bid null for create new bid */
    private val position: Int = 0,
    private val dialogInterface: BulkBidDetailsDialogInterface?=null,
    private val analyticsUtil: AnalyticsUtil,
    private var userPrefs: UserPrefs,
    private var fromPage :String?
) : AlertDialog(context), BulkBidsRVAdapterInterface {

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
                 (adapter as BulkBidsRVAdapter).operation(dialogInterface?.getUserBulkBidsAgainstTrans(transactionBids)!!)
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

    interface BulkBidDetailsDialogInterface {
        fun  getUserBulkBidsAgainstTrans(
            userBids:List<TransactionBid>?
        ):ArrayList<Pair<BaseBulkBidSummaryRVAdapterItem<*>, DataRVAdapterOperationType>>?

    }


}