package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.Repository.UserManagerRepository
import studio.seno.domain.util.LongTaskCallback

class FindPasswordUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        emailAddress: String,
        callback : LongTaskCallback<Boolean>
    ){
        userManagerRepository.sendFindEmail(emailAddress, callback)
    }

}