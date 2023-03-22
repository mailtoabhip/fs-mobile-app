package com.delhivery.axle.ui.searchload.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity
import com.delhivery.axle.databinding.FragmentIntracitySearchLoadBinding
import com.delhivery.axle.databinding.ViewSearchLoadHistoryItemBinding
import com.delhivery.axle.ui.custom.AnimationType.RevealOpen
import com.delhivery.axle.ui.searchload.SearchLoadActivity
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.EVENT_SEARCH_ERROR
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.setup
import com.delhivery.axle.utils.extensions.visible
import com.github.florent37.kotlin.pleaseanimate.please
import javax.inject.Inject

class IntracitySearchLoadFragment:SearchLoadBaseFragment<FragmentIntracitySearchLoadBinding,IntracitySearchLoadFragmentViewModel>() {

  companion object{
    /*singleton instance */
    val _instance: IntracitySearchLoadFragment by lazy { IntracitySearchLoadFragment() }
  }
  override fun getViewModelClass() = IntracitySearchLoadFragmentViewModel::class.java

  override fun layoutId() = R.layout.fragment_intracity_search_load

  @Inject lateinit var autoCompleteUtils: AutoCompleteUtils

  private var reportingCenter: CityModel?= null
  private var requestType: String? =""
  private var contractType:String? =""

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    val activity: SearchLoadActivity? = activity as SearchLoadActivity?
    requestType = activity?.intentRequestType
    contractType = activity?.intentContractType
    viewModel.progressLiveData.observe(this, ProgressObserver())

    binding.btnRetry.setOnClickListener {
      binding.warningItem.visibility = View.GONE
      binding.warningItem.alpha = 0f
      //      viewModel.fetchCities()
    }

    binding.editReportingCenter.setText("")

    toggleVisibility(false)
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
        animate(binding.warningItem).toBe {
          invisible()
        }
      }.setStartDelay(300)
        .start()
    }
    setupSearchScreen()
  }

  private fun toggleVisibility(toggle: Boolean) {
    when (toggle) {
      true -> {
        binding.warningItem.visibility = View.VISIBLE
        binding.containerHistory.visibility = View.GONE
        binding.textHistoryTitle.visibility = View.GONE
        binding.containerSearchLoadHeaderForm.visibility = View.GONE
      }
      false -> {
        binding.warningItem.visibility = View.GONE
        binding.containerHistory.visibility = View.VISIBLE
        binding.textHistoryTitle.visibility = View.VISIBLE
        binding.containerSearchLoadHeaderForm.visibility = View.VISIBLE
      }
    }
  }

  override fun onPause() {
    super.onPause()
    uiUtils.toggleKeyboard()
    autoCompleteUtils.clearDisposable()
  }

  override fun onDestroy() {
    reportingCenter=null
    super.onDestroy()
  }

  private fun setupSearchScreen() {
    autoCompleteUtils.autoCompleteCity(binding.editReportingCenter) {
      reportingCenter = it
    }
    /* truck type */
    binding.spinnerTruckType.setup(R.array.array_truck_type) { pos, v ->

    }
    binding.spinnerStatus.setup(R.array.array_status){ pos, v ->

    }

    /* submit */
    binding.btnAction.setOnClickListener {
      searchLoad(true,reportingCenter,null,binding.spinnerTruckType.selectedItem.toString(),binding.spinnerStatus.selectedItem.toString())
    }
    /* init */
    initObservers()
  }

  /**
   * Search load as per user selections
   */
  private fun searchLoad(
    saveToHistory: Boolean = true,
    reportingCenter: CityModel? = null,
    destination: CityModel? = null,
    truckType: String,
    status: String
  ) {
    uiUtils.toggleKeyboard(true)
    if (reportingCenter == null) {
      binding.editReportingCenter.error = getString(string.error_search_missing_origin)
      binding.editReportingCenter.errorAnimate()
      analyticsUtil.trackEvent(EVENT_SEARCH_ERROR, mutableListOf(), mutableListOf())
      return
    }

    if (truckType.toLowerCase().contains("choose")) {
      binding.spinnerTruckType.errorVibrate()
      return
    }

    if(status.toLowerCase().contains("select")){
      binding.spinnerStatus.errorVibrate()
      return
    }

    /* save to history if needed */

    /* searching progress */
    action(ProgressSearchLoadAction(true,if(requestType=="contract")"Searching contracts" else "Searching loads"))

    /* delay and search for better UX */
  }

  /**
   * init observers
   */
  private fun initObservers() {
    /* observe live data for search history */
    viewModel.searchLoadHistoryLiveData(requestType,contractType)
      ?.observe(this, SearchLoadHistoryObserver())
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
            searchLoad(false, item.originCity, item.destinationCity, item.truckType, binding.spinnerStatus.selectedItem.toString())
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
