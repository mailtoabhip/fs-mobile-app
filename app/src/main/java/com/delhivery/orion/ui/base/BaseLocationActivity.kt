package com.delhivery.orion.ui.base

import android.Manifest
import android.databinding.ViewDataBinding
import com.delhivery.orion.R
import com.delhivery.orion.utils.DialogUtils
import com.delhivery.orion.utils.LocationFlowState
import com.delhivery.orion.utils.LocationUtils
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import com.delhivery.orion.utils.prefs.GlobalPrefs
import javax.inject.Inject

abstract class BaseLocationActivity<B : ViewDataBinding, VM : BaseViewModel> : BaseActivity<B, VM>() {
  @Inject lateinit var locationUtils: LocationUtils
  @Inject lateinit var globalPrefs: GlobalPrefs
  @Inject lateinit var dialogUtils: DialogUtils

  protected fun onLocationButtonClicked(): Boolean {
    return when (locationUtils.getLocationPermissionFlowState()) {
      LocationFlowState.SettingDisabled -> {
        showSettingDisabledDialog()
        false
      }
      LocationFlowState.PermissionRationaleRequired -> {
        showPermissionRationaleRequiredDialog()
        false
      }
      LocationFlowState.PermissionRequired -> {
        requestLocationPermission()
        false
      }
      else -> {
        updateLocationFlowState()
        true
      }
    }
  }

  /**
   * Show Dialog -> Setting for enable Location Setting
   */
  private fun showSettingDisabledDialog() {
    dialogUtils.showBasicConfirmDialog(
        R.string.title_dialog_location_setting_disabled,
        R.string.msg_dialog_location_setting_disabled,
        positiveAction = "GO TO SETTINGS"
    ) {
      locationUtils.startEnableLocationActivity()
    }
  }

  /**
   * Show Dialog -> App Settings to enable permission
   */
  private fun showPermissionRationaleRequiredDialog() {
    dialogUtils.showBasicConfirmDialog(
        R.string.title_dialog_location_permission_disabled,
        R.string.msg_dialog_location_permission_disabled,
        positiveAction = "ALLOW",
        negativeAction = "DENY"
    ) {
      locationUtils.openAppInfo()
    }
  }

  /**
   * Request for Location Permission
   */
  private fun requestLocationPermission() {
    globalPrefs.isLocationPermissionRequested = true
    compositeDisposable += requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        .onBackground()
        .subscribe { granted, error ->
          if (error == null && granted) {
            updateLocationFlowState()
          } else {
            onLocationPermissionDenied()
          }
        }
  }

  protected fun updateLocationFlowState() {
    updateLocationFlowState(locationUtils.getLocationPermissionFlowState())
  }

  protected abstract fun updateLocationFlowState(flowState: LocationFlowState)

  protected fun onLocationPermissionDenied() {
    uiUtils.showSnackbar(R.string.error_location_permission_denied)
  }
}