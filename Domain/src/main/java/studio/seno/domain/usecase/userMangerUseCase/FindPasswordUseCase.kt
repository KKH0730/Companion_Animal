package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class FindPasswordUseCase @Inject constructor(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        emailAddress: String,
        callback : LongTaskCallback<Any>
    ){
        userManagerRepository.sendFindEmail(emailAddress, callback)
    }

}