package com.delhivery.axle.ui.searchload.fragments.searchload

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.service.CityService
import com.delhivery.axle.data.CityModel
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
  private val cityService: CityService
) : BaseViewModel() {

  var citiesLiveData = MutableLiveData<List<CityModel>>()

  /**
   * Search load history live data
   */
  fun searchLoadHistoryLiveData() = appDB.searchHistoryDao().latestSearchEntries()

  /**
   * Save to history
   */
  fun saveToHistory(
    originCity: CityModel,
    destinationCity: CityModel,
    type: String
  ) {
    compositeDisposable += Single.fromCallable {
      appDB.searchHistoryDao()
          .newSearchEntry(SearchLoadHistoryEntity(originCity, destinationCity, type))
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
              converted.add(CityModel(it.city, it.dbCityCode ?: "", it.cityId, state = it.state))
            }
            citiesLiveData.postValue(converted)
          } else {
            citiesLiveData.postValue(null)
          }
        }
  }
}