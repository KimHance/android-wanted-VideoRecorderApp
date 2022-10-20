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

private fun getSelectedVideo() {
    //TODO 영화목록 연결 시 아래 주석해제하고 테스트용 문장 지우기
    //playVideoModel.setSelectedVideo(navArgs.video)
    playVideoModel.setSelectedVideo(testVideo) //테스트용
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


### 황준성




