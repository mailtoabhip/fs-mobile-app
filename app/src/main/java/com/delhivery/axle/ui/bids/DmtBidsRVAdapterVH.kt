package com.delhivery.axle.ui.bids

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.bids.DELETE_ITEM
import com.delhivery.axle.data.bids.DmtBidSummaryItemData
import com.delhivery.axle.data.bids.EXPAND_CARD
import com.delhivery.axle.data.ledger.LedgerSpinnerOptions
import com.delhivery.axle.databinding.ViewBidCreateEditItemBinding
import com.delhivery.axle.databinding.ViewProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.biddetails.DmtBidsAdapterInterface
import com.delhivery.axle.ui.biddetails.TruckSpinnerAdapter
import com.delhivery.axle.ui.ledger.LedgerSpinnerAdapter
import kotlin.math.abs

abstract class BaseDmtBidsRVAdapterVH<out B: ViewDataBinding,
        IT : BaseDmtBidSummaryRVAdapterItem<*>>(binding: B) : BaseViewHolder<B>(binding) {

    abstract fun bind(
        item: IT,
        _interface: DmtBidsAdapterInterface
    )
    /**
     * Add Click Listener for Action
     */
    protected fun View.clickToAction(
        actionId: String,
        item: IT,
        position: Int,
        _interface: DmtBidsAdapterInterface
    ) = setOnClickListener{ action(actionId, item, position, _interface)}

    /**
     * Post action to UI
     */
    protected fun View.action(
        actionId: String,
        item: IT,
        position: Int,
        _interface: DmtBidsAdapterInterface
    ) = post {_interface.handleAction(actionId, position, item) }

}

class DmtBidsSummaryItemVH(binding: ViewBidCreateEditItemBinding) :
    BaseDmtBidsRVAdapterVH<ViewBidCreateEditItemBinding, DmtBidSummaryItem>(binding) {
    override fun bind(
        item: DmtBidSummaryItem,
        _interface: DmtBidsAdapterInterface
    ) {
        binding.item = item.data
       val truckSpinnerAdapter: TruckSpinnerAdapter by lazy { TruckSpinnerAdapter() }
        binding.spVehicleType.apply {
            adapter = truckSpinnerAdapter
            truckSpinnerAdapter.setItems(item.data.truckTypes)
            var index=0
            for( i in item.data.truckTypes){
                if (i.truckUuid == item.data.vehicleType){
                    break
                }
                index+=1
            }
            setSelection(index)
            if (!item.data.isVehicleEnabled()){
                binding.spVehicleType.isEnabled = false
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>) = Unit
                override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                ) {
                    val option = parent.getItemAtPosition(position) as TruckResponseArray
                    binding.item?.vehicleCapacity = option.defaultMG!!.toDouble()
                    binding.item?.vehicleType = option.truckUuid!!
                }
                }
        }
        binding.editTrucks.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?){
                if(s==null || s.toString() ==""){
                   // binding.item!!.truckCount = 0
                    val diff = 0 - binding.item!!.truckCount
                    binding.item!!.truckCount = 0
                    _interface.itemCapacity(diff * item.data.vehicleCapacity)
                }
            }
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null && s.length>0) {
                    val diff = Integer.parseInt(s.toString())- binding.item!!.truckCount
                  binding.item!!.truckCount = Integer.parseInt(s.toString())
                    _interface.itemCapacity(diff * item.data.vehicleCapacity)
                }
                else if (s == null || s == ""){
                    val diff = 0 - binding.item!!.truckCount
                    binding.item!!.truckCount = 0
                    _interface.itemCapacity(diff * item.data.vehicleCapacity)
                }
            }
        })
        binding.editPmtAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s==null || s.toString() ==""){
                    binding.item!!.pmtRate = 0.0
                }
            }
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null && s.length>0) {
                    binding.item!!.pmtRate = (s.toString().toDouble())
                }
                else if (s == null || s == ""){
                    binding.item!!.pmtRate = 0.0
                }
            }
        })
        binding.expandButton.clickToAction(EXPAND_CARD,item, adapterPosition, _interface)
        binding.deleteItem.clickToAction(DELETE_ITEM,item,adapterPosition,_interface)

    }
}

class DmtBidSummaryProgressItemVH(binding: ViewProgressItemBinding) :
    BaseDmtBidsRVAdapterVH<ViewProgressItemBinding,DmtBidSummaryProgressItem>(binding) {
    override fun bind(item: DmtBidSummaryProgressItem, _interface: DmtBidsAdapterInterface) {

    }
}

/**
 * Dmt Bid summary timeout item view holder
 * */
internal class DmtBidSummaryTimeOutItemVH(binding: ViewTimeOutItemBinding) :
    BaseDmtBidsRVAdapterVH<ViewTimeOutItemBinding, BulkBidTimeoutItem>(binding) {
    override fun bind(item: BulkBidTimeoutItem, _interface: DmtBidsAdapterInterface) {
        binding.title = item.data.title
        binding.subTitle = item.data.subtitle
        binding.actionLabel = item.data.actionLabel
        binding.btnAction.clickToAction(item.data.actionId, item, adapterPosition, _interface)
    }

}