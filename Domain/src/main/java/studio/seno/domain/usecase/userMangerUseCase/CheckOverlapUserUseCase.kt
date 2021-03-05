package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.Repository.UserManagerRepository
import studio.seno.domain.util.LongTaskCallback

class CheckOverlapUserUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(email : String, callback : LongTaskCallback<Boolean>) {
        userManagerRepository.checkOverlapUser(email, callback)
    }

}