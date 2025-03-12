package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import javax.inject.Inject

class SetProfileUriUseCase @Inject constructor(private val userManagerRepository: UserManagerRepository) {
    fun execute(profileUri : String) {
        userManagerRepository.setProfileUri(profileUri)
    }

}