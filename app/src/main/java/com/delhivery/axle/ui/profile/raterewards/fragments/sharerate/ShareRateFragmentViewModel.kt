package com.delhivery.axle.ui.profile.raterewards.fragments.sharerate



import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.data.sharerates.ShareRateRoutesItemData
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.database.entity.OffersEntity
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

class ShareRateFragmentViewModel  @Inject constructor(
  private val loadCycleRepository: LoadCycleRepository,private val userRepository: UserRepository,
  val userPrefs: UserPrefs, private val appDatabase: AppDatabase
): BaseViewModel(){

  var userOfferRoutesData =
    MutableLiveData<List<Pair<BaseShareRateRVAdapterItem<*>, DataRVAdapterOperationType>>>()
  var dataLoadingLiveData = MutableLiveData<Boolean>()

  var hasMoreData = true
  var offset = 0
  var total = 0
  var count =0

  private fun fetchSpecificDatabaseOffers(occ:String,odc:String,tdn:String){
    compositeDisposable +=  appDatabase.offersDao().getSpecificOffers(occ,odc,tdn)
      .onBackground()
      .subscribe { _res, error ->
        if (!error) {
            mutableListOf<Pair<BaseShareRateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(ShareRatesProgressItem(), Remove))
              if(_res!=null){
              var shareRateRoutes = ShareRateRoutesItemData(
                _res.occ,
                _res.oc,
                _res.dcc,
                _res.dc,
                _res.tdn,
                "32MT",
                _res.offerType,
                _res.status,
                _res.offerId
              )
              count ++
              if(count<=10)
                add(Pair(ShareRatesItem(shareRateRoutes), DataRVAdapterOperationType.AddUpdate))
            }else{
            if(count==0){
              add(Pair(ShareRatesWarningItem_NoRate, DataRVAdapterOperationType.AddUpdate))
            }
            }
          }.let {
              userOfferRoutesData.postValue(it)
            }
        }}

  }

  private fun fetchTenOffers(){
    compositeDisposable +=  appDatabase.offersDao().getTenOffers()
      .onBackground()
      .subscribe { _res, error ->
        if (!error) {
          mutableListOf<Pair<BaseShareRateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(ShareRatesProgressItem(), Remove))
            if(_res!=null && _res.size>0){
              var itemCount =0
              for (item in _res) {
                itemCount++
                var shareRateRoutes = ShareRateRoutesItemData(
                  item.occ,
                  item.oc,
                  item.dcc,
                  item.dc,
                  item.tdn,
                  "32MT",
                  item.offerType,
                  item.status
                )
                add(Pair(ShareRatesItem(shareRateRoutes), DataRVAdapterOperationType.AddUpdate))
                if(itemCount==10)
                  break
              }
            }else{
              if(count==0){
                add(Pair(ShareRatesWarningItem_NoRate, DataRVAdapterOperationType.AddUpdate))
              }
            }
          }.let {
            userOfferRoutesData.postValue(it)
          }
        }}

  }



  fun getFrequentLanes() {

    Pair(
      ShareRatesProgressItem(), AddUpdate
    ).let { userOfferRoutesData.postValue(listOf(it)) }
    dataLoadingLiveData.postValue(true)
    val myDate = Date()
    val calendar: Calendar = Calendar.getInstance()
    calendar.setTimeZone(TimeZone.getTimeZone("UTC"))
    calendar.setTime(myDate)
    calendar.add(Calendar.DAY_OF_YEAR, -60)
    val time: Date = calendar.getTime()
    val outputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"))
    val dateAsString: String = outputFmt.format(time)

    val jsonObject = JsonObject()
    val arr = JsonArray()
    arr.add("origin_city_id")
    arr.add("destination_city_id")
    arr.add("truck_display_name")
    jsonObject.addProperty("loaded_after", dateAsString)
    jsonObject.addProperty("vendor_id", userRepository.userId())
    jsonObject.add("source_fields", arr)
    jsonObject.addProperty("offset", 0)
    jsonObject.addProperty("limit", 10000)
    compositeDisposable += loadCycleRepository.getFrequentLanes(jsonObject)
      .onBackground()
      .subscribe { _res, error ->
        if (!error) {
          mutableListOf<Pair<BaseShareRateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(ShareRatesProgressItem(), Remove))

            if (_res.trips.isNotEmpty()) {
                for (vt in _res.trips) {
                  fetchSpecificDatabaseOffers(vt.originCityId?:"",vt.destinationCityId?:"",vt.truckDisplayName?:"") }
            } else {
                  fetchTenOffers()
            }
          }
            .let {
              userOfferRoutesData.postValue(it)
            }
        } else {
          mutableListOf<Pair<BaseShareRateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            add(Pair(ShareRatesProgressItem(), DataRVAdapterOperationType.Remove))
            add(Pair(ShareRatesWarningItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
          }
            .let { userOfferRoutesData.postValue(it) }
        }

        dataLoadingLiveData.postValue(false)
      }
  }
}