package com.dfd.delfin.ui.searchCity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.service.CityService
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.database.AppDatabase
import com.dfd.delfin.database.entity.SearchCityEntity
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Single
import javax.inject.Inject

class SearchCityViewModel @Inject constructor(
    private val cityService: CityService,
    private val appDB: AppDatabase
): BaseViewModel() {

    /* city data */
    var searchLiveData =
        MutableLiveData<List<Pair<BaseCityRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    /* data loading live data */
    var dataLoadingLiveData = MutableLiveData<Boolean>()

    var searchText = ""
    var cityType = ""
    var fromDialog = false


    /**
     * Search city history live data
     */
    fun searchCityHistoryLiveData() = appDB.searchCityDao().latestSearchEntries()

    fun searchCity(query: String) {
        val parentJsonObject = JsonObject()
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", "city_sugg")
        jsonObject.addProperty("prefix", query)
        jsonObject.addProperty("field", "city_display")
        jsonObject.addProperty("size", 25)
        parentJsonObject.addProperty("recommendation_type","city_based")

        val jsonArray = JsonArray()
        jsonArray.add(jsonObject)
        parentJsonObject.add("suggesters", jsonArray)
        //parentJsonObject.add("include_old_cities", JsonPrimitive(true))

        showProgress()
        Pair(SearchProgressItem(), DataRVAdapterOperationType.AddUpdate
        ).let { searchLiveData.postValue(listOf(it)) }

        dataLoadingLiveData.postValue(true)

        compositeDisposable += cityService.searchCities(parentJsonObject)
            .onBackground()
            .subscribe { _res, error ->
                if (!error) {
                    mutableListOf<Pair<BaseCityRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add(Pair(SearchProgressItem(), DataRVAdapterOperationType.Remove))

                        if (_res.responseData == null || _res.responseData.cities.isNullOrEmpty()) {
                            add(Pair(SearchCityWarningItem_NoResult, DataRVAdapterOperationType.AddUpdate))
                        }

                        else {
                            for (city in _res.responseData.cities) {
                                add(Pair(SearchDataItem(city), DataRVAdapterOperationType.Add))
                            }
                        }
                    }.let {
                        searchLiveData.postValue(it)
                    }


                }else{
                    mutableListOf<Pair<BaseCityRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add(Pair(SearchProgressItem(), DataRVAdapterOperationType.Remove))
                        /* add api time out item */
                        add(Pair(SearchCityWarningItem_NoResult, DataRVAdapterOperationType.AddUpdate))
                    }
                        .let { searchLiveData.postValue(it) }
                }
                dataLoadingLiveData.postValue(false)
            }
    }

    /**
     * Save to history
     */
    fun saveToHistory(
        originCity: CityModel
    ) {
        compositeDisposable += Single.fromCallable {
            appDB.searchCityDao()
                .newSearchEntry(SearchCityEntity(originCity))
        }
            .onBackground()
            .subscribe { res, error ->
                if (!error) {
                    Log.d("Search City History", "search entry stored $res")
                } else {
                    error.handle()
                }
            }
    }

    fun deleteCity(cityEntity: SearchCityEntity){
        compositeDisposable += Single.fromCallable {
            appDB.searchCityDao()
                .deleteSearchEntry(cityEntity)
        }
            .onBackground()
            .subscribe { res, error ->
                if (!error) {
                    Log.d("Search City History", "search entry deleted $res")
                } else {
                    error.handle()
                }
            }
    }

}