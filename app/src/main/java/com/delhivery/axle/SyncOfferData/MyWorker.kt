package com.delhivery.axle.SyncOfferData

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.PriceRepository
import com.delhivery.axle.database.AppDatabase
import com.delhivery.axle.database.entity.OffersEntity
import com.delhivery.axle.injection.module.DaggerWorkerFactory
import com.delhivery.axle.tokenExpiryHandling.RefreshTokenWorker
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.prefs.UserPrefs
import com.squareup.okhttp.Callback
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class MyWorker(
        private val priceRepository: PriceRepository,
        private val loadCycleRepository: LoadCycleRepository,
        private val appDatabase: AppDatabase,
        context: Context,
        workerParams: WorkerParameters,
        val userPrefs: UserPrefs
) : Worker(context, workerParams) {

    private val TAG: String = MyWorker::class.java.getSimpleName()

    override fun doWork(): Result {
        var success = false
        if(appDatabase.offersDao().getCountOffers().value.isNullOrEmpty()){
            priceRepository.getOffers().subscribe { _res, error ->
                if (!error) {
                    success = true
                    if (_res!=null && !_res.offersList.isNullOrEmpty()) {
                        userPrefs.bidOfferCount = _res.total
                        appDatabase.offersDao().deleteOffers()
                        for (i in _res.offersList) {
                            appDatabase.offersDao().newOfferEntry(OffersEntity(i.occ, i.oc, i.dcc, i.dc, i.tdn, i.sd, i.ed, i.amount, i.status, i.offerType, i.offerId))
                        }
                    }
                } else {
                    success = false
                }
            }
        }else{
            priceRepository.getUpdateFlag().subscribe { t1, t2 ->
                if (!t2) {
                    if (t1.updated == true) {
                        priceRepository.getOffers().subscribe { _res, error ->
                            if (!error) {
                                success = true
                                if (_res!=null &&!_res.offersList.isNullOrEmpty()) {
                                    appDatabase.offersDao().deleteOffers()
                                    for (i in _res.offersList) {
                                        appDatabase.offersDao().newOfferEntry(OffersEntity(i.occ, i.oc, i.dcc, i.dc, i.tdn, i.sd, i.ed, i.amount, i.status, i.offerType, i.offerId))
                                    }
                                }
                            } else {
                                success = false
                            }
                        }
                    } else {
                        success = true
                    }
                } else {
                    success = false
                }
            }
        }

        return if(success) Result.success() else Result.retry()
    }

    class Factory @Inject constructor(
            private val priceRepository: PriceRepository,
            private val loadCycleRepository: LoadCycleRepository,
            private val appDatabase: AppDatabase,
            val userPrefs: UserPrefs
    ) : DaggerWorkerFactory.ChildWorkerFactory {

        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker =
                MyWorker(priceRepository, loadCycleRepository,appDatabase, appContext, params,userPrefs)
    }
}