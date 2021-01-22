package com.delhivery.axle.ui.selectroute.fragments.routeslist

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.data.home.routes.RoutesAction_AddRoute
import com.delhivery.axle.data.home.routes.RoutesAction_ViewDetails
import com.delhivery.axle.databinding.FragmentSelectRouteListBinding
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.axle.ui.selectroute.fragments.AddMoreRoutesAction
import com.delhivery.axle.ui.selectroute.fragments.RouteDetailAction
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteBaseFragment

class SelectRouteListFragment : SelectRouteBaseFragment<FragmentSelectRouteListBinding, SelectRouteListViewModel>(),
    RoutesRVAdapterInterface {

  companion object {
    /* singleton instance */
    val _instance: SelectRouteListFragment by lazy { SelectRouteListFragment() }
  }

  override fun getViewModelClass() = SelectRouteListViewModel::class.java

  override fun layoutId() = R.layout.fragment_select_route_list

  /* routes */
  var routes = mutableListOf<RouteModel>()

  private val adapter by lazy {
    RoutesRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    (activity as SelectRouteActivity).title = ""

    binding.rvRoutes.apply {
      layoutManager =
        LinearLayoutManager(this@SelectRouteListFragment.context)
      adapter = this@SelectRouteListFragment.adapter
    }

    adapter.operation(
        mutableListOf<Pair<RoutesRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
          add(Pair(RoutesProgressItem(), AddUpdate))
        })
  }

  /**
   * Add routes
   */
  fun addRoutes() {
    adapter.clearItems()
    adapter.operation(
        mutableListOf<Pair<RoutesRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
          add(Pair(RoutesProgressItem(), Remove))
          add(Pair(RoutesAddItem(), Add))
          routes.forEach { _item ->
            add(Pair(RoutesRequestItem(_item), Add))
          }
        })
  }

  override fun handleAction(
    actionId: String,
    item: RoutesRVAdapterItem<*>
  ) {
    when (actionId) {
      RoutesAction_ViewDetails -> action(RouteDetailAction((item as RoutesRequestItem).data))
      RoutesAction_AddRoute -> action(AddMoreRoutesAction())
    }
  }
}