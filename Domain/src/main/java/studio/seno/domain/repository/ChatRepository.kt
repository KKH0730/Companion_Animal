package studio.seno.domain.repository

import studio.seno.domain.model.Chat
import studio.seno.domain.util.LongTaskCallback

interface ChatRepository {

    fun addChat(
        myEmail: String,
        targetEmail: String,
        chat: Chat
    )

    fun setAddedChatListener(
        email: String,
        targetEmail: String,
        callback: LongTaskCallback<Chat>
    )


    fun setChatListListener(
        email: String,
        callback : LongTaskCallback<Chat>
    )


    fun deleteChatList(
        targetEmail: String,
        myEmail: String,
        chat: Chat
    )


    fun setCheckDot(
        myEmail: String,
        targetEmail: String
    )
}