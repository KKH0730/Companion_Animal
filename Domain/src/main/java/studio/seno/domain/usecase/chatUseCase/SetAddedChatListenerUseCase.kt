package studio.seno.domain.usecase.chatUseCase

import studio.seno.domain.Repository.ChatRepository
import studio.seno.domain.model.Chat
import studio.seno.domain.util.LongTaskCallback

class SetAddedChatListenerUseCase (private val chatRepository: ChatRepository) {
    fun execute(
        email: String,
        targetEmail: String,
        callback: LongTaskCallback<Chat>
    ){
        chatRepository.setAddedChatListener(email, targetEmail ,callback)
    }
}