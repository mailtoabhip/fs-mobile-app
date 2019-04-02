package com.delhivery.orion.ui.selectroute.fragments.routeslist

import android.os.Bundle
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.LinearLayoutCompat.LayoutParams
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.RouteModel
import com.delhivery.orion.databinding.FragmentSelectRouteListBinding
import com.delhivery.orion.databinding.ViewSelectRouteItemBinding
import com.delhivery.orion.ui.selectroute.fragments.AddMoreRoutesAction
import com.delhivery.orion.ui.selectroute.fragments.LoadRequestsAction
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteBaseFragment

class SelectRouteListFragment : SelectRouteBaseFragment<FragmentSelectRouteListBinding, SelectRouteListViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: SelectRouteListFragment by lazy { SelectRouteListFragment() }
  }

  override fun getViewModelClass() = SelectRouteListViewModel::class.java

  override fun layoutId() = R.layout.fragment_select_route_list

  /* routes */
  var routes = mutableListOf<RouteModel>()

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    /* go to load requests/home */
    binding.btnAction.setOnClickListener {
      action(LoadRequestsAction())
    }

    /* add more routes action */
    binding.cardAddMoreRoute.setOnClickListener {
      action(AddMoreRoutesAction())
    }

    /* add routes */
    addRoutes()
  }

  /**
   * Add routes
   */
  fun addRoutes() {
    binding.containerRoutes.removeAllViews()
    routes.forEach {
      val itemBinding = ViewSelectRouteItemBinding.inflate(
          layoutInflater, binding.containerRoutes, false
      )
      itemBinding.route = it
      binding.containerRoutes.addView(itemBinding.root)
    }
    /* fake view for last item shadow */
    val dummyView = View(context)
    val params = LinearLayoutCompat.LayoutParams(
        LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.size_16dp)
    )
    binding.containerRoutes.addView(dummyView, params)
  }
}