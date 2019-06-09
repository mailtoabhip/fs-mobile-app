package com.delhivery.orion.ui.selectroute.fragments.detail

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.delhivery.orion.R
import com.delhivery.orion.R.string
import com.delhivery.orion.data.RouteModel
import com.delhivery.orion.data.StateModel
import com.delhivery.orion.data.StateModelList
import com.delhivery.orion.databinding.FragmentSelectRouteDetailBinding
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.orion.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.orion.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.orion.ui.selectroute.fragments.RouteDeleteAction
import com.delhivery.orion.ui.selectroute.fragments.RouteUpdateAction
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteBaseFragment
import com.delhivery.orion.utils.DialogUtils
import com.github.florent37.kotlin.pleaseanimate.core.position.PositionAnimExpectation
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
  var visible = false

  @Inject lateinit var dialogUtils: DialogUtils

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

    binding.route = route
    selectedStates.clear()

    states.forEach { t: StateModel ->
      t.checked = false
      val selected = route!!.destinations.contains(t)
      t.checked = selected
      if (selected) {
        selectedStates.add(t)
      }
    }

    binding.rvDestinations.apply {
      layoutManager = LinearLayoutManager(this@SelectRouteDetailFragment.context)
      adapter = this@SelectRouteDetailFragment.adapter
      addOnScrollListener(DestinationsRVScrollListener(binding.btnSave))
    }

    adapter.clearItems()
    adapter.operation(mutableListOf<Pair<StateModel, DataRVAdapterOperationType>>().apply {
      states
          .forEach { _item ->
            add(Pair(_item, Add))
          }
    })

    binding.btnSave.setOnClickListener {
      action(RouteUpdateAction(selectedStates.toMutableList()))
    }
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    setHasOptionsMenu(true)
    super.onActivityCreated(savedInstanceState)
  }

  override fun onItemClicked(item: StateModel) {
    if (selectedStates.contains(item)) {
      selectedStates.remove(item)
      item.checked = false;
    } else {
      item.checked = true;
      selectedStates.add(item)
    }

    binding.textDestinationHeading.text = "Destination States(" + selectedStates.size + ")"
    visible = true
    adapter.updateItem(item)

    if (binding.btnSave.visibility == View.GONE)
      binding.btnSave.visibility = View.VISIBLE
    else
      show()
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
          action(RouteDeleteAction())
        }
        return true
      }
      else ->
        return super.onOptionsItemSelected(item)
    }
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
    binding.btnSave.animate()
        .translationY(
            -PositionAnimExpectation.dpToPx(
                this@SelectRouteDetailFragment.context!!, 0f
            )
        )
        .setInterpolator(DecelerateInterpolator(2f))
        .setDuration(300L)
        .start()
  }

  inner class DestinationsRVScrollListener(
    private val view: View
  ) : OnScrollListener() {

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