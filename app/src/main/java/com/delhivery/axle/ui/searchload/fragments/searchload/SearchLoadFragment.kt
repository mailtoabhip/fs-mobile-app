package com.delhivery.axle.ui.searchload.fragments.searchload

import android.R.layout
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity
import com.delhivery.axle.databinding.FragmentSearchLoadBinding
import com.delhivery.axle.databinding.ViewSearchLoadHistoryItemBinding
import com.delhivery.axle.ui.custom.AnimationType.RevealOpen
import com.delhivery.axle.ui.searchload.SearchLoadActivity
import com.delhivery.axle.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.EVENT_SEARCH_ERROR
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.setHintColor
import com.delhivery.axle.utils.extensions.setup
import com.delhivery.axle.utils.extensions.visible
import com.github.florent37.kotlin.pleaseanimate.please
import com.jakewharton.rxbinding2.view.RxView.selected
import javax.inject.Inject

/**
 * Search load screen
 */
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
  private var requestType: String? =  ""
  private var contractType:String? = ""
  private var truckDisplayNames = arrayListOf("Select Type")

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    val activity: SearchLoadActivity? = activity as SearchLoadActivity?
    requestType = activity?.intentRequestType
    contractType = activity?.intentContractType
    viewModel.fetchTruckDisplayNames()
    viewModel.progressLiveData.observe(this, ProgressObserver())
    if(contractType==getString(R.string.intracity_contract_type)){
      viewModel.loadingProgressLiveData.observe(this){
        if(it) uiUtils.showProgress()
        else uiUtils.hideProgress()
      }
      viewModel.truckDisplayNamesLiveData.observe(this){
          truckDisplayNames.clear()
          truckDisplayNames.add("Select Type")
          if(it!=null)
            truckDisplayNames.addAll(it)
          binding.spinnerTruckDisplayName.setTruckDisplayNamesAdapter()
      }
    }
    binding.btnRetry.setOnClickListener {
      binding.warningItem.visibility = View.GONE
      binding.warningItem.alpha = 0f
      //      viewModel.fetchCities()
    }

    binding.editOriginCity.setText("")
    binding.editDestinationCity.setText("")

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
    super.onDestroy()
    origin = null
    destination = null
  }

  private fun setupSearchScreen() {
    if(contractType==getString(string.intracity_contract_type)){
      showIntracityRelatedViews()
      hideIntercityRelatedViews()
      binding.editReportingCenter.setText("")
      binding.spinnerStatus.apply{
        setup(R.array.array_status){ p, v ->
          if(p==0) setHintColor(v)
        }
      }
      autoCompleteUtils.autoCompleteCity(binding.editReportingCenter){
        origin = it
      }
      binding.btnSearch.setOnClickListener{
        searchLoad(true, origin, destination, null, binding.spinnerTruckDisplayName.selectedItem.toString(),binding.spinnerStatus.selectedItem.toString())
      }
    }
    else{
      hideIntracityRelatedViews()
      showIntercityRelatedViews()
      autoCompleteUtils.autoCompleteCity(binding.editOriginCity) {
        origin = it
        binding.editDestinationCity.requestFocus()
      }

      autoCompleteUtils.autoCompleteCity(binding.editDestinationCity) {
        destination = it
        uiUtils.toggleKeyboard()
      }
      /* truck type */
      binding.spinnerTruckType.setup(R.array.array_truck_type) { p, v -> }
      binding.btnAction.setOnClickListener{
        searchLoad(true, origin, destination, binding.spinnerTruckType.selectedItem.toString(),null,null)
        }
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
          val origin = binding.editOriginCity.text.toString()
          binding.editOriginCity.setText(binding.editDestinationCity.text.toString())
          binding.editDestinationCity.setText(origin)
        }
            .setStartDelay(300)
            .start()
      }
          .start()
    }

    /* init */
    initObservers()
  }

   private fun AppCompatSpinner.setTruckDisplayNamesAdapter() {
    val truckDisplayAdapter =
      ArrayAdapter(this.context, layout.simple_spinner_item, truckDisplayNames).apply {
        setDropDownViewResource(layout.simple_spinner_dropdown_item)
        onItemSelectedListener = object : OnItemSelectedListener {
          override fun onItemSelected(
            p0: AdapterView<*>?,
            p1: View?,
            p2: Int,
            p3: Long
          ) {
            setHintColor(getItemAtPosition(p2).toString())
          }
          override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
      }
    adapter = truckDisplayAdapter
  }

  /**
   * Search load as per user selections
   */
  private fun searchLoad(
    saveToHistory: Boolean = true,
    origin: CityModel? = null,
    destination: CityModel? = null,
    truckType: String?,
    truckDisplayName: String?,
    status: String?
  ) {
    uiUtils.toggleKeyboard(true)
    if(contractType==getString(string.intracity_contract_type)){
      if(origin==null){
        binding.editReportingCenter.error="Please select reporting center"
        binding.editReportingCenter.errorAnimate()
        return
      }
      if(truckDisplayName!!.toLowerCase().contains("select")) {
        binding.spinnerTruckDisplayName.errorVibrate()
        return
      }
    }
    else{
      if (origin == null) {
        binding.editOriginCity.error = getString(string.error_search_missing_origin)
        binding.editOriginCity.errorAnimate()
        analyticsUtil.trackEvent(EVENT_SEARCH_ERROR, mutableListOf(), mutableListOf())
        return
      }

      if (truckType!!.toLowerCase().contains("choose")) {
        binding.spinnerTruckType.errorVibrate()
        return
      }
    }
    /* save to history if needed */
    if (saveToHistory) {
      viewModel.saveToHistory(
          origin, destination ?: CityModel(
          city = getString(string.label_anywhere),
          state = getString(string.label_anywhere)
      ),
          truckType,
        truckDisplayName,
        status,
        requestType,contractType
      )
    }
    /* searching progress */
    action(ProgressSearchLoadAction(true,if(requestType=="contract")"Searching contracts" else "Searching loads"))

    /* delay and search for better UX */
      Handler().postDelayed({
        action(SearchLoadAction(origin, destination, truckType,truckDisplayName, status, requestType,contractType,truckDisplayNames,saveToHistory))
      }, 200)
  }

  private fun showIntracityRelatedViews() {
    binding.selectReportingCenterHeader.visibility = View.VISIBLE
    binding.editReportingCenter.visibility = View.VISIBLE
    binding.btnSearch.visibility = View.VISIBLE
    binding.truckTypeHeader.visibility = View.VISIBLE
    binding.spinnerTruckDisplayName.visibility = View.VISIBLE
    binding.statusHeader.visibility = View.VISIBLE
    binding.spinnerStatus.visibility = View.VISIBLE
  }

  private fun showIntercityRelatedViews() {
    binding.tilOrigin.visibility = View.VISIBLE
    binding.tilDestination.visibility = View.VISIBLE
    binding.viewSelectOriginCity.visibility=View.VISIBLE
    binding.viewSelectDestinationCity.visibility=View.VISIBLE
    binding.imgForward.visibility = View.VISIBLE
    binding.spinnerTruckType.visibility = View.VISIBLE
    binding.btnAction.visibility = View.VISIBLE
  }

  private fun hideIntracityRelatedViews(){
    binding.selectReportingCenterHeader.visibility = View.GONE
    binding.editReportingCenter.visibility = View.GONE
    binding.btnSearch.visibility = View.GONE
    binding.truckTypeHeader.visibility = View.GONE
    binding.spinnerTruckDisplayName.visibility = View.GONE
    binding.statusHeader.visibility = View.GONE
    binding.spinnerStatus.visibility = View.GONE
  }

  private fun hideIntercityRelatedViews() {
    binding.tilOrigin.visibility = View.GONE
    binding.tilDestination.visibility = View.GONE
    binding.viewSelectOriginCity.visibility=View.GONE
    binding.viewSelectDestinationCity.visibility=View.GONE
    binding.imgForward.visibility = View.GONE
    binding.spinnerTruckType.visibility = View.GONE
    binding.btnAction.visibility = View.GONE
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
            searchLoad(false, item.originCity, item.destinationCity, item.truckType, item.truckDisplayName, item.status)
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