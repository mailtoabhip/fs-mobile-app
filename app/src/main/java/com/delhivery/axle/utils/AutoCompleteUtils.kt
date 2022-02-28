package com.delhivery.axle.utils

import android.widget.Toast
import com.delhivery.axle.api.service.CityService
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.custom.DelhiveryCityAutoEditText
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
  private val activity: DaggerAppCompatActivity
) {

  private var disposable: Disposable? = null
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
              editText.progress(false)
              editText.showDropDown()
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

  fun clearDisposable() {
    disposable?.dispose()
  }

}