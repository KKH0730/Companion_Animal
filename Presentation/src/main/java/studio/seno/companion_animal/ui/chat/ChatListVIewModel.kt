package studio.seno.companion_animal.ui.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.companion_animal.R
import studio.seno.companion_animal.util.Constants
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

    fun updateChatLog(chat: Chat, recyclerView: RecyclerView, lifecycleCoroutineScope: LifecycleCoroutineScope) {
        var tempList = chatListLiveData.value?.toMutableList()
        if (tempList == null) {
            tempList = mutableListOf()
        }



        tempList.add(chat)
        tempList.toSet()
        chatListLiveData.value = tempList.toList()

        lifecycleCoroutineScope.launch(Dispatchers.Main){
            recyclerView.smoothScrollToPosition(tempList.size - 1)
        }
    }

    fun requestLoadChatLog(myEmail: String, targetEmail: String, recyclerView: RecyclerView, callback: LongTaskCallback<Boolean>) {
        remoteRepository.requestLoadChatLog(
            myEmail,
            targetEmail,
            object : LongTaskCallback<List<Chat>> {
                override fun onResponse(result: Result<List<Chat>>) {
                    if (result is Result.Success) {
                        if(result.data == null) {
                            callback.onResponse(Result.Success(false))
                        }

                    } else if (result is Result.Error) {
                        Log.e(
                            "error", "ChatListVIewModel requestLoadChatLog error : ${result.exception}"
                        )
                    }
                }
            })
    }

    fun clearChatList(){
        chatListLiveData.value = null
    }

    fun setAddedChat(chat: Chat, position :Int) {
        val tempChatList = chatListLiveData.value?.toMutableList()
        tempChatList?.set(position, chat)

        chatListLiveData.value = tempChatList
    }

    fun requestLoadChatList(myEmail: String, callback: LongTaskCallback<Boolean>) {
        remoteRepository.requestLoadChatList(myEmail, object : LongTaskCallback<List<Chat>> {
            override fun onResponse(result: Result<List<Chat>>) {
                if (result is Result.Success) {
                    if(result.data != null) {
                        val tempList = result.data

                        Collections.sort(tempList, object : Comparator<Chat> {
                            override fun compare(o1: Chat?, o2: Chat?): Int {
                                return if (o1?.timestamp!! > o2?.timestamp!!) -1 else 1
                            }
                        })
                        chatListLiveData.value = tempList
                        callback.onResponse(Result.Success(true))
/*
                        for(i in 0 until result.data.size) {
                            setAddedChatListener(result.data[i], i)
                        }

 */

                    } else {
                        callback.onResponse(Result.Success(false))
                    }

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


        var deleteChat = chat
        deleteChat.content = String.format(context.getString(R.string.chat_remove_content), myNickname)
        deleteChat.isExit = true
        remoteRepository.requestRemoveChatList(targetEmail, myEmail, deleteChat)
    }

    fun requestUpdateCheckDot(myEmail : String, targetEmail : String){
        remoteRepository.requestUpdateCheckDot(myEmail, targetEmail)
    }
}