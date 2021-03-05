package studio.seno.domain.usecase.userMangerUseCase

import studio.seno.domain.repository.UserManagerRepository

class SetNicknameUseCase(private val userManagerRepository: UserManagerRepository) {
    fun execute(
        content: String
    ) {
        userManagerRepository.setNickname(content)
    }

}