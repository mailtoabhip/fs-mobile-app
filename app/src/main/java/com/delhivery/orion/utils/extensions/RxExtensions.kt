package com.delhivery.orion.utils.extensions

import com.delhivery.orion.api.response.BaseResponse
import com.delhivery.orion.exception.APIException
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
 * Handle response and based on [BaseResponse.isSuccess] flag,
 * response is passed or exception is thrown
 *
 */
fun <M : Any, T : BaseResponse<M>> Single<T>.convertResponse(): Single<M> =
  map {
    if (it.isSuccess) {
      return@map it.responseData
    } else {
      throw APIException(
          it.errorBody?.errorCode() ?: -1, it.errorBody?.errorMessage ?: "Unknown error"
      )
    }
  }