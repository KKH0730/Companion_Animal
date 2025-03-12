package studio.seno.domain.usecase.followUseCase

import studio.seno.domain.repository.FollowRepository
import studio.seno.domain.util.Mapper
import javax.inject.Inject

class SetFollowUseCase @Inject constructor(private val followRepository: FollowRepository) {
    fun execute(
        targetEmail : String,
        targetNickname: String,
        targetProfileUri : String,
        myEmail : String,
        myNickName : String,
        myProfileUri : String,
        flag: Boolean,
    ){
        val targetFollow = Mapper.getInstance()!!.mapperToFollow(targetEmail, targetNickname, targetProfileUri)
        val myFollow = Mapper.getInstance()!!.mapperToFollow(myEmail, myNickName, myProfileUri)
        followRepository.setFollower(targetEmail, flag, myFollow, targetFollow)
    }
}