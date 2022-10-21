package com.preonboarding.videorecorder.domain.usecase

import com.preonboarding.videorecorder.domain.repository.FirebaseRepository
import javax.inject.Inject

class GetVideoListUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
//    suspend operator fun invoke() = firebaseRepository.getVideoList()

    suspend operator fun invoke() = firebaseRepository.getVideo()
}