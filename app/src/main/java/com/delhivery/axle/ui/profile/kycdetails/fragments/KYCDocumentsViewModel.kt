package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.doc.DocDetailData
import com.delhivery.axle.data.gst.GstDetailData
import com.delhivery.axle.data.gst.GstDetailItemData
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.kyc.gst.BaseGstRVAdapterItem
import com.delhivery.axle.ui.kyc.gst.GstDataItem
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class KYCDocumentsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val loadboardRepository: LoadboardRepository
) : BaseViewModel(){

   var delegationDownloadLiveData = MutableLiveData<Triple<DelegationToken, String, File>>()
    var imagePath = ""
    var docLiveData = MutableLiveData<List<Pair<BaseDocRVAdapterItem<*>, DataRVAdapterOperationType>>>()

    var docDetailsLiveData = MutableLiveData<DocDetailData>()

    fun setDataDoc(){
        compositeDisposable += loadboardRepository.getKycDetails(userRepository.userId())
                .onBackground()
                .subscribe { _res, error ->
                    mutableListOf<Pair<BaseDocRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        /* remove progress item */
                        add(Pair(DocProgressItem(), DataRVAdapterOperationType.Remove))

                        if (!error) {
                            if (_res.kycData.isNullOrEmpty()) {
                                add(Pair(DocItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
                            } else {
                                val arrdoc = ArrayList<String>()
                                for(obj in _res.kycData) {
                                    if(!obj.documentUrls.isNullOrEmpty()) {
                                          for (url in obj.documentUrls) {
                                               if(!(url.contains("account_proof",true)||url.contains("194C") )) {
                                                   if(!arrdoc.contains(url)) {
                                                       arrdoc.add(url)
                                                       add(Pair(DocDataItem(DocDetailData(url, null, obj.verificationStatus, obj.verificationOverallType, obj.verificationType, obj.verificationStatusReasonCode, obj.verificationStatusReasonMessage)), DataRVAdapterOperationType.Add))
                                                   }
                                               }
                                           }
                                    }
                                }
                            }
                        } else {
                            /* add api time out item */
                            add(Pair(DocItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
                        }
                    }
                            .let { docLiveData.postValue(it) }
                }
    }


    fun getDownloadDelegationToken(awsPath: String, file: File) {
        compositeDisposable += userRepository.getDelegationToken(AWSConfig.Target.value())
                .onBackground()
                .subscribe { _res, error ->
                    if (!error) {
                       delegationDownloadLiveData.postValue(Triple(_res.delegationToken, awsPath, file))
                    } else
                        error.handle()
                }
    }

    fun fetchDetails(data: DocDetailData) {
       docDetailsLiveData.postValue(data)
    }

}