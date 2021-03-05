package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository

class SetProfileUriUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(profileUri : String) {
        userManagerRepository.setProfileUri(profileUri)
    }

}