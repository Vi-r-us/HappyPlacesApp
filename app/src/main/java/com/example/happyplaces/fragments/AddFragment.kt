package com.example.happyplaces.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.happyplaces.R
import com.example.happyplaces.activities.HappyPlaceDetailActivity
import com.example.happyplaces.database.PlaceApp
import com.example.happyplaces.database.PlaceDao
import com.example.happyplaces.database.PlaceEntity
import com.example.happyplaces.databinding.FragmentAddBinding
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentAddBinding? = null

    private val builder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
    private val picker: MaterialDatePicker<*> = builder.build()

    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mPlaceDetail: PlaceEntity? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    // Gallery Intent
    private var galleryImageResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data!=null) {
                    val contentUri = data.data
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver, contentUri)

                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image: ", "Path :: $saveImageToInternalStorage")

                        binding?.ivPlaceUploadedPhoto?.setImageBitmap(selectedImageBitmap)
                        binding?.ivPlaceUploadedPhoto?.visibility = View.VISIBLE
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(context, "Failed to load image from gallery",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    // Camera Intent
    private var cameraImageResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap

                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image: ", "Path :: $saveImageToInternalStorage")

                binding?.ivPlaceCameraPhoto?.setImageBitmap(thumbnail)
                binding?.ivPlaceCameraPhoto?.visibility = View.VISIBLE

            }
        }
    // Map Intent
    private var mapLocationResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e("AddHappyPlace", "${Activity.RESULT_OK}, ${result.resultCode}")

            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                Log.e("AddHappyPlace", "onActivityResult: place=${place}")

                binding?.etLocation?.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude

            } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status: Status? = Autocomplete.getStatusFromIntent(result.data!!)
                Log.e("AddHappyPlace", "onResult[PLACE] error: ${status?.statusMessage}")
            }
        }

    // Save the options selected in category after the fragment has been changed
    override fun onResume() {
        val categories = resources.getStringArray(R.array.Place_Category)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding?.actvCategory?.setAdapter(arrayAdapter)

        super.onResume()
    }

    private fun clearTextView() {
        binding?.etPlaceName?.setText("")
        binding?.actvCategory?.setText("")
        binding?.etDescription?.setText("")
        binding?.etDate?.setText("")

        binding?.etPhoneNumber?.setText("")
        binding?.etWebsite?.setText("")
        binding?.etEmail?.setText("")

        binding?.etLocation?.setText("")

        binding?.etStreetAddress?.setText("")
        binding?.etApt?.setText("")

        binding?.etState?.setText("")
        binding?.etCountry?.setText("")
        binding?.etZip?.setText("")

        binding?.ratingBar?.rating = 0.0f
        binding?.ivPlaceUploadedPhoto?.setImageDrawable(null)
        binding?.ivPlaceCameraPhoto?.setImageDrawable(null)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater, container, false)

        // Set builder
        builder.setTitleText("Select a Text")
        // Set StatusBar theme
        val window: Window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.add)
        window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.add)

        // Get Place Detail for update
        mPlaceDetail = arguments?.getParcelable(HomeFragment.EXTRA_PLACE_DETAILS) as PlaceEntity?
        // Fused Location Client initialization
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Setup Places Module
        if(!Places.isInitialized()) {
            Places.initialize(requireContext(), resources.getString(R.string.google_maps_api_key))
        }

        if (mPlaceDetail != null) {
            setAllEditText(mPlaceDetail!!)
            binding?.btnSave?.text = "Update"
        } else {
            clearTextView()
        }

        picker.addOnPositiveButtonClickListener {
            binding?.etDate?.setText(picker.headerText)
        }

        binding?.etDate?.setOnClickListener(this)
        binding?.cvUploadPhotoCard?.setOnClickListener(this)
        binding?.cvTakePhotoCard?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.btnMyLocation?.setOnClickListener(this)

        return binding?.root
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.et_date -> {
                builder.setTheme(R.style.MaterialCalendarTheme)
                picker.show(parentFragmentManager, picker.toString())
            }
            R.id.cv_uploadPhotoCard -> {
                choosePhotoFromGallery()
            }
            R.id.cv_takePhotoCard -> {
                takePhotoFromCamera()
            }
            R.id.btn_my_location -> {
                // If location is turned off
                if (!isLocationEnabled()) {
                    Toast.makeText(requireContext(),
                    "Your Location provider is turned off. Please turn on location",
                    Toast.LENGTH_LONG).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                // if location in on
                else {
                    Dexter.withContext(activity).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        @RequiresApi(Build.VERSION_CODES.S)
                        override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                            requestLocationData()
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<PermissionRequest>?,
                            p1: PermissionToken?
                        ) {
                            showRationalDialogForPermission()
                        }
                    }).onSameThread().check()
                }
            }
            R.id.et_location -> {
                try {
                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                                        Place.Field.ADDRESS)
                    val intent = Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(requireContext())
                    mapLocationResultLauncher.launch(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.btn_Save -> {
                // Remove required text
                binding?.tilPlaceName?.helperText = ""
                binding?.tilDescription?.helperText = ""
                binding?.tilLocation?.helperText = ""
                binding?.tilDate?.helperText = ""

                when {
                    binding?.etPlaceName?.text.isNullOrEmpty() -> {
                        binding?.tilPlaceName?.helperText = "Required"
                        binding?.svMain?.smoothScrollTo(0,0)
                    }
                    binding?.etDescription?.text.isNullOrEmpty() -> {
                        binding?.tilDescription?.helperText = "Required"
                        binding?.svMain?.smoothScrollTo(0,0)
                    }
                    binding?.etDate?.text.isNullOrEmpty() -> {
                        binding?.tilDate?.helperText = "Required"
                        binding?.svMain?.smoothScrollTo(0,0)
                    }
                    binding?.etLocation?.text.isNullOrEmpty() -> {
                        binding?.tilLocation?.helperText = "Required"
                        binding?.svMain?.smoothScrollTo(600,600)
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(requireContext(), "Require a Image",
                            Toast.LENGTH_LONG).show()
                    } else -> {
                        val placeDao = (requireActivity().application as PlaceApp).db.placeDao()
                        if (mPlaceDetail == null)
                            addRecord(placeDao)
                        else
                            updateRecord(placeDao, mPlaceDetail!!)

                        // Going to detail page of the Place
                        if (mPlaceDetail != null) {
                            val intent = Intent(requireContext(), HappyPlaceDetailActivity::class.java)
                            intent.putExtra(HomeFragment.EXTRA_PLACE_DETAILS, mPlaceDetail)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        mPlaceDetail = null
                    }
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val myLocationRequest = create()
        myLocationRequest.priority = LocationRequest.QUALITY_HIGH_ACCURACY
        myLocationRequest.interval = 1000
        myLocationRequest.numUpdates = 1

        Looper.myLooper()?.let {
            mFusedLocationClient.requestLocationUpdates(
                myLocationRequest, mLocationCallBack, it
            )
        }
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val mLastLocation: Location? = p0.lastLocation
            mLatitude = mLastLocation!!.latitude
            mLongitude = mLastLocation.longitude

            //Code to translate the lat and lng values to a human understandable address text
            val addressTask = GetAddressFromLatLng(requireContext(),
                lat = mLatitude,lng = mLongitude)
            addressTask.setCustomAddressListener(object : GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String) {
                    binding?.etLocation?.setText(address)
                }

                override fun onError() {
                    Log.e("Get address:: ", "onError: Something went wrong")
                    Toast.makeText(requireContext(),
                        "Something went Wrong",
                        Toast.LENGTH_LONG).show()
                }
            })

            lifecycleScope.launch(Dispatchers.IO) {
                // CoroutineScope tied to this LifecycleOwner's Lifecycle.
                // This scope will be cancelled when the Lifecycle is destroyed
                // starts the task to get the address in text from the lat and lng values
                addressTask.launchBackgroundProcessForRequest()
            }
        }
    }

    private fun setAllEditText(mPlaceDetail: PlaceEntity) {
        // Mandatory Fields
        binding?.etPlaceName?.setText(mPlaceDetail.title)
        binding?.etDate?.setText(mPlaceDetail.date)
        binding?.etDescription?.setText(mPlaceDetail.description)
        binding?.etLocation?.setText(mPlaceDetail.location)
        mLatitude = mPlaceDetail.latitude
        mLongitude = mPlaceDetail.longitude

        saveImageToInternalStorage = Uri.parse(mPlaceDetail.image)
        binding?.ivPlaceUploadedPhoto?.setImageURI(saveImageToInternalStorage)
        binding?.ivPlaceUploadedPhoto?.visibility = View.VISIBLE

        // Not-Mandatory Fields
        binding?.actvCategory?.setText(mPlaceDetail.category)
        binding?.ratingBar?.rating = mPlaceDetail.rating
        binding?.etStreetAddress?.setText(mPlaceDetail.streetAddress1)
        binding?.etApt?.setText(mPlaceDetail.streetAddress2)
        binding?.etCountry?.setText(mPlaceDetail.country)
        binding?.etState?.setText(mPlaceDetail.state)
        binding?.etZip?.setText(mPlaceDetail.zip)
        binding?.etPhoneNumber?.setText(mPlaceDetail.phoneNumber)
        binding?.etEmail?.setText(mPlaceDetail.emailAddress)
        binding?.etWebsite?.setText(mPlaceDetail.website)
    }

    private fun updateRecord(placeDao: PlaceDao, placeEntity: PlaceEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            placeDao.update( PlaceEntity(
                // Mandatory Fields
                id = placeEntity.id,
                title = binding?.etPlaceName?.text.toString(),
                date = binding?.etDate?.text.toString(),
                description = binding?.etDescription?.text.toString(),
                location = binding?.etLocation?.text.toString(),
                image = saveImageToInternalStorage.toString(),
                latitude = mLatitude,
                longitude = mLongitude,

                // Not-Mandatory Fields
                category = binding?.actvCategory?.text.toString(),
                rating = binding?.ratingBar?.rating!!.toFloat(),
                streetAddress1 = binding?.etStreetAddress?.text.toString(),
                streetAddress2 = binding?.etApt?.text.toString(),
                country = binding?.etCountry?.text.toString(),
                state = binding?.etState?.text.toString(),
                zip = binding?.etZip?.text.toString(),
                phoneNumber = binding?.etPhoneNumber?.text.toString(),
                emailAddress = binding?.etEmail?.text.toString(),
                website = binding?.etWebsite?.text.toString(),
                isFavorite = placeEntity.isFavorite
            ) )
        }
        Toast.makeText(requireContext(),
            "Record Updated", Toast.LENGTH_LONG).show()
    }

    private fun addRecord(placeDao: PlaceDao) {
        lifecycleScope.launch(Dispatchers.IO) {
            placeDao.insert( PlaceEntity(
                // Mandatory Fields
                title = binding?.etPlaceName?.text.toString(),
                date = binding?.etDate?.text.toString(),
                description = binding?.etDescription?.text.toString(),
                location = binding?.etLocation?.text.toString(),
                image = saveImageToInternalStorage.toString(),
                latitude = mLatitude,
                longitude = mLongitude,

                // Not-Mandatory Fields
                category = binding?.actvCategory?.text.toString(),
                rating = binding?.ratingBar?.rating!!.toFloat(),
                streetAddress1 = binding?.etStreetAddress?.text.toString(),
                streetAddress2 = binding?.etApt?.text.toString(),
                country = binding?.etCountry?.text.toString(),
                state = binding?.etState?.text.toString(),
                zip = binding?.etZip?.text.toString(),
                phoneNumber = binding?.etPhoneNumber?.text.toString(),
                emailAddress = binding?.etEmail?.text.toString(),
                website = binding?.etWebsite?.text.toString()
            ) )

            placeDao.getLastInsertedPlace().collect {
                mPlaceDetail = it
            }
        }
        Toast.makeText(requireContext(),
            "Record Saved", Toast.LENGTH_LONG).show()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(activity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryImageResultLauncher.launch(galleryIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?) {
                showRationalDialogForPermission()
                token?.continuePermissionRequest()
            }
        }).onSameThread().check()
    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(activity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraImageResultLauncher.launch(cameraIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?) {
                showRationalDialogForPermission()
                token?.continuePermissionRequest()
            }
        }).onSameThread().check()
    }

    // Save Image clicked with camera to gallery
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    // If permissions are not granted show Alert-Dialog
    private fun showRationalDialogForPermission() {
        Toast.makeText(requireContext(),
            "." ,
            Toast.LENGTH_SHORT).show()
        val builder = MaterialAlertDialogBuilder(requireContext())
        with(builder) {
            setTitle("Change Setting")
            setMessage("Its looks like you have turned off the " +
                    "permission required for this feature. It can be enabled under the Application's " +
                    "Setting.")
            setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName  , null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()

        val buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        with(buttonPositive) {
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
        val buttonNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        with(buttonNegative) {
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }



    companion object {
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

}