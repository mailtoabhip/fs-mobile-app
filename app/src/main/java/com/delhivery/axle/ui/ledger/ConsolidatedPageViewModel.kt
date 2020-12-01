package com.delhivery.axle.ui.ledger

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.PayableRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import java.util.*
import javax.inject.Inject
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.text.SimpleDateFormat


class ConsolidatedPageViewModel @Inject constructor(
        private val payableRepository: PayableRepository
) : BaseViewModel() {
    var loadsLiveData = MutableLiveData<List<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    var selectedMonth = -1
    var selectedYear = -1

    var isLoadedNow : Boolean = true

    @SuppressLint("SimpleDateFormat")
    fun generateDateString(type: String, monthNumber: Int, year: String): String {
        val parser = SimpleDateFormat("yy")
        val formatter = SimpleDateFormat("yyyy")
        val fullYear = formatter.format(parser.parse(year)).toInt()

        val calendar = Calendar.getInstance()
        calendar.set(fullYear, monthNumber, 1)

        val endDay = calendar.getActualMaximum(Calendar.DATE)

        var month = "" + (monthNumber + 1)
        if (month.length == 1) {
            month = "0$month"
        }

        if (type == "startDate") {
            return "$fullYear-$month-01T00:00:00"
        } else if (type == "endDate") {
            return "" + fullYear + "-" + month + "-" + endDay + "T23:59:59"
        }
        return ""
    }

    private fun generateLedgerPayload(startDate: String, endDate: String): JsonObject {
        val root = JsonObject()
        val rangeFilterArray = JsonArray()
        val startObject = JsonObject()
        val endObject = JsonObject()

        startObject.add("column", JsonPrimitive("invoice_date"))
        startObject.add("value", JsonPrimitive(startDate))
        startObject.add("operator", JsonPrimitive("gte"))

        endObject.add("column", JsonPrimitive("invoice_date"))
        endObject.add("value", JsonPrimitive(endDate))
        endObject.add("operator", JsonPrimitive("lte"))

        rangeFilterArray.add(startObject)
        rangeFilterArray.add(endObject)

        root.add("range_filters", rangeFilterArray)
        root.add("limit", JsonPrimitive(100))
        root.add("offset", JsonPrimitive(0))

        return root
    }

//    private fun fetchLedgerData(jsonObject: JsonObject) {
//        compositeDisposable += payableRepository.fetchConsolidatedLedgerList(jsonObject)
//                .onBackground()
//                .subscribe{
//                    _res,error ->
//                    if(!error){
//                        if(_res.isNotEmpty()){
//                            Log.d("Result from API",""+_res)
//                        }
////                        mutableListOf<Pair<BaseConsolidatedPageRVAdapterItem<*>,DataRVAdapterOperationType>>().apply {
////                            add(Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()),Remove))
////                            for(key in ledgers){
////                                add(
////                                        Pair(
////                                                ConsolidatedPageLedgerItem(
////                                                        ConsolidatedLedgerItemData("some_id", listOf() )
////                                                ),Add
////                                        )
////                                )
////                            }
////                        }.let {ledgerLiveData.postValue(it)}
//                    }
//                }
//
//    }

    fun initiateLedgerData(){
        val year = 2020+selectedYear
        val startDate = generateDateString("startDate",selectedMonth,year.toString())
        val endDate = generateDateString("endDate",selectedMonth,year.toString())
        val jsonObject = generateLedgerPayload(startDate,endDate)

        //fetchLedgerData(jsonObject)
    }
}