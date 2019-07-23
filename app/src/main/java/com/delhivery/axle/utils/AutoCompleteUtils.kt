package com.delhivery.axle.utils

import com.delhivery.axle.api.CityService
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.custom.DelhiveryCityAutoEditText
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
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
    disposable = cityService.searchCities(query)
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
              editText.setItems(cities) {
                disposable?.dispose()
                action(it)
              }
            }
          }
        }
  }

  fun clearDisposable() {
    disposable?.dispose()
  }

}