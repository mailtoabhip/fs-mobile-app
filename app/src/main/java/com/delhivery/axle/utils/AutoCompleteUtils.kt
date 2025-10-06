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
  private var driverNameDisposable: Disposable? = null
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

    // Load drivers for a specific vehicle
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
                    editText.post { editText.progress() }
                }
                ?.doFinally {
                    editText.post { editText.progress(false) }
                }
                ?.subscribe { _res, _err ->
                    if (!_err && _res != null) {
                        _res.responseData?.let { res ->
                            val test1 = DriverDataResponse("Rahul", "9675435502")
                            val test2 = DriverDataResponse("Rajat", "9898779988")
                            val arrayListTest = ArrayList<DriverDataResponse>()
                            arrayListTest.add(test1)
                            arrayListTest.add(test2)
                            val drivers = arrayListTest
                            val currentQuery = editText.text.toString()
                            
                            // Filter drivers by current query
                            val filteredDrivers = drivers.filter { driver ->
                                driver.driverName?.lowercase()?.contains(currentQuery.lowercase()) == true
                            }
                            
                            editText.post {
                                editText.setItemsWithData(filteredDrivers) { selectedDriver ->
                                    action(selectedDriver)
                                }
                                // Only show dropdown if there are results and user is actively typing
                                // Don't show if this is a result of a selection
                                if (filteredDrivers.isNotEmpty() && !editText.isSelectionInProgress()) {
                                    editText.showDropdownIfNeeded()
                                }
                            }
                        }
                    }
                }
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

    // Simple driver name autocomplete with phone number
    fun autoCompleteDriverNameWithPhone(
        editText: DelhiveryDriverNameAutoEditText,
        vehicleNumberProvider: () -> String,
        action: (DriverDataResponse) -> Unit
    ) {
        driverNameDisposable?.dispose()
        
        driverNameDisposable = RxTextView.textChanges(editText)
            .debounce(300, java.util.concurrent.TimeUnit.MILLISECONDS)
            .subscribe({ query ->
                val queryText = query.toString()
                val vehicleNumber = vehicleNumberProvider()
                
                if (queryText.length >= 2 && vehicleNumber.isNotEmpty()) {
                    // Load drivers for the vehicle
                    loadDriversForVehicle(vehicleNumber, editText, action)
                } else {
                    editText.post { editText.dismissDropDown() }
                }
            }, {
                android.util.Log.e("AutoCompleteUtils", "Error in text changes", it)
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
              editText.post { 
                  editText.progress()
                  editText.dismissDropDown()
              }
          }
          .doFinally {
              try{
                  editText.post {
                      editText.progress(false)
                      editText.showDropDown()
                  }
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
                        editText.post {
                            editText.progress()
                            editText.dismissDropDown()
                        }
                    }
                    .doFinally {
                        try{
                            editText.post {
                                editText.progress(false)
                                // Only show dropdown if not in selection progress
                                if (!editText.isSelectionInProgress()) {
                                    editText.showDropDown()
                                }
                            }
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
                    editText.post {
                        editText.progress()
                        editText.dismissDropDown()
                    }
                }
                ?.doFinally {
                    try{
                        editText.post {
                            editText.progress(false)
                            editText.showDropDown()
                        }
                    }catch (e:Exception){

                    }
                }
                ?.subscribe { _res, _err ->
                    if (!_err && _res != null) {
                        _res.responseData?.let { res ->
                            val arrayList = ArrayList<DriverDataResponse>()
                            val test1 = DriverDataResponse("Rahul", "9675435502")
                            val test2 = DriverDataResponse("Rajat", "9898779988")
                            val arrayListTest = ArrayList<DriverDataResponse>()
                            arrayListTest.add(test1)
                            arrayListTest.add(test2)
                            val drivers = arrayListTest
                            for(item in drivers)
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
            disposable = tpsService.getRecentDriverNameOnVehicle(vehicleNumber)
                ?.onBackground()
                ?.doOnSubscribe {
                    editText.post {
                        editText.progress()
                        editText.dismissDropDown()
                    }
                }
                ?.doFinally {
                    try{
                        editText.post {
                            editText.progress(false)
                            editText.showDropDown()
                        }
                    }catch (e:Exception){

                    }
                }
                ?.subscribe { _res, _err ->
                    if (!_err && _res != null) {
                        _res.responseData?.let { res ->
                            val arrayList = ArrayList<DriverDataResponse>()
                            val test1 = DriverDataResponse("Rahul", "9675435502")
                            val test2 = DriverDataResponse("Rajat", "9898779988")
                            val arrayListTest = ArrayList<DriverDataResponse>()
                            arrayListTest.add(test1)
                            arrayListTest.add(test2)
                            val drivers =  arrayListTest
                            // Filter drivers by name query (case insensitive)
                            for(item in drivers) {
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
  }
  

}