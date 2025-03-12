package studio.seno.domain.usecase.chatUseCase

import studio.seno.domain.repository.ChatRepository
import studio.seno.domain.util.Mapper
import javax.inject.Inject

class AddChatUseCase @Inject constructor(private val chatRepository: ChatRepository) {
    fun execute(
        myEmail: String,
        realMyEmail :String,
        targetEmail: String,
        targetRealEmail : String,
        myNickname: String,
        targetNickname: String,
        content: String,
        profileUri: String,
        targetProfileUri: String,
        timestamp: Long
    ){
        val chat = Mapper.getInstance()!!.mapperToChat(
            myEmail,
            realMyEmail,
            targetEmail,
            targetRealEmail,
            myNickname,
            targetNickname,
            content,
            profileUri,
            targetProfileUri,
            timestamp,
            isExit = false,
            isRead = false
        )
        chatRepository.addChat(myEmail, targetEmail ,chat)
    }
}