package studio.seno.domain.usecase.followUseCase

import studio.seno.domain.repository.FollowRepository
import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class CheckFollowUseCase @Inject constructor(private val followRepository: FollowRepository) {
    fun execute(
        targetEmail: String,
        callback: LongTaskCallback<Any>
    ){
        followRepository.checkFollow(targetEmail, callback)
    }
}