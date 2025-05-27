package com.delhivery.axle.ui.userroutes

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.data.home.routes.RoutesAction_DeleteRoute
import com.delhivery.axle.data.home.routes.RoutesAction_ViewDetails
import com.delhivery.axle.data.home.routes.RoutesAction_ViewOptions
import com.delhivery.axle.data.userroutes.UserRoutesWarningItem_NoMember
import com.delhivery.axle.data.userroutes.WarningAction_NoRoutes
import com.delhivery.axle.databinding.ActivityUserRoutesBinding
import com.delhivery.axle.databinding.DialogTeamMemberBottomOptionsBinding
import com.delhivery.axle.databinding.DialogUserRouteBottomOptionsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.dialogs.RouteDeleteDialog
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.AddNewRoute
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.DeleteRoute
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.axle.ui.selectroute.activity.SelectRouteOriginCityExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.REQCODE_ADD_ROUTES
import com.delhivery.axle.utils.REQCODE_DELETE_ROUTES
import com.delhivery.axle.utils.REQCODE_EDIT_ROUTE
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import okhttp3.Route

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 5/1/21
 */

class UserRoutesActivity : BaseActivity<ActivityUserRoutesBinding, UserRoutesViewModel>(),
    UserRoutesRVAdapterInterface {

  init {
    hasInlineProgress = true
  }
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  override fun getViewModelClass() = UserRoutesViewModel::class.java

  override fun layoutId() = R.layout.activity_user_routes

  override fun requireConnection() = true

  private val adapter: UserRoutesRVAdapter by lazy { UserRoutesRVAdapter(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("UserRoutesActivity_SetupTime")
    activitySetupTrace?.start()
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "My Routes"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    /* setup recycler view */
    binding.rvRoutes.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@UserRoutesActivity)
      adapter = this@UserRoutesActivity.adapter
    }

    viewModel.allRoutesLiveData.observe(this, Observer {
      it?.let {
        _items -> adapter.operation(_items)
      }
    })

    viewModel.emptyState.observe(this, Observer {
      if(it){
        binding.addRouteNewButton.visibility = View.GONE
      }else{
        binding.addRouteNewButton.visibility = View.VISIBLE
      }
    })

    viewModel.updatedLanes.observe(this, Observer {
      uiUtils.hideProgress()
      it?.let {
        if(it){
          refreshData()
          viewModel.updatedLanes.postValue(false)
        }
      }
    })

    binding.addRouteNewButton.setOnClickListener {
      navigationUtils.navigateForActivityResult(
          intent = manageRouteIntent(this@UserRoutesActivity,AddNewRoute),
          requestCode = REQCODE_ADD_ROUTES
      )
    }

    refreshData()
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }
  private fun refreshData() {
    adapter.clearItems()
    viewModel.fetchUserRoutes()
  }

  override fun handleAction(
    actionId: String,
    item: BaseUserRouteRVAdapterItem<*>
  ) {
    when (actionId) {
      RoutesAction_ViewDetails -> {
        val data  = item.data as RouteModel
        val bundle = Bundle()
        bundle.putSerializable(SelectedRouteIntentExtra, data)
        navigationUtils.navigateForActivityResult(
            intent = manageRouteIntent(this@UserRoutesActivity, EditRoute),
            requestCode = REQCODE_EDIT_ROUTE, extras = bundle
        )
      }
      RoutesAction_DeleteRoute -> {
        val data  = item.data as RouteModel
        val bundle = Bundle()
        bundle.putSerializable(SelectedRouteIntentExtra, data)
        navigationUtils.navigateForActivityResult(
          intent = manageRouteIntent(this@UserRoutesActivity, DeleteRoute),
          requestCode = REQCODE_DELETE_ROUTES, extras = bundle
        )
      }
      
      WarningAction_NoRoutes -> {
        navigationUtils.navigateForActivityResult(
          intent = manageRouteIntent(this@UserRoutesActivity,AddNewRoute),
          requestCode = REQCODE_ADD_ROUTES
        )
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseUserRouteRVAdapterItem<*>,
    position: Int
  ) {
    when(actionId){
      RoutesAction_ViewOptions -> showOptionsDialog( item.data as RouteModel, position)
    }
  }

  private fun showOptionsDialog(data: RouteModel, position: Int){
    val dialog = Dialog(this)
    val bindingDialog= DialogUserRouteBottomOptionsBinding.inflate(layoutInflater)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(bindingDialog.root)

    bindingDialog.closeBtn.setOnClickListener{
      dialog.dismiss()
    }
    if(adapter.itemCount==1){
      bindingDialog.deleteRouteLayout.visibility = View.GONE
    }else{
      bindingDialog.deleteRouteLayout.visibility = View.VISIBLE
    }
    bindingDialog.editRouteLayout.setOnClickListener {
      val bundle = Bundle()
      bundle.putSerializable(SelectedRouteIntentExtra, data)
      navigationUtils.navigateForActivityResult(
        intent = manageRouteIntent(this@UserRoutesActivity, EditRoute),
        requestCode = REQCODE_EDIT_ROUTE, extras = bundle
      )
      dialog.dismiss()
    }
    bindingDialog.deleteRouteLayout.setOnClickListener {
      val bundle = Bundle()
      bundle.putSerializable(SelectedRouteIntentExtra, data)
      navigationUtils.navigateForActivityResult(
        intent = manageRouteIntent(this@UserRoutesActivity, DeleteRoute),
        requestCode = REQCODE_DELETE_ROUTES, extras = bundle
      )
      dialog.dismiss()
    }

    dialog.show()
    dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
    dialog.window!!.setGravity(Gravity.BOTTOM)
  }


  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    refreshData()
  }

}

fun userRoutesIntent(
  context: Context
): Intent = Intent(context, UserRoutesActivity::class.java).apply {

}