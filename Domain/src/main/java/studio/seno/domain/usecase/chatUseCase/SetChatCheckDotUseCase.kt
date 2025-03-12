package studio.seno.domain.usecase.chatUseCase

import studio.seno.domain.repository.ChatRepository
import javax.inject.Inject

class SetChatCheckDotUseCase @Inject constructor(private val chatRepository: ChatRepository) {
    fun execute(
        myEmail: String,
        targetEmail: String
    ){
        chatRepository.setCheckDot(myEmail, targetEmail)
    }
}