package com.delhivery.axle.data.ledger

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.annotation.DrawableRes
import com.delhivery.axle.api.response.ConsolidatedLedgerResponse
import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.utils.DrawableProviderUtils
import com.google.gson.annotations.SerializedName

class ConsolidatedLedgerItemData(
        var id: String,
        var data : List<ConsolidatedLedgerResponse>,
        var expanded: Boolean = false

) : BaseKeyTypeModel<String>(){

    override fun key() = id

    /**
     * @return expanded resource basis [expanded]
     */
    @DrawableRes
    fun expandedResource() = DrawableProviderUtils.expandedResLedger(expanded)

}
const val ConsolidatedLedgerItemAction = "toggle_ledger"