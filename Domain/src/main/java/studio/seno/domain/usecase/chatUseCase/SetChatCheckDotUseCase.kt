package studio.seno.domain.usecase.chatUseCase

import studio.seno.domain.Repository.ChatRepository

class SetChatCheckDotUseCase (private val chatRepository: ChatRepository) {
    fun execute(
        myEmail: String,
        targetEmail: String
    ){
        chatRepository.setCheckDot(myEmail, targetEmail)
    }
}