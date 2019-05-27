package com.delhivery.orion.ui.selectroute.fragments.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentSelectRouteDetailBinding
import com.delhivery.orion.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteBaseFragment
import com.delhivery.orion.utils.DialogUtils
import javax.inject.Inject

/**
 * Created by saurabh on 27,May,2019
 * for Delhivery Private Limited
 *
 * <Define what the class does>
 *
 */
class SelectRouteDetailFragment : SelectRouteBaseFragment<FragmentSelectRouteDetailBinding, SelectRouteDetailViewModel>() {
  companion object {
    /* singleton instance */
    val _instance: SelectRouteDetailFragment by lazy { SelectRouteDetailFragment() }
  }

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

    (activity as SelectRouteActivity)?.title = "Route Detail"
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
            R.string.title_dialog_delete_route,
            R.string.msg_dialog_delete_route,
            positiveAction = "Delete",
            negativeAction = "No, Don’t Delete"
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