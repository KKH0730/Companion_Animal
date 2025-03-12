package studio.seno.domain.usecase.followUseCase

import studio.seno.domain.repository.FollowRepository
import studio.seno.domain.model.Follow
import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class GetFollowUseCase @Inject constructor(private val followRepository: FollowRepository) {
    fun execute(
        fieldName: String,
        callback: LongTaskCallback<Any>
    ){
        followRepository.getFollow(fieldName, callback)
    }
}