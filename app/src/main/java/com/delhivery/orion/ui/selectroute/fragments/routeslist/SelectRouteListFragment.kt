package com.delhivery.orion.ui.selectroute.fragments.routeslist

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.home.routes.RouteModel
import com.delhivery.orion.data.home.routes.RoutesAction_AddRoute
import com.delhivery.orion.data.home.routes.RoutesAction_ViewDetails
import com.delhivery.orion.databinding.FragmentSelectRouteListBinding
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.orion.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.orion.ui.selectroute.fragments.AddMoreRoutesAction
import com.delhivery.orion.ui.selectroute.fragments.RouteDetailAction
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteBaseFragment

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
      layoutManager = LinearLayoutManager(this@SelectRouteListFragment.context)
      adapter = this@SelectRouteListFragment.adapter
      ItemTouchHelper(
          SwipeToDeleteCallback(
              this@SelectRouteListFragment.context, this@SelectRouteListFragment.adapter
          )
      ).attachToRecyclerView(this)
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