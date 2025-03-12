package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository
import javax.inject.Inject

class SetNicknameUseCase @Inject constructor(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        content: String
    ) {
        userManagerRepository.setNickname(content)
    }

}