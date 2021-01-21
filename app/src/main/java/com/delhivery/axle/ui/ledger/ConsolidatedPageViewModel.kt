package com.delhivery.axle.ui.ledger

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.PayableRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimitConsolidatedAPI
import com.delhivery.axle.api.response.DownloadLedgerResponse
import com.delhivery.axle.data.home.loads.HomeLoadsTimeOutItemData
import com.delhivery.axle.data.home.loads.HomeLoadsWarningItemData
import com.delhivery.axle.data.ledger.ConsolidatedLedgerItemData
import com.delhivery.axle.data.ledger.ConsolidatedProgressItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.dialogs.DownloadLedgerInterface

import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.*
import javax.inject.Inject
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.text.SimpleDateFormat


class ConsolidatedPageViewModel @Inject constructor(
        private val payableRepository: PayableRepository,
        private val userRepository: UserRepository,
        val userPrefs: UserPrefs
) : BaseViewModel(), DownloadLedgerInterface {
    var ledgerLiveData = MutableLiveData<List<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    var dataLoadingLiveData = MutableLiveData<Boolean>()

    var emailLoadingLiveData = MutableLiveData<String>()

    var downloadLoadingLiveData = MutableLiveData<DownloadLedgerResponse>()

    var downloadPressed = MutableLiveData<Boolean>()

    var currentStartMonth = -1
    var currentStartYear = -1

    var currentEndMonth = -1
    var currentEndYear = -1

    var ledgerStartDate = -1
    var ledgerEndDate = -1

    var hasMoreData: Boolean = false
    var offset:Int? = 0
    var total = 0


    @SuppressLint("SimpleDateFormat")
    fun generateDateString(type: String, monthNumber: Int, year: String, recent: Boolean=false): String {
        val parser = SimpleDateFormat("yy")
        val formatter = SimpleDateFormat("yyyy")
        var fullYear = formatter.format(parser.parse(year)).toInt()

        val calendar = Calendar.getInstance()
        calendar.set(fullYear, monthNumber, 1)

        var endDay = calendar.getActualMaximum(Calendar.DATE).toString()
        var startDay = "01"

        var month = "" + (monthNumber + 1)
        if (month.length == 1) {
            month = "0$month"
        }

        var finalDate = ""
        if (type == "startDate") {
            if(recent){
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                val today = format.format(Date())
                val todayFormatted: Date = format.parse(today)
                val fifteenDaysFormatted = Date(todayFormatted.getTime() - 1296000000L) // 15 * 24 * 60 * 60 * 1000
                val fifteenDays = format.format(fifteenDaysFormatted.time)

                startDay = fifteenDays.substring(8,10)
                month = fifteenDays.substring(5,7)
                fullYear = fifteenDays.substring(0,4).toInt()
            }
            if(ledgerStartDate != -1){
                startDay = ledgerStartDate.toString()
                if(startDay.length == 1){
                    startDay = "0$startDay"
                }
                ledgerStartDate = -1
            }
            finalDate = "" + fullYear + "-" + month + "-" + startDay + "T00:00:00"
        } else if (type == "endDate") {
            if(ledgerEndDate != -1){
                endDay = ledgerEndDate.toString()
                if(endDay.length == 1){
                    endDay = "0$endDay"
                }
                ledgerEndDate = -1
            }
            finalDate = "" + fullYear + "-" + month + "-" + endDay + "T23:59:59"
        }
        return finalDate
    }

    private fun generatePayloadConsolidatedLedger(startDate: String, endDate: String): JsonObject {
        val root = JsonObject()
        val rangeFilterArray = JsonArray()
        val startObject = JsonObject()
        val endObject = JsonObject()

        startObject.add("column", JsonPrimitive("pmt_success_dt"))
        startObject.add("value", JsonPrimitive(startDate))
        startObject.add("operator", JsonPrimitive("gte"))

        endObject.add("column", JsonPrimitive("pmt_success_dt"))
        endObject.add("value", JsonPrimitive(endDate))
        endObject.add("operator", JsonPrimitive("lte"))

        rangeFilterArray.add(startObject)
        rangeFilterArray.add(endObject)

        root.add("payee_id", JsonPrimitive(userRepository.userId()))
        root.add("range_filters", rangeFilterArray)
        root.add("limit", JsonPrimitive(UserSearchLimitConsolidatedAPI))
        root.add("offset", JsonPrimitive(offset))
        return root
    }

    private fun generatePayloadDownloadEmailLedger(startDate: String, endDate: String, email: String = ""): JsonObject {
        val root = JsonObject()
        val rangeFilterArray = JsonArray()
        val startObject = JsonObject()
        val endObject = JsonObject()

        startObject.add("column", JsonPrimitive("status_update_info.truck_loaded.at"))
        startObject.add("value", JsonPrimitive(startDate))
        startObject.add("operator", JsonPrimitive("gte"))

        endObject.add("column", JsonPrimitive("status_update_info.truck_loaded.at"))
        endObject.add("value", JsonPrimitive(endDate))
        endObject.add("operator", JsonPrimitive("lte"))

        rangeFilterArray.add(startObject)
        rangeFilterArray.add(endObject)

        root.add("payee_id", JsonPrimitive(userRepository.userId()))
        root.add("range_filters", rangeFilterArray)

        if(email != "") {
            root.add("email_id", JsonPrimitive(email))
        }

        return root
    }

    private fun downloadVendorLedger(jsonObject: JsonObject){
        compositeDisposable += payableRepository.downloadVendorLedger(jsonObject)
                .onBackground()
                .subscribe{
                    _res,error ->
                    if(!error){
                        Log.d("DOwnload response",""+_res)
                        downloadLoadingLiveData.postValue(_res)
                    }
                }
    }

    private fun emailVendorLedger(jsonObject: JsonObject){
        compositeDisposable += payableRepository.emailVendorLedger(jsonObject)
                .onBackground()
                .subscribe{
                    _res,error ->
                    if(!error){
                        Log.d("Email response",""+_res)
                        emailLoadingLiveData.postValue(_res.message)
                    }
                }

    }
    private fun fetchLedgerData(jsonObject: JsonObject, paginate: Boolean) {
        Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()), DataRVAdapterOperationType.AddUpdate).let { ledgerLiveData.postValue(listOf(it)) }
        compositeDisposable += payableRepository.fetchConsolidatedLedgerList(jsonObject)
                .onBackground()
                .subscribe{
                    _res,error ->
                    if(!error){
                        offset = offset?.plus(20)
                        hasMoreData = _res.hasNext!!
                        mutableListOf<Pair<BaseConsolidatedPageRVAdapterItem<*>,DataRVAdapterOperationType>>().apply {
                            add(Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()), DataRVAdapterOperationType.Remove))
                            if (!paginate && _res.count == 0) {
                                add(Pair(ConsolidatedWarningItem_NoLedger, DataRVAdapterOperationType.AddUpdate))
                            }else{
                                total = _res.count
                                for(ledger in _res.ledgers){
                                    if(ledger.amount!=0.0){
                                        add(Pair(ConsolidatedPageLedgerItem(ConsolidatedLedgerItemData(ledger.paymentEvent,ledger.amount,ledger.uuid,ledger.paymentType,ledger.tripId,ledger.lrs,ledger.paymentSuccessDate,ledger.utrNumber,ledger.month,ledger.deductions,ledger.invoiceId, userPrefs.userType)),DataRVAdapterOperationType.Add))
                                    }
                                }
                            }
                        }.let {ledgerLiveData.postValue(it)}

                    }
                    else {
                        mutableListOf<Pair<BaseConsolidatedPageRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                            /* remove progress item */
                            add(Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()), DataRVAdapterOperationType.Remove))
                            /* add api time out item */
                            add(Pair(ConsolidatedWarningItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
                        }.let { ledgerLiveData.postValue(it) }
                    }
                    dataLoadingLiveData.postValue(false)
                }

    }

    fun initiateLedgerData(startMonth:Int, startYear:Int,endMonth:Int, endYear:Int, paginate: Boolean = false, recent:Boolean = false){
        val startDate = generateDateString("startDate",startMonth,startYear.toString(), recent)
        val endDate = generateDateString("endDate",endMonth,endYear.toString())
        val jsonObject = generatePayloadConsolidatedLedger(startDate,endDate)

        if (!paginate) {
            offset = 0
        } else if (paginate && (total == offset)) {
            return
        }

        /* add progress if not paginating */
        if (paginate) {
            Pair(ConsolidatedPageProgressItem(ConsolidatedProgressItemData()), DataRVAdapterOperationType.AddUpdate).let { ledgerLiveData.postValue(listOf(it)) }
        }
        dataLoadingLiveData.postValue(true)

        fetchLedgerData(jsonObject, paginate)
    }

    fun initiateDownloadAndEmail(type: String, startMonth:Int, startYear:Int,endMonth:Int, endYear:Int, email: String = ""){
        val startRange = generateDateString("startDate",startMonth,startYear.toString())
        val endRange = generateDateString("endDate",endMonth,endYear.toString())

        if (type == "email" && email != ""){
            val jsonObject = generatePayloadDownloadEmailLedger(startRange,endRange,email)
            emailVendorLedger(jsonObject)
        }else if(type == "download"){
            val jsonObject = generatePayloadDownloadEmailLedger(startRange,endRange)
            downloadVendorLedger(jsonObject)
        }
    }

    override fun onEmailClick(startDate: Int, startMonth: Int, startYear: Int, endDate: Int, endMonth: Int, endYear: Int, email: String) {
        Log.d("Email->","$startDate-$startMonth-$startYear---->$email")
        ledgerStartDate = startDate
        ledgerEndDate = endDate
        initiateDownloadAndEmail("email", startMonth,startYear, endMonth, endYear,email)
    }

    override fun onDownloadClick(startDate: Int, startMonth: Int, startYear: Int, endDate: Int, endMonth: Int, endYear: Int) {
        Log.d("Download->","$endDate-$endMonth-$endYear")
        ledgerStartDate = startDate
        ledgerEndDate = endDate
        downloadPressed.postValue(true)
        initiateDownloadAndEmail("download", startMonth,startYear, endMonth, endYear)
    }
}