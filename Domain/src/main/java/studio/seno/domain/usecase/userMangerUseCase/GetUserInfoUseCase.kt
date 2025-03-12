package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import studio.seno.domain.model.User

import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        email : String,
        callback: LongTaskCallback<Any>
    ) {
        userManagerRepository.getUserInfo(email, callback)
    }
}