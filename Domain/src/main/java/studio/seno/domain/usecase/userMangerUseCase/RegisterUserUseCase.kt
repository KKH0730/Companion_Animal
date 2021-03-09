package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import studio.seno.domain.util.LongTaskCallback

class RegisterUserUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        email: String,
        password: String,
        callback: LongTaskCallback<Any>
    ){
        userManagerRepository.registerUser(email, password, callback)
    }
}