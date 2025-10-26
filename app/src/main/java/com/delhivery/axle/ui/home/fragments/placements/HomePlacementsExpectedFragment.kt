package com.delhivery.axle.ui.home.fragments.placements

import android.Manifest
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomePlacementsExpectedBinding
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.data.home.placements.HomePlacementRequested_ViewDetails
import com.delhivery.axle.data.home.placements.HomePlacementsCallDriver
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.data.home.placements.HomePlacementsShareOnWhatsapp
import com.delhivery.axle.data.home.placements.HomePlacementsTimeoutItemAction
import com.delhivery.axle.databinding.DialogDurationFiltersBinding
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.biddetails.placementsBidDetailsIntent
import com.delhivery.axle.ui.contractDetails.contractDetailsIntent
import com.delhivery.axle.ui.contractDetails.placementsContractDetailsIntent
import com.delhivery.axle.ui.placementdetails.placementDetailsIntent
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsRVAdapterInterface
import com.delhivery.axle.ui.placementdetails.FilterDurationAdapter
import com.delhivery.axle.ui.placementdetails.FilterItemOnClickListener
import com.delhivery.axle.ui.placementdetails.REFRESH_ON_BACK_PLACEMENT
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_EXPECTED_TAB
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_DEMAND_CARD_CLICKED
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_FILTER
import com.delhivery.axle.utils.LoadTypeUtils
import com.delhivery.axle.utils.PROPERTY_DEMAND_TYPE
import com.delhivery.axle.utils.PROPERTY_EXPECTED_TIME
import com.delhivery.axle.utils.PROPERTY_MISSING_FLAG
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Expected placements fragment
 */
class HomePlacementsExpectedFragment : HomeBaseFragment<FragmentHomePlacementsExpectedBinding, HomePlacementsViewModel>(),
    HomePlacementsRVAdapterInterface, FilterItemOnClickListener {

    @Inject lateinit var userPrefs: UserPrefs
    @Inject lateinit var dialogUtils: DialogUtils
    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true
    private var filterItem = ""

    companion object {
        /* singleton instance */
        val _instance: HomePlacementsExpectedFragment by lazy { HomePlacementsExpectedFragment() }
    }


    override fun getViewModelClass() = HomePlacementsViewModel::class.java

    override fun layoutId() = R.layout.fragment_home_placements_expected

    /* RV adapter */
    private val adapter: HomePlacementsRVAdapter by lazy {
        HomePlacementsRVAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomePlacementsExpectedFragment_SetupTime")
        fragmentSetupTrace?.start()

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refreshData()
        }

        /* setup recycler view */
        binding.rvLoads.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomePlacementsExpectedFragment.adapter
        }
        binding.rvLoads.itemAnimator = null

        viewModel.dataLoadingLiveData.reobserve(viewLifecycleOwner, Observer {
            isLoadingData = it ?: false
        })

        viewModel.userLoadsData.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items -> adapter.operation(_items) }
        })

        viewModel.userLoadsDataFetch.reobserve(viewLifecycleOwner, Observer {
            it?.let { _items -> adapter.operation(_items) }
        })

        // Track analytics for expected tab
        analyticsUtil.moEngageTrackEvent(
            EVENT_HOME_PLACEMENT_EXPECTED_TAB,
            mutableListOf(
                PROPERTY_USER_ID,
                PROPERTY_PHONE_NO
            ),
            mutableListOf(
                userPrefs.userId(),
                userPrefs.phoneNumber ?: ""
            )
        )

        refreshData()
    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
        if(REFRESH_ON_BACK_PLACEMENT){
            refreshData()
        }
    }

    private fun refreshData() {
        adapter.resetStaticData()
        viewModel.fetchPlacementLoads(PlacementTypes.Expected.name)
    }

    override fun handleAction(actionId: String, item: BaseHomePlacementsRVAdapterItem<*>) {
        when (actionId) {
            HomePlacementRequested_ViewDetails -> {
                val data = item.data as HomePlacementsItemData
                val missingDetails = data.vehicleNumber == null || data.driverName == null || data.driverPhone == null
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
                        userPrefs.phoneNumber ?: "",
                        data.loadType ?: "",
                        data.reportingTime ?: "",
                        missingDetails.toString()
                    )
                )

                userPrefs.setPreviousScreen(this.javaClass.name)
                if(data.loadType==LoadTypes.orionSpot.name  || data.loadType==LoadTypes.intracityAdhoc.name || data.loadType==LoadTypes.ftlAdhoc.name ||data.loadType==LoadTypes.orionFixed.name){
                    val transactionId = data.transactionId
                    val contractCode = data.contractCode
                    val context = this.context
                    Log.d("transactionId", "$transactionId")
                    Log.d("contractCode", "$contractCode")
                    if ((transactionId.isNotNullOrEmpty() || contractCode.isNotNullOrEmpty()) && context != null) {
                        val intent = placementsBidDetailsIntent(placementType = LoadTypeUtils.getLoadType(data.loadType?:"N/A"), transactionId = transactionId, contractCode = contractCode, context, forPlacement = true, homePlacementsItemData = data)
                        startActivity(intent)
                    }
                }else if(data.loadType==LoadTypes.intracityRegular.name || data.loadType==LoadTypes.ftlRegular.name ){
                    val transactionId = data.transactionId
                    val contractCode = data.contractCode
                    val context = this.context
                    if ((transactionId.isNotNullOrEmpty() || contractCode.isNotNullOrEmpty()) && context != null) {
                        val intent = placementsContractDetailsIntent(placementType = LoadTypeUtils.getLoadType(data.loadType?:"N/A"), transactionId = transactionId, contractCode = contractCode, context, forPlacement = true, homePlacementsItemData = data)
                        startActivity(intent)
                    }
                }

            }
            HomePlacementsCallDriver ->{
                val data = item.data as HomePlacementsItemData
                callDriver(data.driverPhone?:"")
            }
            HomePlacementsShareOnWhatsapp ->{
                val data = item.data as HomePlacementsItemData
                dialogUtils.shareOnWhatsApp(dialogUtils.generatePlacementWhatsappContent(data))

            }
            HomePlacementsTimeoutItemAction -> {
                refreshData()
            }
        }
    }
    private fun callDriver(phoneNumber:String?){
        compositeDisposable += requestPermission(
            Manifest.permission.CALL_PHONE
        )
            .onBackground()
            .subscribe { granted, error ->
                if (error == null && granted) {
                    when (phoneNumber?.let { it1 ->
                        contactUtils.callDriver(
                            it1
                        )
                    }) {
                        false -> {
                            uiUtils.showSnackbar("Unable to place call")
                        }
                        else -> {
                        }
                    }
                } else {
                    uiUtils.showSnackbar(getString(R.string.msg_call_permission))
                }
            }
    }

    private fun scrollToDuration() {
        try {
            val position  = adapter.itemsList().indexOfFirst { it.data.key()== "Expected in "+filterItem+" hrs"
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
                        userPrefs.phoneNumber ?: ""
                    )
                )
                Log.i("onOptionsItemSelected", "Clicked")

                // Filter functionality can be implemented in child fragments if needed
                showDurationFilter()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
    private fun showDurationFilter() {
        Log.i("showDurationFilter", "Clicked")
        val dialog = Dialog(requireContext())
        val bindingDialog= DialogDurationFiltersBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        val dataItemList = ArrayList<Pair<String,Boolean>>()
        for (item in resources.getStringArray(R.array.array_duration).asList()){
            dataItemList.add(Pair(item,false))
        }
        val durationItemAdapter = FilterDurationAdapter(requireContext(),dataItemList, this@HomePlacementsExpectedFragment)
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

    override fun handleAction(
        actionId: String,
        item: BaseHomePlacementsRVAdapterItem<*>,
        position: Int
    ) {
        // Not implemented for this fragment
    }
} 