package studio.seno.domain.usecase.followUseCase

import studio.seno.domain.Repository.FollowRepository
import studio.seno.domain.model.Follow
import studio.seno.domain.util.LongTaskCallback

class GetFollowUseCase(private val followRepository: FollowRepository) {
    fun execute(
        fieldName: String,
        callback: LongTaskCallback<List<Follow>>
    ){
        followRepository.getFollow(fieldName, callback)
    }
}