package com.delhivery.orion.utils

import android.R.layout
import android.widget.ArrayAdapter
import com.delhivery.orion.api.OrionDataService
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.names
import com.delhivery.orion.injection.scope.ActivityScope
import com.delhivery.orion.ui.custom.DelhiveryCityAutoEditText
import com.delhivery.orion.utils.extensions.not
import com.delhivery.orion.utils.extensions.onBackground
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * AutoCompleteutils acts as Auto Complete controller including network interactions
 */
@ActivityScope
class AutoCompleteUtils @Inject constructor(
  private val orionDataService: OrionDataService,
  private val activity: DaggerAppCompatActivity
) {

  private var disposable: Disposable? = null

  /**
   * Attach auto complete for cities
   */
  fun autoCompleteCity(
    editText: DelhiveryCityAutoEditText,
    action: (CityModel) -> Unit
  ) {
    val d = RxTextView.textChanges(editText)
        .filter { it.length >= 3 }
        .subscribe({
          resetSuggestions(it.toString(), editText, action)
        }, {
          it.printStackTrace()
        })
  }

  /**
   * Re-fetch suggestions
   */
  private fun resetSuggestions(
    query: String,
    editText: DelhiveryCityAutoEditText,
    action: (CityModel) -> Unit
  ) {
    disposable?.dispose()
    disposable = orionDataService.searchCities(query)
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
              val adapter =
                ArrayAdapter<String>(activity, layout.simple_dropdown_item_1line, cities.names())
              editText.setAdapter(adapter)
              editText.setOnItemClickListener { _, _, i, _ ->
                action(cities[i])
              }
            }
          }
        }

  }
}