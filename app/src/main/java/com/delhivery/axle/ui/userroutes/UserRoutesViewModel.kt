package com.delhivery.axle.ui.userroutes

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.dialogs.RouteDeleteDialogInterface
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 5/1/21
 */

class UserRoutesViewModel @Inject constructor(
  private val userRepository: UserRepository,
  val userPrefs: UserPrefs
): BaseViewModel(), RouteDeleteDialogInterface {

  var routesLiveData =
    MutableLiveData<Pair<Pair<String, String>, MutableList<RouteModel>>>()

  var allRoutesLiveData = MutableLiveData<List<Pair<BaseUserRouteRVAdapterItem<*>, DataRVAdapterOperationType>>>()

  /* selected route models */
  var routes = mutableListOf<RouteModel>()

  /**
   * Fetch user routes
   */
  fun fetchUserRoutes() {

    Pair(UserRouteProgressItem(), AddUpdate).let { allRoutesLiveData.postValue(listOf(it)) }

    compositeDisposable += userRepository.getUser(false)
        .onBackground()
        .progress()
        .subscribe { _user, error ->
          if (!error) {
            mutableListOf<Pair<BaseUserRouteRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(UserRouteProgressItem(), Remove))

              for (route in _user.userRoutes()) {
                add(Pair(UserRouteItem(route), Add))
              }
            }.let {
              allRoutesLiveData.postValue(it)
            }

            routes.addAll(_user.userRoutes())
            routesLiveData.postValue(
                Pair(Pair(_user.baseCity, _user.baseCityCode), routes)
            )
          } else {
            mutableListOf<Pair<BaseUserRouteRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
              add(Pair(UserRouteProgressItem(), Remove))
            }.let {
              allRoutesLiveData.postValue(it)
            }
          }
        }
  }

  override fun deleteRoute() {
    Log.d("delete","deleted")
  }


}