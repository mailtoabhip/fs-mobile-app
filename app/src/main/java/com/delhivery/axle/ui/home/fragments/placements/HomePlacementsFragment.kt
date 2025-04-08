package com.delhivery.axle.ui.home.fragments.placements

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.delhivery.axle.R
import com.delhivery.axle.data.home.placements.HomePlacementRequested_ViewDetails
import com.delhivery.axle.data.home.placements.HomePlacementsFilterDelay
import com.delhivery.axle.data.home.placements.HomePlacementsFilterExpected
import com.delhivery.axle.data.home.placements.HomePlacementsFilterMissing
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.data.home.placements.HomePlacementsTimeoutItemAction
import com.delhivery.axle.databinding.DialogDurationFiltersBinding
import com.delhivery.axle.databinding.FragmentHomePlacementsBinding
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.placementdetails.FilterDurationAdapter
import com.delhivery.axle.ui.placementdetails.FilterItemOnClickListener
import com.delhivery.axle.ui.placementdetails.REFRESH_ON_BACK_PLACEMENT
import com.delhivery.axle.ui.placementdetails.placementDetailsIntent
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_DELAYED_TAB
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_DEMAND_CARD_CLICKED
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_DETAIL_MISSING
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_EXPECTED_TAB
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_FILTER
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_TAB
import com.delhivery.axle.utils.PROPERTY_DEMAND_TYPE
import com.delhivery.axle.utils.PROPERTY_EXPECTED_TIME
import com.delhivery.axle.utils.PROPERTY_MISSING_FLAG
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject


/**
 * All bids screen on home
 */
class HomePlacementsFragment : HomeBaseFragment<FragmentHomePlacementsBinding, HomePlacementsViewModel>(),
    HomePlacementsRVAdapterInterface, TitleProvider, FilterItemOnClickListener
{

    private var _title: String = "Placements"
    @Inject lateinit var userPrefs: UserPrefs
    private var placementType: String = ""
    private var filterItem = ""
    private var currentItems = mutableListOf<BaseHomePlacementsRVAdapterItem<*>>()
    override val title: CharSequence
        get() = _title

    init {
        toolbarElevationLiveData = MutableLiveData()
        hasInlineProgress = true
    }

    companion object {
        /* singleton instance */
        val _instance: HomePlacementsFragment by lazy { HomePlacementsFragment() }
    }

    override fun getViewModelClass() = HomePlacementsViewModel::class.java

    override fun layoutId() = R.layout.fragment_home_placements

     /* RV adapter */
     private val adapter: HomePlacementsRVAdapter by lazy {
         HomePlacementsRVAdapter(this)
      }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        placementType = PlacementTypes.Delayed.name

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refreshData()
        }

        /* setup recycler view */
        binding.rvLoads.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomePlacementsFragment.adapter
       //     addOnScrollListener(HomeContractsRVScrollListener(binding.editStickySearch))
        //    addOnScrollListener(PaginationInterface())
        }
        binding.rvLoads.itemAnimator = null;

        viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
            isLoadingData = it ?: false
        })
        viewModel.userLoadsData.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items -> adapter.operation(_items) }
        })
        viewModel.userLoadsDataFetch.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items -> adapter.operation(_items) }
        })


        refreshData()
    }

    override fun onResume() {
        super.onResume()

        if (REFRESH_ON_BACK_PLACEMENT) {
            refreshData()
            REFRESH_ON_BACK_PLACEMENT = false
        }
    }
    private fun refreshData() {
        adapter.resetStaticData()
       viewModel.fetchPlacementLoads(placementType)
    }

    override fun handleAction(actionId: String, item: BaseHomePlacementsRVAdapterItem<*>) {
        when (actionId) {
            HomePlacementRequested_ViewDetails -> {
                val data = item.data as HomePlacementsItemData
                val missingDetails = data.vehicleNumber==null || data.driverName==null || data.driverPhone==null
                analyticsUtil.moEngageTrackEvent(
                        EVENT_HOME_PLACEMENT_DEMAND_CARD_CLICKED,
                        mutableListOf(
                                PROPERTY_USER_ID,
                                PROPERTY_PHONE_NO,
                                PROPERTY_DEMAND_TYPE,
                                PROPERTY_EXPECTED_TIME,
                                PROPERTY_MISSING_FLAG
                        ),
                        mutableListOf(
                                userPrefs.userId(),
                                userPrefs.phoneNumber?:"",
                                data.loadType?:"",
                                data.reportingTime?:"",
                                missingDetails.toString()
                        )
                )
                context?.let {
                    userPrefs.setPreviousScreen(this.javaClass.name)
                    startActivity(placementDetailsIntent(data, it))
                }
            }
            HomePlacementsFilterDelay ->{
                placementType = PlacementTypes.Delayed.name
                analyticsUtil.moEngageTrackEvent(
                        EVENT_HOME_PLACEMENT_DELAYED_TAB,
                        mutableListOf(
                                PROPERTY_USER_ID,
                                PROPERTY_PHONE_NO
                        ),
                        mutableListOf(
                                userPrefs.userId(),
                                userPrefs.phoneNumber?:""
                        )
                )
                refreshData()
            }
            HomePlacementsFilterExpected ->{

                placementType = PlacementTypes.Expected.name
                analyticsUtil.moEngageTrackEvent(
                        EVENT_HOME_PLACEMENT_EXPECTED_TAB,
                        mutableListOf(
                                PROPERTY_USER_ID,
                                PROPERTY_PHONE_NO
                        ),
                        mutableListOf(
                                userPrefs.userId(),
                                userPrefs.phoneNumber?:""
                        )
                )
                refreshData()
            }
            HomePlacementsFilterMissing ->{
                placementType = PlacementTypes.MissingDetails.name
                analyticsUtil.moEngageTrackEvent(
                        EVENT_HOME_PLACEMENT_DETAIL_MISSING,
                        mutableListOf(
                                PROPERTY_USER_ID,
                                PROPERTY_PHONE_NO
                        ),
                        mutableListOf(
                                userPrefs.userId(),
                                userPrefs.phoneNumber?:""
                        )
                )
                refreshData()
            }
            HomePlacementsTimeoutItemAction ->{
                refreshData()
            }
        }
    }

    override fun handleAction(
        actionId: String,
        item: BaseHomePlacementsRVAdapterItem<*>,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_call, menu)
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        // Hide the menu item
        menu.findItem(R.id.nav_call).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_filter -> {
                analyticsUtil.moEngageTrackEvent(
                        EVENT_HOME_PLACEMENT_FILTER,
                        mutableListOf(
                                PROPERTY_USER_ID,
                                PROPERTY_PHONE_NO
                        ),
                        mutableListOf(
                                userPrefs.userId(),
                                userPrefs.phoneNumber?:""
                        )
                )
                showDurationFilter()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun scrollToDuration() {
        try {
            val position  = adapter.itemsList().indexOfFirst { it.data.key()== filterItem+" hrs"
            }
            val smoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }

            smoothScroller.targetPosition = position
            binding.rvLoads.layoutManager?.startSmoothScroll(smoothScroller)

        }catch (e:Exception){
            Log.i("filter", "no bucket found")
        }

    }

    private fun showDurationFilter() {
        val dialog = Dialog(requireContext())
        val bindingDialog= DialogDurationFiltersBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        val dataItemList = ArrayList<Pair<String,Boolean>>()
        for (item in resources.getStringArray(R.array.array_duration).asList()){
            dataItemList.add(Pair(item,false))
        }
        val durationItemAdapter = FilterDurationAdapter(requireContext(),dataItemList, this@HomePlacementsFragment)
        bindingDialog.rvTime.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = durationItemAdapter
        }

        bindingDialog.cancel.setOnClickListener{
            dialog.dismiss()
        }

        bindingDialog.btnSubmit.setOnClickListener{
            scrollToDuration()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    override fun handleItemClick(item: Pair<String, Boolean?>) {
        filterItem = item.first.split(" ")[0]
    }
}
