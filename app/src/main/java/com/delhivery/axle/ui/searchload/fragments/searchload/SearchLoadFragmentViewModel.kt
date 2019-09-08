package com.delhivery.axle.ui.searchload.fragments.searchload

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.CityService
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import io.reactivex.Single
import javax.inject.Inject

class SearchLoadFragmentViewModel @Inject constructor(
  private val appDB: AppDatabase,
  private val cityService: CityService
) : BaseViewModel() {

  /* search results live data */
  var citiesLiveData = MutableLiveData<List<CityModel>>()

  /* search load history live data */
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

  fun fetchCities() {
    compositeDisposable += cityService.getAllCities()
        .onBackground()
        .progress()
        .subscribe { _res, _ ->
          if (_res != null) {
            citiesLiveData.postValue(_res.responseData?.cities ?: listOf())
          } else {
            citiesLiveData.postValue(null)
          }
        }
  }
}