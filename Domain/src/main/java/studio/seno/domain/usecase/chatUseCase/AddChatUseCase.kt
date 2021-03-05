package studio.seno.domain.usecase.chatUseCase

import studio.seno.domain.Repository.ChatRepository
import studio.seno.domain.util.Mapper

class AddChatUseCase(private val chatRepository: ChatRepository) {
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