package com.delhivery.axle.ui.searchload.fragments.searchload

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity
import com.delhivery.axle.databinding.FragmentSearchLoadBinding
import com.delhivery.axle.databinding.ViewSearchLoadHistoryItemBinding
import com.delhivery.axle.ui.custom.AnimationType.RevealOpen
import com.delhivery.axle.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.EVENT_SEARCH_ERROR
import com.delhivery.axle.utils.extensions.setup
import com.delhivery.axle.utils.extensions.visible
import com.github.florent37.kotlin.pleaseanimate.please
import javax.inject.Inject

class SearchLoadFragment : SearchLoadBaseFragment<FragmentSearchLoadBinding, SearchLoadFragmentViewModel>() {

  companion object {
    /* singleton instance */
    val _instance: SearchLoadFragment by lazy { SearchLoadFragment() }
  }

  override fun getViewModelClass() = SearchLoadFragmentViewModel::class.java

  override fun layoutId() = R.layout.fragment_search_load

  @Inject lateinit var autoCompleteUtils: AutoCompleteUtils

  private var origin: CityModel? = null
  private var destination: CityModel? = null

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.progressLiveData.observe(this, ProgressObserver())

    viewModel.citiesLiveData.observe(this, Observer {
      /* animate open */
      binding.arcView.animate(RevealOpen) {
        please {
          animate(binding.containerHistory) toBe {
            visible()
          }
          animate(binding.textHistoryTitle) toBe {
            visible()
          }
          animate(binding.containerSearchLoadHeaderForm) toBe {
            visible()
          }
        }.setStartDelay(300)
            .start()
      }

      setupSearchScreen(it)
    })

    viewModel.fetchCities()
  }

  override fun onPause() {
    super.onPause()
    uiUtils.toggleKeyboard()
    autoCompleteUtils.clearDisposable()
  }

  private fun setupSearchScreen(cities: List<CityModel>) {
    autoCompleteUtils.autoCompleteCity(binding.editOriginCity, cities) {
      origin = it
      binding.editDestinationCity.requestFocus()
    }

    autoCompleteUtils.autoCompleteCity(binding.editDestinationCity, cities) {
      destination = it
      uiUtils.toggleKeyboard()
    }

    /* truck type */
    binding.spinnerTruckType.setup(R.array.array_truck_type) { p, v ->

    }

    /* submit */
    binding.btnAction.setOnClickListener {
      searchLoad()
    }

    /* reverse origin/destination cities */
    binding.imgForward.setOnClickListener {
      please {
        animate(it) toBe {
          toBeRotated(180f)
        }
      }.thenCouldYou {
        please {
          animate(it) toBe {
            originalRotation()
          }
        }.withEndAction {
          val _origin = binding.editOriginCity.text.toString()
          binding.editOriginCity.setText(binding.editDestinationCity.text.toString())
          binding.editDestinationCity.setText(_origin)
        }
            .setStartDelay(300)
            .start()
      }
          .start()
    }

    /* init */
    initObservers()
  }

  /**
   * Search load as per user selections
   */
  private fun searchLoad(
    saveToHistory: Boolean = true,
    origin: CityModel? = this@SearchLoadFragment.origin,
    destination: CityModel? = this@SearchLoadFragment.destination
  ) {
    uiUtils.toggleKeyboard(true)

    if (origin == null) {
      binding.editOriginCity.setError(getString(string.error_search_missing_origin))
      binding.editOriginCity.errorAnimate()
      analyticsUtil.trackEvent(EVENT_SEARCH_ERROR, mutableListOf(), mutableListOf())
      return
    }

    /* form data */
    val type = binding.spinnerTruckType.selectedItem.toString()

    /* save to history if needed */
    if (saveToHistory) {
      viewModel.saveToHistory(
          origin, destination ?: CityModel(
          city = "Anywhere", cityId = "", state = "Anywhere"
      ), type
      )
    }
    /* searching progress */
    action(ProgressSearchLoadAction(true))

    /* delay and search for better UX */
    Handler().postDelayed({
      action(SearchLoadAction(origin, destination, type, saveToHistory))
    }, 200)
  }

  private fun validate() = origin != null

  /**
   * init observers
   */
  private fun initObservers() {
    /* observe live data for search history */
    viewModel.searchLoadHistoryLiveData()
        .observe(this, SearchLoadHistoryObserver())
  }

  /**
   * Search load history observer
   */
  inner class SearchLoadHistoryObserver : Observer<List<SearchLoadHistoryEntity>> {
    override fun onChanged(t: List<SearchLoadHistoryEntity>?) {
      t?.let { items ->
        binding.containerHistory.removeAllViews()
        items.forEachIndexed { index, item ->
          val itemBinding = historyItemBinding()
          itemBinding.data = item
          itemBinding.root.setOnClickListener {
            searchLoad(false, item.originCity, item.destinationCity)
          }
          itemBinding.root.setOnLongClickListener {
            viewModel.deleteSearchResult(item)
            true
          }
          binding.containerHistory.addView(itemBinding.root, index)
        }
      }
      /* title as per search results */
      binding.textHistoryTitle.visible(!t.isNullOrEmpty())
    }
  }

  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> uiUtils.showDelhiveryProgress(
              "Getting details", "This usually takes few seconds to load. please be patient.",
              "This usually takes few seconds to load. please be patient."
          )
          false -> uiUtils.hideDelhiveryProgress()
        }
      }
    }
  }

  /**
   * Create new history item binding
   */
  private fun historyItemBinding() = ViewSearchLoadHistoryItemBinding.inflate(
      layoutInflater, binding.containerHistory, false
  )
}