package com.delhivery.axle.ui.base

import android.Manifest
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.R
import com.delhivery.axle.utils.LocationFlowState
import com.delhivery.axle.utils.LocationUtils
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.GlobalPrefs
import javax.inject.Inject

abstract class BaseLocationActivity<B : ViewDataBinding, VM : BaseViewModel> : BaseActivity<B, VM>() {
  @Inject lateinit var locationUtils: LocationUtils
  @Inject lateinit var globalPrefs: GlobalPrefs

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
        positiveAction = "GO TO SETTINGS",
        positiveClickListener = {
          locationUtils.startEnableLocationActivity()
        }
    )
  }

  /**
   * Show Dialog -> App Settings to enable permission
   */
  private fun showPermissionRationaleRequiredDialog() {
    dialogUtils.showBasicConfirmDialog(
        R.string.title_dialog_location_permission_disabled,
        R.string.msg_dialog_location_permission_disabled,
        positiveAction = "ALLOW",
        negativeAction = "DENY",
        positiveClickListener = {
          locationUtils.openAppInfo()
        }
    )
  }

  /**
   * Request for Location Permission
   */
  private fun requestLocationPermission() {
    globalPrefs.isLocationPermissionRequested = true
    compositeDisposable += requestPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
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