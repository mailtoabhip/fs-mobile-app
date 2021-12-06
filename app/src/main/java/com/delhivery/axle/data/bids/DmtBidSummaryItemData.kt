package com.delhivery.axle.data.bids

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingListener
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils

data class DmtBidSummaryItemData (
    var vehicleType: String,
    var pmtRate: Double,
    var truckCount: Int,
    var status :String,
    var expanded: Boolean = false,
    var deleted: Boolean = false

    ): BaseKeyTypeModel<String>() {

        override fun key() = vehicleType

        fun visibility() = if(expanded)
            View.VISIBLE
        else
            View.GONE

        @DrawableRes
        fun toggleButton() = DrawableProviderUtils.expandedRes(expanded)

        fun pmtRate() = pmtRate.toString()

        fun truckCount() =truckCount.toString()

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
