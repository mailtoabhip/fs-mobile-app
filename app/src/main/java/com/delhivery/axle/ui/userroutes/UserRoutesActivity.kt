package com.delhivery.axle.ui.userroutes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.data.home.routes.RoutesAction_DeleteRoute
import com.delhivery.axle.data.home.routes.RoutesAction_ViewDetails
import com.delhivery.axle.databinding.ActivityUserRoutesBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.RouteDeleteDialog
import com.delhivery.axle.ui.selectroute.SelectRouteFlowType.EditRoute
import com.delhivery.axle.ui.selectroute.activity.SelectRouteOriginCityExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.REQCODE_ADD_ROUTES
import com.delhivery.axle.utils.REQCODE_EDIT_ROUTE

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 5/1/21
 */

class UserRoutesActivity : BaseActivity<ActivityUserRoutesBinding, UserRoutesViewModel>(),
    UserRoutesRVAdapterInterface {

  init {
    hasInlineProgress = true
  }

  override fun getViewModelClass() = UserRoutesViewModel::class.java

  override fun layoutId() = R.layout.activity_user_routes

  override fun requireConnection() = true

  private val adapter: UserRoutesRVAdapter by lazy { UserRoutesRVAdapter(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Your Route Preferences"
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

    viewModel.updatedLanes.observe(this, Observer {
      uiUtils.hideProgress()
      it?.let {
        if(it){
          refreshData()
          viewModel.updatedLanes.postValue(false)
        }
      }
    })

    binding.fabAddRoute.setOnClickListener {
      navigationUtils.navigateForActivityResult(
          intent = selectRouteIntent(this@UserRoutesActivity),
          requestCode = REQCODE_ADD_ROUTES
      )
    }

    refreshData()
  }

  private fun refreshData() {
    adapter.resetStaticData()
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
        bundle.putString(SelectRouteOriginCityExtra, data.origin.orion_db_city_code)
        navigationUtils.navigateForActivityResult(
            intent = selectRouteIntent(this@UserRoutesActivity, EditRoute),
            requestCode = REQCODE_EDIT_ROUTE, extras = bundle
        )
      }
      RoutesAction_DeleteRoute -> {
        RouteDeleteDialog(this, item.data as RouteModel , viewModel, uiUtils).show()
      }
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseUserRouteRVAdapterItem<*>,
    position: Int
  ) {
    TODO("Not yet implemented")
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