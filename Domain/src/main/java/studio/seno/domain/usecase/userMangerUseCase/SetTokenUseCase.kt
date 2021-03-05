package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.Repository.UserManagerRepository

class SetTokenUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(token : String) {
        userManagerRepository.setToken(token)
    }

}