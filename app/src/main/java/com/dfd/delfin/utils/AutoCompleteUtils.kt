package com.dfd.delfin.utils

import com.dfd.delfin.api.response.DriverDataResponse
import com.dfd.delfin.api.service.CityService
import com.dfd.delfin.api.service.TPSService
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.injection.scope.ActivityScope
import com.dfd.delfin.ui.custom.DelfinCityAutoEditText
import com.dfd.delfin.ui.custom.DelfinDriverNameAutoEditText
import com.dfd.delfin.ui.custom.DelfinTrucksAutoEditText
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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
      editText: DelfinCityAutoEditText,
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
      editText: DelfinCityAutoEditText,
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
        editText: DelfinTrucksAutoEditText,
        action: (String) -> Unit
    ) {
        val d = RxTextView.textChanges(editText)
                .filter { it.length >= 2 }
                .subscribe({
                    //resetNetworkTruckSuggestions(it.toString(), editText, action)
                }, {
                    it.printStackTrace()
                })
    }

    // Load drivers for a specific vehicle
    private fun loadDriversForVehicle(
        vehicleNumber: String,
        editText: DelfinDriverNameAutoEditText,
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
                            /*val test1 = DriverDataResponse("Rahul", "9675435502")
                            val test2 = DriverDataResponse("Rajat", "9898779988")
                            val arrayListTest = ArrayList<DriverDataResponse>()
                            arrayListTest.add(test1)
                            arrayListTest.add(test2)*/
                            val drivers = res.recommendedDrivers ?: emptyList()
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
        editText: DelfinDriverNameAutoEditText,
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
        editText: DelfinDriverNameAutoEditText,
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
                    editText.post {
                        editText.progress(false)
                        editText.dismissDropDown()
                    }
                }
            }, {
                android.util.Log.e("AutoCompleteUtils", "Error in text changes", it)
            })
    }


  fun skipFirstAutoCompleteCity(
      editText: DelfinCityAutoEditText,
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
      editText: DelfinCityAutoEditText,
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
      editText: DelfinCityAutoEditText,
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
                      //editText.showDropDown()

                      // Only show if not in selection AND text hasn't been programmatically set
                      if (!editText.isSelectionInProgress() && editText.hasFocus()) {
                          editText.showDropDown()
                      }
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

    private fun resetNetworkDriverNameSuggestions(
        query: String,
        editText: DelfinDriverNameAutoEditText,
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
        editText: DelfinDriverNameAutoEditText,
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