package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class CheckEnableLoginUseCase @Inject constructor(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        email: String,
        password: String,
        callback : LongTaskCallback<Any>
    ){
        userManagerRepository.checkEnableLogin(email, password, callback)
    }

}