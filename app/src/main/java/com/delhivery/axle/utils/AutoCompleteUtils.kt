package com.delhivery.axle.utils

import android.widget.Toast
import com.delhivery.axle.api.response.DriverDataResponse
import com.delhivery.axle.api.response.TruckDisplayNameItem
import com.delhivery.axle.api.service.CityService
import com.delhivery.axle.api.service.InventoryService
import com.delhivery.axle.api.service.TPSService
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trucks.HomeTrucksRequestItemData
import com.delhivery.axle.data.home.trucks.names
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.custom.DelhiveryCityAutoEditText
import com.delhivery.axle.ui.custom.DelhiveryDriverNameAutoEditText
import com.delhivery.axle.ui.custom.DelhiveryTrucksAutoEditText
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * AutoCompleteUtils acts as Auto Complete controller including network interactions
 */
@ActivityScope
class AutoCompleteUtils @Inject constructor(
  private val cityService: CityService,
  private val inventoryService: InventoryService,
  private val tpsService: TPSService,
  private val activity: DaggerAppCompatActivity
) {

  private var disposable: Disposable? = null
  
  // Cache for driver data by vehicle number
  private var cachedDriverData: List<DriverDataResponse> = emptyList()
  private var cachedVehicleNumber: String = ""
  private var driverNameDisposable: Disposable? = null
  private var vehicleNumberDisposable: Disposable? = null
  private lateinit var cities: List<CityModel>

  @Inject lateinit var userPrefs: UserPrefs

  /**
   * Attach auto complete for cities
   */
  fun autoCompleteCity(
    editText: DelhiveryCityAutoEditText,
    cities: List<CityModel>,
    action: (CityModel) -> Unit
  ) {
    this.cities = cities
    val d = RxTextView.textChanges(editText)
        .filter { it.length >= 3 }
        .subscribe({
          resetStaticSuggestions(it.toString(), editText, action)
        }, {
          it.printStackTrace()
        })
  }

  fun autoCompleteCity(
    editText: DelhiveryCityAutoEditText,
    action: (CityModel) -> Unit
  ) {
    val d = RxTextView.textChanges(editText)
        .filter { it.length >= 3 }
        .subscribe({
          resetNetworkSuggestions(it.toString(), editText, action)
        }, {
          it.printStackTrace()
        })
  }

    fun autoCompleteTruck(
            editText: DelhiveryTrucksAutoEditText,
            action: (String) -> Unit
    ) {
        val d = RxTextView.textChanges(editText)
                .filter { it.length >= 2 }
                .subscribe({
                    resetNetworkTruckSuggestions(it.toString(), editText, action)
                }, {
                    it.printStackTrace()
                })
    }

    // Load drivers for a specific vehicle (API call)
    private fun loadDriversForVehicle(
        vehicleNumber: String,
        editText: DelhiveryDriverNameAutoEditText,
        action: (DriverDataResponse) -> Unit
    ) {
        disposable?.dispose()
        if(userPrefs.jwtToken != null) {
            disposable = tpsService.getRecentDriverNameOnVehicle(vehicleNumber)
                ?.onBackground()
                ?.doOnSubscribe {
                    editText.progress()
                    editText.dismissDropDown()
                }
                ?.doFinally {
                    try{
                        editText.progress(false)
                        editText.showDropDown()
                    }catch (e:Exception){

                    }
                }
                ?.subscribe { _res, _err ->
                    if (!_err && _res != null) {
                        _res.responseData?.let { res ->
                            cachedDriverData = res
                            // Filter with current driver name query
                            val currentQuery = editText.text.toString()
                            if (currentQuery.length >= 2) {
                                filterCachedDrivers(currentQuery, editText, action)
                            }
                        }
                    }
                }
        }
    }

    // Filter cached drivers by driver name query
    private fun filterCachedDrivers(
        driverNameQuery: String,
        editText: DelhiveryDriverNameAutoEditText,
        action: (DriverDataResponse) -> Unit
    ) {
        if (cachedDriverData.isEmpty()) {
            editText.dismissDropDown()
            return
        }
        
        val filteredDrivers = cachedDriverData.filter { driver ->
            driver.driverName?.lowercase()?.contains(driverNameQuery.lowercase()) == true
        }
        
        editText.setItemsWithData(filteredDrivers) {
            action(it)
        }
    }


    fun autoCompleteDriverName(
        editText: DelhiveryDriverNameAutoEditText,
        action: (String) -> Unit
    ) {
        val d = RxTextView.textChanges(editText)
            .filter { it.length >= 2 }
            .subscribe({
                resetNetworkDriverNameSuggestions(it.toString(), editText, action)
            }, {
                it.printStackTrace()
            })
    }

    // Optimized overload that caches driver data by vehicle number
    fun autoCompleteDriverNameWithPhone(
        editText: DelhiveryDriverNameAutoEditText,
        vehicleNumberProvider: () -> String,
        action: (DriverDataResponse) -> Unit
    ) {
        // Clear previous disposables
        driverNameDisposable?.dispose()
        vehicleNumberDisposable?.dispose()
        
        // Listen to vehicle number changes
        vehicleNumberDisposable = RxTextView.textChanges(editText)
            .map { vehicleNumberProvider() }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .subscribe({ vehicleNumber ->
                if (vehicleNumber != cachedVehicleNumber) {
                    cachedVehicleNumber = vehicleNumber
                    loadDriversForVehicle(vehicleNumber, editText, action)
                }
            }, {
                it.printStackTrace()
            })
        
        // Listen to driver name changes for filtering and manual typing validation
        driverNameDisposable = RxTextView.textChanges(editText)
            .subscribe({ driverNameQuery ->
                val queryText = driverNameQuery.toString()
                if (queryText.length >= 2) {
                    // Filter cached drivers and show dropdown
                    filterCachedDrivers(queryText, editText, action)
                    
                    // Also create a temporary DriverDataResponse for manual typing validation
                    val tempDriverData = DriverDataResponse(queryText, null)
                    action(tempDriverData)
                } else {
                    // Clear dropdown and create empty driver data for validation
                    editText.dismissDropDown()
                    val tempDriverData = DriverDataResponse(queryText, null)
                    action(tempDriverData)
                }
            }, {
                it.printStackTrace()
            })
    }


  fun skipFirstAutoCompleteCity(
    editText: DelhiveryCityAutoEditText,
    action: (CityModel) -> Unit
  ) {
    val d = RxTextView.textChanges(editText)
      .filter { it.length >= 3 }
      .skip(1)
      .subscribe({
        resetNetworkSuggestions(it.toString(), editText, action)
      }, {
        it.printStackTrace()
      })
  }

  /**
   * Re-fetch suggestions
   */
  private fun resetStaticSuggestions(
    query: String,
    editText: DelhiveryCityAutoEditText,
    action: (CityModel) -> Unit
  ) {
    val d = RxTextView.textChanges(editText)
        .filter { it.length >= 3 }
        .subscribe({
          resetNetworkSuggestions(it.toString(), editText, action)
        }, {
          it.printStackTrace()
        })
//    editText.setItems(
//        cities.filter {
//          it.city.toLowerCase()
//              .startsWith(query.toLowerCase())
//        }) {
//      editText.dismissDropDown()
//      action(it)
//    }
  }

  private fun resetNetworkSuggestions(
    query: String,
    editText: DelhiveryCityAutoEditText,
    action: (CityModel) -> Unit
  ) {
    val parentJsonObject = JsonObject()
    val jsonObject = JsonObject()
    jsonObject.addProperty("name", "city_sugg")
    jsonObject.addProperty("prefix", query)
    jsonObject.addProperty("field", "city_display")
    jsonObject.addProperty("size", "100")
      parentJsonObject.addProperty("recommendation_type","city_based")
    val jsonArray = JsonArray()
    jsonArray.add(jsonObject)
    parentJsonObject.add("suggesters", jsonArray)

    disposable?.dispose()
    if(userPrefs.jwtToken != null) {
      disposable = cityService.searchCities(parentJsonObject)
          .onBackground()
          .doOnSubscribe {
              editText.progress()
              editText.dismissDropDown()
          }
          .doFinally {
              try{
              editText.progress(false)
              editText.showDropDown()
              }catch (e:Exception){

              }
          }
          .subscribe { _res, _err ->
              if (!_err && _res != null) {
                  _res.responseData?.let { cities ->
                      editText.setItems(cities.cities) {
                          disposable?.dispose()
                          action(it)
                      }
                  }
              }
          }
    }
  }

    private fun resetNetworkTruckSuggestions(
            query: String,
            editText: DelhiveryTrucksAutoEditText,
            action: (String) -> Unit
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("supplier_id", userPrefs.parentId)

        jsonObject.addProperty("offset", 0)
        jsonObject.addProperty("limit", 10)
        jsonObject.addProperty("vehicle_prefix",query)

        disposable?.dispose()
        if(userPrefs.jwtToken != null) {
            disposable = inventoryService.getInventories(jsonObject)
                    .onBackground()
                    .doOnSubscribe {
                        editText.progress()
                        editText.dismissDropDown()
                    }
                    .doFinally {
                        try{
                            editText.progress(false)
                            editText.showDropDown()
                        }catch (e:Exception){

                        }
                    }
                    .subscribe { _res, _err ->
                        if (!_err && _res != null) {
                            _res.responseData?.let { res ->
                                val arrayList = ArrayList<String>()
                                for(item in res.trucks.names())
                                    arrayList.add(item)
                                arrayList.add("Add New Truck")
                                editText.setItems(arrayList) {
                                    disposable?.dispose()
                                    action(it)
                                }
                            }
                        }
                    }
        }
    }

    private fun resetNetworkDriverNameSuggestions(
        query: String,
        editText: DelhiveryDriverNameAutoEditText,
        action: (String) -> Unit
    ) {
        disposable?.dispose()
        if(userPrefs.jwtToken != null) {
            disposable = tpsService.getRecentDriverNameOnVehicle(query)
                ?.onBackground()
                ?.doOnSubscribe {
                    editText.progress()
                    editText.dismissDropDown()
                }
                ?.doFinally {
                    try{
                        editText.progress(false)
                        editText.showDropDown()
                    }catch (e:Exception){

                    }
                }
                ?.subscribe { _res, _err ->
                    if (!_err && _res != null) {
                        _res.responseData?.let { res ->
                            val arrayList = ArrayList<DriverDataResponse>()
                            for(item in res)
                                arrayList.add(item)
                           // arrayList.add("Add New Truck")
                            editText.setItems(arrayList) {
                                disposable?.dispose()
                                action(it)
                            }
                        }
                    }
                }
        }
    }

    private fun resetNetworkDriverNameSuggestionsWithPhone(
        driverNameQuery: String,
        vehicleNumber: String,
        editText: DelhiveryDriverNameAutoEditText,
        action: (DriverDataResponse) -> Unit
    ) {
        disposable?.dispose()
        if(userPrefs.jwtToken != null && vehicleNumber.isNotEmpty()) {
            disposable = tpsService.getRecentDriverNameOnVehicle("MP09QT0003")
                ?.onBackground()
                ?.doOnSubscribe {
                    editText.progress()
                    editText.dismissDropDown()
                }
                ?.doFinally {
                    try{
                        editText.progress(false)
                        editText.showDropDown()
                    }catch (e:Exception){

                    }
                }
                ?.subscribe { _res, _err ->
                    if (!_err && _res != null) {
                        _res.responseData?.let { res ->
                            val arrayList = ArrayList<DriverDataResponse>()
                            // Filter drivers by name query (case insensitive)
                            for(item in res) {
                                if(item.driverName?.lowercase()?.contains(driverNameQuery.lowercase()) == true) {
                                    arrayList.add(item)
                                }
                            }
                            editText.setItemsWithData(arrayList) {
                                disposable?.dispose()
                                action(it)
                            }
                        }
                    }
                }
        }
    }







  fun clearDisposable() {
    disposable?.dispose()
    driverNameDisposable?.dispose()
    vehicleNumberDisposable?.dispose()
  }

}