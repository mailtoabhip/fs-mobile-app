package com.delhivery.axle.ui.searchload.fragments.searchload

import android.R.layout
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.ContractType
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
  private var truckDisplayNames = arrayListOf("Select Vehicle Type")

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    val activity: SearchLoadActivity? = activity as SearchLoadActivity?
    requestType = activity?.intentRequestType
    contractType = activity?.intentContractType
    binding.intracity = contractType!=null && contractType==ContractType.INTRACITY.type

    if(contractType==ContractType.INTRACITY.type){
      viewModel.fetchTruckDisplayNames()
    }

    viewModel.progressLiveData.observe(this, ProgressObserver())

    if(contractType==ContractType.INTRACITY.type){
      viewModel.loadingProgressLiveData.observe(this){
        if(it) uiUtils.showProgress()
        else uiUtils.hideProgress()
      }
      viewModel.truckDisplayNamesLiveData.observe(this){
          truckDisplayNames.clear()
          truckDisplayNames.add("Select Vehicle Type")
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
          if(contractType!=ContractType.INTRACITY.type)
          visible()
        }
        animate(binding.textHistoryTitle) toBe {
          if(contractType!=ContractType.INTRACITY.type)
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

  override fun onResume() {
    binding.editOriginCity.setText("")
    binding.editDestinationCity.setText("")
    binding.editReportingCenter.setText("")
    super.onResume()
  }

  private fun toggleVisibility(toggle: Boolean) {
    when (toggle) {
      true -> {
        binding.warningItem.visibility = View.VISIBLE
        binding.containerHistory.visibility = View.GONE
        binding.textHistoryTitle.visibility = View.GONE
        binding.containerSearchLoadHeaderForm.visibility = View.GONE
        binding.clIntracitySearch.visibility = View.GONE
      }
      false -> {
        binding.warningItem.visibility = View.GONE
        if(contractType!=null && contractType==ContractType.INTRACITY.type){
          binding.containerHistory.visibility = View.GONE
          binding.textHistoryTitle.visibility = View.GONE
        }else{
          binding.containerHistory.visibility = View.VISIBLE
          binding.textHistoryTitle.visibility = View.VISIBLE
        }
        binding.containerSearchLoadHeaderForm.visibility = View.VISIBLE
        binding.clIntracitySearch.visibility = View.VISIBLE
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
    if(contractType== ContractType.INTRACITY.type){
      binding.editReportingCenter.setText("")
      binding.checkBoxFixedIntracity.setOnCheckedChangeListener { _, isChecked ->
        binding.checkBoxFixedIntracity.isChecked = isChecked
      }
      binding.checkBoxFlexibleIntracity.setOnCheckedChangeListener { _, isChecked ->
        binding.checkBoxFlexibleIntracity.isChecked = isChecked
      }
      binding.spinnerStatus.apply{
        setup(R.array.array_status){ p, v ->
          if(p==0) setHintColor(v)
        }
      }
      autoCompleteUtils.autoCompleteCity(binding.editReportingCenter){
        origin = it
      }
      binding.btnSearch.setOnClickListener{
        if(binding.checkBoxFixedIntracity.isChecked|| binding.checkBoxFlexibleIntracity.isChecked){
          searchLoad(false, origin, destination, null, binding.spinnerTruckDisplayName.selectedItem.toString(),binding.spinnerStatus.selectedItem.toString(),if(binding.checkBoxFlexibleIntracity.isChecked&&binding.checkBoxFixedIntracity.isChecked)null else if (binding.checkBoxFixedIntracity.isChecked)false else if (binding.checkBoxFlexibleIntracity.isChecked) true else null, true)
        }else{
          uiUtils.showToast("Please select contract type")
        }

      }
    }
    else{
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
          override fun onNothingSelected(parent: AdapterView<*>?) {
            return
          }
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
    contractStatus: String?,
    isFlexible: Boolean?=null,
    includeFlexibleContracts:Boolean?=null
  ) {
    uiUtils.toggleKeyboard(true)
    if(contractType== ContractType.INTRACITY.type){
      if(origin==null){
        binding.editReportingCenter.error="Please select reporting center"
        binding.editReportingCenter.errorAnimate()
        return
      }
      if(truckDisplayName!!.lowercase().contains("select")) {
        binding.spinnerTruckDisplayName.errorVibrate()
        return
      }
    }
    else{
      if (origin == null) {
        binding.editOriginCity.error = getString(R.string.error_search_missing_origin)
        binding.editOriginCity.errorAnimate()
        analyticsUtil.trackEvent(EVENT_SEARCH_ERROR, mutableListOf(), mutableListOf())
        return
      }

      if (truckType!!.lowercase().contains("choose")) {
        binding.spinnerTruckType.errorVibrate()
        return
      }
    }
    /* save to history if needed */
    if (saveToHistory) {
      viewModel.saveToHistory(
          origin, destination ?: CityModel(
          city = getString(R.string.label_anywhere),
          state = getString(R.string.label_anywhere)
      ),
          truckType,
        truckDisplayName,
        contractStatus,
        requestType,contractType
      )
    }
    /* searching progress */
    action(ProgressSearchLoadAction(true,if(requestType=="contract")"Searching contracts" else "Searching loads"))

    /* delay and search for better UX */
      Handler(Looper.myLooper()!!).postDelayed({
        action(SearchLoadAction(origin, destination, truckType,truckDisplayName, contractStatus, requestType,contractType,truckDisplayNames,saveToHistory,isFlexible,includeFlexibleContracts))
      }, 200)
  }

  /**
   * init observers
   */
  private fun initObservers() {
    /* observe live data for search history */
    viewModel.searchLoadHistoryLiveData(requestType,contractType)
        ?.observe(viewLifecycleOwner, SearchLoadHistoryObserver())
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
      if(contractType!=ContractType.INTRACITY.type)
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