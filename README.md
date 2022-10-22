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

## 기술 스택
- MVVM + Cleanarchitecture
- dataBinding
- Coroutine + Flow
- Hilt
- Glide
- Firebase Storage
- ExoPlayer
- CameraX

## 패키지
```
├── const
├── data
│   ├── datasource
│   │   └── impl
│   ├── entity
│   └── repositoryimpl
├── di
├── domain
│   ├── mapper
│   ├── model
│   ├── repository
│   └── usecase
└── presentation
    ├── base
    ├── list
    │   └── adapter
    ├── play
    │   └── adapter
    └── record
```
## 데모 영상

https://user-images.githubusercontent.com/51078673/197353603-1340f032-a9f4-4bfe-86e2-66e6eb6c1f91.mp4



## 김현수

### 역할
- 프로젝트 베이스세팅
- DI 모듈 세팅
- 네비게이션 세팅

### DI 모듈 세팅
- 디스패처 모듈
```kotlin
@Module
@InstallIn(SingletonComponent::class)
class DispatcherModule {
    @Provides
    @DispatcherIO
    fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DispatcherMain
    fun provideDispatcherMain(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @DispatcherDefault
    fun provideDispatcherDefault(): CoroutineDispatcher = Dispatchers.Default

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DispatcherIO

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DispatcherMain

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DispatcherDefault
}
```
- 파이어베이스 스토리지 모듈
```kotlin
@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseStorage() = FirebaseStorage.getInstance()
}
```
- 데이터 소스와 레포지토리 모듈
```kotlin
// DataSourceModule
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindFirebaseDataSource(
        impl: FirebaseDataSourceImpl
    ): FirebaseDataSource
}
// RepositoryModule
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFirebaseRepository(
        impl: FirebaseRepositoryImpl
    ): FirebaseRepository
}
```
### 네비게이션 세팅
- 네비게이션 그래프
<img src="https://user-images.githubusercontent.com/86879099/196983592-606809cb-23ff-4a93-8ab2-537fbb4f0579.png" width="600" height="600"/>
- 액션을 통해 Video 객체 전달

```kotlin
 private fun doOnClick(video: Video) {
        val action =
            ListFragmentDirections.actionListFragmentToPlayFragment(
                video
            )
        requireView().findNavController().navigate(action)
    }
```
### 그 외
```kotlin
// viewModel
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getVideoListUseCase: GetVideoListUseCase,
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase
) : ViewModel() {

    private val _videoList = MutableStateFlow<List<Video>>(emptyList())
    val videoList = _videoList.asStateFlow()
    
    private val _selectedVideo = MutableStateFlow<Video>(Video())
    val selectedVideo = _selectedVideo.asStateFlow()

...
// ListFragment
private fun collectFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                listViewModel.videoList.collect { videoList ->
                    // TODO 비디오 리스트 변경시 어댑터 리스트 업데이트 필요
                }
            }
        }
    }
...
// PlayFragment
private fun collectFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                playVideoModel.selectedVideo.collect { video ->
                    // TODO 선택된 비디오 ExoPlayer와 작업 필요
                }
            }
        }
    }
```
- 멤버들이 후에 편하게 작업할 수 있도록 베이스 코드 작성
## 이재성
### 역할
- 동영상 리스트
- 동영상 삭제
- 롱클릭 시 동영상 5초간 재생

### 동영상 리스트
* 문제 상황
```kotlin
class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : FirebaseRepository {
    override suspend fun getVideoList() {
        val videoList = mutableListOf<RemoteVideo>()

        firebaseDataSource.getVideoList().addOnSuccessListener { result ->
            Timber.e("${result.items.size}")
            Timber.e("${result.items}")
            result.items.forEach { reference ->
                videoList.add(
                    RemoteVideo(
                        videoName = reference.name,
                        downloadUrl = getDownloadUrl(reference.downloadUrl),
                        videoTimeStamp = getTimeMillis(reference.metadata)
                    )
                )
            }
        }
    }

    ...

    private fun getTimeMillis(metadata: Task<StorageMetadata>): String {
        var timeMillis = ""
        metadata.addOnSuccessListener { storageMetaData ->
            timeMillis = storageMetaData.creationTimeMillis.toString()
        }
        return timeMillis
    }

    private fun getDownloadUrl(downloadUrl: Task<Uri>): String {
        var url = ""
        downloadUrl.addOnSuccessListener {
            url = it.toString()
        }
        return url
    }
}
```
- `addOnSuccessListener`, `addOnFailureListener`와 같은 함수들은 비동기적으로 호출됨
- 비동기 작업을 연쇄적으로 해야하는 경우에는 적합하지 않았음

<br>

* 해결 방법
```kotlin
class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    @DispatcherModule.DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : FirebaseRepository {

    override suspend fun getVideoList() = callbackFlow {
        firebaseDataSource.getVideoList().map { reference ->
            val downloadUrl = reference.downloadUrl
            val getMetadata = reference.metadata
            Tasks.whenAll(
                downloadUrl,
                getMetadata
            ).addOnSuccessListener {
                trySend(
                    Video(
                        date = getMetadata.result.creationTimeMillis.toString(),
                        title = reference.name,
                        videoUrl = downloadUrl.result.toString()
                    )
                )
            }
        }
        awaitClose()
    }.flowOn(dispatcherIO)
    
    ...
}
```
- `Tasks.whenAll`은 해당 Task가 모두 완료된 경우에만 `addOnSuccessListener`를 수행한다.

<br>

``` kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getVideoListUseCase: GetVideoListUseCase,
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase
) : ViewModel() {

    private val _videoList = MutableStateFlow<List<Video>>(emptyList())
    val videoList = _videoList.asStateFlow()

    ...

    fun getVideoList() {
        viewModelScope.launch {
            _videoList.update {
                emptyList()
            }
            getVideoListUseCase.invoke().collect { video ->
                val listBuffer = mutableListOf<Video>().apply {
                    addAll(_videoList.value)
                    add(video)
                }
                _videoList.update {
                    listBuffer
                }
            }
        }
    }
    
    ...
    
    fun deleteVideo(video: Video) {
        viewModelScope.launch {
            kotlin.runCatching {
                deleteVideoUseCase.invoke(video)
            }.onSuccess {
                launch {
                    _videoList.update {
                        emptyList()
                    }
                    
                    getVideoListUseCase.invoke().collect { video ->
                        val listBuffer = mutableListOf<Video>().apply {
                            addAll(_videoList.value)
                            add(video)
                        }

                        _videoList.update {
                            listBuffer
                        }
                    }
                }
            }
        }
    }
}
```

### UI
```kotlin
class VideoAdapter(
    private val onItemClicked: (Video) -> Unit,
    private val onItemLongClicked: (String) -> Unit,
    private val onItemDeleted: (Video) -> Unit
) : ListAdapter<Video, VideoAdapter.VideoViewHolder>(VIDEO_DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemVideoInfoBinding.inflate(inflater, parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bindItems(getItem(position), onItemClicked, onItemLongClicked, onItemDeleted)
    }

    class VideoViewHolder(private val binding: ItemVideoInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(
            item: Video,
            onItemClicked: (Video) -> Unit,
            onItemLongClicked: (String) -> Unit,
            onItemDeleted: (Video) -> Unit
        ) {
            with(binding) {
                video = item
                executePendingBindings()

                cvVideo.setOnLongClickListener {
                    onItemLongClicked.invoke(item.videoUrl)
                    return@setOnLongClickListener true // Long Click 이후 Click 발생 방지
                }

                cvVideo.setOnClickListener {
                    onItemClicked.invoke(item)
                }

                tvDelete.setOnClickListener {
                    onItemDeleted.invoke(item)
                }
            }
        }
    }

    companion object {
        private val VIDEO_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem.videoUrl == newItem.videoUrl
            }

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
                return oldItem == newItem
            }
        }
    }
}
```

<br>

### 동영상 5초 프리뷰
- Player.Listener 사용

``` kotlin
private fun exoPreviewListener() = object : Player.Listener {

    ...

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
  
        if (isPlaying) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(5000L)
                releasePlayer()
                dialog?.dismiss()
            }
        } else {
            if (exoPlayer?.currentPosition!! >= 5000) {
                dialog?.dismiss()
            }
        }
    }
}
```
---
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



