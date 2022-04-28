package com.delhivery.axle.data.bids

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingListener
import com.delhivery.axle.R
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils

data class DmtBidSummaryItemData (
    var vehicleType: String,
    var vehicleCapacity:Double,
    var pmtRate: Double =0.0,
    var truckCount: Int =0,
    var status :String,
    var expanded: Boolean = false,
    var truckTypes : List<TruckResponseArray> = mutableListOf(),
    var bidIds: List<String> = mutableListOf(),
    var added: Boolean = false,
    var deleted: Boolean = false,
    var expectedArrivalTimePickup:String = "",
    var expectedArrivalTimePickupRemark:String = ""
    ): BaseKeyTypeModel<String>() {

        override fun key() = vehicleType

        fun visibility() = if(expanded)
            View.VISIBLE
        else
            View.GONE

        @DrawableRes
        fun toggleButton() = DrawableProviderUtils.expandedRes(expanded)

        fun pmtRate() = if(pmtRate!=0.0)pmtRate.toString()
        else ""

        fun truckCount() = if(truckCount!=0) truckCount.toString()
           else ""

        fun repTime() = if(truckCount!=0) truckCount.toString()
        else ""

        fun isEnabled()= status != "confirmed"

        fun isVehicleEnabled() = status!="confirmed" && !added

        fun deleteVisibility() = if(status == "confirmed")
            View.GONE
        else
            View.VISIBLE

        fun confirmVisibility() = if(status != "confirmed")
            View.GONE
        else
            View.VISIBLE


    @DrawableRes
    fun background() = if(status == "confirmed")
        R.drawable.background_border_disable
    else
        R.drawable.background_rectangle_border


    @BindingAdapter("android:text")
    fun setTextChangeListener(editText: EditText, listener: InverseBindingListener) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = listener.onChange()

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
    }

    }
    const val EXPAND_CARD = "expand"

    const val DELETE_ITEM = "delete"
