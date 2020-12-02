package com.delhivery.axle.ui.ledger

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemAction
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemAction
import com.delhivery.axle.databinding.LayoutDeductionViewBinding
import com.delhivery.axle.databinding.ViewConsolidatedPageLedgerItemBinding
import com.delhivery.axle.databinding.ViewConsolidatedPageMonthItemBinding
import com.delhivery.axle.databinding.ViewHomeSearchItemBinding
import com.delhivery.axle.databinding.ViewProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.databinding.ViewWarningItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

abstract class BaseConsolidatedPageRVAdapterViewHolder<out B : ViewDataBinding,
        IT : BaseConsolidatedPageRVAdapterItem<*>>(binding: B) : BaseViewHolder<B>(binding){
    abstract fun bind(
            item: IT,
            _interface: ConsolidatedPageRVAdapterInterface
    )

    /**
     * Add Click Listener for Action
     */
    protected fun View.clickToAction(
            actionId: String,
            item: IT,
            position: Int,
            _interface: ConsolidatedPageRVAdapterInterface
    ) = setOnClickListener{ action(actionId, item, position, _interface)}

    /**
     * Post action to UI
     */
    protected fun View.action(
            actionId: String,
            item: IT,
            position: Int,
            _interface: ConsolidatedPageRVAdapterInterface
    ) = post {_interface.handleAction(actionId, position, item)}
}

/**
 * Consolidated Page Month Item View Holder
 * */
class ConsolidatedPageMonthItemVH(binding: ViewConsolidatedPageMonthItemBinding):
        BaseConsolidatedPageRVAdapterViewHolder<ViewConsolidatedPageMonthItemBinding, ConsolidatedPageMonthItem>(binding){
    override fun bind(item: ConsolidatedPageMonthItem, _interface: ConsolidatedPageRVAdapterInterface) {
        binding.itemRequest = item.data
        binding.root.clickToAction(ConsolidatedMonthItemAction, item, adapterPosition, _interface)
    }
}

/**
 * Consolidated Page Ledger Item View Holder
 * */
class ConsolidatedPageLedgerItemVH(binding: ViewConsolidatedPageLedgerItemBinding):
        BaseConsolidatedPageRVAdapterViewHolder<ViewConsolidatedPageLedgerItemBinding, ConsolidatedPageLedgerItem>(binding){
    override fun bind(item: ConsolidatedPageLedgerItem, _interface: ConsolidatedPageRVAdapterInterface) {
        binding.ledger = item.data
        val deductions = binding.deductionsContainer
        var index = 0

        if(item.data.expanded) {
            deductions.visibility =View.VISIBLE
            deductions.removeAllViews()
            for (i in item.data.deductions) {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val deductionBinding = LayoutDeductionViewBinding.inflate(inflater)
                deductionBinding.textDeductions.text = item.data.getDeductionsTitle(index)
                deductionBinding.textDeductionsValue.text = item.data.getDeductionsAmount(index)
                if(item.data.deductions[index]["deduction_type"] == "dn_deduction"){
                    deductionBinding.root.setOnClickListener {
                        // open activity
                    }
                }
                deductions.addView(deductionBinding.root)
                index +=1
            }
        }else{
            deductions.visibility =View.GONE
            deductions.removeAllViews()
        }


        binding.root.clickToAction(ConsolidatedLedgerItemAction, item, adapterPosition, _interface)
    }
}

/**
 * Consolidated Page Warning Item View Holder
 * */
internal class ConsolidatedPageWarningItemVH(binding: ViewWarningItemBinding):
BaseConsolidatedPageRVAdapterViewHolder<ViewWarningItemBinding, ConsolidatedPageWarningItem>(binding){
    override fun bind(item: ConsolidatedPageWarningItem, _interface: ConsolidatedPageRVAdapterInterface) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
    }
}

/**
 * Consolidated Page Timeout Item View Holder
 * */
internal class ConsolidatedPageTimeOutItemVH(binding: ViewTimeOutItemBinding) :
        BaseConsolidatedPageRVAdapterViewHolder<ViewTimeOutItemBinding, ConsolidatedPageTimeoutItem>(binding) {
    override fun bind(item: ConsolidatedPageTimeoutItem, _interface: ConsolidatedPageRVAdapterInterface
    ) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
    }
}

/**
 * Consolidated Page Timeout Item View Holder
 * */
internal class ConsolidatedPageSearchItemVH(binding: ViewHomeSearchItemBinding) :
        BaseConsolidatedPageRVAdapterViewHolder<ViewHomeSearchItemBinding, ConsolidatedPageSearchItem>(binding) {
    override fun bind(item: ConsolidatedPageSearchItem, _interface: ConsolidatedPageRVAdapterInterface) {
        binding.editQuery.hint = "Search by UTR Number"
        binding.editQuery.clickToAction(HomeTripsSearchAction_Search, item, adapterPosition, _interface)
    }
}

/**
 * Consolidated Page Progress Item View Holder
 * */
class ConsolidatedPageProgressItemVH(binding: ViewProgressItemBinding) :
        BaseConsolidatedPageRVAdapterViewHolder<ViewProgressItemBinding, ConsolidatedPageProgressItem>(binding) {
    override fun bind(item: ConsolidatedPageProgressItem, _interface: ConsolidatedPageRVAdapterInterface
    ) {

    }
}
