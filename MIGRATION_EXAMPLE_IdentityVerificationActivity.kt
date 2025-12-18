// MIGRATION EXAMPLE: IdentityVerificationActivity.kt
// This shows the BEFORE and AFTER code for migrating from AWS S3 to Document API

// ============================================================================
// BEFORE (Current AWS S3 Implementation)
// ============================================================================

/*
class IdentityVerificationActivity : BaseActivity(), AWSUtils.AWSProgressInterface {
    
    @Inject
    lateinit var awsUtils: AWSUtils
    
    private var mPhotoFile: File? = null
    private var uploadImageName = ""
    private var localImageName = ""
    private val awsPath = "documents/identity_verification/"
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQCODE_TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    // ... file processing code ...
                    var docType = ""
                    when {
                        binding.textCin.isChecked -> {
                            docType = "CIN_"
                        }
                        binding.textUdyog.isChecked -> {
                            docType = "Udyog_"
                        }
                        binding.textShop.isChecked -> {
                            docType = "Shop_"
                        }
                    }
                    this.uploadImageName = docType + System.currentTimeMillis() + "." + imageScopedFile.extension
                    this.localImageName = docType + System.currentTimeMillis() + "." + imageScopedFile.extension
                    
                    // ... file compression ...
                    uiUtils.showProgress()
                    viewModel.getDelegationToken(mPhotoFile!!)
                }
            }
        }
    }
    
    override fun onAWSSuccess(path: String) {
        uiUtils.hideProgress()
        val s3url = awsUtils.awsBasePath()
        val imageUrls = mutableListOf<String>()
        imageUrls.add(s3url + awsPath + path.replace(awsPath, ""))
        viewModel.verifyByDoc(imageUrls)
    }
    
    override fun onAWSFailure() {
        uiUtils.hideProgress()
        uiUtils.showToast("Upload failed")
    }
}

// In IdentityVerificationViewModel.kt
class IdentityVerificationViewModel {
    var delegationLiveData = MutableLiveData<Pair<DelegationToken, File>>()
    
    fun getDelegationToken(file: File) {
        compositeDisposable += userRepository.getDelegationToken(AWSConfig.Target.value())
            .onBackground()
            .progress()
            .subscribe { _res, error ->
                if (!error) {
                    delegationLiveData.postValue(Pair(_res.delegationToken, file))
                } else
                    error.handle()
            }
    }
    
    fun verifyByDoc(docList: List<String>) {
        uploadDocForVerification(VerificationDocUploadRequest(
            verificationId = if(cinNumber.isNotEmpty())cinNumber else if(udyogNumber.isNotEmpty())udyogNumber else if(shopNumber.isNotEmpty())shopNumber else null,
            proofDocumentType = selected,
            documentUrls = docList
        ))
    }
}
*/

// ============================================================================
// AFTER (New Document API Implementation)
// ============================================================================

class IdentityVerificationActivity : BaseActivity(), DocumentUtils.DocumentProgressInterface {
    
    @Inject
    lateinit var documentUtils: DocumentUtils
    
    private var mPhotoFile: File? = null
    private var uploadImageName = ""
    private var localImageName = ""
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQCODE_TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    // ... file processing code ...
                    var docType = ""
                    when {
                        binding.textCin.isChecked -> {
                            docType = "cin"
                        }
                        binding.textUdyog.isChecked -> {
                            docType = "udyog_aadhaar"
                        }
                        binding.textShop.isChecked -> {
                            docType = "shop_establishment"
                        }
                    }
                    this.uploadImageName = docType + "_" + System.currentTimeMillis() + "." + imageScopedFile.extension
                    this.localImageName = docType + "_" + System.currentTimeMillis() + "." + imageScopedFile.extension
                    
                    // ... file compression ...
                    uiUtils.showProgress()
                    // Direct upload - no delegation token needed
                    documentUtils.uploadDocument(mPhotoFile!!, docType, this)
                }
            }
        }
    }
    
    override fun onDocumentSuccess(message: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Document uploaded successfully")
        // For verification, we might need to list the documents or use a different approach
        // since the new API doesn't return file URLs directly
        viewModel.verifyByDoc(listOf(message)) // or handle differently based on requirements
    }
    
    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Upload failed: $error")
    }
}

// In IdentityVerificationViewModel.kt
class IdentityVerificationViewModel {
    // Remove delegationLiveData - no longer needed
    
    // Remove getDelegationToken method - no longer needed
    
    fun verifyByDoc(docList: List<String>) {
        // This might need to be updated based on how verification works
        // with the new document API
        uploadDocForVerification(VerificationDocUploadRequest(
            verificationId = if(cinNumber.isNotEmpty())cinNumber else if(udyogNumber.isNotEmpty())udyogNumber else if(shopNumber.isNotEmpty())shopNumber else null,
            proofDocumentType = selected,
            documentUrls = docList
        ))
    }
}

// ============================================================================
// KEY CHANGES SUMMARY
// ============================================================================

/*
1. Replace AWSUtils with DocumentUtils
2. Change interface from AWSProgressInterface to DocumentProgressInterface
3. Remove delegation token logic completely
4. Update document type mapping (CIN_ -> cin, etc.)
5. Direct upload call instead of delegation token flow
6. Update success/failure handling
7. Remove awsPath and s3url construction
8. Simplify file naming (remove AWS-specific prefixes)
*/
