package com.delhivery.axle.ui.ledger

import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemAction
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemAction
import com.delhivery.axle.databinding.*
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
        val deductionText = binding.textDeductions
        val deductionTextValue = binding.textDeductionsValue

        for(i in item.data.deductions){
            if(deductionText.parent != null){
                (deductionText.parent as ViewGroup).removeAllViews()
            }
            if(deductionTextValue.parent != null){
                (deductionTextValue.parent as ViewGroup).removeAllViews()
            }
            deductionText.text = item.data.getDeductionsTitle(index)
            deductionTextValue.text = item.data.getDeductionsAmount(index)
            deductions.addView(deductionText)
            deductions.addView(deductionTextValue)
            index += 1
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
