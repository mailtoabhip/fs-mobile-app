package com.delhivery.orion.utils

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.util.Log
import com.delhivery.orion.utils.LocationFlowState.PermissionGranted
import com.delhivery.orion.utils.LocationFlowState.PermissionRationaleRequired
import com.delhivery.orion.utils.LocationFlowState.PermissionRequired
import com.delhivery.orion.utils.LocationFlowState.SettingDisabled
import com.delhivery.orion.utils.prefs.GlobalPrefs
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.exceptions.Exceptions
import javax.inject.Inject

@SuppressLint("LogNotTimber")
class LocationUtils @Inject constructor(
  private val activity: Activity,
  private val globalPrefs: GlobalPrefs
) {
  private val LOG_TAG = javaClass.simpleName
  private val UPDATE_MIN_TIME: Long = 5000 // Ms
  private val UPDATE_MIN_DISTANCE = 10f // Meters

  private val locationManager: LocationManager by lazy {
    activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  }

  /**
   * Get Location Permission Flow State
   *
   * @return [LocationFlowState]
   */
  fun getLocationPermissionFlowState(): LocationFlowState {
    //Setting Disabled
    if (!checkIfLocationEnabled()) {
      return SettingDisabled
    }

    //Permission Granted for pre-M devices
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return PermissionGranted
    }

    val isPermissionGranted = ContextCompat.checkSelfPermission(
        activity,
        permission.ACCESS_FINE_LOCATION
    ) == PermissionChecker.PERMISSION_GRANTED
    val shouldShowRequestPermissionRationale = activity.shouldShowRequestPermissionRationale(
        permission.ACCESS_FINE_LOCATION
    )

    return if (isPermissionGranted) {
      PermissionGranted
    } else if (globalPrefs.isLocationPermissionRequested && !shouldShowRequestPermissionRationale) {
      PermissionRationaleRequired
    } else {
      PermissionRequired
    }
  }

  /**
   * Get Periodic location updates
   * throw error if location permission is not granted or setting is not enabled
   *
   * @return Observable<Location> - Location updates in observable
   */
  fun getLocationUpdates(): Observable<Location> {
    return Observable.just(getLocationPermissionFlowState())
        .flatMap {
          if (it != PermissionGranted) {
            Exceptions.propagate(Exception("Location Setting/Permission is required"))
          }
          return@flatMap requestLocationUpdates()
        }
  }

  /**
   * Get Location once
   * throw error if location permission is not granted or setting is not enabled
   *
   * @return Single<Location> - Location update in Single
   */
  fun getLocation(): Single<Location> {
    return Single.just(getLocationPermissionFlowState())
        .flatMap {
          if (it != PermissionGranted) {
            Exceptions.propagate(Exception("Location Setting/Permission is required"))
          }
          return@flatMap requestLocationUpdateOnce()
        }
  }

  /**
   * Check if Location setting is enabled or not
   */
  private fun checkIfLocationEnabled(): Boolean {
    val locationManager: LocationManager = activity.getSystemService(
        Context.LOCATION_SERVICE
    ) as LocationManager
    return locationManager.isProviderEnabled(
        LocationManager.GPS_PROVIDER
    ) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
  }

  /**
   * Start Location settings page
   */
  fun startEnableLocationActivity() {
    val locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    activity.startActivityForResult(locationIntent, 1121)
  }

  /**
   * Open App info page to allow user enable location settings,
   * Usually called after checking rationale permission
   */
  fun openAppInfo() {
    try {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
      intent.data = Uri.parse("package:" + activity.packageName)
      activity.startActivity(intent)

    } catch (e: ActivityNotFoundException) {
      val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
      activity.startActivity(intent)
    }
  }

  @SuppressLint("MissingPermission")
  fun getLastKnownLocation() = locationManager.getLastKnownLocation(
      LocationManager.NETWORK_PROVIDER
  )

  @SuppressLint("MissingPermission")
  private fun requestLocationUpdates(): Observable<Location> {
    return Observable.create<Location> {
      locationManager.requestLocationUpdates(
          LocationManager.NETWORK_PROVIDER, UPDATE_MIN_TIME,
          UPDATE_MIN_DISTANCE,
          object : LocationListener {
            override fun onLocationChanged(location: Location?) {
              Log.d(LOG_TAG, "onLocationChanged(location: ${location.toString()})")
              location?.let { it1 -> it.onNext(it1) }
            }

            override fun onStatusChanged(
              provider: String?,
              status: Int,
              extras: Bundle?
            ) {
              Log.d(
                  LOG_TAG, "onStatusChanged(provider: $provider, status: $status, extras: $extras)"
              )
            }

            override fun onProviderEnabled(provider: String?) {
              Log.d(LOG_TAG, "onProviderEnabled(provider: $provider)")
            }

            override fun onProviderDisabled(provider: String?) {
              Log.d(LOG_TAG, "onProviderDisabled(provider: $provider)")
            }
          }, Looper.getMainLooper()
      )
    }
  }

  @SuppressLint("MissingPermission")
  private fun requestLocationUpdateOnce(): Single<Location> {
    return Single.create<Location> {
      locationManager.requestSingleUpdate(
          LocationManager.NETWORK_PROVIDER,
          object : LocationListener {
            override fun onLocationChanged(location: Location?) {
              Log.d(LOG_TAG, "onLocationChanged(location: ${location.toString()})")
              location?.let { it1 -> it.onSuccess(it1) }
            }

            override fun onStatusChanged(
              provider: String?,
              status: Int,
              extras: Bundle?
            ) {
              Log.d(
                  LOG_TAG, "onStatusChanged(provider: $provider, status: $status, extras: $extras)"
              )
            }

            override fun onProviderEnabled(provider: String?) {
              Log.d(LOG_TAG, "onProviderEnabled(provider: $provider)")
            }

            override fun onProviderDisabled(provider: String?) {
              Log.d(LOG_TAG, "onProviderDisabled(provider: $provider)")
            }
          }, Looper.getMainLooper()
      )
    }
  }
}

/**
 * Location Flow State
 * [SettingDisabled] - Location setting disabled
 * [PermissionRequired] - Request Location Permission
 * [PermissionRationaleRequired] - Never show permission dialog set, show app permission settings page
 * [PermissionGranted] - Location Permission Granted
 */
enum class LocationFlowState {
  SettingDisabled,
  PermissionRequired,
  PermissionRationaleRequired,
  PermissionGranted
}