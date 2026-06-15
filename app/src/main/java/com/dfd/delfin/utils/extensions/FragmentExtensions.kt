package com.dfd.delfin.utils.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.dfd.delfin.ui.base.BaseActivity

/**
 * Shorthand for [activityViewModels] that automatically uses the Dagger
 * [ViewModelProvider.Factory] injected into [BaseActivity].
 */
fun Fragment.viewModelFactoryExtension(): ViewModelProvider.Factory {
    return (requireActivity() as BaseActivity<*, *>).viewModelFactory
}
