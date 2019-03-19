package com.delhivery.orion.ui.searchload

import android.util.Log
import com.delhivery.orion.database.AppDatabase
import com.delhivery.orion.database.entity.SearchLoadHistoryEntity
import com.delhivery.orion.ui.base.BaseViewModel
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import io.reactivex.Single
import javax.inject.Inject

class SearchLoadViewModel @Inject constructor(private val appDB: AppDatabase) : BaseViewModel() {

  /* search load history live data */
  fun searchLoadHistoryLiveData() = appDB.searchHistoryDao().latestSearchEntries()

  /**
   * Search load
   */
  fun searchLoad(
    originCity: String,
    destinationCity: String,
    type: String,
    size: String,
    saveToHistory: Boolean
  ) {
    if (saveToHistory) {
      compositeDisposable += Single.fromCallable {
        appDB.searchHistoryDao()
            .newSearchEntry(SearchLoadHistoryEntity(originCity, destinationCity, type, size))
      }
          .onBackground()
          .subscribe { res, error ->
            if (!error) {
              Log.d("harish", "search entry stored $res")
            } else {
              error.printStackTrace()
            }
          }
    }
    //just search
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
            error.printStackTrace()
          }
        }
  }
}