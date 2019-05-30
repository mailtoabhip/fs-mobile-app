package com.delhivery.orion.ui.selectroute.fragments.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.R.string
import com.delhivery.orion.data.RouteModel
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.data.StateModelList
import com.delhivery.orion.databinding.FragmentSelectRouteDetailBinding
import com.delhivery.orion.databinding.ViewSelectRouteDestinationItemBinding
import com.delhivery.orion.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteBaseFragment
import com.delhivery.orion.utils.DialogUtils
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Displays the selected route detail information,
 * you can edit origin cities and destination cities
 *
 **
 */
class SelectRouteDetailFragment : SelectRouteBaseFragment<FragmentSelectRouteDetailBinding, SelectRouteDetailViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: SelectRouteDetailFragment by lazy { SelectRouteDetailFragment() }
  }

  var currentRoute: RouteModel? = null

  private var selectedStates = mutableSetOf<StateModel>()

  @Inject lateinit var dialogUtils: DialogUtils

  override fun getViewModelClass() = SelectRouteDetailViewModel::class.java

  override fun layoutId() = R.layout.fragment_select_route_detail

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    setHasOptionsMenu(true)
    super.onActivityCreated(savedInstanceState)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    (activity as SelectRouteActivity).title = "Route Detail"

    binding.route = currentRoute
    selectedStates.clear()
    selectedStates.addAll((currentRoute!!.destinations).toMutableSet())

    addDestinations()
  }

  private fun addDestinations() {
    StateModelList.forEach {
      val itemBinding = ViewSelectRouteDestinationItemBinding.inflate(
          layoutInflater, binding.containerDestination, false
      )

      itemBinding.state = it
      itemBinding.check.isChecked = selectedStates.contains(it)
      itemBinding.check.setOnCheckedChangeListener { _, checked ->
        if (itemBinding.check.isPressed) {
          if (checked) {
            selectedStates.add(it)
          } else {
            selectedStates.remove(it)
          }
        }
      }
      binding.containerDestination.addView(itemBinding.root)
    }
  }

  override fun onCreateOptionsMenu(
    menu: Menu?,
    inflater: MenuInflater?
  ) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater?.inflate(R.menu.menu_delete, menu);
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.nav_delete -> {
        dialogUtils.showBasicConfirmDialog(
            string.title_dialog_delete_route,
            string.msg_dialog_delete_route,
            positiveAction = getString(string.action_delete),
            negativeAction = getString(string.action_no_dont_delete)
        ) {
          it.dismiss()
          viewModel.deleteRoute();
        }
        return true
      }
      else ->
        return super.onOptionsItemSelected(item)
    }
  }
}