package com.delhivery.axle.ui.ledger

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemData
import com.delhivery.axle.data.ledger.ConsolidatedProgressItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import java.util.*
import javax.inject.Inject
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove


class ConsolidatedPageViewModel @Inject constructor(): BaseViewModel() {
    var loadsLiveData =
            MutableLiveData<List<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    private var loads = mutableListOf<ConsolidatedLedgerItemData>()

    var months = mutableListOf<String>()
    var monthName = arrayOf<String>("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    fun initiateMonths(){
        var calendar = Calendar.getInstance()
        var monthNum = calendar.get((Calendar.MONTH))
        var year = calendar.get(Calendar.YEAR)
        while (year >= 2020 && monthNum >= 4){
            if(monthNum == -1) {
                monthNum = 11;
                year = year - 1;
            }else{
                var month = monthName[monthNum]
                var yy = year.toString().substring(2)
                var formattedMonth = ""+month+" '"+yy
                Log.d("",""+formattedMonth)
                months.add(formattedMonth);
                monthNum--;
            }
        }

        mutableListOf<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()), Remove))
            for(month in months){
                add(
                        Pair(
                                ConsolidatedPageMonthItem(
                                        ConsolidatedMonthItemData(month, listOf())
                                ), Add
                        )
                )
            }
        }.let { loadsLiveData.postValue(it) }
    }
}