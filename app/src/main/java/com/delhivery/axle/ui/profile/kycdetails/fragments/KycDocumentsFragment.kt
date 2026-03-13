package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.Manifest
import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.lifecycle.Observer
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.data.doc.DocAction_ViewDetails
import com.delhivery.axle.data.doc.DocDetailData
import com.delhivery.axle.data.transactions.TransactionTimeOutAction
import com.delhivery.axle.databinding.FragmentKycDocumentsBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.api.response.DocumentFile
import com.delhivery.axle.api.response.FileData
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.NavigationUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.delhivery.axle.utils.StepKey
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import javax.inject.Inject


class KycDocumentsFragment : ProfileKYCBaseFragment<FragmentKycDocumentsBinding, KYCDocumentsViewModel>(),
    DocRVAdapterInterface, DocumentUtils.DocumentListInterface {

    init {
        hasInlineProgress = true
    }

    companion object {
        fun newInstance(): KycDocumentsFragment = KycDocumentsFragment()
    }

    lateinit var path:String
    var dList:HashMap<String, DocDetailData?> = HashMap()
    var docItem:DocDetailData = DocDetailData("", null, null, null,null, null, null)
    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true
    
    // ✅ Map s3_path -> original URL to handle concurrent downloads
    private val s3PathToUrlMap: HashMap<String, String> = HashMap()
    // ✅ Track download type per s3_path to avoid race conditions
    private val s3PathDownloadType: HashMap<String, DownloadType> = HashMap()
    
    private enum class DownloadType {
        VIEW_DETAILS,  // downloadLogo flow - save to Downloads
        PREVIEW_IMAGE  // downloadImage flow - save to app documents
    }

    @Inject lateinit var bitmapUtils:BitmapUtils
    @Inject lateinit var documentUtils: DocumentUtils

    override fun getViewModelClass()= KYCDocumentsViewModel::class.java

    /* RV adapter */
    private val docRVAdapter: DocRVAdapter by lazy { DocRVAdapter(this) }

    override fun layoutId() = R.layout.fragment_kyc_documents

    @Inject lateinit var userPrefs: UserPrefs

    @Inject lateinit var navigationUtils: NavigationUtils

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("KycDocumentsFragment_SetupTime")
        fragmentSetupTrace?.start()

        binding.attachmentList.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@KycDocumentsFragment.docRVAdapter
        }

        if(userPrefs.verificationStatus.equals("failed")){
            binding.btnRetry.visibility = View.VISIBLE
        }else{
            binding.btnRetry.visibility = View.GONE
        }
        if(userPrefs.noOfVerificationIssues.equals("1")&&userPrefs.isBankDetailsRejected==true){
            binding.btnRetry.isEnabled=false
        }else{
            binding.btnRetry.isEnabled=true
        }
        binding.btnRetry.setOnClickListener {
            userPrefs.retryVerification = true
            userPrefs.retryVerificationOnBack=false
            val bundle = Bundle()
            bundle.putInt(StepKey, 0)
            context?.let { it1 -> navigationUtils.navigateKyc(it1, true, bundle) }
        }

        dList.clear()
        dList = HashMap()
        docItem = DocDetailData("", null, null, null,null, null, null)
        refreshData()

        viewModel.docLiveData.reobserve(this, Observer {
            it?.let { _items ->
                docRVAdapter.operation(_items)
            }
        })

        viewModel.docDetailsLiveData.reobserve(this, Observer {
            docRVAdapter.notifyDataSetChanged()
        })

    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun downloadLogo(docUrl: String) {
        compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onBackground()
            .subscribe { granted, error ->
                if (error == null && granted) {
                    uiUtils.showProgress()
                    // Extract s3_path from full S3 URL
                    val s3Path = extractS3PathFromUrl(docUrl)
                    if (s3Path != null) {
                        // ✅ Map s3_path to original URL for lookup in callback
                        s3PathToUrlMap[s3Path] = docUrl
                        s3PathDownloadType[s3Path] = DownloadType.VIEW_DETAILS
                        documentUtils.downloadByS3Path(s3Path, this)
                    } else {
                        uiUtils.hideProgress()
                        uiUtils.showSnackbar("Unable to extract document path from URL")
                    }
                } else {
                    uiUtils.hideProgress()
                    uiUtils.showSnackbar(getString(R.string.storage_permission))
                }
            }
    }

    private fun downloadImage(data: DocDetailData, docUrl: String) {
        // Store reference in dList before starting download
        if (!dList.containsKey(docUrl)) {
            dList[docUrl] = data
        }
        
        // Extract s3_path from full S3 URL
        val s3Path = extractS3PathFromUrl(docUrl)
        
        if (s3Path != null) {
            // ✅ Map s3_path to original URL for lookup in callback
            s3PathToUrlMap[s3Path] = docUrl
            s3PathDownloadType[s3Path] = DownloadType.PREVIEW_IMAGE
            
            // Use secure Document API: GET /document/download?s3_path=...
            documentUtils.downloadByS3Path(s3Path, this)
        } else {
            Log.e(LOG_TAG, "Failed to extract s3Path from URL: $docUrl")
            uiUtils.showSnackbar("Unable to extract document path from URL")
        }
    }

    /**
     * Extract s3_path from full S3 URL or return relative path as-is
     * 
     * Handles full S3 URLs:
     * Example: "https://orion-service-prod-mum.s3.ap-south-1.amazonaws.com/documents/user-supplier/pancard/1735911510372.png"
     * Returns: "documents/user-supplier/pancard/1735911510372.png"
     * 
     * Also handles: "https://orion-service-prod-mum.s3.ap-south-1.amazonaws.com/trips/vendor_pod/docket/docket_1764233770054.jpg"
     * Returns: "trips/vendor_pod/docket/docket_1764233770054.jpg"
     * 
     * Handles relative paths (already s3_path):
     * Example: "documents/user-supplier/bankaccount/1747041740988.png"
     * Returns: "documents/user-supplier/bankaccount/1747041740988.png"
     */
    private fun extractS3PathFromUrl(docUrl: String): String? {
        return try {
            // Check if it's already a relative path (no http/https protocol and no .amazonaws.com)
            if (!docUrl.startsWith("http://") && !docUrl.startsWith("https://") && !docUrl.contains(".amazonaws.com")) {
                // Already a relative path, return as-is (remove query parameters if any)
                return docUrl.split("?")[0]
            }
            
            // Construct AWS base path from config
            val bucket = com.delhivery.axle.config.AWSConfig.Bucket.value()
            val region = com.delhivery.axle.config.AWSConfig.ServerRegion.value()
            val awsBasePath = "https://$bucket.s3.$region.amazonaws.com/"
            
            // Remove base URL from full S3 URL
            if (docUrl.startsWith(awsBasePath)) {
                docUrl.replace(awsBasePath, "").split("?")[0] // Remove query parameters if any
            } else {
                // Fallback: Try to extract path from URL pattern (bucket.s3.region.amazonaws.com/path)
                val parts = docUrl.split(".amazonaws.com/")
                if (parts.size > 1) {
                    parts[1].split("?")[0] // Remove query parameters if any
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error extracting s3_path: ${e.message}")
            null
        }
    }


    /**
     * Create file for downloads: {timestamp}{filename} in Downloads directory
     */
    private fun getFile(url: String): File? {
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val arrString = url.split("/")
        val fileName = arrString[arrString.size - 1].split("?")[0] // Remove query parameters
        val timestamp = System.currentTimeMillis()
        val file = File(storageDir, "${timestamp}${fileName}")
        // Ensure directory exists
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return file
    }

    /**
     * Create file for viewing: {filename} in app's documents/kyc_docs directory
     * ✅ FIX #3: Use consistent filename (not timestamp) for file persistence
     */
    private fun getImageFile(url: String): File? {
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (storageDir == null) return null
        val kycDocsDir = File(storageDir, "kyc_docs")
        if (!kycDocsDir.exists()) {
            kycDocsDir.mkdirs()
        }

        // Extract doc type and timestamp
        val s3Path = extractS3PathFromUrl(url) ?: return null
        val parts = s3Path.split("/")

        val docType = if (parts.size >= 3) parts[parts.size - 2] else "unknown"

        // Get filename with timestamp
        val arrString = url.split("/")
        val fileName = arrString.lastOrNull()?.split("?")?.firstOrNull() ?: "doc.png"

        // Use: pancard_1735911510372.png (type + timestamp)
        return File(kycDocsDir, "${docType}_${fileName}")
    }
    
    /**
     * ✅ FIX #3: Check if file already exists on disk
     */
    private fun getExistingImageFile(url: String): File? {
        val file = getImageFile(url)
        return if (file?.exists() == true && file.length() > 0) {
            Log.d("KycDocumentsFragment", "File already exists: ${file.absolutePath}, size=${file.length()}")
            file
        } else {
            null
        }
    }


    override fun handleAction(actionId: String, item: BaseDocRVAdapterItem<*>) {
        when (actionId) {
            DocAction_ViewDetails -> {
                // docUrl is already a complete URL from the backend
                downloadLogo(item.data.key())
            }
            TransactionTimeOutAction -> {
                refreshData()
            }
        }
    }

    private fun refreshData() {
        docRVAdapter.resetStaticData()
        viewModel.setDataDoc()
    }

    override fun fetchDetails(data: DocDetailData) {
        // ✅ Fixed: Use containsKey() instead of contains() to check HashMap keys
        if(!dList.containsKey(data.docUrl)){
            dList[data.docUrl] = data
            
            // ✅ FIX #3: Check if file already exists on disk before downloading
            val existingFile = getExistingImageFile(data.docUrl)
            
            if (existingFile != null) {
                // File already exists, use it directly
                data.docPath = existingFile.absolutePath
                dList[data.docUrl] = data
                
                // ✅ FIX: Post notification to avoid "computing layout" crash
                // Cannot call notifyDataSetChanged during onBindViewHolder
                binding.attachmentList.post {
                    docRVAdapter.notifyDataSetChanged()
                }
            } else {
                // File doesn't exist, download it
                downloadImage(data, data.docUrl)
            }
        }
    }

    override fun showImage(data: DocDetailData, textView: TextView, imageView: ImageView) {
        try {
            if (data.docPath?.endsWith(".pdf") == true) {
                // Keep the circular background from layout
                imageView.background = ContextCompat.getDrawable(imageView.context, R.drawable.ic_doc_business_empty)
                
                // Add padding so PDF icon fits nicely inside the circular background
                val padding = imageView.context.resources.getDimensionPixelSize(R.dimen.size_12dp)
                imageView.setPadding(padding, padding, padding, padding)

                // Use fitCenter instead of circleCrop - this scales down to fit WITHOUT cropping
                GlideApp.with(imageView.context)
                    .load(R.drawable.pdf_svg)
                    .apply(RequestOptions().fitCenter())
                    .into(imageView)

                data.docPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val fileSizeKb = file.length() / 1024
                        textView.text = "$fileSizeKb KB"
                        textView.visibility = View.VISIBLE
                    } else {
                        textView.visibility = View.GONE
                    }
                }
            } else {
                // Reset for actual images - no background, no padding, use circleCrop
                imageView.background = null
                imageView.setPadding(0, 0, 0, 0)
                loadImage(data.docPath, imageView, textView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun loadImage(
            path: String?,
            view: ImageView,
            textView: TextView
    ) {
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                val imageViewHeight = view.measuredHeight
                val imageViewWidth = view.measuredWidth
                path?.let {
                    // Clear placeholder background when loading actual image
                    view.background = null
                    // Reset padding for images
                    view.setPadding(0, 0, 0, 0)
                    GlideApp.with(view.context)
                            .load(bitmapUtils.decodeSampledBitmap(path, imageViewWidth, imageViewHeight))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                            .listener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                        isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }

                                override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                ): Boolean {
                                    var byteCount = Int.MIN_VALUE
                                    byteCount = (resource as BitmapDrawable).bitmap.byteCount / 1024
                                    textView.text = byteCount.toString() + " KB"
                                    return false
                                }

                            }).circleCrop().into(view)
                }
                return true
            }
        })
    }

    // DocumentListInterface implementation for secure download API
    override fun onDocumentListSuccess(files: List<FileData>) {
        if (files.isNotEmpty()) {
            val documentFile = files.first()
            // ✅ Use s3_path from response to find the original URL and download type
            val s3Path = documentFile.s3Path
            val originalUrl = s3Path?.let { s3PathToUrlMap[it] }
            val downloadType = s3Path?.let { s3PathDownloadType[it] }
            val dataFromList = originalUrl?.let { dList[it] }
            
            // Validate based on download type
            // For VIEW_DETAILS (downloadLogo), we only need originalUrl
            // For PREVIEW_IMAGE (downloadImage), we need both originalUrl and dataFromList
            if (originalUrl == null || downloadType == null) {
                Log.e(LOG_TAG, "Validation failed: originalUrl or downloadType is null for s3Path: $s3Path")
                uiUtils.hideProgress()
                uiUtils.showSnackbar("Unable to determine original document URL")
                // Clean up
                s3Path?.let { 
                    s3PathToUrlMap.remove(it)
                    s3PathDownloadType.remove(it)
                }
                return
            }
            
            if (downloadType == DownloadType.PREVIEW_IMAGE && dataFromList == null) {
                Log.e(LOG_TAG, "Validation failed: dataFromList is null for PREVIEW_IMAGE, URL: $originalUrl")
                uiUtils.hideProgress()
                uiUtils.showSnackbar("Unable to find document data")
                // Clean up
                s3Path?.let { 
                    s3PathToUrlMap.remove(it)
                    s3PathDownloadType.remove(it)
                }
                return
            }
            
            // Download file from pre-signed URL
            downloadFileFromUrl(documentFile.downloadUrl, originalUrl, downloadType)
        } else {
            Log.e(LOG_TAG, "API returned empty files list")
            uiUtils.hideProgress()
            uiUtils.showSnackbar("No documents found")
        }
    }

    override fun onDocumentListFailure(error: String) {
        Log.e(LOG_TAG, "API failed: $error")
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Download failed: $error")
    }

    /**
     * Download file from pre-signed URL using OkHttpClient
     * ✅ Now thread-safe: each download tracked by its own downloadType
     * ✅ FIX #2: Uses downloadCompositeDisposable to persist across fragment lifecycle
     */
    private fun downloadFileFromUrl(downloadUrl: String, originalDocUrl: String, downloadType: DownloadType) {
        
        compositeDisposable += io.reactivex.Observable.fromCallable {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(downloadUrl)
                .build()
            
            val response: Response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val file = if (downloadType == DownloadType.VIEW_DETAILS) {
                    getFile(originalDocUrl)
                } else {
                    getImageFile(originalDocUrl)
                }
                
                if (file != null) {
                    response.body()?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    file.absolutePath
                } else {
                    Log.e(LOG_TAG, "File creation failed for: $originalDocUrl")
                    null
                }
            } else {
                Log.e(LOG_TAG, "HTTP request failed with code: ${response.code()} for URL: $downloadUrl")
                null
            }
        }
        .onBackground()
        .subscribe({ filePath ->
            // ✅ FIX #2: Check if fragment is still attached before updating UI
            if (!isAdded || context == null) {
                return@subscribe
            }
            
            if (filePath != null) {
                if (downloadType == DownloadType.VIEW_DETAILS) {
                    // For downloadLogo
                    uiUtils.showSnackbar("File downloaded successfully")
                } else {
                    // For downloadImage - ✅ Look up correct data from dList using originalDocUrl
                    val data = dList[originalDocUrl]
                    
                    if (data != null) {
                        data.docPath = filePath
                        dList[originalDocUrl] = data  // ✅ Update dList
                        
                        // ✅ FIX: Post notification to avoid "computing layout" crash
                        binding.attachmentList.post {
                            docRVAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Log.e(LOG_TAG, "Data not found in dList for URL: $originalDocUrl")
                    }
                }
            } else {
                uiUtils.showSnackbar("Download failed")
            }
            
            uiUtils.hideProgress()
            
            // ✅ Clean up mappings for this specific download
            val s3Path = extractS3PathFromUrl(originalDocUrl)
            s3Path?.let { 
                s3PathToUrlMap.remove(it)
                s3PathDownloadType.remove(it)
            }
        }, { error ->
            Log.e(LOG_TAG, "Download error for $originalDocUrl: ${error.message}")
            
            // ✅ FIX #2: Check if fragment is still attached before updating UI
            if (!isAdded || context == null) {
                return@subscribe
            }
            
            uiUtils.hideProgress()
            uiUtils.showSnackbar("Download error: ${error.message}")
            
            // ✅ Clean up mappings on error
            val s3Path = extractS3PathFromUrl(originalDocUrl)
            s3Path?.let { 
                s3PathToUrlMap.remove(it)
                s3PathDownloadType.remove(it)
            }
        })
    }
}