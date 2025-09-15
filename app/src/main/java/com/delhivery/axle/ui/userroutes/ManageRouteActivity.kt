package com.delhivery.axle.ui.userroutes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.databinding.ActivityManageRouteBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.searchcitystate.CityType
import com.delhivery.axle.ui.searchcitystate.HaveOldDestinations
import com.delhivery.axle.ui.searchcitystate.SelectedData
import com.delhivery.axle.ui.searchcitystate.searchCityIntent
import com.delhivery.axle.ui.searchcitystate.searchOriginCityIntent
import com.delhivery.axle.ui.searchcitystate.selectedCityStates
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.AddNewRoute
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.axle.utils.REQCODE_DESTINATION_SELECT_CITY
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.getSerializable
import com.delhivery.axle.utils.extensions.getSerializableExtra
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

class ManageRouteActivity : BaseActivity<ActivityManageRouteBinding, ManageRouteViewModel>() {

  @Inject
  lateinit var userPrefs: UserPrefs
  /* flow type */
  private var flowType: SelectRouteFlowType = AddNewRoute
  private var selectedData:RouteModel?=null
  var changedDestinationData = false
  var changedOriginData =false
  var startTime: Long = 0
  var endTime: Long = 0
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  override fun getViewModelClass() = ManageRouteViewModel::class.java

  override fun layoutId() = R.layout.activity_manage_route

  override fun requireConnection() = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("ManageRouteActivity_SetupTime")
    activitySetupTrace?.start()
    if( intent?.extras?.getSerializableExtra(SelectedRouteIntentExtra, RouteModel::class.java)!=null)
      selectedData = intent?.extras?.getSerializableExtra(SelectedRouteIntentExtra, RouteModel::class.java)
    /* flow type */
    try {
      flowType = intent?.getIntExtra(
        ManageRouteFlowTypeIntentExtra, AddNewRoute.typeId
      )?.let { SelectRouteFlowType.byTypeId(it) } ?: AddNewRoute
    } catch (e: Exception) {
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    startTime = System.currentTimeMillis()

    binding.editOrigin.setOnClickListener {
      val bundle = Bundle()
      bundle.putString(CityType,"origin")
      if(viewModel.selectedOrigin!=null){
        bundle.putString(SelectedData,viewModel.selectedOrigin!!.city)
      }
      navigationUtils.navigateForActivityResult(
        intent = searchOriginCityIntent(this@ManageRouteActivity),
        requestCode = REQCODE_SELECT_CITY, extras = bundle
      )
    }
    binding.editDestination.setOnClickListener {
      val bundle = Bundle()
      bundle.putString(CityType,"destination")
      if(binding.editDestination.text.isNullOrEmpty()){
        bundle.putBoolean(HaveOldDestinations,false)
      }else{
        bundle.putBoolean(HaveOldDestinations,true)
      }
      navigationUtils.navigateForActivityResult(
        intent = searchCityIntent(this@ManageRouteActivity),
        requestCode = REQCODE_DESTINATION_SELECT_CITY, extras = bundle
      )
    }


    if(flowType==AddNewRoute){
      binding.btnSubmitDetails.text = "Add Route"
    }else if(flowType==EditRoute){
      binding.btnSubmitDetails.text = "Edit Route"
    }else{
      binding.btnSubmitDetails.text = "Delete Route"
      binding.btnSubmitDetails.isEnabled = true
      binding.editDestination.isEnabled = false
      binding.editOrigin.isEnabled =false
    }
    binding.btnSubmitDetails.setOnClickListener{
      if(flowType==AddNewRoute){
        viewModel.addUserRouteDetails()
      }else if(flowType ==EditRoute){
        viewModel.editUserRouteDetails()
      }else{
        viewModel.deleteUserRouteDetails()
      }

    }

    viewModel.userAddRouteLiveData.observe(this, Observer {
      if (it) {
        setResult(Activity.RESULT_OK)
        finish()
      } else {
        uiUtils.showSnackbar("Update Failed, Please try again")
      }
    })
  }

  override fun onResume() {
    super.onResume()
    if(selectedData!=null && !changedOriginData){
      viewModel.selectedOrigin = CityModel(selectedData!!.origin.city,selectedData!!.origin.orion_db_city_code,"","",selectedData!!.origin.type?:"city")
      binding.editOrigin.setText(viewModel.selectedOrigin!!.cityName().trim())
      viewModel.oldOrigin =   viewModel.selectedOrigin
    }
    if(selectedData!=null&&changedOriginData){
      viewModel.oldOrigin =  CityModel(selectedData!!.origin.city,selectedData!!.origin.orion_db_city_code,"","",selectedData!!.origin.type?:"city")
    }
    if(selectedData!=null && !changedDestinationData){
      selectedCityStates = ArrayList<CityModel>()
      for(item in selectedData!!.destinations){
        val cityModel = CityModel(item.state,item.stateId,"",item.state,item.type?:"state")
        selectedCityStates.add(cityModel)
      }
      val citiesNames = ArrayList<String>()
      for(item in selectedCityStates){
        citiesNames.add(item.cityName())
      }
      binding.editDestination.setText(citiesNames.joinToString(separator = ", "))

    }
    if (activitySetupTrace != null && isFirstResume) {
          activitySetupTrace?.stop()
          isFirstResume = false
        }

  }
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQCODE_SELECT_CITY -> {
        if(data != null) {
          val type = data.getStringExtra(CityType)
          val city = data.getSerializable("City",CityModel::class.java)!!
          if(type =="origin") {
            viewModel.selectedOrigin = city
            changedOriginData = true
            binding.editOrigin.setText(city.cityName().trim())
            enableSubmit()
          }

        }
      }
      REQCODE_DESTINATION_SELECT_CITY -> {
        if(data != null) {
          val type = data.getStringExtra(CityType)
          val cities: ArrayList<CityModel> =
            data.getSerializable("City", ArrayList<CityModel>().javaClass)!!
          val citiesNames = ArrayList<String>()
          if(type =="destination") {
            for(item in cities){
              citiesNames.add(item.cityName())
            }
            changedDestinationData = true
            binding.editDestination.setText(citiesNames.joinToString(separator = ", "))
            enableSubmit()
          }

        }
      }
    }

    super.onActivityResult(requestCode, resultCode, data)
  }

  fun enableSubmit(){
    binding.btnSubmitDetails.isEnabled = !binding.editDestination.text.isNullOrEmpty() && !binding.editOrigin.text.isNullOrEmpty()
  }
}
/*  route basis flow */
const val ManageRouteFlowTypeIntentExtra = "manage_route_flow_type"

const val SelectedRouteIntentExtra = "selected_route_extra"

fun manageRouteIntent(
  context: Context,
  type: SelectRouteFlowType = AddNewRoute
): Intent = Intent(context, ManageRouteActivity::class.java).apply {
  putExtra(ManageRouteFlowTypeIntentExtra, type.typeId)
}