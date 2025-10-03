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
  
  // Test mode flag - set to true for testing with mock data
  var isTestMode: Boolean = false

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
        
        // Use mock data if in test mode
        if (isTestMode) {
            loadMockDriversForVehicle(vehicleNumber, editText, action)
            return
        }
        
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
                    }catch (e:Exception){

                    }
                }
                ?.subscribe { _res, _err ->
                    if (!_err && _res != null) {
                        _res.responseData?.let { res ->
                            cachedDriverData = res.recommendedDrivers ?: emptyList()
                            // Filter with current driver name query
                            val currentQuery = editText.text.toString()
                            if (currentQuery.length >= 2) {
                                filterCachedDrivers(currentQuery, editText, action)
                            } else {
                                // If no query, just show all drivers
                                editText.setItemsWithData(cachedDriverData) {
                                    action(it)
                                }
                            }
                        }
                    }
                }
        }
    }
    
    // Load mock drivers for testing
    private fun loadMockDriversForVehicle(
        vehicleNumber: String,
        editText: DelhiveryDriverNameAutoEditText,
        action: (DriverDataResponse) -> Unit
    ) {
        android.util.Log.d("AutoCompleteUtils", "Loading mock drivers for vehicle: $vehicleNumber")
        android.util.Log.d("AutoCompleteUtils", "Test mode enabled: $isTestMode")
        
        // Ensure UI operations run on main thread
        editText.post {
            // Simulate network delay
            editText.progress()
            editText.dismissDropDown()
            
            // Simulate async loading
            editText.postDelayed({
                try {
                    cachedDriverData = MockDriverData.getMockDriverDataForVehicle(vehicleNumber)
                    android.util.Log.d("AutoCompleteUtils", "Loaded ${cachedDriverData.size} drivers: ${cachedDriverData.map { it.driverName }}")
                    editText.progress(false)
                    
                    // Filter with current driver name query
                    val currentQuery = editText.text.toString()
                    android.util.Log.d("AutoCompleteUtils", "Current query: '$currentQuery', length: ${currentQuery.length}")
                    
                    if (currentQuery.length >= 2) {
                        android.util.Log.d("AutoCompleteUtils", "Filtering with query: '$currentQuery'")
                        filterCachedDrivers(currentQuery, editText, action)
                    } else {
                        // If no query, just show all drivers
                        android.util.Log.d("AutoCompleteUtils", "Showing all drivers")
                        editText.setItemsWithData(cachedDriverData) {
                            action(it)
                        }
                        
                        // Force show dropdown after setting items
                        editText.post {
                            android.util.Log.d("AutoCompleteUtils", "Forcing dropdown to show after showing all drivers")
                            editText.forceShowDropDown()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AutoCompleteUtils", "Error loading mock drivers", e)
                    editText.progress(false)
                }
            }, 100) // 100ms delay for better responsiveness
        }
    }

    // Filter cached drivers by driver name query
    private fun filterCachedDrivers(
        driverNameQuery: String,
        editText: DelhiveryDriverNameAutoEditText,
        action: (DriverDataResponse) -> Unit
    ) {
        android.util.Log.d("AutoCompleteUtils", "Filtering drivers with query: '$driverNameQuery'")
        android.util.Log.d("AutoCompleteUtils", "Cached drivers count: ${cachedDriverData.size}")
        android.util.Log.d("AutoCompleteUtils", "Cached drivers: ${cachedDriverData.map { it.driverName }}")
        
        if (cachedDriverData.isEmpty()) {
            android.util.Log.d("AutoCompleteUtils", "No cached drivers, dismissing dropdown")
            editText.dismissDropDown()
            return
        }
        
        val filteredDrivers = cachedDriverData.filter { driver ->
            driver.driverName?.lowercase()?.contains(driverNameQuery.lowercase()) == true
        }
        
        android.util.Log.d("AutoCompleteUtils", "Filtered drivers count: ${filteredDrivers.size}")
        android.util.Log.d("AutoCompleteUtils", "Filtered drivers: ${filteredDrivers.map { it.driverName }}")
        
        if (filteredDrivers.isNotEmpty()) {
            android.util.Log.d("AutoCompleteUtils", "Showing filtered drivers in dropdown")
            android.util.Log.d("AutoCompleteUtils", "About to call setItemsWithData")
            
            // Ensure UI operations run on main thread
            editText.post {
                editText.setItemsWithData(filteredDrivers) {
                    android.util.Log.d("AutoCompleteUtils", "Driver selected: ${it.driverName}")
                    action(it)
                }
                android.util.Log.d("AutoCompleteUtils", "setItemsWithData completed")
                
                // Force show dropdown after setting items
                editText.post {
                    android.util.Log.d("AutoCompleteUtils", "Forcing dropdown to show after setItemsWithData")
                    editText.forceShowDropDown()
                }
            }
        } else {
            android.util.Log.d("AutoCompleteUtils", "No matching drivers, dismissing dropdown")
            editText.post {
                editText.dismissDropDown()
            }
            // Don't call action with empty data to prevent validation loops
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
        
        // Listen to driver name changes for filtering and manual typing validation
        driverNameDisposable = RxTextView.textChanges(editText)
            .debounce(100, java.util.concurrent.TimeUnit.MILLISECONDS) // Add debounce to prevent rapid firing
            .subscribe({ driverNameQuery ->
                val queryText = driverNameQuery.toString()
                val currentVehicleNumber = vehicleNumberProvider()
                
                android.util.Log.d("AutoCompleteUtils", "Text changed: '$queryText', Vehicle: '$currentVehicleNumber', Cached: '$cachedVehicleNumber', TestMode: $isTestMode")
                
                // If vehicle number is available and different from cached, load new driver data
                if (currentVehicleNumber.isNotEmpty() && currentVehicleNumber != cachedVehicleNumber) {
                    android.util.Log.d("AutoCompleteUtils", "Vehicle number changed, loading new drivers")
                    cachedVehicleNumber = currentVehicleNumber
                    loadDriversForVehicle(currentVehicleNumber, editText, action)
                } else if (currentVehicleNumber.isNotEmpty() && cachedDriverData.isEmpty()) {
                    // Load driver data if vehicle number is available but no cached data
                    android.util.Log.d("AutoCompleteUtils", "Vehicle number available but no cached data, loading drivers")
                    cachedVehicleNumber = currentVehicleNumber
                    loadDriversForVehicle(currentVehicleNumber, editText, action)
                } else if (queryText.length >= 2 && cachedDriverData.isNotEmpty()) {
                    // Filter cached drivers and show dropdown
                    android.util.Log.d("AutoCompleteUtils", "Query length >= 2, filtering cached drivers")
                    filterCachedDrivers(queryText, editText, action)
                } else if (queryText.length >= 2 && cachedDriverData.isEmpty()) {
                    // If we have a query but no cached data, try to load drivers
                    android.util.Log.d("AutoCompleteUtils", "Query length >= 2 but no cached data, loading drivers")
                    if (currentVehicleNumber.isNotEmpty()) {
                        loadDriversForVehicle(currentVehicleNumber, editText, action)
                    }
                } else {
                    // Clear dropdown
                    android.util.Log.d("AutoCompleteUtils", "Query length < 2, dismissing dropdown")
                    editText.dismissDropDown()
                }
            }, {
                android.util.Log.e("AutoCompleteUtils", "Error in text changes", it)
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
                            val drivers = res.recommendedDrivers ?: emptyList()
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
                            val drivers = res.recommendedDrivers ?: emptyList()
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
    vehicleNumberDisposable?.dispose()
  }
  
  /**
   * Enable test mode with mock data
   */
  fun enableTestMode() {
    isTestMode = true
  }
  
  /**
   * Disable test mode (use real API)
   */
  fun disableTestMode() {
    isTestMode = false
  }

}