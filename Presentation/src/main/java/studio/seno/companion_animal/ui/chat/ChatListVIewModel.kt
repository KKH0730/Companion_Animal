package studio.seno.companion_animal.ui.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import studio.seno.companion_animal.R
import studio.seno.datamodule.RemoteRepository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Chat
import java.util.*

class ChatListVIewModel : ViewModel() {
    private val chatListLiveData = MutableLiveData<List<Chat>>()
    private val remoteRepository = RemoteRepository()

    fun getChatListLiveData(): MutableLiveData<List<Chat>> {
        return chatListLiveData
    }

    fun setChatListLiveData(list: List<Chat>) {
        chatListLiveData.value = list
    }

    fun requestAddChat(
        myEmail: String, realMyEmail :String, targetEmail: String, targetRealEmail : String, myNickname: String, targetNickname: String,
        content: String, profileUri: String, targetProfileUri: String, timestamp: Long) {
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
        remoteRepository.requestAddChat(myEmail, targetEmail, chat)
    }

    fun updateChatLog(chat: Chat) {
        var tempList = chatListLiveData.value?.toMutableList()
        if (tempList == null) {
            tempList = mutableListOf()
        }

        tempList.add(chat)
        chatListLiveData.value = tempList
    }

    fun requestLoadChatLog(myEmail: String, targetEmail: String, recyclerView: RecyclerView, callback: LongTaskCallback<Boolean>) {
        remoteRepository.requestLoadChatLog(
            myEmail,
            targetEmail,
            object : LongTaskCallback<List<Chat>> {
                override fun onResponse(result: Result<List<Chat>>) {
                    if (result is Result.Success) {
                        chatListLiveData.value = result.data
                        callback.onResponse(Result.Success(true))
                        recyclerView.scrollToPosition(result.data.size - 1)
                    } else if (result is Result.Error) {
                        Log.e(
                            "error",
                            "ChatListVIewModel requestLoadChatLog error : ${result.exception}"
                        )
                    }
                }
            })
    }


    fun requestLoadChatList(myEmail: String, callback: LongTaskCallback<Boolean>) {
        remoteRepository.requestLoadChatList(myEmail, object : LongTaskCallback<List<Chat>> {
            override fun onResponse(result: Result<List<Chat>>) {
                if (result is Result.Success) {
                    val tempList = result.data

                    Collections.sort(tempList, object : Comparator<Chat> {
                        override fun compare(o1: Chat?, o2: Chat?): Int {
                            if (o1?.timestamp!! > o2?.timestamp!!) {
                                return -1
                            } else
                                return 1
                        }
                    })
                    chatListLiveData.value = tempList
                    callback.onResponse(Result.Success(true))
                } else if (result is Result.Error) {
                    Log.e(
                        "error",
                        "ChatListVIewModel requestLoadChatList error : ${result.exception}"
                    )
                }
            }
        })
    }

    fun requestRemoveChatList(context : Context, targetEmail : String, myEmail : String, myNickname : String, chat : Chat){
        val tempList = chatListLiveData.value?.toMutableList()
        tempList?.remove(chat)
        chatListLiveData.value = tempList?.toList()

        Log.d("hi", "targetNickname $myNickname")
        var deleteChat = chat
        deleteChat.content = String.format(context.getString(R.string.chat_remove_content), myNickname)
        deleteChat.isExit = true
        remoteRepository.requestRemoveChatList(targetEmail, myEmail, deleteChat)
    }

    fun requestUpdateCheckDot(myEmail : String, targetEmail : String){
        remoteRepository.requestUpdateCheckDot(myEmail, targetEmail)
    }
}