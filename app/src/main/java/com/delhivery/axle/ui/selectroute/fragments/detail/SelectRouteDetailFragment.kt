package com.delhivery.axle.ui.selectroute.fragments.detail

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.delhivery.axle.R
import com.delhivery.axle.data.StateModel
import com.delhivery.axle.data.StateModelList
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.databinding.FragmentSelectRouteDetailBinding
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.axle.ui.selectroute.fragments.RouteEditOriginAction
import com.delhivery.axle.ui.selectroute.fragments.RouteUpdateAction
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteBaseFragment
import com.github.florent37.kotlin.pleaseanimate.core.position.PositionAnimExpectation

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
class SelectRouteDetailFragment : SelectRouteBaseFragment<FragmentSelectRouteDetailBinding, SelectRouteDetailViewModel>(),
    ItemClickListener<StateModel> {

  companion object {
    /* singleton instance */
    val _instance: SelectRouteDetailFragment by lazy { SelectRouteDetailFragment() }
  }

  private val MINIMUM = 25
  var route: RouteModel? = null

  private var selectedStates = mutableSetOf<StateModel>()
  private var states = StateModelList.toMutableList()

  var scrollDist = 0
  var visible = true

  private val adapter by lazy {
    DestinationsRVAdapter(this)
  }

  override fun getViewModelClass() = SelectRouteDetailViewModel::class.java

  override fun layoutId() = R.layout.fragment_select_route_detail

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    (activity as SelectRouteActivity).title = "Route Detail"

    binding.textOriginCityName.text = "Origin City"
    binding.route = route
    selectedStates.clear()

    states.forEach { t: StateModel ->
      t.checked = false
    }

    binding.rvDestinations.apply {
      layoutManager =
        androidx.recyclerview.widget.LinearLayoutManager(this@SelectRouteDetailFragment.context)
      adapter = this@SelectRouteDetailFragment.adapter
      addOnScrollListener(DestinationsRVScrollListener())
    }

    adapter.clearItems()
    adapter.operation(mutableListOf<Pair<StateModel, DataRVAdapterOperationType>>().apply {
      states.forEach { _item ->
        add(Pair(_item, Add))
      }
    })

    binding.btnSave.setOnClickListener {
      val _route = RouteModel(route!!.origin, selectedStates.toMutableSet())
      action(RouteUpdateAction(_route))
    }

    binding.imgEditOrigin.setOnClickListener {
      action(RouteEditOriginAction(route!!))
    }

  }

  override fun onItemClicked(item: StateModel) {
    if (selectedStates.contains(item)) {
      selectedStates.remove(item)
      item.checked = false;
    } else {
      item.checked = true;
      selectedStates.add(item)
    }

    binding.textDestinationHeading.text = "Destination States(${selectedStates.size})"
    adapter.updateItem(item)

    when (selectedStates.isEmpty()) {
      true -> {
        hide()
        visible = false
      }
      false -> {
        if (binding.btnSave.visibility == View.GONE) {
          binding.btnSave.visibility = View.VISIBLE
        }
        if (!visible && selectedStates.size == 1) {
          show()
        }
      }
    }
  }

  override fun onCreateOptionsMenu(
    menu: Menu?,
    inflater: MenuInflater?
  ) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater?.inflate(R.menu.menu_delete, menu);
  }

  fun hide() {
    binding.btnSave.animate()
        .translationY(
            PositionAnimExpectation.dpToPx(
                this@SelectRouteDetailFragment.context!!, 72f
            )
        )
        .setInterpolator(AccelerateInterpolator(2f))
        .setDuration(100L)
        .start();
  }

  fun show() {
    if (!selectedStates.isEmpty()) {
      visible = true
      binding.btnSave.animate()
          .translationY(
              -PositionAnimExpectation.dpToPx(
                  this@SelectRouteDetailFragment.context!!, 0f
              )
          )
          .setInterpolator(DecelerateInterpolator(2f))
          .setDuration(300L)
          .start()
    } else {
      binding.btnSave.visibility = View.GONE
    }
  }

  fun populateRoute() {
    binding.textOriginCityName.text =
      route?.origin?.cityState() ?: getString(R.string.not_available)

    adapter.clearItems()
    selectedStates.clear()

    states.forEach { t: StateModel ->
      t.checked = false
      val selected = route!!.destinations.contains(t)
      t.checked = selected
      if (selected) {
        selectedStates.add(t)
      }
    }
    binding.textDestinationHeading.text = "Destination States(${selectedStates.size})"
    adapter.operation(mutableListOf<Pair<StateModel, DataRVAdapterOperationType>>().apply {
      states.forEach { _item ->
        add(Pair(_item, Add))
      }
    })
  }

  inner class DestinationsRVScrollListener() : OnScrollListener() {

    override fun onScrolled(
      recyclerView: androidx.recyclerview.widget.RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      if (visible && scrollDist > MINIMUM) {
        hide()
        scrollDist = 0
        visible = false;
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