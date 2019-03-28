package com.delhivery.orion.ui.selectroute.fragments.destination

import android.os.Bundle
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.data.StateModelList
import com.delhivery.orion.databinding.FragmentSelectRouteDestinationBinding
import com.delhivery.orion.databinding.ViewSelectRouteDestinationItemBinding
import com.delhivery.orion.ui.selectroute.fragments.DestinationSelectedAction
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteBaseFragment

class SelectRouteDestinationFragment : SelectRouteBaseFragment<FragmentSelectRouteDestinationBinding, SelectRouteDestinationViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: SelectRouteDestinationFragment by lazy { SelectRouteDestinationFragment() }
  }

  override fun getViewModelClass() = SelectRouteDestinationViewModel::class.java

  override fun layoutId() = R.layout.fragment_select_route_destination

  /* selected states */
  private var selectedStates = mutableSetOf<StateModel>()

  /* origin city */
  var originCity: CityModel? = null

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.origin = originCity

    /* clear all selections */
    selectedStates.clear()

    /* add states */
    addStates()

    /* select all */
    binding.checkSelectAll.setOnCheckedChangeListener { v, selected ->
      v.post {
        selectedStates.clear()
        if (selected) {
          StateModelList.forEach { selectedStates.add(it) }
        }
        addStates()
      }
    }

    /* submit clicked */
    binding.btnAction.setOnClickListener {
      action(DestinationSelectedAction(selectedStates.toList()))
    }
  }

  /**
   * Add states to selection list
   */
  private fun addStates() {
    binding.containerDestinations.removeAllViews()
    StateModelList.forEach {
      val itemBinding = ViewSelectRouteDestinationItemBinding.inflate(
          layoutInflater, binding.containerDestinations, false
      )
      itemBinding.state = it
      itemBinding.check.isChecked = selectedStates.contains(it)
      itemBinding.check.setOnCheckedChangeListener { _, checked ->
        if (checked) {
          selectedStates.add(it)
        } else {
          selectedStates.remove(it)
        }
      }
      binding.containerDestinations.addView(itemBinding.root)
    }
  }
}