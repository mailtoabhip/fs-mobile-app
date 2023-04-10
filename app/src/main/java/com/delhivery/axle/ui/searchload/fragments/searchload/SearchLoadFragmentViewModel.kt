package com.delhivery.axle.ui.searchload.fragments.searchload

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.BidsRepository
import com.delhivery.axle.api.service.CityService
import com.delhivery.axle.api.service.TransactionService
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Single
import javax.inject.Inject

/**
 * View model for [SearchLoadFragment]
 */
class SearchLoadFragmentViewModel @Inject constructor(
  private val appDB: AppDatabase,
  private val cityService: CityService,
  private val transactionService: TransactionService,
  private val bidsRepository: BidsRepository
) : BaseViewModel() {

  var citiesLiveData = MutableLiveData<List<CityModel>>()
  var lowestBidLiveData = MutableLiveData<Pair<Int, HomeBidsRequestItemData>>()
  var truckDisplayNamesLiveData = MutableLiveData<List<String>>()
  var loadingProgressLiveData = MutableLiveData<Boolean>()
  /**
   * Search load history live data
   */
  fun searchLoadHistoryLiveData(requestType: String?, contractType: String?) = if(requestType=="load")appDB.searchHistoryDao().latestLoadSearchEntries() else contractType?.let { appDB.searchHistoryDao().latestContractSearchEntries(contractType = it) }

  /**
   * Save to history
   */
  fun saveToHistory(
    originCity: CityModel,
    destinationCity: CityModel,
    truckType: String?,
    truckDisplayName: String?,
    status: String?,
    requestType: String?,
    contractType: String?,
  ) {
    compositeDisposable += Single.fromCallable {
      appDB.searchHistoryDao()
          .newSearchEntry(SearchLoadHistoryEntity(originCity, destinationCity, truckType, truckDisplayName, status, if(requestType=="load") "fixed,spot" else "contract",contractType))
    }
        .onBackground()
        .subscribe { res, error ->
          if (!error) {
            Log.d("Search Load History", "search entry stored $res")
          } else {
            error.handle()
          }
        }
  }

  /**
   * Delete from history
   */
  fun deleteSearchResult(entry: SearchLoadHistoryEntity) {
    compositeDisposable += Single.fromCallable {
      appDB.searchHistoryDao()
          .deleteSearchEntry(entry)
    }
        .onBackground()
        .subscribe { res, error ->
          if (!error) {
            Log.d("Search Load History", "entry deleted: $res")
          } else {
            error.handle()
          }
        }
  }

  /**
   * Fetch all cities
   */
  fun fetchCities() {
    val jsonObject = JsonObject()
    jsonObject.addProperty("all", true)
    jsonObject.addProperty("primary", true)
    val jsonArray = JsonArray()
    jsonArray.add("city_code")
    jsonArray.add("city")
    jsonArray.add("orion_db_city_code")
    jsonObject.add("source_fields", jsonArray)
    compositeDisposable += cityService.getAllCities(jsonObject)
        .onBackground()
        .progress()
        .subscribe { _res, _ ->
          if (_res != null) {
            val converted = mutableListOf<CityModel>()
            _res.responseData?.cities?.forEach { it ->
              converted.add(CityModel(it.city, it.dbCityCode ?: "", state = it.state))
            }
            citiesLiveData.postValue(converted)
          } else {
            citiesLiveData.postValue(null)
          }
        }
  }

  /**
   * Fetch truck display names
   */
  fun fetchTruckDisplayNames(){
    compositeDisposable+=transactionService.getTruckDisplayNames("yes")
      .onBackground()
      .doOnSubscribe{
        loadingProgressLiveData.postValue(true)
      }
      .doFinally{
        loadingProgressLiveData.postValue(false)
      }
      .subscribe { res, _ ->
        if(res!=null){
          val converted= mutableListOf<String>()
          res.responseData?.truckDisplayNames?.forEach { it ->
            converted.add(it.truckDisplayName)
          }
          truckDisplayNamesLiveData.postValue(converted)
        }
        else
          truckDisplayNamesLiveData.postValue(null)
      }
  }
}