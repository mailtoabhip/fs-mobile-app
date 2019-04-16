package com.delhivery.orion.ui.searchload.fragments.searchload

import android.util.Log
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.database.AppDatabase
import com.delhivery.orion.database.entity.SearchLoadHistoryEntity
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import javax.inject.Inject

class SearchLoadFragmentViewModel @Inject constructor(private val appDB: AppDatabase) :
    BaseViewModel() {

  /* search load history live data */
  fun searchLoadHistoryLiveData() = appDB.searchHistoryDao().latestSearchEntries()

  /**
   * Save to history
   */
  fun saveToHistory(
    originCity: CityModel,
    destinationCity: CityModel,
    type: String,
    size: String
  ) {
    compositeDisposable += Single.fromCallable {
      appDB.searchHistoryDao()
          .newSearchEntry(SearchLoadHistoryEntity(originCity, destinationCity, type, size))
    }
        .onBackground()
        .subscribe { res, error ->
          if (!error) {
            Log.d("harish", "search entry stored $res")
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
            Log.d("harish", "entry deleted: $res")
          } else {
            error.handle()
          }
        }
  }
}