package com.delhivery.orion.network

import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

/**
 * Connection Live Data, Observe to get updated network state
 *
 * @param context Context
 */
class ConnectionLiveData constructor(private val context: Context) : MutableLiveData<Boolean>() {
    override fun onActive() {
        super.onActive()

        // Emit current connection state
//        postValue(isConnected())

        //register connection change receiver
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).let {
            context.registerReceiver(networkReceiver, it)
        }
    }

    /**
     * Listen to any network change state
     */
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            postValue(isConnected())
        }
    }

    /**
     * Is connected to internet or not
     *
     * @return [Boolean] current connection state
     */
    fun isConnected(): Boolean {
        return (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).let {
            it.activeNetworkInfo != null && it.activeNetworkInfo.isConnected
        }
    }
}