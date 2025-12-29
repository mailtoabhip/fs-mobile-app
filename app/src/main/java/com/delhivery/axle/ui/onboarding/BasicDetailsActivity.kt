package com.delhivery.axle.ui.onboarding

import android.graphics.Color
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityBasicDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.ui.searchcitystate.CityType
import com.delhivery.axle.ui.searchcitystate.HaveOldDestinations
import com.delhivery.axle.ui.searchcitystate.SelectedData
import com.delhivery.axle.ui.searchcitystate.searchCityIntent
import com.delhivery.axle.ui.searchcitystate.searchOriginCityIntent
import com.delhivery.axle.ui.searchcitystate.selectedCityStates
import com.delhivery.axle.utils.EVENT_SUBMITTED_ROUTES_TRUCKS
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_TTL
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.REQCODE_DESTINATION_SELECT_CITY
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import com.delhivery.axle.utils.StepKey
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.getSerializable
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

class BasicDetailsActivity: BaseActivity<ActivityBasicDetailsBinding, BasicDetailsViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#181818")
    }

    @Inject
    lateinit var userPrefs:UserPrefs

    var startTime: Long = 0
    var endTime: Long = 0
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    
    // Selection states
    private var selectedVendorType: String? = null
    private var selectedRouteType: String? = null
    private var selectedCity: CityModel? = null
    private val selectedTruckTypes = mutableSetOf<String>() // Can select multiple: "open", "closed", "trailer"
    
    // Truck views mapping for efficient updates
    private data class TruckViews(
        val checkbox: androidx.appcompat.widget.AppCompatImageButton,
        val image: androidx.appcompat.widget.AppCompatImageView,
        val text: androidx.appcompat.widget.AppCompatTextView
    )
    
    private val truckViewsMap by lazy {
        mapOf(
            "open" to TruckViews(binding.checkBoxOpenBody, binding.imgOpenTruck, binding.txtOpenBody),
            "closed" to TruckViews(binding.checkBoxContainer, binding.imgContainer, binding.txtContainer),
            "trailer" to TruckViews(binding.checkBoxTrailer, binding.imgTrailer, binding.txtTrailer)
        )
    }
    
    override fun getViewModelClass() = BasicDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_basic_details

    override fun requireConnection() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("BasicDetailsActivity_SetupTime")
        activitySetupTrace?.start()

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.progressStepLayout.toolbar)
    
        /* Handle window insets for edge-to-edge display (API 35+) */
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.progressStepLayout.toolbar)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationUtils.showProgressSteps(binding.progressStepLayout, 1)
        startTime = System.currentTimeMillis()
        
        setupBackPress()
        setupVendorTypeCards()
        setupRouteTypeCards()
        setupTruckSelection()
        setupLocationInputs()
        setupSubmitButton()
        loadSavedData()
    }
    
    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
                finishAffinity()
            }
        })
    }
    
    /**
     * Generic function to update card selection state
     * Reduces code duplication for vendor and route type cards
     */
    private fun updateCardSelection(
        card: com.google.android.material.card.MaterialCardView,
        titleView: android.widget.TextView,
        subtitleView: android.widget.TextView,
        iconView: android.widget.ImageView,
        isSelected: Boolean,
        selectedIcon: Int,
        unselectedIcon: Int
    ) {
        card.strokeColor = ContextCompat.getColor(
            this,
            if (isSelected) R.color.colorDelhiveryRed else R.color.light_grey
        )
        card.strokeWidth = if (isSelected) 4 else 2
        card.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isSelected) R.color.light_red_background else R.color.white
            )
        )
        titleView.setTextColor(
            ContextCompat.getColor(
                this,
                if (isSelected) R.color.colorDelhiveryRed else R.color.heading_black
            )
        )
        subtitleView.setTextColor(ContextCompat.getColor(this, R.color.sub_details_grey))
        iconView.setImageResource(if (isSelected) selectedIcon else unselectedIcon)
    }
    
    private fun setupVendorTypeCards() {
        binding.cardFleetOwner.setOnClickListener {
            selectedVendorType = "fleet_owner"
            updateVendorTypeSelection()
        }

        binding.cardBroker.setOnClickListener {
            selectedVendorType = "broker"
            updateVendorTypeSelection()
        }
    }
    
    private fun updateVendorTypeSelection() {
        // Update Fleet Owner card
        updateCardSelection(
            binding.cardFleetOwner,
            binding.tvFleetOwnerTitle,
            binding.tvFleetOwnerSubtitle,
            binding.imgFleetOwner,
            selectedVendorType == "fleet_owner",
            R.drawable.ic_icon_vehicle_red,
            R.drawable.ic_icon_vehicle_grey
        )

        // Update Broker card
        updateCardSelection(
            binding.cardBroker,
            binding.tvBrokerTitle,
            binding.tvBrokerSubtitle,
            binding.imgBroker,
            selectedVendorType == "broker",
            R.drawable.ic_team_members_red,
            R.drawable.ic_team_members
        )

        // Reset all selections when vendor type changes
        resetRouteTypeSelection()
        
        // Show route type section when vendor type is selected
        val routeTypeSection = binding.root.findViewById<android.widget.LinearLayout>(R.id.layoutRouteTypeSection)
        if (selectedVendorType != null) {
            routeTypeSection.visibility = android.view.View.VISIBLE
        } else {
            routeTypeSection.visibility = android.view.View.GONE
        }

        enableSubmit()
    }
    
    private fun resetRouteTypeSelection() {
        // Clear route type selection
        selectedRouteType = null
        
        // Reset route type card visuals using generic function
        updateCardSelection(
            binding.cardLocal,
            binding.tvLocalTitle,
            binding.tvLocalSubtitle,
            binding.imgLocal,
            false,
            R.drawable.ic_location_pincode_red,
            R.drawable.ic_location_pincode_grey
        )
        
        updateCardSelection(
            binding.cardNational,
            binding.tvNationalTitle,
            binding.tvNationalSubtitle,
            binding.imgNational,
            false,
            R.drawable.ic_route_red,
            R.drawable.ic_route_grey
        )
        
        // Hide route input section
        binding.layoutRouteAndTruckDetails.visibility = android.view.View.GONE
        
        // Clear all input data
        selectedCity = null
        selectedTruckTypes.clear()
        viewModel.selectedOrigin = null
        selectedCityStates.clear()
        
        // Clear all input fields
        val editLocalCity = binding.root.findViewById<com.delhivery.axle.ui.custom.DelhiveryOTPViewEditText>(R.id.edit_local_city)
        editLocalCity.setText("")
        binding.editOrigin.setText("")
        binding.editDestination.setText("")
        
        // Reset truck selections using optimized function
        clearTruckSelections()
    }
    
    private fun setupRouteTypeCards() {
        binding.cardLocal.setOnClickListener {
            selectedRouteType = "local"
            updateRouteTypeSelection()
            showRouteInputSection()
        }

        binding.cardNational.setOnClickListener {
            selectedRouteType = "national"
            updateRouteTypeSelection()
            showRouteInputSection()
        }
    }
    
    private fun updateRouteTypeSelection() {
        // Update Local card
        updateCardSelection(
            binding.cardLocal,
            binding.tvLocalTitle,
            binding.tvLocalSubtitle,
            binding.imgLocal,
            selectedRouteType == "local",
            R.drawable.ic_location_pincode_red,
            R.drawable.ic_location_pincode_grey
        )

        // Update National card
        updateCardSelection(
            binding.cardNational,
            binding.tvNationalTitle,
            binding.tvNationalSubtitle,
            binding.imgNational,
            selectedRouteType == "national",
            R.drawable.ic_route_red,
            R.drawable.ic_route_grey
        )

        // Clear truck selections when route type changes
        clearTruckSelections()

        enableSubmit()
    }
    
    private fun showRouteInputSection() {
        // Show the parent container
        binding.layoutRouteAndTruckDetails.visibility = android.view.View.VISIBLE

        when (selectedRouteType) {
            "local" -> {
                binding.layoutLocalRoute.visibility = android.view.View.VISIBLE
                binding.layoutNationalRoute.visibility = android.view.View.GONE
                // Clear national data
                binding.editOrigin.setText("")
                binding.editDestination.setText("")
                viewModel.selectedOrigin = null
                selectedCityStates.clear()
                
                // Ensure local city field is properly initialized
                val editLocalCity = binding.root.findViewById<com.delhivery.axle.ui.custom.DelhiveryOTPViewEditText>(R.id.edit_local_city)
                if (selectedCity != null) {
                    editLocalCity.setText(selectedCity!!.cityName().trim())
                }
            }
            "national" -> {
                binding.layoutLocalRoute.visibility = android.view.View.GONE
                binding.layoutNationalRoute.visibility = android.view.View.VISIBLE
                // Clear local data
                val editLocalCity = binding.root.findViewById<com.delhivery.axle.ui.custom.DelhiveryOTPViewEditText>(R.id.edit_local_city)
                editLocalCity.setText("")
                selectedCity = null
            }
            else -> {
                binding.layoutLocalRoute.visibility = android.view.View.GONE
                binding.layoutNationalRoute.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun setupTruckSelection() {
        // Setup click listeners for all truck types
        truckViewsMap.forEach { (truckType, views) ->
            val clickListener = android.view.View.OnClickListener { selectTruck(truckType) }
            views.checkbox.setOnClickListener(clickListener)
            views.image.setOnClickListener(clickListener)
            views.text.setOnClickListener(clickListener)
        }
    }
    
    /**
     * Helper function to update truck UI state
     */
    private fun updateTruckUI(truckType: String, isSelected: Boolean) {
        truckViewsMap[truckType]?.let { views ->
            views.checkbox.isSelected = isSelected
            views.image.isSelected = isSelected
            views.text.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (isSelected) R.color.dark_blue else R.color.heading_black
                )
            )
        }
    }
    
    private fun selectTruck(truckType: String) {
        // Toggle truck selection (multi-select)
        if (selectedTruckTypes.contains(truckType)) {
            selectedTruckTypes.remove(truckType)
        } else {
            selectedTruckTypes.add(truckType)
        }

        // Update all truck UIs efficiently
        truckViewsMap.keys.forEach { type ->
            updateTruckUI(type, selectedTruckTypes.contains(type))
        }

        // Update ViewModel
        viewModel.selectedTrucks.clear()
        viewModel.selectedTrucks.addAll(selectedTruckTypes)

        enableSubmit()
    }
    
    private fun clearTruckSelections() {
        // Clear the selected trucks set
        selectedTruckTypes.clear()
        
        // Reset all truck UI states efficiently
        truckViewsMap.keys.forEach { type ->
            updateTruckUI(type, false)
        }
        
        // Clear ViewModel
        viewModel.selectedTrucks.clear()
    }
    
    private fun setupLocationInputs() {
        // Local city input
        val editLocalCity = binding.root.findViewById<com.delhivery.axle.ui.custom.DelhiveryOTPViewEditText>(R.id.edit_local_city)
        editLocalCity.setOnClickListener {
            val bundle = Bundle()
            // Use "origin" type so SearchOriginCityActivity returns a result
            bundle.putString(CityType, "origin")
            if (selectedCity != null) {
                bundle.putString(SelectedData, selectedCity!!.city)
            }
            // Use a different request code to differentiate from national origin
            navigationUtils.navigateForActivityResult(
                intent = searchOriginCityIntent(this@BasicDetailsActivity),
                requestCode = REQCODE_SELECT_CITY,
                extras = bundle
            )
        }

        // Origin input
        binding.editOrigin.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CityType, "origin")
            if (viewModel.selectedOrigin != null) {
                bundle.putString(SelectedData, viewModel.selectedOrigin!!.city)
            }
            navigationUtils.navigateForActivityResult(
                intent = searchOriginCityIntent(this@BasicDetailsActivity),
                requestCode = REQCODE_SELECT_CITY,
                extras = bundle
            )
        }

        // Destination input
        binding.editDestination.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CityType, "destination")
            bundle.putBoolean(HaveOldDestinations, !binding.editDestination.text.isNullOrEmpty())
            navigationUtils.navigateForActivityResult(
                intent = searchCityIntent(this@BasicDetailsActivity),
                requestCode = REQCODE_DESTINATION_SELECT_CITY,
                extras = bundle
            )
        }
    }
    
    private fun setupSubmitButton() {
        val submitButton = binding.root.findViewById<android.widget.Button>(R.id.btn_submit_details)
        submitButton.setOnClickListener {
            viewModel.vendorType = selectedVendorType
            viewModel.routeType = selectedRouteType
            
            // Save vendor type and route type to SharedPreferences
            userPrefs.vendorType = selectedVendorType
            userPrefs.routeType = selectedRouteType

            // For local route type, set origin = destination = selected city
            if (selectedRouteType == "local" && selectedCity != null) {
                viewModel.selectedOrigin = selectedCity
                selectedCityStates.clear()
                selectedCityStates.add(selectedCity!!)
            }

            viewModel.updateUserDetails()
        }

        viewModel.userUpdateLiveData.observe(this, Observer {
            if (it) {
                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.moEngageTrackEvent(
                    EVENT_SUBMITTED_ROUTES_TRUCKS,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber ?: "dummy", ttl.toString())
                )

                val bundle = Bundle()
                bundle.putInt(StepKey, 0)
                navigationUtils.navigateKyc(this, true, bundle)
            } else {
                uiUtils.showSnackbar("Update Failed, Please try again")
            }
        })
    }
    
    private fun loadSavedData() {
        // Load and restore vendor type
        userPrefs.vendorType?.let { savedVendorType ->
            selectedVendorType = savedVendorType
            updateVendorTypeSelection()
        }
        
        // Load and restore route type
        userPrefs.routeType?.let { savedRouteType ->
            selectedRouteType = savedRouteType
            updateRouteTypeSelection()
            showRouteInputSection()
        }
        
        if (!userPrefs.getLanesPreference().isNullOrEmpty()) {
            if (userPrefs.getLanesPreference()!!.isNotEmpty()) {
                selectedCityStates = ArrayList<CityModel>()
                for ((i, item) in userPrefs.getLanesPreference()!!.withIndex()) {
                    if (item != null) {
                        if (i == 0) {
                            viewModel.selectedOrigin = CityModel(
                                item!!.origin.city,
                                item.origin.orion_db_city_code,
                                "",
                                "",
                                item.origin.type ?: "city"
                            )
                        }
                        if (viewModel.selectedOrigin!!.city.equals(item!!.origin.city)) {
                            val cityModel = CityModel(
                                item.destination.state,
                                item.destination.stateId,
                                "",
                                item.destination.state,
                                item.destination.type ?: "state"
                            )
                            selectedCityStates.add(cityModel)
                        }
                    }
                }
                
                // Restore city fields based on route type
                if (selectedRouteType == "local" && viewModel.selectedOrigin != null) {
                    // For local route, restore the single city field
                    selectedCity = viewModel.selectedOrigin
                    val editLocalCity = binding.root.findViewById<com.delhivery.axle.ui.custom.DelhiveryOTPViewEditText>(R.id.edit_local_city)
                    editLocalCity.setText(viewModel.selectedOrigin!!.cityName().trim())
                } else if (selectedRouteType == "national" && viewModel.selectedOrigin != null) {
                    // For national route, restore origin and destination fields
                    binding.editOrigin.setText(viewModel.selectedOrigin!!.cityName().trim())
                    val citiesNames = ArrayList<String>()
                    for (item in selectedCityStates) {
                        citiesNames.add(item.cityName())
                    }
                    binding.editDestination.setText(citiesNames.joinToString(separator = ", "))
                }

                if (userPrefs.truckTypes != null) {
                    // Select all truck types from the API response (comma-separated string)
                    val truckTypesList = userPrefs.truckTypes!!.split(",").map { it.trim() }
                    truckTypesList.forEach { truckType ->
                        if (truckType == "open") {
                            selectTruck("open")
                        } else if (truckType == "closed") {
                            selectTruck("closed")
                        } else if (truckType == "trailer") {
                            selectTruck("trailer")
                        }
                    }
                }
                enableSubmit()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQCODE_SELECT_CITY -> {
                if (data != null) {
                    val type = data.getStringExtra(CityType)
                    val city = data.getSerializable("City", CityModel::class.java)!!

                    if (type == "origin") {
                        // Check which route type is selected to determine which field to update
                        if (selectedRouteType == "local") {
                            // For local/intracity route type - update local city field
                            selectedCity = city
                            val editLocalCity = binding.root.findViewById<com.delhivery.axle.ui.custom.DelhiveryOTPViewEditText>(R.id.edit_local_city)
                            
                            // Set the text
                            val cityName = city.cityName().trim()
                            editLocalCity.setText(cityName)
                            
                            android.util.Log.d("BasicDetails", "Set LOCAL city: $cityName, text='${editLocalCity.text}'")
                        } else if (selectedRouteType == "national") {
                            // For national/intercity route type - update origin field
                            viewModel.selectedOrigin = city
                            binding.editOrigin.setText(city.cityName().trim())
                            android.util.Log.d("BasicDetails", "Set NATIONAL origin: ${city.cityName().trim()}")
                        }
                        enableSubmit()
                    }
                }
            }
            REQCODE_DESTINATION_SELECT_CITY -> {
                if (data != null) {
                    val type = data.getStringExtra(CityType)
                    val cities: ArrayList<CityModel> =
                        data.getSerializable("City", ArrayList<CityModel>().javaClass)!!
                    val citiesNames = ArrayList<String>()
                    if (type == "destination") {
                        for (item in cities) {
                            citiesNames.add(item.cityName())
                        }
                        binding.editDestination.setText(citiesNames.joinToString(separator = ", "))
                        enableSubmit()
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun enableSubmit() {
        val isVendorTypeSelected = selectedVendorType != null
        val isTruckSelected = selectedTruckTypes.isNotEmpty()

        val isLocationValid = when (selectedRouteType) {
            "local" -> {
                val editLocalCity = binding.root.findViewById<com.delhivery.axle.ui.custom.DelhiveryOTPViewEditText>(R.id.edit_local_city)
                !editLocalCity.text.isNullOrEmpty()
            }
            "national" -> !binding.editOrigin.text.isNullOrEmpty() &&
                    !binding.editDestination.text.isNullOrEmpty()
            else -> false
        }

        val submitButton = binding.root.findViewById<android.widget.Button>(R.id.btn_submit_details)
        val isEnabled = isVendorTypeSelected && isLocationValid && isTruckSelected
        
        submitButton.isEnabled = isEnabled
        
        // Set text color: white when enabled, grey when disabled
        submitButton.setTextColor(
            ContextCompat.getColor(
                this,
                if (isEnabled) R.color.white else R.color.sub_details_grey
            )
        )
    }

    /*override fun onBackPressed() {
        super.onBackPressed()
        finish()
        finishAffinity()
    }*/
}

