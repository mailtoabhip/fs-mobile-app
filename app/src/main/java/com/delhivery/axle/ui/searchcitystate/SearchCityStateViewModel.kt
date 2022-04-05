package com.delhivery.axle.ui.searchcitystate

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.service.CityService
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import javax.inject.Inject

class SearchCityStateViewModel @Inject constructor(
    private val cityService: CityService
): BaseViewModel() {

    /* city data */
    var searchLiveData =
        MutableLiveData<List<Pair<BaseCityStateRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    /* data loading live data */
    var dataLoadingLiveData = MutableLiveData<Boolean>()

    var searchText = ""
    var cityType = "origin"
    var fromDialog = false


    /**
     * Search city history live data
     */
   // fun searchCityHistoryLiveData() = appDB.searchCityDao().latestSearchEntries()

    fun searchCity(query: String) {
        val parentJsonObject = JsonObject()
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", "city_sugg")
        jsonObject.addProperty("prefix", query)
        jsonObject.addProperty("field", "city_display")
        jsonObject.addProperty("size", 25)
        val jsonArray = JsonArray()
        jsonArray.add(jsonObject)
        parentJsonObject.add("suggesters", jsonArray)
        //parentJsonObject.add("include_old_cities", JsonPrimitive(true))

        showProgress()
        Pair(SearchProgressItem(), DataRVAdapterOperationType.AddUpdate
        ).let { searchLiveData.postValue(listOf(it)) }

        dataLoadingLiveData.postValue(true)

        compositeDisposable+= cityService.searchCities(parentJsonObject)
            .onBackground()
            .subscribe { _res, error ->
                if (!error) {
                    mutableListOf<Pair<BaseCityStateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add(Pair(SearchProgressItem(), DataRVAdapterOperationType.Remove))
                        if (_res.responseData == null || _res.responseData.cities.isNullOrEmpty()) {
                            add(Pair(SearchCityStateWarningItem_NoResult, DataRVAdapterOperationType.AddUpdate))
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
                    mutableListOf<Pair<BaseCityStateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add(Pair(SearchProgressItem(), DataRVAdapterOperationType.Remove))
                        /* add api time out item */
                        add(Pair(SearchCityStateWarningItem_NoResult, DataRVAdapterOperationType.AddUpdate))
                    }
                        .let { searchLiveData.postValue(it) }
                }
                dataLoadingLiveData.postValue(false)
            }
    }


}