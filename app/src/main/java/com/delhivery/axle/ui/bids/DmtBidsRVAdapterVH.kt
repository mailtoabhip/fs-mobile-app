package com.delhivery.axle.ui.bids

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.bids.DELETE_ITEM
import com.delhivery.axle.data.bids.EXPAND_CARD
import com.delhivery.axle.databinding.ViewBidCreateEditItemBinding
import com.delhivery.axle.databinding.ViewProgressItemBinding
import com.delhivery.axle.databinding.ViewTimeOutItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.biddetails.DmtBidsAdapterInterface
import com.delhivery.axle.ui.biddetails.TruckSpinnerAdapter
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.text.SimpleDateFormat
import java.util.*

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


        val items= context.resources.getStringArray(R.array.reportingTimeItems)
        val spinnerAdapter= object : ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, items) {

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(position, convertView, parent) as TextView
                if(position == 0) {
                    view.setTextColor(Color.GRAY)
                }
                return view
            }

        }

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editTime.adapter = spinnerAdapter

      if(item.data.expectedArrivalTimePickupRemark.isNotNullOrEmpty()){
        val text = item.data.expectedArrivalTimePickupRemark
        if(text.equals("Immediately")) {
          binding.editTime.setSelection(1)
        }else if(text.equals("Within 4 hours")) {
          binding.editTime.setSelection(2)
        }else if(text.equals("Between 4-12 hours")) {
          binding.editTime.setSelection(3)
        }else if(text.equals("Tomorrow")) {
          binding.editTime.setSelection(4)
        }
      }

      binding.editTime.isEnabled = item.data.isEnabled()

      binding.editTime.apply {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>) = Unit
                override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                ) {

                    val option = parent.getItemAtPosition(position) as String
                    if(option == items[0]){
                        (view as TextView).setTextColor(Color.GRAY)
                    }
                    val text: String = binding.editTime.selectedItem.toString()
                    if(text.equals("Select Time")){
                        binding.item?.expectedArrivalTimePickup = ""
                        binding.item?.expectedArrivalTimePickupRemark = ""
                     }else{
                        val myDate = Date()
                        val calendar: Calendar = Calendar.getInstance()
                        calendar.setTimeZone(TimeZone.getTimeZone("UTC"))
                        calendar.setTime(myDate)
                        if(text.equals("Immediately")) {
                            calendar.add(Calendar.MINUTE,30);
                        }else if(text.equals("Within 4 hours")) {
                            calendar.add(Calendar.HOUR, 4);
                        }else if(text.equals("Between 4-12 hours")) {
                            calendar.add(Calendar.HOUR, 12);
                        }else if(text.equals("Tomorrow")) {
                            calendar.add(Calendar.HOUR, 24);
                        }
                        val time: Date = calendar.getTime()
                        val outputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
                        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"))
                        val dateAsString: String = outputFmt.format(time)
                        binding.item?.expectedArrivalTimePickup = dateAsString
                        binding.item?.expectedArrivalTimePickupRemark = option
                    }

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
        binding.expandButton.clickToAction(EXPAND_CARD,item, bindingAdapterPosition, _interface)
        binding.deleteItem.clickToAction(DELETE_ITEM,item,bindingAdapterPosition,_interface)

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
        binding.btnAction.clickToAction(item.data.actionId, item, bindingAdapterPosition, _interface)
    }

}