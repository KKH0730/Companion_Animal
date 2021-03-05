package studio.seno.domain.usecase.chatUseCase

import studio.seno.domain.repository.ChatRepository
import studio.seno.domain.model.Chat
import studio.seno.domain.util.LongTaskCallback

class SetChatListListenerUseCase (private val chatRepository: ChatRepository) {
    fun execute(
        email: String,
        callback : LongTaskCallback<Chat>
    ){
        chatRepository.setChatListListener(email, callback)
    }
}