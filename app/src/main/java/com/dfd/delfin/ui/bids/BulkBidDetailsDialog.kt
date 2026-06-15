package com.dfd.delfin.ui.bids

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.dfd.delfin.data.biddetail.BulkBidSummaryItemData
import com.dfd.delfin.data.biddetail.EXPAND_CARD
import com.dfd.delfin.data.bids.TransactionBid
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.databinding.DialogBidDetailsBinding
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.biddetails.BaseBulkBidSummaryRVAdapterItem
import com.dfd.delfin.ui.biddetails.BulkBidsRVAdapter
import com.dfd.delfin.ui.biddetails.BulkBidsRVAdapterInterface
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Show related bids dialog
 */
class BulkBidDetailsDialog  @Inject constructor(
    context: Context,
    private val transaction: HomeBidsRequestItemData,
    private val transactionBids: List<TransactionBid>? = null, /* transaction bid null for create new bid */
    private val dialogInterface: BulkBidDetailsDialogInterface?=null,
    private val analyticsUtil: AnalyticsUtil,
    private var userPrefs: UserPrefs
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