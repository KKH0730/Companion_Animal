package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import studio.seno.domain.util.LongTaskCallback

class CheckOverlapUserUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(email : String, callback : LongTaskCallback<Any>) {
        userManagerRepository.checkOverlapUser(email, callback)
    }

}