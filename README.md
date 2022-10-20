# VideoRecorder 어플리케이션

## 팀원
<table style="font-weight : bold">
    <tr>
        <td align="center">김현수</td>
        <td align="center">이재성</td>
        <td align="center">한혜원</td>
        <td align="center">황준성</td>
    </tr>
</table>

## 김현수
## 이재성

## 한혜원

### 역할

- 맡은 일
    - 녹화된 영상 플레이 구현
    - 화면 하단 컨트롤 바 구현 (재생, 일시정지, 플레이 seekbar)
- 기여한 점
    - 앱이 백그라운드로 내려가면 멈추고 정보를 간직
    - 포그라운드로 돌아왔을 때 해당 정보부터 다시 재생
- 남은 일
    - 컨트롤 바 커스텀 해보기
    - 전체화면 기능 추가해보기
- 실행영상
 
 https://user-images.githubusercontent.com/35549958/196947036-c9a2207e-bfc3-459d-9d12-b8f189f86c32.mp4

### 구현
- navArgs 이용하여 video data를 전달 받습니다
- video 객체를 뷰모델에서 관리할 수 있도록 합니다
```kotlin
private val navArgs: PlayFragmentArgs by navArgs()
private var uri: String = testUri

private fun getSelectedVideo() {
    playVideoModel.setSelectedVideo(navArgs.video)
    collectFlow()
}
private fun collectFlow() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            playVideoModel.selectedVideo.collect { video ->
                uri = video.uri
            }
        }
    }
}
```

- setExoPlayer() -> ExoPlayer2를 이용해 ExoPlayer 객체를 생성하고 재생할 영상의 uri가진 MediaItem을 추가합니다
- releasePlayer() -> 플레이어가 포커스를 잃으면 리소스를 해제하고 그 때의 정보를 저장합니다
```kotlin
private var exoPlayWhenReady = true
private var exoCurrentWindow = 0
private var exoPlaybackPosition = 0L
    
private fun setExoPlayer() {
    val mediaItem = MediaItem.fromUri(uri)
    exoPlayer = ExoPlayer.Builder(requireContext()).build().also {
        binding.pvPlayVideo.player = it
        it.setMediaItem(mediaItem)
        it.playWhenReady = exoPlayWhenReady
        it.seekTo(exoCurrentWindow, exoPlaybackPosition)
        it.prepare()
    }
}
private fun releasePlayer() {
    exoPlayer?.run {
        exoPlaybackPosition = this.currentPosition
        exoCurrentWindow = this.currentMediaItemIndex
        exoPlayWhenReady = this.playWhenReady
        release()
    }
    exoPlayer = null
}
```

- 생명주기를 통해 플레이어의 생성과 리소스 해제를 관리합니다
```kotlin
override fun onStart() {
    super.onStart()
    if (Util.SDK_INT >= 24) {
        setExoPlayer()
    }
}

override fun onResume() {
    super.onResume()
    if ((Util.SDK_INT < 24 || exoPlayer == null)) {
        setExoPlayer()
    }
}

override fun onPause() {
    super.onPause()
    if (Util.SDK_INT < 24) {
        releasePlayer()
    }
}

override fun onStop() {
    super.onStop()
    if (Util.SDK_INT >= 24) {
        releasePlayer()
    }
}
```


## 황준성


### **역할**

- 맡은 일
    - 카메라를 통한 영상 녹화
    - 전면, 후면 카메라 변경
    - 녹화한 영상 저장
    - Firebase storage를 이용하여 녹화한 영상 백업
- 기여한 점
    - CameraX 를 사용한 동영상 녹화 기능 구현
    - Firebase storage를 이용하여 녹화한 영상 백업
- 남은 일
    - 현재 전면,후면 카메라 변경이 녹화도중에 실행시 녹화가 중단되어 수정중입니다.
- 실행영상

- 구현
    - 카메라를 통한 영상 녹화

Camera X를 사용한 동영상 녹화 기능 구현

녹화화면을 보여줌과 동시에 녹화가 진행해야 하므로 CameraX의 프리뷰와 동영상 녹화 UseCase를 함께 bind 시켜준다.

```kotlin
private fun initCamera(cameraFacing: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraView.surfaceProvider) }
            val cameraSelector = cameraFacing

            try {
                cameraProvider.unbindAll()

                val myCamera = cameraProvider.bindToLifecycle(
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
```

VideoRecordEvent리스너로 start(),pause,resume(),stop()의 이벤트를 제어한다.

```kotlin
is VideoRecordEvent.Pause -> {
                        binding.apply {
                            pauseTime = chronometer.base
                            chronometer.stop()
                            btnPause.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24)
                            btnPause.setOnClickListener {
                                recording?.resume()
                            }

                        }
                    }

is VideoRecordEvent.Resume -> {
                        binding.apply {
                            btnPause.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                            btnPause.setOnClickListener {
                                recording?.pause()
                            }
                            chronometer.base = (SystemClock.elapsedRealtime()+pauseTime)
                            chronometer.start()
                        }
                    }
```

- 전면, 후면 카메라 변경

카메라 생성시 카메라의 방향을 수정해준다.

```kotlin

private var myCameraFacing = CameraSelector.DEFAULT_BACK_CAMERA

binding.btnSwitch.setOnClickListener {
                if (myCameraFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
                    myCameraFacing = CameraSelector.DEFAULT_BACK_CAMERA
                    initCamera(myCameraFacing)
                } else {
                    myCameraFacing = CameraSelector.DEFAULT_FRONT_CAMERA
                    initCamera(myCameraFacing)
                }
            }

```

- 녹화한 영상 저장

카메라의 이름,타입,경로 를 지정해주고, 영상 녹화 종료시에 해당 경로에 영상이 저장되게 한다.

```kotlin
name = "Wanted_camera" + SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(System.currentTimeMillis())
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
```

- Firebase storage를 이용하여 녹화한 영상 백업

```kotlin
override suspend fun uploadVideo(video: Video) {
        ref.child("test").child(video.date).putFile(
            Uri.fromFile(File(video.uri))
        ).addOnSuccessListener {
            Log.d("UPLOAD SUCCESS", "uploadVideo: ${video.uri}")
        }.addOnFailureListener {
            Log.d("UPLOAD FAIL", it.message.toString())
        }
    }
```



https://user-images.githubusercontent.com/55780312/196967608-97dbb64f-b42e-451b-9040-922ba2ef5f96.mp4



