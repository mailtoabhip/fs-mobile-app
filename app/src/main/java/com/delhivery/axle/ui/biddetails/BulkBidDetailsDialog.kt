package com.delhivery.axle.ui.biddetails

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private val transactionBid: TransactionBid? = null, /* transaction bid null for create new bid */
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

            /**To be changed after integrating API*/
            (adapter as BulkBidsRVAdapter).operation(listOf(Pair(BulkBidSummaryItem(BulkBidSummaryItemData("6_Tyre (7.5 MT)",1050.0,4,"open",false)), DataRVAdapterOperationType.Add),

                Pair(BulkBidSummaryItem(BulkBidSummaryItemData("12_Tyre (21 MT)",1060.0,2,"open",false)), DataRVAdapterOperationType.Add),
                Pair(BulkBidSummaryItem(BulkBidSummaryItemData("18_Tyre (32 MT)",1070.0,1,"open",false)), DataRVAdapterOperationType.Add)))
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