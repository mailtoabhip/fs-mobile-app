package com.delhivery.axle.ui.home.fragments.loads_truck

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.ui.base.BaseFragment
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.fragments.BaseHomeFragmentAction
import com.delhivery.axle.utils.NavigationUtils
import javax.inject.Inject

abstract class HomeLoadsTruckBaseFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {

    /* toolbar elevation Live Data */
    var toolbarElevationLiveData: MutableLiveData<Float>? = null

    var isLoadingData = true

    @Inject
    lateinit var navigationUtils: NavigationUtils

    /* elevation default value */
    protected val defToolbarElevation: Float by lazy {
        resources.getDimension(R.dimen.toolbar_elevation)
    }

    fun <T> LiveData<T>.observeOnce(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<T>
    ) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    /**
     *
     * Post new action to activity
     *
     */
    protected fun action(action: BaseHomeFragmentAction) {

    }
}