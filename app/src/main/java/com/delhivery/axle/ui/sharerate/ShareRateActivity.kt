package com.delhivery.axle.ui.sharerate

import android.Manifest
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.request.PriceDetailRequest
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.api.response.GetSupplierRewardsResponse
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.yourrewards.YourRewardsItemData
import com.delhivery.axle.database.entity.OffersEntity
import com.delhivery.axle.databinding.ActivityShareRateBinding
import com.delhivery.axle.databinding.DialogBottomTruckValueBinding
import com.delhivery.axle.databinding.DialogRateUploadSuccessBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_TYPE
import com.delhivery.axle.fcm.ARGS_OFFER_ID
import com.delhivery.axle.fcm.ARGS_PRICING_ID
import com.delhivery.axle.fcm.ARGS_PRICING_SORT_KEY
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.businessverification.DocUploadAdapter
import com.delhivery.axle.ui.home.activity.home.OFFER_LANE_UPLOADED
import com.delhivery.axle.ui.home.activity.home.OFFER_REJECTED
import com.delhivery.axle.ui.loadAlert.HomeLoadAlertRequestItemData
import com.delhivery.axle.ui.searchCity.searchCityIntent
import com.delhivery.axle.ui.trucks.TruckSizeAdapter
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject


/**
 * Share rate screen
 */
class ShareRateActivity : BaseActivity<ActivityShareRateBinding, ShareRateViewModel>(), DatePickerDialog.OnDateSetListener, AWSUtils.AWSProgressInterface {

    companion object {
        val __INSTANCE: ShareRateActivity by lazy { ShareRateActivity() }
    }

    override fun getViewModelClass() = ShareRateViewModel::class.java

    override fun layoutId() = R.layout.activity_share_rate

    override fun requireConnection() = false

    var capacityArr = mutableListOf<String>()
    var sourcedAs : String = ""
    var truckItems = mutableListOf<TruckResponseArray>()
    var homeLoadAlertRequestItemData= HomeLoadAlertRequestItemData(inventoryUuid = "")
    val adapter : TruckSizeAdapter by lazy { TruckSizeAdapter() }
    private var calendar: Calendar = Calendar.getInstance()
    var proofTypeFilled = false
    var docUploadProof = false
    var offerId:String? = ""
    var startTime: Long = 0
    var endTime: Long = 0
    private val CityType = "city_type"

    private var isCamera: Boolean = false
    private var mPhotoFile: File? = null
    private lateinit var uploadImageName: String
    private lateinit var localImageName: String
    val awsPath = "loadboard/sharerate/"
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter() }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()

    @Inject
    lateinit var imageUtils: ImageUtils
    @Inject
    lateinit var awsUtils: AWSUtils
    @Inject
    lateinit var fileCompressor: FileCompressor
    @Inject
    lateinit var bitmapUtils: BitmapUtils
    @Inject
    lateinit var userPrefs: UserPrefs

    var itemTD:String? = null
    var offerTD:String? = null
    var priceId:String? = null
    var priceSortKey:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if( intent?.extras?.getString(ARGS_NOTIFICATION_TYPE)!=null) {
            if(intent?.extras?.getString(ARGS_NOTIFICATION_TYPE)== OFFER_REJECTED) {
                priceId = intent?.extras?.getString(ARGS_PRICING_ID)
                priceSortKey = intent?.extras?.getString(ARGS_PRICING_SORT_KEY)

                if (priceId != null && priceSortKey != null) {
                    viewModel.getPricingData(PriceDetailRequest(priceId!!, priceSortKey!!))
                }
            }else if(intent?.extras?.getString(ARGS_NOTIFICATION_TYPE)== OFFER_LANE_UPLOADED){
               if(intent?.extras?.getString(ARGS_OFFER_ID)!=null)
                viewModel.searchOffer(intent?.extras?.getString(ARGS_OFFER_ID)!!).observe(this, Observer {
                    if (it!=null) {
                        val yourRewardsItemData = YourRewardsItemData(pricingId=it.offerId!!, originCity = it.oc, originCityCode = it.occ, destinationCity = it.dc, destinationCityCode = it.dcc, truckDisplayName = it.tdn)
                        fillODVTData(yourRewardsItemData)
                    }
                })

            }
        }
        viewModel.origin = intent?.extras?.getString("originname")?.let { CityModel(it, intent?.extras?.getString("occ")) }
        viewModel.destination = intent?.extras?.getString("destname")?.let { CityModel(it, intent?.extras?.getString("dcc")) }
        //viewModel.getCityData(viewModel.origin?.orionDbCityCode, "origin")
        //viewModel.getCityData(viewModel.destination?.orionDbCityCode, "dest")

        startTime = System.currentTimeMillis()

        binding.editTruckNumber.setFilters(arrayOf<InputFilter>(AllCaps()))


        if( intent?.extras?.getString("offerid")!=null) {
            offerId = intent?.extras?.getString("offerid")
        }

        if( intent?.extras?.getString("itemTD")!=null) {
            itemTD = intent?.extras?.getString("itemTD")
        }

        if( intent?.extras?.getString("offerTD")!=null) {
            offerTD = intent?.extras?.getString("offerTD")
        }

        if(itemTD!=null){
            if(itemTD.equals(offerTD)){
                viewModel.selected_truck_type = itemTD as String
                if(intent?.extras?.getString("truckCapacity")!=null && !intent?.extras?.getString("truckCapacity").toString().equals("null")) {
                    viewModel.selected_truck_capacity = intent?.extras?.getString("truckCapacity")
                }
            }
        }

        if(viewModel.selected_truck_capacity.isNotNullOrEmpty() &&  viewModel.selected_truck_type.isNotNullOrEmpty()) {
                binding.textTruckSize.text = viewModel.selected_truck_type + " " + viewModel.selected_truck_capacity.toString()
        }else {
                binding.textTruckSize.text = viewModel.selected_truck_type
        }

        if(intent?.extras?.getString("truckNumber")!=null) {
            viewModel.selected_vehicle_number = intent?.extras?.getString("truckNumber")!!
            binding.editTruckNumber.setText(viewModel.selected_vehicle_number)
        }



        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
        title = "Share rate & earn reward"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setOriginDestinationTruck()

        binding.editTruckSize.setOnClickListener {
            if(viewModel.selected_truck_type.isNotNullOrEmpty() && viewModel.selected_truck_capacity.isNullOrEmpty()) {
                showTruckSizeDialog("", "fill")
            }else{
                showTruckSizeDialog("", null)
            }
        }

        binding.uploadDocLay.setOnClickListener {
            val imageName = "IMG_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
        }
        binding.docRemove.setOnClickListener {
            showUploadImage()
        }

        binding.editDate.setOnClickListener {
            dialogUtils.datePicker(listener = this@ShareRateActivity, maxDate = 0, minDate = -6)
        }

        viewModel.truckGetLiveData.observe(this, Observer {
            if (it != null) {
                truckItems.addAll(it)
                if (!homeLoadAlertRequestItemData.truckSpecifications.isNullOrEmpty()) {
                    if (homeLoadAlertRequestItemData.truckSpecifications?.get(0)!!.truck_uuid.isNotNullOrEmpty()
                    ) {
                        showTruckSizeDialog(
                                homeLoadAlertRequestItemData.truckSpecifications?.get(0)!!.truck_uuid, null
                        )
                    }
                }
            }
        })

        viewModel.pricingLiveData.observe(this, Observer {
            if (it != null) {
                fillODVTData(it.pricingData.get(0))
            }
        })


        binding.spinnerProof.setup(R.array.array_rate_proof) { p, v ->
            if(p>0){
                proofTypeFilled = true
                viewModel.proofType = v
               enableSaveAlertButton()
            }else{
                proofTypeFilled =false
                viewModel.proofType = null
                enableSaveAlertButton()
            }
        }

        binding.editPriceAddTruck.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null && p0.toString().trim().isNotNullOrEmpty()) {
                    viewModel.expectedPrice = p0.toString().toDouble()
                } else {
                    viewModel.expectedPrice = null
                }
                enableSaveAlertButton()
            }
        })

        binding.editTruckNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                viewModel.selected_vehicle_number = p0.toString()
                enableSaveAlertButton()
            }
        })

        initializeData()

        viewModel.priceUnit= "PMT"

        binding.btnRadioPerTon.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                viewModel.priceUnit= "PMT"
            }
        }

        binding.btnRadioFixed.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                viewModel.priceUnit= "FTL"
            }
        }

        binding.layOrigin.setOnClickListener {
            startActivityForResult(searchCityIntent(this, "origin"), REQCODE_SELECT_CITY)

        }
        binding.layDest.setOnClickListener {
            startActivityForResult(searchCityIntent(this, "destination"), REQCODE_SELECT_CITY)
        }

        binding.btnSubmitDetails.setOnClickListener {
            binding.errorLane.visibility = View.GONE
            if(!viewModel.selected_vehicle_number?.let { it1 -> validateTruckNumber(it1) }) {
                binding.editTruckNumber.error = "Invalid vehicle number entered"
                uiUtils.showToast("Invalid vehicle number entered")
            }else {
                    endTime = System.currentTimeMillis()
                    val ttl = endTime - startTime
                    analyticsUtil.trackEvent(
                            EVENT_SUBMIT_OFFER,
                            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL, PROPERTY_OFFER_ID),
                            mutableListOf(userPrefs.userId(), userPrefs.phoneNumber
                                    ?: "dummy", ttl.toString(), offerId ?: "")
                    )

                    if(viewModel.selected_truck_capacity.isNotNullOrEmpty() && !viewModel.selected_truck_capacity.toString().equals("null")){
                        viewModel.sharerate()
                        binding.errorTruck.visibility = View.GONE
                    }else{
                        uiUtils.showToast("Please choose both truck size and capacity")
                        binding.errorTruck.text = "Please choose both truck size and capacity"
                        binding.errorTruck.visibility = View.VISIBLE
                    }
            }
        }

        viewModel.rateUpdatedLiveData.observe(this, Observer {
            if (it != null && it) {
                showSuccessDialog()
            }
        })

        viewModel.errorrateUpdatedLiveData.observe(this, Observer {
            if (it != null && it.isNotNullOrEmpty()) {
                if (it.equals("The selection is not applicable for current offer period")) {
                    binding.errorLane.text = it
                    binding.errorLane.visibility = View.VISIBLE
                }
                uiUtils.showToast(it)
                viewModel.errorrateUpdatedLiveData.postValue(null)
            }
        })

        viewModel.delegationLiveData.observe(this, Observer {
            uploadImage(it.first, it.second)
        })
    }

   private fun setOriginDestinationTruck(){
       try {
           binding.editOrigin.setText(viewModel.origin!!.cityName())
           binding.editOrigin.isEnabled = false
           binding.editDestination.setText(viewModel.destination!!.cityName())
           binding.editDestination.isEnabled = false
           viewModel.selected_vehicle_number = binding.editTruckNumber.text.toString()
       }catch (e: Exception){

       }
    }

    private fun fillODVTData(it:YourRewardsItemData){
        viewModel.origin = CityModel(it.originCity!!,it.originCityCode!!)
        viewModel.destination= CityModel(it.destinationCity!!,it.destinationCityCode!!)
        if(it.sortKey!=null) {
            offerId = it.sortKey
        }

        if(it.truckDisplayName!=null) {
            offerTD = it.truckDisplayName
            itemTD = it.truckDisplayName
        }

        if(itemTD!=null){
            if(itemTD.equals(offerTD)){
                viewModel.selected_truck_type = itemTD as String
                if(it.truckCapacity!=null && !it.truckCapacity.toString().equals("null")) {
                    viewModel.selected_truck_capacity = it.truckCapacity
                }
            }
        }

        if(viewModel.selected_truck_capacity.isNotNullOrEmpty() &&  viewModel.selected_truck_type.isNotNullOrEmpty()) {
            binding.textTruckSize.text = viewModel.selected_truck_type + " " + viewModel.selected_truck_capacity.toString()+" MT"
        }else {
            binding.textTruckSize.text = viewModel.selected_truck_type
        }

        if(it.vehicleNumber!=null) {
            viewModel.selected_vehicle_number =it.vehicleNumber
            binding.editTruckNumber.setText(viewModel.selected_vehicle_number)
        }
        if(it.rejectionReason!=null) {
            var rejectedReason = getString(string.rejected_due)+" "+it.rejectionReason.replace("_"," ")
            binding.errorLane.setText(rejectedReason)
            binding.errorLane.visibility = View.VISIBLE
        }
        setOriginDestinationTruck()
    }
    private fun showSuccessDialog() {
        runOnUiThread {
                val dialog = Dialog(this)
                val bindingDialog= DialogRateUploadSuccessBinding.inflate(layoutInflater)

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(bindingDialog.root)
                dialog.setCancelable(false)

                bindingDialog.buttonCancel.setOnClickListener {
                    onBackPressed()
                    dialog.dismiss()
                }

                dialog.show()
                dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val back = ColorDrawable(Color.TRANSPARENT)
                val inset = InsetDrawable(back, 30)
                dialog.window!!.setBackgroundDrawable(inset)
                dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
                dialog.window!!.setGravity(Gravity.BOTTOM)
        }
    }

    private fun initializeData() {
        viewModel.fetchTruckType()
       // viewModel.fetchSupplierTrucks()
    }

    private fun showUploadImage() {
        binding.uploadDocLay.visibility=View.VISIBLE
        binding.docUploadedLay.visibility=View.GONE
        if(uploadArray.size>0){
            uploadArray.clear()
        }
        viewModel.documentProofUrl.clear()
        docUploadProof= false
        enableSaveAlertButton()
    }

    private fun validateTruckNumber(number: String): Boolean{
        val pattern = Pattern.compile(
                "[a-zA-Z]{2}((([0-9]{1,2}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}\$|^[a-zA-Z]{3}[0-9]{4}"
        )
        return pattern.matcher(number).matches()
    }

    private fun enableSaveAlertButton() {
                binding.btnSubmitDetails.isEnabled = viewModel.tripDate.isNotNullOrEmpty()
                        && viewModel.origin != null
                        && viewModel.destination != null
                        && viewModel.selected_truck_type.isNotNullOrEmpty() &&
                        viewModel.selected_vehicle_number.isNotNullOrEmpty()
                        && viewModel.expectedPrice != null
                        && viewModel.expectedPrice.toString().trim().isNotNullOrEmpty()
                        && viewModel.expectedPrice!! > 0
                        && viewModel.selected_truck_type.isNotNullOrEmpty()
                        && viewModel.proofType.isNotNullOrEmpty()
                        && docUploadProof
    }

    private fun showTruckSizeDialog(type1: String, typ: String?) {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.selectText.text = "Choose truck size & Capacity"
        bindingDialog.selectText2.visibility = View.VISIBLE
        bindingDialog.selectText2.text = "Choose Size \u2022 Step 1 of 2"

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        val truckSizeList = mutableListOf<TruckResponseArray>()

        for(truck in (truckItems.sortedByDescending { it.truckUuid }.reversed()).sortedBy { it.defaultMG }){
                truckSizeList.add(truck)
        }
        adapter.setItems(truckSizeList)
        bindingDialog.truckList.adapter = this@ShareRateActivity.adapter

        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckSize.text = adapter.getItem(position).truckUuid
            sourcedAs = adapter.getItem(position).sourcedAs?: ""
            var min = adapter.getItem(position).minCapacity
            val max = adapter.getItem(position).maxCapacity
            viewModel.selected_truck_capacity = ""
            viewModel.selected_truck_type = adapter.getItem(position).truckUuid.toString()
            capacityArr.clear()
            if(min !=null &&  max!=null){
                while (min <= max) {
                    capacityArr.add("$min MT")
                    min += (1.0)
                }
            }
            adapter.notifyDataSetChanged()
            dialog.dismiss()
            showTruckCapacityDialog()
            enableSaveAlertButton()
        }


        if(type1.isNotNullOrEmpty()){
            var pos:Int=0
            for (truckSize in truckSizeList){
                if(truckSize.truckUuid.equals(type1)){
                    break
                }
                pos=pos+1

            }
            binding.textTruckSize.text = viewModel.selected_truck_type +" "+viewModel.selected_truck_capacity.toString()
            sourcedAs = adapter.getItem(pos).sourcedAs?: ""
            var min = adapter.getItem(pos).minCapacity
            val max = adapter.getItem(pos).maxCapacity
            capacityArr.clear()
            if(min !=null &&  max!=null){
                while (min <= max) {
                    capacityArr.add("$min MT")
                    min += (1.0)
                }
            }
            dialog.dismiss()
            enableSaveAlertButton()
        }

        if(typ!=null && typ.equals("fill")){
            binding.textTruckSize.text = viewModel.selected_truck_type.toString()
            var itemData:TruckResponseArray? = null
            for(r in truckSizeList){
                if(r.truckDisplayName.equals(viewModel.selected_truck_type)){
                    itemData = r
                }
            }
            sourcedAs = itemData?.sourcedAs?: ""
            var min = itemData?.minCapacity
            val max = itemData?.maxCapacity
            viewModel.selected_truck_capacity = ""
            //viewModel.selected_truck_type = adapter.getItem(position).truckUuid.toString()
            capacityArr.clear()
            if(min !=null &&  max!=null){
                while (min <= max) {
                    capacityArr.add("$min MT")
                    min += (1.0)
                }
            }
            adapter.notifyDataSetChanged()
            dialog.dismiss()
            showTruckCapacityDialog()
            enableSaveAlertButton()
        }else{
            if (!type1.isNotNullOrEmpty()) {
                dialog.show()
            }
        }

        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showTruckCapacityDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.selectText.text = "Choose truck size & Capacity"
        bindingDialog.selectText2.visibility = View.VISIBLE
        bindingDialog.selectText2.text = "Choose Capacity \u2022 Step 2 of 2"

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, capacityArr);
        bindingDialog.truckList.adapter = adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            var str = binding.textTruckSize.text
            binding.textTruckSize.text = "$str "+adapter.getItem(position)
            viewModel.selected_truck_capacity =  if(adapter.getItem(position)?.isNotEmpty() == true)
                adapter.getItem(position).toString().split("\\s+".toRegex())[0].toDouble().toString() else "0.0"
            dialog.dismiss()
            enableSaveAlertButton()
        }
        adapter.notifyDataSetChanged();

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    override fun onDateSet(
            p0: DatePicker?,
            year: Int,
            month: Int,
            day: Int
    ){
        calendar.set(year, month, day)
        val format = "%1$02d"
        val dy = String.format(format, day)
        val yr = String.format(format, year)
        val mt = String.format(format, month + 1)
        val dateTime = "$dy/$mt/$yr"
        binding.textDate.text = dateTime

        val time: Date = calendar.getTime()
        val outputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"))
        val dateAsString: String = outputFmt.format(time)
        viewModel.tripDate = dateAsString
        enableSaveAlertButton()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            mPhotoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider", mPhotoFile!!
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQCODE_TAKE_PHOTO)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(localImageName, ".jpg", storageDir)
    }

    private fun dispatchGalleryIntent() {

        val intent = Intent()
        intent.type = "*/*"
        val mimetypes = arrayOf("image/*", "application/pdf")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
                intent,
                REQCODE_FILE_ATTACHMENTS
        )
    }

    private fun uploadImage(
            delegationToken: DelegationToken,
            file: File
    ) {
        uiUtils.showProgress()
        val path = "$awsPath$uploadImageName"
        awsUtils.startUpload(delegationToken, path, file, this)
    }

    private fun captureImage(
            uploadImageName: String,
            localImageName: String
    ) {
        this.uploadImageName = uploadImageName
        this.localImageName = localImageName

        val items = arrayOf<CharSequence>("Take Photo", "Choose from file", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> requestImageCapturePermissions(true)
                items[item] == "Choose from file" -> requestImageCapturePermissions(false)
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun requestImageCapturePermissions(isCamera: Boolean) {
        this.isCamera = isCamera
        compositeDisposable += requestPermission(
                arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                )
        )
                .onBackground()
                .subscribe { granted, error ->
                    if (error == null && granted) {
                        if (isCamera) {
                            dispatchTakePictureIntent()
                        } else {
                            dispatchGalleryIntent()
                        }
                    } else {
                        uiUtils.showSnackbar(getString(R.string.storage_camera_permission))
                    }
                }

    }


    private fun showFileSelected() {
        binding.uploadDocLay.visibility=View.GONE
        binding.docUploadedLay.visibility=View.VISIBLE
        docUploadProof=true
        binding.docTitle.setText(uploadArray.get(0).first)
        binding.docSize.setText(uploadArray.get(0).second + " KB")
        enableSaveAlertButton()
    }




    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQCODE_CAMERA -> {
                requestImageCapturePermissions(isCamera)
            }
        }
    }

    override fun onAWSSuccess(
            path: String
    ) {
        uiUtils.hideProgress()
        val s3url= awsUtils.awsBasePath()
        viewModel.documentProofUrl.add(s3url + path)
        uploadArray.add(Pair(path.replace(awsPath, ""), (mPhotoFile?.length()?.div(1024)).toString()))
        showFileSelected()
        enableSaveAlertButton()
        resetUploadData()
    }
//    private fun setBannerTExt() {
//        val configSettings = FirebaseRemoteConfigSettings.Builder()
//            .setMinimumFetchIntervalInSeconds(0)
//            .build()
//
//        val remoteConfig = FirebaseRemoteConfig.getInstance()
//        remoteConfig.setConfigSettingsAsync(configSettings)
//
//        FirebaseRemoteConfig.getInstance()
//            .fetchAndActivate()
//            .addOnCompleteListener(
//                this
//            ) {
//                if (it.isSuccessful) {
//
//                    try{
//                       viewModel.bannerText= remoteConfig.getString("advert_share_rate_page_banner_text")
//
//                    } catch (e: Exception) {
//                        //Do Nothing
//                    }
//                }
//            }
//
//    }

    override fun onAWSFailure() {
        uiUtils.hideProgress()
        resetUploadData()
    }
    private fun resetUploadData() {
        mPhotoFile = null
        uploadImageName = ""
        localImageName = ""
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQCODE_TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (mPhotoFile == null) {
                        uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                        return
                    }

                    try {
                        mPhotoFile = imageUtils.compressToFile(mPhotoFile!!, localImageName)
                        uiUtils.showProgress()
                        viewModel.getDelegationToken(mPhotoFile!!)
                    } catch (e: IOException) {
                        uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                    }
                } else {
                    uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                }
            }

            REQCODE_SELECT_CITY -> {
                if (data != null) {
                    val type = data.getStringExtra(CityType)
                    val city = data.getSerializableExtra("City") as CityModel
                    if (type == "origin") {
                        viewModel.origin = city
                        binding.editOrigin.setText(city.cityName().trim())
                    } else if (type == "destination") {
                        viewModel.destination = city
                        binding.editDestination.setText(city.cityName().trim())
                    }
                }
            }

            REQCODE_FILE_ATTACHMENTS -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val selectedFile = data?.data
                        require(selectedFile != null)
                        val parcelFileDescriptor =
                                contentResolver?.openFileDescriptor(selectedFile, "r", null)
                        require(parcelFileDescriptor != null)
                        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                        require(
                                contentResolver != null && contentResolver?.getFileName(selectedFile) != null
                        )
                        val imageScopedFile =
                                File(cacheDir, contentResolver?.getFileName(selectedFile)!!)
                        val outputStream = FileOutputStream(imageScopedFile)
                        IOUtils.copy(inputStream, outputStream)
                        this.uploadImageName = "IMG_" + System.currentTimeMillis() + "." + imageScopedFile.extension
                        this.localImageName = "IMG_" + System.currentTimeMillis() + "." + imageScopedFile.extension
                        if (imageScopedFile.extension == ".jpg" || imageScopedFile.extension == ".png" || imageScopedFile.extension == ".jpeg") {
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        } else {
                            mPhotoFile = imageScopedFile
                        }

                        if (mPhotoFile == null) {
                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }
                        uiUtils.showProgress()
                        viewModel.getDelegationToken(mPhotoFile!!)
                    } catch (e: IOException) {
                        uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                    }
                } else {
                    uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                }
            }
        }
    }
}

