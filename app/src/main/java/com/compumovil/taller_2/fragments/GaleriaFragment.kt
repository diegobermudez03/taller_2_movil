package com.compumovil.taller_2.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.compumovil.taller_2.databinding.FragmentGaleriaBinding

class GaleriaFragment : Fragment() {
    private lateinit var binding: FragmentGaleriaBinding


    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickMediaLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startGallery(binding.isPhotoOrVideoSwitchGaleria.isChecked)
            } else {
                println("Permiso no concedido")
            }
        }

        // Register launcher
        pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                displayMedia(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGaleriaBinding.inflate(inflater, container, false)

        // Configurar el botón para abrir la galería
        binding.buttonGallery.setOnClickListener {
            checkStoragePermissionAndGetImage()
        }

        return binding.root
    }

    // Check if the permission has been granted or request it
    private fun checkStoragePermissionAndGetImage() {
        val permission = when {
            android.os.Build.VERSION.SDK_INT < 33 -> {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            binding.isPhotoOrVideoSwitchGaleria.isChecked -> {
                Manifest.permission.READ_MEDIA_VIDEO
            }

            else -> {
                Manifest.permission.READ_MEDIA_IMAGES
            }
        }
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permisss granted
                startGallery(binding.isPhotoOrVideoSwitchGaleria.isChecked)
            }
            else -> {
                // Permiss not granted, request it
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    // Open images or video depending
    private fun startGallery(isVideo: Boolean) {
        val type = if (isVideo) "video/*" else "image/*"
        pickMediaLauncher.launch(type)
    }

    // display the image or video
    private fun displayMedia(uri: Uri) {
        binding.previewGaleria.removeAllViews()

        val newView = if (!binding.isPhotoOrVideoSwitchGaleria.isChecked) {
            ImageView(activity).apply {
                setImageURI(uri)
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
            }
        } else {
            VideoView(activity).apply {
                setVideoURI(uri)
                setMediaController(MediaController(activity))
                setOnPreparedListener { mp -> mp.isLooping = true }
                start()
            }
        }

        newView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        binding.previewGaleria.addView(newView)
    }
}
