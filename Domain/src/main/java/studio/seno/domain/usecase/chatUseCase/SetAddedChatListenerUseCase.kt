package studio.seno.domain.usecase.chatUseCase

import studio.seno.domain.repository.ChatRepository
import studio.seno.domain.model.Chat
import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class SetAddedChatListenerUseCase @Inject constructor(private val chatRepository: ChatRepository) {
    fun execute(
        email: String,
        targetEmail: String,
        callback: LongTaskCallback<Chat>
    ){
        chatRepository.setAddedChatListener(email, targetEmail ,callback)
    }
}