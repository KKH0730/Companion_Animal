package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.Repository.UserManagerRepository
import studio.seno.domain.util.Mapper

class SetUserInfoUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        id : Long,
        email: String,
        nickname: String,
        follower: Long,
        following: Long,
        feedCount: Long,
        token : String,
        profileUri : String
    ) {
        val user = Mapper
            .getInstance()!!
            .mapperToUser(
                id, email, nickname, follower, following, feedCount, token, profileUri
            )
        userManagerRepository.setUserInfo(user)
    }
}