package com.compumovil.taller_2.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.compumovil.taller_2.databinding.FragmentCameraBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding
    private lateinit var currentFilePath: String
    private lateinit var fileURI: Uri
    private val REQUEST_CAPTURE = 1
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // Check if the permission has been granted or request it
    private fun checkStoragePermissionAndGetImage() {
        val permission = if(binding.isPhotoOrVideoSwitchCamara.isChecked) Manifest.permission.CAMERA else Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permisss granted
                dispatchTakePictureIntent()
            }
            else -> {
                // Permiss not granted, request it
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                println("Permiso no concedido")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        binding.buttonCamera.setOnClickListener { checkStoragePermissionAndGetImage() }

        return binding.root
    }

    @Throws(IOException::class)
    private fun createImageOrVideoFile(isVideo: Boolean): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? =
            activity?.getExternalFilesDir(if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${if (isVideo) "MP4" else "JPEG"}_${timeStamp}_", /* prefix */
            if (isVideo) ".mp4" else ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentFilePath = absolutePath
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(if (binding.isPhotoOrVideoSwitchCamara.isChecked) MediaStore.ACTION_VIDEO_CAPTURE else MediaStore.ACTION_IMAGE_CAPTURE).also { takeIntent ->
            // Ensure that there's a camera activity to handle the intent
            takeIntent.resolveActivity(activity?.packageManager!!).also {
                // Create the File where the photo should go
                val routeFile: File? = try {
                    createImageOrVideoFile(binding.isPhotoOrVideoSwitchCamara.isChecked)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    null
                }
                // Continue only if the File was successfully created
                routeFile?.also { route ->
                    fileURI = FileProvider.getUriForFile(
                        activity?.applicationContext!!,
                        "com.compumovil.taller_2.fileprovider",
                        route
                    )
                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileURI)
                    takeIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024 * 1024)
                    startActivityForResult(takeIntent, REQUEST_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("request codd ${requestCode} result code ${resultCode}")
        when {
            requestCode == REQUEST_CAPTURE && resultCode == RESULT_OK -> {
                binding.previewCamara.removeAllViews()
                val newView =
                    if (!binding.isPhotoOrVideoSwitchCamara.isChecked)
                        ImageView(activity)
                    else
                        VideoView(activity)

                newView.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
                binding.previewCamara.foregroundGravity = View.TEXT_ALIGNMENT_CENTER

                if (!binding.isPhotoOrVideoSwitchCamara.isChecked) {
                    (newView as ImageView).setImageURI(fileURI)
                    newView.scaleType = ImageView.ScaleType.FIT_CENTER
                    newView.adjustViewBounds = true
                } else {
                    (newView as VideoView).setVideoURI(fileURI)
                    newView.setMediaController(MediaController(activity))
                    newView.start()
                    newView.setOnPreparedListener { mp ->
                        mp.isLooping = true
                    }

                    // Add an OnErrorListener to handle any playback errors
                    newView.setOnErrorListener { _, what, extra ->
                        // Log or handle the error
                        println("Video playback error: $what, $extra")
                        true
                    }
                }
                binding.previewCamara.addView(newView)
            }
            REQUEST_CAPTURE == requestCode && resultCode != RESULT_OK -> {
                File(currentFilePath).delete()
            }
        }
    }

}