package com.delhivery.axle.ui.ledger

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.PayableRepository
import com.delhivery.axle.api.response.ConsolidatedLedgerResponse
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.ConsolidatedMonthItemData
import com.delhivery.axle.data.ledger.ConsolidatedProgressItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import java.util.*
import javax.inject.Inject
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.text.SimpleDateFormat


class ConsolidatedPageViewModel @Inject constructor(
        private val payableRepository: PayableRepository
) : BaseViewModel() {
    var loadsLiveData = MutableLiveData<List<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var ledgerLiveData = MutableLiveData<List<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var months = mutableMapOf<String, Int>()
    private var monthName = arrayOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December")

    private var ledgers = mutableListOf<ConsolidatedLedgerResponse>()

    fun initiateMonths() {
        val calendar = Calendar.getInstance()
        var monthNum = calendar.get((Calendar.MONTH))
        var year = calendar.get(Calendar.YEAR)
        while (year >= 2020 && monthNum >= 4) {
            if (monthNum == -1) {
                monthNum = 11
                year -= 1
            } else {
                val month = monthName[monthNum]
                val yy = year.toString().substring(2)
                val formattedMonth = "$month '$yy"
                months[formattedMonth] = monthNum
                monthNum--
            }
        }

        mutableListOf<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()), Remove))
            for ((key, value) in months) {
                add(
                        Pair(
                                ConsolidatedPageMonthItem(
                                        ConsolidatedMonthItemData(key, value, listOf())
                                ), Add
                        )
                )
            }
        }.let { loadsLiveData.postValue(it) }
    }

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

    private fun fetchLedgerData(jsonObject: JsonObject) {
//        compositeDisposable += payableRepository.fetchConsolidatedLedgerList(jsonObject)
//                .onBackground()
//                .subscribe{
//                    _res,error ->
//                    if(!error){
//                        if(_res.isNotEmpty()){
//                            Log.d("Result from API",""+_res)
//                        }
//                        mutableListOf<Pair<BaseConsolidatedPageRVAdapterItem<*>,DataRVAdapterOperationType>>().apply {
//                            add(Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()),Remove))
//                            for(key in ledgers){
//                                add(
//                                        Pair(
//                                                ConsolidatedPageLedgerItem(
//                                                        ConsolidatedLedgerItemData("some_id", listOf() )
//                                                ),Add
//                                        )
//                                )
//                            }
//                        }.let {ledgerLiveData.postValue(it)}
//                    }
//                }

//        var ledgerItem = mutableMapOf<String,Any>()
//
//        ledgerItem.put("payment_event","loading")
//        ledgerItem.put("invoice_id","AJ938001")
//        ledgerItem.put("payment_type","payment")
//        ledgerItem.put("deductions",JsonObject().add("tds",JsonPrimitive(500)))
//        ledgerItem.put("utr","38924JJJS")
//        ledgerItem.put("payment_success_date","5th November 2020, 7:10 am")
//        ledgerItem.put("lrs", arrayOf("12938719"))
//        ledgerItem.put("trip_id","101064")
//        ledgerItem.put("amount",5000)
//
//
//        var consolidatedItemData = ConsolidatedLedgerItemData("1",)

//        mutableListOf<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
//            add(Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()), Remove))
//            for (key in ledgers) {
//                add(
//                        Pair(
//                                ConsolidatedPageLedgerItem(
//                                        ConsolidatedLedgerItemData("some_id", )
//                                ), Add
//                        )
//                )
//            }
//        }.let { ledgerLiveData.postValue(it) }

    }

    fun initiateLedgerData(data: ConsolidatedMonthItemData) {

        val year = data.month.substring(data.month.length - 2)
        val startDate = generateDateString("startDate", data.monthId, year)
        val endDate = generateDateString("endDate", data.monthId, year)
        val jsonObject = generateLedgerPayload(startDate, endDate)

        fetchLedgerData(jsonObject)

    }
}