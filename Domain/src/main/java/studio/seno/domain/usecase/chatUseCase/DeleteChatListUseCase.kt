package studio.seno.domain.usecase.chatUseCase

import studio.seno.domain.repository.ChatRepository
import studio.seno.domain.model.Chat
import javax.inject.Inject

class DeleteChatListUseCase @Inject constructor(private val chatRepository: ChatRepository) {
    fun execute(
        targetEmail: String,
        myEmail: String,
        chat: Chat
    ){

        chatRepository.deleteChatList(myEmail, targetEmail, chat)
    }
}