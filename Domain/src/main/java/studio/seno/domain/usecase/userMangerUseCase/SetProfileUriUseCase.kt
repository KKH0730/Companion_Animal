package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.Repository.UserManagerRepository

class SetProfileUriUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(profileUri : String) {
        userManagerRepository.setProfileUri(profileUri)
    }

}