package com.dfd.delfin.ui.base

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.annotation.StringRes
import com.dfd.delfin.utils.extensions.disposeAndClear
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel(), LifecycleObserver, Observable {

  //Databinding property changes registry
  private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

  /* Make sure to add all disposables to compositeDisposables to avoid memory leaks and crashes */
  protected val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

  /* Base Live data variables to update UI for generic methods */
  var toastLiveData = MutableLiveData<Int>() //Toast live data
  var snackbarLiveData = MutableLiveData<Int>() //Snackbar live data
  var progressLiveData = MutableLiveData<Boolean>() //Progress live data
  var exceptionLiveData = MutableLiveData<Throwable>() /* Error/Exception live data */

  // Network connection state reference, updated by baseactivity
  var isConnected: Boolean = false

  override fun onCleared() {
    super.onCleared()

    //dispose and clear all running processes
    compositeDisposable.disposeAndClear()
  }

  /**
   * Show Toast message
   *
   * @param resId Toast message resource Id
   */
  protected fun toast(@StringRes resId: Int) = toastLiveData.postValue(resId)

  /**
   * Show Snackbar
   *
   * @param resId Snackbar message resource Id
   */
  protected fun snackbar(@StringRes resId: Int) = snackbarLiveData.postValue(resId)

  /**
   * Show/hide progress
   *
   * @param show Show/Hide progress flag, by default show
   */
  protected fun showProgress(show: Boolean = true) = progressLiveData.postValue(show)

  /**
   * Handle throwables
   */
  protected fun Throwable.handle() = exceptionLiveData.postValue(this)

  /**
   * Handle progress on any [Single] process chain
   *
   * Show progress onSubscribe and hide finally
   */
  protected fun <T> Single<T>.progress(): Single<T> =
    doOnSubscribe { showProgress() }
        .doFinally { showProgress(false) }

  override fun addOnPropertyChangedCallback(
    callback: Observable.OnPropertyChangedCallback
  ) {
    callbacks.add(callback)
  }

  override fun removeOnPropertyChangedCallback(
    callback: Observable.OnPropertyChangedCallback
  ) {
    callbacks.remove(callback)
  }

  /**
   * Notifies observers that all properties of this instance have changed.
   */
  fun notifyChange() {
    callbacks.notifyCallbacks(this, 0, null)
  }

  /**
   * Notifies observers that a specific property has changed. The getter for the
   * property that changes should be marked with the @Bindable annotation to
   * generate a field in the BR class to be used as the fieldId parameter.
   *
   * @param fieldId The generated BR id for the Bindable field.
   */
  fun notifyPropertyChanged(fieldId: Int) {
    callbacks.notifyCallbacks(this, fieldId, null)
  }
}