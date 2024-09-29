package com.compumovil.taller_2.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.compumovil.taller_2.databinding.ActivityCameraBinding
import java.io.File

class CameraActivity:AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding


    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView
    private lateinit var toggleVideo: Switch
    private var photoFile: File? = null
    private var videoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = binding.cameraFrame as ImageView
        videoView = binding.cameraFrame as VideoView

        binding.cameraButton.setOnClickListener {
            if (binding.cameraSwitch.isChecked) {
                //captureVideo()
            } else {
                //capturePhoto()
            }
        }

    }
}