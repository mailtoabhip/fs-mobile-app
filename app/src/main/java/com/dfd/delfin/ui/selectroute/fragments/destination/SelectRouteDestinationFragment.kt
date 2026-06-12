package com.dfd.delfin.ui.selectroute.fragments.destination

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dfd.delfin.R
import com.dfd.delfin.data.StateModel
import com.dfd.delfin.data.StateModelList
import com.dfd.delfin.data.UserCity
import com.dfd.delfin.databinding.FragmentSelectRouteDestinationBinding
import com.dfd.delfin.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType.Add
import com.dfd.delfin.ui.selectroute.fragments.DestinationSelectedAction
import com.dfd.delfin.ui.selectroute.fragments.SelectRouteBaseFragment
import com.dfd.delfin.ui.selectroute.fragments.detail.DestinationsRVAdapter
import com.dfd.delfin.utils.extensions.dpToPx
import com.google.android.material.snackbar.Snackbar

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
  var originCity: UserCity? = null

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
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
          this@SelectRouteDestinationFragment.context
      )
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
        selectedStates.clear()

        if (!selected) {
          binding.checkSelectAll.text = getString(R.string.action_select_all_states)
          adapter.itemsList()
              .forEach { state -> state.checked = false }
          adapter.notifyDataSetChanged()
          hide()
        } else {
          binding.checkSelectAll.text = getString(R.string.action_deselect_all_states)
          adapter.itemsList()
              .forEach { state -> state.checked = true }
          selectedStates.addAll(states)
          adapter.notifyDataSetChanged()
          show()
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
      item.checked = false
    } else {
      item.checked = true
      selectedStates.add(item)
    }

    adapter.updateItem(item)

    when (selectedStates.isEmpty()) {
      true -> {
        hide()
      }
      false -> {
        if (binding.btnAction.visibility == View.GONE) {
          binding.btnAction.visibility = View.VISIBLE
        }
        if (!visible && selectedStates.size == 1) {
          show()
        }
      }
    }
  }

  fun hide() {
    visible = false
    binding.actionContainer.animate()
        .translationY(
            requireContext().dpToPx(
              this@SelectRouteDestinationFragment.requireContext(),
                binding.actionContainer.height.toFloat()
            )
        )
        .setInterpolator(AccelerateInterpolator(2f))
        .setDuration(100L)
        .start()
  }

  fun show() {
    if (!selectedStates.isEmpty()) {
      visible = true
      binding.actionContainer.animate()
          .translationY(
              -requireContext().dpToPx(
                  this@SelectRouteDestinationFragment.requireContext(), 0f
              )
          )
          .setInterpolator(DecelerateInterpolator(2f))
          .setDuration(300L)
          .start()
    }
  }

  inner class DestinationsRVScrollListener : OnScrollListener() {

    override fun onScrolled(
      recyclerView: androidx.recyclerview.widget.RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      if (visible && scrollDist > MINIMUM) {
        hide()
        scrollDist = 0
      } else if (!visible && scrollDist < -MINIMUM) {
        show()
        scrollDist = 0
      }

      if ((visible && dy > 0) || (!visible && dy < 0)) {
        scrollDist += dy
      }
    }
  }
}