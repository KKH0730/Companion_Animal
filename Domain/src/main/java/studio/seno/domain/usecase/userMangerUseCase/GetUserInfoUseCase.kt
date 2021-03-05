package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import studio.seno.domain.model.User

import studio.seno.domain.util.LongTaskCallback

class GetUserInfoUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        email : String,
        callback: LongTaskCallback<User>
    ) {
        userManagerRepository.getUserInfo(email, callback)
    }
}