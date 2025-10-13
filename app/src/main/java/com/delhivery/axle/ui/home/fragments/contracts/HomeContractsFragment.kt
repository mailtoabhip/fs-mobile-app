package com.delhivery.axle.ui.home.fragments.contracts

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.ContractType
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.api.repository.UserTripsLoadLimit
import com.delhivery.axle.data.home.bids.HomeBidsRequestAction_ViewDetails
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.data.home.contracts.*
import com.delhivery.axle.data.home.loads.HomeLoadsTimeOutAction
import com.delhivery.axle.data.home.loads.HomeLoadsVehicleFilterAction
import com.delhivery.axle.data.home.loads.HomeLoadsWarningAction_NoLoads
import com.delhivery.axle.data.home.trips.HomeTripsSearchAction_Search
import com.delhivery.axle.databinding.DialogBottomVehicleFilterBinding
import com.delhivery.axle.databinding.DialogContractsTypeInfoBinding
import com.delhivery.axle.databinding.FragmentHomeContractsBinding
import com.delhivery.axle.ui.contractDetails.contractDetailsIntent
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsSearchItem
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.delhivery.axle.ui.searchload.SearchLoadActivity
import com.delhivery.axle.ui.searchload.searchLoadContractsIntent
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

class HomeContractsFragment :HomeLoadsTruckBaseFragment<FragmentHomeContractsBinding,HomeContractsViewModel>(),
  HomeContractsRVAdapterInterface, TitleProvider {

  var _title: String = "Contracts"

  override val title: CharSequence
    get() = _title

  @Inject lateinit var dialogUtils: DialogUtils
  @Inject lateinit var fcmUtils: FCMUtils
  @Inject lateinit var userPrefs: UserPrefs

  var visible = false
  var demandType: String = ""
  var contractType:String? = null
  var isflexible:Boolean? = null
  var includeFlexibleContract:Boolean? = null
  var pos = 0
  private var fragmentSetupTrace: Trace? = null
  private var isFirstResume = true


  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomeContractsFragment by lazy { HomeContractsFragment() }
  }

  override fun getViewModelClass() = HomeContractsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_contracts

  /* RV adapter */
  private val adapter: HomeContractsRVAdapter by lazy {
    HomeContractsRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeContractsFragment_SetupTime")
    fragmentSetupTrace?.start()
    demandType= if(userPrefs.demandType.contains(DemandType.Intracity.type)&& userPrefs.contractDemand) {DemandType.Intracity.type} else if(userPrefs.demandType.contains(DemandType.Internal.type)&&userPrefs.contractDemand){ DemandType.Corporate.type }else if (userPrefs.demandType.contains(DemandType.Others.type)){DemandType.Corporate.type} else{DemandType.Corporate.type}
    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    /* setup recycler view */
    binding.rvLoads.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeContractsFragment.adapter
   //   addOnScrollListener(HomeContractsRVScrollListener(binding.editStickySearch))
      addOnScrollListener(PaginationInterface())
    }

    viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
      isLoadingData = it ?: false
    })

    binding.rvLoads.setItemAnimator(null);

    viewModel.userLoadsData.reobserve(viewLifecycleOwner, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })
    viewModel.userLoadsDataFetch.reobserve(viewLifecycleOwner, Observer {
      it?.let { _items -> adapter.operation(_items) }
    })
    viewModel.contractsCountLiveData.reobserve(viewLifecycleOwner, Observer {
      HomeLoadsTruckFragment._instance.dataToUpdate("contracts",it>0,it)
    })

    refreshData()
  }



  override fun onResume() {
    super.onResume()
    viewModel.paginateCount = 0
    if (viewModel.fromNotification || REFRESH_ON_BACK) {
      refreshData()
      viewModel.fromNotification = false
      REFRESH_ON_BACK = false
    }
    if (fragmentSetupTrace != null && isFirstResume) {
      fragmentSetupTrace?.stop()
      isFirstResume = false
    }

  }

  override fun onStop() {
    super.onStop()
    viewModel.paginateCount = 0
  }

  override fun onPause() {
    super.onPause()
    viewModel.paginateCount = 0
  }

  private fun refreshData() {
    viewModel.paginateCount = 0
    viewModel.hasOrionLoadOnce = false
    adapter.resetStaticData()
    viewModel.fetchUserTransactions(false, demandType,contractType,isflexible,includeFlexibleContract)
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeContractsRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomeBidsRequestAction_ViewDetails -> {
        val data = item.data as HomeBidsRequestItemData

        context?.let {
          userPrefs.setPreviousScreen(this.javaClass.name)
          startActivity(contractDetailsIntent(data.key(), it,VALUE_ORDER_LISTING))
        }
      }
      HomeContractsSearchAction_Search -> {
        context?.let {
          startActivity(
            Intent(searchLoadContractsIntent(it,"contract",if(demandType=="Intracity") ContractType.INTRACITY.type else ContractType.LH_FTL.type))
          )
        }
      }
      HomeContractsWarningAction_NoLoads -> {
       refreshData()
      }

      HomeContractsTimeOutAction -> {
        refreshData()
      }
      HomeLoadsVehicleFilterAction -> {
        showVehicleFilterDialog()
      }
      HomeContractsFilterExpress -> {
        // Merged functionality: now handles both Internal (express) and Corporate (non-express) demand types
        // This will show all intercity contracts (both LH_FTL and FRC types)
        demandType = DemandType.Corporate.type // Use Corporate to represent intercity contracts
        contractType = null
        isflexible = null
        includeFlexibleContract= null
        refreshData()
      }
      HomeContractsFilterIntracity -> {
        demandType = DemandType.Intracity.type
        contractType = ContractType.INTRACITY.type
        includeFlexibleContract = true
        isflexible = null
        refreshData()
      }
      HomeContractsIntracityFilterFixed -> {
        demandType = DemandType.Intracity.type
        contractType = ContractType.INTRACITY.type
        isflexible = false
        includeFlexibleContract = true
        refreshData()
      }
      HomeContractsIntracityFilterFlexible -> {
        demandType = DemandType.Intracity.type
        contractType = ContractType.INTRACITY.type
        isflexible = true
        includeFlexibleContract = true
        refreshData()
      }
      HomeContractsIntracityFilterAll -> {
        demandType = DemandType.Intracity.type
        contractType = ContractType.INTRACITY.type
        isflexible = null
        includeFlexibleContract = true
        refreshData()
      }
      HomeContractsFilterInfo -> {
        infoDialog()
      }
    }
  }

  private fun infoDialog() {
    if(activity!=null) {
      val dialog = Dialog(requireActivity())
      val bindingDialog = DialogContractsTypeInfoBinding.inflate(requireActivity().layoutInflater)
      bindingDialog.buttonCancel.setOnClickListener {
        dialog.cancel()
      }
      bindingDialog.rule1.text =   HtmlCompat.fromHtml(getString(R.string.non_express_load_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
      bindingDialog.rule2.text =   HtmlCompat.fromHtml(getString(R.string.express_load_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
      bindingDialog.rule3.text =   HtmlCompat.fromHtml(getString(R.string.dlv_intracity_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
      bindingDialog.rule4.text =   HtmlCompat.fromHtml(getString(R.string.dlv_intracity_fixed_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
      bindingDialog.rule5.text =   HtmlCompat.fromHtml(getString(R.string.dlv_intracity_flexible_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      dialog.setContentView(bindingDialog.root)
      dialog.show()
      dialog.window!!.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
    }
  }

  private fun showVehicleFilterDialog() {
    val dialog = Dialog(requireContext())
    val bindingDialog = DialogBottomVehicleFilterBinding.inflate(layoutInflater)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(bindingDialog.root)

    // Get current filter selection
    var currentVehicleFilterList = listOf<String>()
    currentVehicleFilterList = if (viewModel.passing_vehicle_type != null && viewModel.passing_vehicle_type!!.isNotEmpty()) {
      viewModel.passing_vehicle_type!!.split(",")
    } else if (userPrefs.truckTypes != null) {
      userPrefs.truckTypes!!.split(",")
    } else {
      listOf()
    }

    // Set initial checkbox states based on current filter
    bindingDialog.checkboxOpen.isChecked = currentVehicleFilterList.contains("open")
    bindingDialog.checkboxClosed.isChecked = currentVehicleFilterList.contains("closed")
    bindingDialog.checkboxTrailer.isChecked = currentVehicleFilterList.contains("trailer")

    // Close button listener
    bindingDialog.closeBtn.setOnClickListener {
      dialog.dismiss()
    }

    // Individual checkbox click listeners for better UX
    bindingDialog.openVehicleLayout.setOnClickListener {
      bindingDialog.checkboxOpen.isChecked = !bindingDialog.checkboxOpen.isChecked
    }

    bindingDialog.closedVehicleLayout.setOnClickListener {
      bindingDialog.checkboxClosed.isChecked = !bindingDialog.checkboxClosed.isChecked
    }

    bindingDialog.trailerVehicleLayout.setOnClickListener {
      bindingDialog.checkboxTrailer.isChecked = !bindingDialog.checkboxTrailer.isChecked
    }

    // Apply filter button
    bindingDialog.btnApplyFilter.setOnClickListener {
      val selectedVehicleTypes = mutableListOf<String>()
      
      if (bindingDialog.checkboxOpen.isChecked) {
        selectedVehicleTypes.add("open")
      }
      if (bindingDialog.checkboxClosed.isChecked) {
        selectedVehicleTypes.add("closed")
      }
      if (bindingDialog.checkboxTrailer.isChecked) {
        selectedVehicleTypes.add("trailer")
      }

      viewModel.vehicleStr = selectedVehicleTypes.joinToString(separator = ",") { it }
      viewModel.filterVehicleType = true
      refreshData()
      dialog.dismiss()
    }

    // Clear filter button
    bindingDialog.btnClearFilter.setOnClickListener {
      bindingDialog.checkboxOpen.isChecked = false
      bindingDialog.checkboxClosed.isChecked = false
      bindingDialog.checkboxTrailer.isChecked = false
      
      viewModel.vehicleStr = null
      viewModel.filterVehicleType = null
      refreshData()
      dialog.dismiss()
    }

    // Set dialog properties for bottom sheet appearance
    dialog.show()
    dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
    dialog.window!!.setGravity(Gravity.BOTTOM)
  }

  override fun handleAction(
    actionId: String,
    item: BaseHomeContractsRVAdapterItem<*>,
    position: Int
  ) {
  }

  /**
   * Home contracts rv scroll listener for search bar animation related stuff
   */
  inner class HomeContractsRVScrollListener(
    private val stickyView: DelhiveryAnimatedSearchBar,
    private val elevation: Float = 12f
  ) : OnScrollListener() {
    /* Current toolbar elevation */
    private var toolbarElevation = -1f

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      val layoutManager: LinearLayoutManager? = recyclerView.layoutManager as? LinearLayoutManager
      val pos = layoutManager?.findFirstVisibleItemPosition()
      val toolbarElevation = if (pos == 0) {
        val childView = recyclerView.findViewHolderForAdapterPosition(0)!!.itemView
        val viewTopGap = childView.height - stickyView.height * 1f
        val viewTop = childView.top + viewTopGap
        if (viewTop > 0) {
          val factor = viewTop / viewTopGap
          val invFactor = 1f - factor
          stickyView.translationY = viewTop
          stickyView.alpha = invFactor
          ViewCompat.setElevation(stickyView, elevation * invFactor)
        } else {
          stickyView.translationY = stickyView.top * 1f
          stickyView.alpha = 1f
          ViewCompat.setElevation(stickyView, elevation)
        }
        val factor =
          (childView.height.toFloat() - childView.bottom.toFloat()) / childView.height.toFloat()
        stickyView.setRatio((1 - factor))
        defToolbarElevation
      } else {
        stickyView.translationY = 0f
        stickyView.alpha = 1f
        stickyView.setRatio(0f)
        0f
      }
      if (toolbarElevation != this.toolbarElevation && toolbarElevationLiveData != null) {
        this.toolbarElevation = toolbarElevation
        toolbarElevationLiveData?.postValue(this.toolbarElevation)
      }
    }
  }


  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(UserTripsLoadLimit) {
    override fun loadMore() = viewModel.fetchUserTransactions(true, demandType, contractType, isflexible, includeFlexibleContract)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

}
 var REFRESH_ON_BACK = false