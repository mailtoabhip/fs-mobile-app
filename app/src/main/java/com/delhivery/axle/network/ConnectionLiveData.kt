package com.delhivery.axle.network

import android.accounts.AccountManagerCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

/**
 * Connectivity monitor to handle network callbacks both pre and post and Android Q
 */
//internal sealed class ConnectivityMonitor(
//  protected val connectivityManager: ConnectivityManager
//) {
//
//  protected var callbackFunction: ((Boolean) -> Unit) = {}
//
//  abstract fun startListening(callback: (Boolean) -> Unit)
//  abstract fun stopListening()
//
//  @TargetApi(Build.VERSION_CODES.N)
//  private class NougatConnectivityMonitor(connectivityManager: ConnectivityManager) :
//      ConnectivityMonitor(connectivityManager) {
//
//    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
//      override fun onAvailable(network: Network) {
//        super.onAvailable(network)
//        callbackFunction(true)
//      }
//
//      override fun onLost(network: Network) {
//        super.onLost(network)
//        callbackFunction(false)
//      }
//    }
//
//    override fun startListening(callback: (Boolean) -> Unit) {
//      callbackFunction = callback
//      callbackFunction(false)
//      connectivityManager.registerDefaultNetworkCallback(networkCallback)
//    }
//
//    override fun stopListening() {
//      connectivityManager.unregisterNetworkCallback(networkCallback)
//      callbackFunction = {}
//    }
//  }
//
//  @Suppress("Deprecation")
//  private class LegacyConnectivityMonitor(
//    private val context: Context,
//    connectivityManager: ConnectivityManager
//  ) : ConnectivityMonitor(connectivityManager) {
//
//    private val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//
//    private val isNetworkConnected: Boolean
//      get() = connectivityManager.activeNetworkInfo?.isConnected == true
//
//    override fun startListening(callback: (Boolean) -> Unit) {
//      callbackFunction = callback
//      callbackFunction(isNetworkConnected)
//      context.registerReceiver(receiver, filter)
//    }
//
//    override fun stopListening() {
//      context.unregisterReceiver(receiver)
//      callbackFunction = {}
//    }
//
//    private val receiver = object : BroadcastReceiver() {
//      override fun onReceive(
//        context: Context,
//        intent: Intent
//      ) {
//        callbackFunction(isNetworkConnected)
//      }
//    }
//  }
//
//  companion object {
//    fun getInstance(context: Context): ConnectivityMonitor {
//      val connectivityManager =
//        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        NougatConnectivityMonitor(connectivityManager)
//      } else {
//        LegacyConnectivityMonitor(context, connectivityManager)
//      }
//    }
//  }
//}

/**
 * Connection Live Data, Observe to get updated network state
 *
 * @param context Context
 */
class ConnectionLiveData constructor(private val context: Context) : MutableLiveData<Boolean>() {

  private var connectivityManager: ConnectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

  private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback

  override fun onActive() {
    super.onActive()
    //register connection change receiver
    if(Build.VERSION.SDK_INT >= 28)
      connectivityManager.registerDefaultNetworkCallback(getConnectivityManagerCallback())
    else
    {
      IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).let {
        ContextCompat.registerReceiver(context,networkReceiver, it,ContextCompat.RECEIVER_NOT_EXPORTED)
      }
    }
  }

  override fun onInactive() {
    super.onInactive()
    if(Build.VERSION.SDK_INT >= 28)
      connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
    else
      context.unregisterReceiver(networkReceiver)
  }
  private fun getConnectivityManagerCallback() : ConnectivityManager.NetworkCallback{
    connectivityManagerCallback = object : ConnectivityManager.NetworkCallback(){
      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        networkCapabilities.let {
          if(it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            postValue(true)
        }
      }

      override fun onLost(network: Network) {
        postValue(false)
      }
    }
    return connectivityManagerCallback
  }
  /**
   * Listen to any network change state
   */
  private val networkReceiver = object : BroadcastReceiver() {
    override fun onReceive(
      context: Context?,
      intent: Intent?
    ) {
      postValue(isConnected())
    }
  }

  /**
   * Is connected to internet or not
   *
   * @return [Boolean] current connection state
   */
  fun isConnected(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val networkCapabilities = connectivityManager.activeNetwork ?: return false
      val activeNetwork =
        connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

      return when {

        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        else -> false
      }
    } else {
      return connectivityManager.activeNetworkInfo != null &&
              connectivityManager.activeNetworkInfo!!.isConnected
    }
  }

}