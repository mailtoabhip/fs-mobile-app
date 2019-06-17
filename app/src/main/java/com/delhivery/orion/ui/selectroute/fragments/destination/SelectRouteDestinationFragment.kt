package com.delhivery.orion.ui.selectroute.fragments.destination

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.delhivery.orion.R
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.data.StateModelList
import com.delhivery.orion.databinding.FragmentSelectRouteDestinationBinding
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.selectroute.fragments.DestinationSelectedAction
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteBaseFragment
import com.delhivery.orion.ui.selectroute.fragments.detail.DestinationsRVAdapter
import com.github.florent37.kotlin.pleaseanimate.core.position.PositionAnimExpectation

class SelectRouteDestinationFragment : SelectRouteBaseFragment<FragmentSelectRouteDestinationBinding, SelectRouteDestinationViewModel>(),
    ItemClickListener<StateModel> {

  companion object {
    /* singleton instance */
    val _instance: SelectRouteDestinationFragment by lazy { SelectRouteDestinationFragment() }
  }

  override fun getViewModelClass() = SelectRouteDestinationViewModel::class.java

  override fun layoutId() = R.layout.fragment_select_route_destination

  /* selected states */
  private var selectedStates = mutableSetOf<StateModel>()
  private var states = StateModelList.toMutableList()

  private val MINIMUM = 25
  var scrollDist = 0
  var visible = true

  private val adapter by lazy {
    DestinationsRVAdapter(this)
  }

  /* origin city */
  var originCity: CityModel? = null

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.origin = originCity

    states.forEach { state -> state.checked = true }
    /* clear all selections */
    selectedStates.addAll(states)

    binding.rvDestinations.apply {
      layoutManager = LinearLayoutManager(this@SelectRouteDestinationFragment.context)
      adapter = this@SelectRouteDestinationFragment.adapter
      addOnScrollListener(DestinationsRVScrollListener())
    }

    adapter.clearItems()
    adapter.operation(mutableListOf<Pair<StateModel, DataRVAdapterOperationType>>().apply {
      states.forEach { _item ->
        add(Pair(_item, Add))
      }
    })

    /* select all */
    binding.checkSelectAll.setOnCheckedChangeListener { v, selected ->
      v.post {
        if (!selected) {
          selectedStates.clear()
          adapter.itemsList()
              .forEach { state -> state.checked = false }
          adapter.notifyDataSetChanged()
          v.isEnabled = false
          hide()
        }
      }
    }

    /* submit clicked */
    binding.btnAction.setOnClickListener {
      when (selectedStates.size) {
        0 -> {
          hide()
          uiUtils.showSnackbar("Please select destination states", Snackbar.LENGTH_INDEFINITE)
        }
        else -> action(DestinationSelectedAction(selectedStates.toList()))
      }
    }

    binding.actionContainer.setOnClickListener {
      hide()
    }
  }

  override fun onItemClicked(item: StateModel) {
    uiUtils.dismissSnackbar()

    if (selectedStates.contains(item)) {
      selectedStates.remove(item)
      item.checked = false;
    } else {
      item.checked = true;
      selectedStates.add(item)
    }

    when (selectedStates.size) {
      0 -> {
        binding.checkSelectAll.isChecked = false
        binding.checkSelectAll.isEnabled = false
      }
      else -> {
        binding.checkSelectAll.isChecked = true
        binding.checkSelectAll.isEnabled = true
      }
    }

    adapter.updateItem(item)
  }

  fun hide() {
    binding.actionContainer.animate()
        .translationY(
            PositionAnimExpectation.dpToPx(
                this@SelectRouteDestinationFragment.context!!,
                binding.actionContainer.height.toFloat()
            )
        )
        .setInterpolator(AccelerateInterpolator(2f))
        .setDuration(100L)
        .start();
  }

  fun show() {
    binding.actionContainer.animate()
        .translationY(
            -PositionAnimExpectation.dpToPx(
                this@SelectRouteDestinationFragment.context!!, 0f
            )
        )
        .setInterpolator(DecelerateInterpolator(2f))
        .setDuration(300L)
        .start()
  }

  inner class DestinationsRVScrollListener() : OnScrollListener() {

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      if (visible && scrollDist > MINIMUM) {
        hide();
        scrollDist = 0;
        visible = false;
      } else if (!visible && scrollDist < -MINIMUM) {
        show();
        scrollDist = 0;
        visible = true;
      }

      if ((visible && dy > 0) || (!visible && dy < 0)) {
        scrollDist += dy;
      }
    }
  }
}