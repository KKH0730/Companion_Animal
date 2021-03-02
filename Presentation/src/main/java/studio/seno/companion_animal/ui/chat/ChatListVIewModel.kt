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
import studio.seno.companion_animal.module.CommonFunction
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

    fun clearChatList(){
        chatListLiveData.value = null
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

    fun addChatLog(chat: Chat, sort: String, recyclerView: RecyclerView, lifecycleCoroutineScope: LifecycleCoroutineScope) {
        var tempList = chatListLiveData.value?.toMutableList()
        if (tempList == null) {
            tempList = mutableListOf()
        }

        if(!tempList.contains(chat)) {

                tempList.add(chat)
        }

        chatListLiveData.value = tempList.toList()

        lifecycleCoroutineScope.launch(Dispatchers.Main){
            recyclerView.smoothScrollToPosition(tempList.size - 1)
        }
    }



    fun setAddedChat(chat: Chat, position :Int) {
        var tempChatList = chatListLiveData.value?.toMutableList()
        if(tempChatList == null) {
            tempChatList = mutableListOf()
        }

        if(position >= tempChatList.size)
            tempChatList.add(chat)
        else {
            var targetEmail : String? = null
            var targetEmail2 : String? = null

            targetEmail = if (chat.realEmail == FirebaseAuth.getInstance().currentUser?.email.toString()) chat.targetEmail else chat.email
            targetEmail2 = if(tempChatList[position].realEmail == FirebaseAuth.getInstance().currentUser?.email.toString()) chat.targetEmail else chat.email

            if(targetEmail == targetEmail2) {
                tempChatList[position] = chat
            }
        }

        chatListLiveData.value = tempChatList.toList()
    }


    fun requestSetAddedChatListener(email: String, targetEmail: String, position: Int, sort : String, recyclerView: RecyclerView?, lifecycleScope: LifecycleCoroutineScope?){
        remoteRepository.requestSetAddedChatListener(email, targetEmail, object : LongTaskCallback<Chat>{
            override fun onResponse(result: Result<Chat>) {
                if(result is Result.Success) {
                    if(sort == "chat_list")
                        setAddedChat(result.data, position)
                    else
                        addChatLog(result.data, sort, recyclerView!!, lifecycleScope!!)
                }
            }
        })
    }

    fun requestSetChatListListener(email: String, sort: String, chatRecyclerView: RecyclerView, lifecycleScope : LifecycleCoroutineScope, callback : LongTaskCallback<Boolean>){
        remoteRepository.requestSetChatListListener(email, object  : LongTaskCallback<Chat>{
            override fun onResponse(result: Result<Chat>) {
                if(result is Result.Success) {
                    addChatLog(result.data, sort, chatRecyclerView, lifecycleScope)
                    callback.onResponse(Result.Success(true))
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