package com.dfd.delfin.ui.profile.kycdetails.fragments

import androidx.lifecycle.MutableLiveData
import com.dfd.delfin.api.repository.LoadboardRepository
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.data.doc.DocDetailData
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.base.adapter.DataRVAdapterOperationType
import com.dfd.delfin.utils.extensions.not
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import javax.inject.Inject

class KYCDocumentsViewModel @Inject constructor(
        private val userRepository: UserRepository,
        private val loadboardRepository: LoadboardRepository
) : BaseViewModel(){

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

    fun fetchDetails(data: DocDetailData) {
       docDetailsLiveData.postValue(data)
    }

}