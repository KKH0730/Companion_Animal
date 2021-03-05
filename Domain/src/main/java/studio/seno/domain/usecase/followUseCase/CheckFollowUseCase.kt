package studio.seno.domain.usecase.followUseCase

import studio.seno.domain.repository.FollowRepository
import studio.seno.domain.util.LongTaskCallback

class CheckFollowUseCase(private val followRepository: FollowRepository) {
    fun execute(
        targetEmail: String,
        callback: LongTaskCallback<Boolean>
    ){
        followRepository.checkFollow(targetEmail, callback)
    }
}