package com.delhivery.axle.ui.searchcitystate

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.service.CityService
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.CityStateModelList
import com.delhivery.axle.data.StateModelList
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.*
import javax.inject.Inject

class SearchCityStateViewModel @Inject constructor(
    private val cityService: CityService
): BaseViewModel() {

    /* city data */
    var searchLiveData =
        MutableLiveData<List<Pair<BaseCityStateRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    var popularCitiesLiveData =
        MutableLiveData<List<Pair<BaseCityStateRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    private var states = CityStateModelList.toMutableList()
    /* data loading live data */
    var dataLoadingLiveData = MutableLiveData<Boolean>()

    var searchText = ""
    var cityType = "origin"
    var fromDialog = false
    var selectedData= ""

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
                            if(cityType=="destination"){
                            var statePresent = false
                            for(state in states ){
                                if (state.city.toLowerCase(Locale.ROOT).startsWith(prefix = query.toLowerCase(
                                        Locale.ROOT))) {
                                    statePresent = true
                                    add(Pair(SearchDataItem(state), DataRVAdapterOperationType.Add))
                                }
                            }
                            if(!statePresent)
                            add(Pair(SearchCityStateWarningItem_NoResult, DataRVAdapterOperationType.AddUpdate))
                            }else{
                                add(Pair(SearchCityStateWarningItem_NoResult, DataRVAdapterOperationType.AddUpdate))
                            }
                        }

                        else {
                            for (city in _res.responseData.cities) {
                                add(Pair(SearchDataItem(city), DataRVAdapterOperationType.Add))
                            }
                            if(cityType=="destination"){
                            for(state in states ){
                                if (state.city.toLowerCase(Locale.ROOT).startsWith(prefix = query.toLowerCase(
                                        Locale.ROOT
                                    )
                                    )
                                ) {
                                    add(Pair(SearchDataItem(state), DataRVAdapterOperationType.Add))
                                }
                            }
                            }
                        }
                    }.let {
                        searchLiveData.postValue(it)
                    }


                }else{
                    var statePresent =false
                    mutableListOf<Pair<BaseCityStateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        if(cityType=="destination") {
                            for (state in states) {
                                if (state.city.toLowerCase(Locale.ROOT).startsWith(
                                        prefix = query.toLowerCase(
                                            Locale.ROOT
                                        )
                                    )
                                ) {
                                    statePresent = true
                                    add(Pair(SearchDataItem(state), DataRVAdapterOperationType.Add))
                                }
                            }
                            /* remove progress item */
                            add(Pair(SearchProgressItem(), DataRVAdapterOperationType.Remove))
                            /* add api time out item */
                            if (!statePresent)
                                add(
                                    Pair(
                                        SearchCityStateWarningItem_NoResult,
                                        DataRVAdapterOperationType.AddUpdate
                                    )
                                )
                        }else{
                            add(Pair(SearchProgressItem(), DataRVAdapterOperationType.Remove))
                            /* add api time out item */
                                add(
                                    Pair(
                                        SearchCityStateWarningItem_NoResult,
                                        DataRVAdapterOperationType.AddUpdate
                                    )
                                )

                        }
                    }
                        .let { searchLiveData.postValue(it) }


                dataLoadingLiveData.postValue(false)
            }
    }
    }

    fun getPopularCities(){
        showProgress()
        Pair(SearchProgressItem(), DataRVAdapterOperationType.AddUpdate
        ).let { popularCitiesLiveData.postValue(listOf(it)) }
        mutableListOf<Pair<BaseCityStateRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
            /* remove progress item */
            add(Pair(SearchProgressItem(), DataRVAdapterOperationType.Remove))
                // to be replace with popular cities api
                for(state in states ){
                        add(Pair(SearchDataItem(state), DataRVAdapterOperationType.Add))
                }


        }.let {
            popularCitiesLiveData.postValue(it)
        }
    }

}