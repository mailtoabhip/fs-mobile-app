package com.delhivery.orion.ui.searchload

import com.delhivery.orion.ui.base.BaseViewModel
import javax.inject.Inject

class SearchLoadViewModel @Inject constructor() : BaseViewModel() {

//  /* search load history live data */
//  fun searchLoadHistoryLiveData() = appDB.searchHistoryDao().latestSearchEntries(4)
//
//  /**
//   * Search load
//   */
//  fun searchLoad(
//    originCity: String,
//    destinationCity: String,
//    type: String,
//    size: String,
//    saveToHistory: Boolean
//  ) {
//    if (saveToHistory) {
//      compositeDisposable += Single.fromCallable {
//        appDB.searchHistoryDao()
//            .newSearchEntry(SearchLoadHistoryEntity(originCity, destinationCity, type, size))
//      }
//          .onBackground()
//          .subscribe { res, error ->
//            if (!error) {
//              Log.d("harish", "search entry stored $res")
//            } else {
//              error.printStackTrace()
//            }
//          }
//    }
//    //just search
//  }
//
//  fun deleteSearchResult(entry: SearchLoadHistoryEntity) {
//    compositeDisposable += Single.fromCallable {
//      appDB.searchHistoryDao()
//          .deleteSearchEntry(entry)
//    }
//        .onBackground()
//        .subscribe { res, error ->
//          if (!error) {
//            Log.d("harish", "entry deleted: $res")
//          } else {
//            error.printStackTrace()
//          }
//        }
//  }
}