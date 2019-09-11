package com.delhivery.axle.utils.extensions

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Single - onBackground()
 * Subscribe on IO thread and observer on Android Main Thread
 */
fun <T> Single<T>.onBackground() =
  subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

/**
 * Observable - onBackground()
 * Subscribe on IO thread and observer on Android Main Thread
 */
fun <T> Observable<T>.onBackground() =
  subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

/**
 * Add Disposables to CompositeDisposable,
 * should be disposed when creater is destroyed
 *
 * @param disposable Disposable to be added to composite disposable
 */
operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
  add(disposable)
}

/**
 * Dispose and clear composite disposable, if not disposed already
 */
fun CompositeDisposable.disposeAndClear() {
  if (!isDisposed) {
    dispose()
    clear()
  }
}

/**
 * Safe dispose
 */
fun Disposable?.safeDispose() {
  if (this != null && !isDisposed) {
    dispose()
  }
}