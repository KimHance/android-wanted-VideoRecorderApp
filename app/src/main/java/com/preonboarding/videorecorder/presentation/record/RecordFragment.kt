package com.preonboarding.videorecorder.presentation.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.preonboarding.videorecorder.R
import com.preonboarding.videorecorder.const.REQUEST_CODE_PERMISSIONS
import com.preonboarding.videorecorder.const.REQUIRED_PERMISSIONS
import com.preonboarding.videorecorder.const.TAG
import com.preonboarding.videorecorder.databinding.FragmentRecordBinding
import com.preonboarding.videorecorder.domain.model.Video
import com.preonboarding.videorecorder.presentation.MainViewModel
import com.preonboarding.videorecorder.presentation.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class RecordFragment : BaseFragment<FragmentRecordBinding>(R.layout.fragment_record) {
    private val recordVideoModel: MainViewModel by activityViewModels()
    private lateinit var recordedVideo: Video
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraFacing = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (checkPermissions()) {
            initCamera(cameraFacing)

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        binding.btnRecord.setOnClickListener {
            startRecord()
        }
        binding.btnStop.setOnClickListener {
            val curRecording = recording
            if (curRecording != null) {
                curRecording.stop()
                recording = null
            }
        }
        binding.btnSwitch.setOnClickListener {
            if (cameraFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
                cameraFacing = CameraSelector.DEFAULT_BACK_CAMERA
                Snackbar.make(
                    requireView(),
                    "Switch",
                    Snackbar.LENGTH_SHORT
                ).show()
                initCamera(cameraFacing)
            }
            else{
                cameraFacing = CameraSelector.DEFAULT_FRONT_CAMERA
                Snackbar.make(
                    requireView(),
                    "Switch",
                    Snackbar.LENGTH_SHORT
                ).show()
                initCamera(cameraFacing)
            }
            try {

            }
            catch (_: Exception) { }
        }


        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    private fun checkPermissions() = REQUIRED_PERMISSIONS.all { it: String ->
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (checkPermissions()) {
                initCamera(cameraFacing)
            } else {
                Snackbar.make(
                    requireView(),
                    "Permissions not granted by the user.",
                    Snackbar.LENGTH_SHORT
                ).show()

            }
        }
    }

    //@RequiresPermission(Manifest.permission.CAMERA)
    private fun initCamera(cameraFacing:CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraView.surfaceProvider)
                }
            val cameraSelector = cameraFacing

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
    }




    private fun startRecord() {
        val videoCapture = this.videoCapture ?: return
        binding.btnRecord.isEnabled = false
        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }

        val name = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Wanted-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(requireActivity().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        //영상 녹화 시작시
                        //TODO 시간 시작
                        binding.apply {
                            btnRecord.isEnabled = false
                            btnRecord.visibility = View.GONE
                            btnPause.isEnabled = true
                            btnPause.visibility = View.VISIBLE
                            btnStop.isEnabled = true
                            btnStop.visibility = View.VISIBLE


                            binding.btnPause.setOnClickListener {
                                val curRecording = recording
                                if (curRecording != null) {
                                    curRecording?.pause()
                                    //TODO 시간 정지
                                    recording = null
                                }
                            }
                        }
                    }
                    is VideoRecordEvent.Pause -> {
                        binding.apply {
                            binding.btnPause.setOnClickListener {
                                val curRecording = recording
                                if (curRecording != null) {
                                    curRecording?.resume()
                                    //TODO 시간 다시 시작
                                    recording = null
                                }
                            }
                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            //영상 녹화 종료시
                            //TODO 시간 초기화화

                            binding.apply {
                                btnRecord.isEnabled = true
                                btnRecord.visibility = View.VISIBLE
                                btnPause.isEnabled = false
                                btnPause.visibility = View.GONE
                                btnStop.isEnabled = false
                                btnStop.visibility = View.GONE
                            }

                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Snackbar.make(
                                requireView(),
                                msg,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            Log.d(TAG, msg)
                            val nowDate =
                                SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(System.currentTimeMillis())
                                    .toString()
                            //saveFireBase()
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(
                                TAG, "Video capture ends with error: " +
                                        "${recordEvent.error}"
                            )
                        }
                    }
                }
            }
    }


    private fun saveFireBase() {
        recordVideoModel.uploadVideo(recordedVideo)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }



}
