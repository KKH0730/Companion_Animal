package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.Repository.UserManagerRepository
import studio.seno.domain.util.LongTaskCallback

class CheckEnableLoginUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        email: String,
        password: String,
        callback : LongTaskCallback<Boolean>
    ){
        userManagerRepository.checkEnableLogin(email, password, callback)
    }

}