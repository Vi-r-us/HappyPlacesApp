package com.example.happyplaces.fragments

import android.Manifest
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.happyplaces.R
import com.example.happyplaces.databinding.FragmentAddBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentAddBinding? = null

    private val builder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
    private val picker: MaterialDatePicker<*> = builder.build()

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

                        val saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
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

                val saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image: ", "Path :: $saveImageToInternalStorage")

                binding?.ivPlaceCameraPhoto?.setImageBitmap(thumbnail)
                binding?.ivPlaceCameraPhoto?.visibility = View.VISIBLE

            }
        }

    // Save the options selected in category after the fragment has been changed
    override fun onResume() {
        val categories = resources.getStringArray(R.array.Place_Category)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding?.actvCategory?.setAdapter(arrayAdapter)

        super.onResume()
    }

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

        picker.addOnPositiveButtonClickListener {
            binding?.etDate?.setText(picker.headerText)
        }

        binding?.etDate?.setOnClickListener(this)
        binding?.cvUploadPhotoCard?.setOnClickListener(this)
        binding?.cvTakePhotoCard?.setOnClickListener(this)

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
        }
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