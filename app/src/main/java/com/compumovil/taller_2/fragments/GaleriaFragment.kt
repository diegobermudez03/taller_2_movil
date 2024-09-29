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

    // Launchers para la galería y permisos
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickMediaLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrar el lanzador para la solicitud de permisos
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startGallery(binding.isPhotoOrVideoSwitchGaleria.isChecked)
            } else {
                println("Permiso no concedido")
            }
        }

        // Registrar el lanzador para seleccionar imágenes o videos de la galería
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

    // Verificar si el permiso de almacenamiento está concedido y solicitarlo si es necesario
    private fun checkStoragePermissionAndGetImage() {
        val permission = if(binding.isPhotoOrVideoSwitchGaleria.isChecked) Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_MEDIA_IMAGES
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso concedido, abrir galería
                startGallery(binding.isPhotoOrVideoSwitchGaleria.isChecked)
            }
            else -> {
                // Permiso no concedido, solicitarlo
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    // Abrir la galería dependiendo si es foto o video
    private fun startGallery(isVideo: Boolean) {
        val type = if (isVideo) "video/*" else "image/*"
        pickMediaLauncher.launch(type)
    }

    // Mostrar la imagen o video seleccionado
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
