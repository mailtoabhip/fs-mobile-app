package com.dfd.delfin.ui.searchload.fragments.searchload

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.service.CityService
import com.dfd.delfin.api.service.TransactionService
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.database.AppDatabase
import com.dfd.delfin.database.entity.SearchLoadHistoryEntity
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
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
    contractStatus: String?,
    requestType: String?,
    contractType: String?,
  ) {
    compositeDisposable += Single.fromCallable {
      appDB.searchHistoryDao()
          .newSearchEntry(SearchLoadHistoryEntity(originCity, destinationCity, truckType, truckDisplayName, contractStatus, if(requestType=="load") "fixed,spot" else "contract",contractType))
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