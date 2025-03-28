package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import javax.inject.Inject

class SetTokenUseCase @Inject constructor(private val userManagerRepository: UserManagerRepository) {
    fun execute(token : String) {
        userManagerRepository.setToken(token)
    }

}