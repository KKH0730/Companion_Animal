package studio.seno.companion_animal.ui.chat

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.companion_animal.R
import studio.seno.domain.model.Chat
import studio.seno.domain.usecase.chatUseCase.*
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import javax.inject.Inject

@HiltViewModel
class ChatListVIewModel @Inject constructor(
    private val addChatUseCase: AddChatUseCase,
    private val setAddedChatListenerUseCase: SetAddedChatListenerUseCase,
    private val setChatListListenerUseCase: SetChatListListenerUseCase,
    private val chatCheckDotUseCase: SetChatCheckDotUseCase,
    private val deleteChatListUseCase: DeleteChatListUseCase

) : ViewModel() {
    private val chatListLiveData = MutableLiveData<List<Chat>>()


    fun getChatListLiveData(): MutableLiveData<List<Chat>> {
        return chatListLiveData
    }

    fun setChatListLiveData(list: List<Chat>) {
        chatListLiveData.value = list
    }

    fun clearChatList(){
        chatListLiveData.value = listOf()
    }

    fun requestAddChat(
        myEmail: String, realMyEmail :String, targetEmail: String, targetRealEmail : String, myNickname: String, targetNickname: String,
        content: String, profileUri: String, targetProfileUri: String, timestamp: Long) {

        addChatUseCase.execute(myEmail, realMyEmail, targetEmail, targetRealEmail, myNickname, targetNickname, content, profileUri, targetProfileUri, timestamp)
    }

    fun addChatLog(chat: Chat, recyclerView: RecyclerView, lifecycleCoroutineScope: LifecycleCoroutineScope) {
        var tempList = chatListLiveData.value?.toMutableList()
        if (tempList == null) {
            tempList = mutableListOf()
        }

        if(!tempList.contains(chat))
            tempList.add(chat)

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
        setAddedChatListenerUseCase.execute(email, targetEmail, object :
            LongTaskCallback<Chat> {
            override fun onResponse(result: Result<Chat>) {
                if(result is Result.Success) {

                    if(sort == "chat_list")
                        setAddedChat(result.data, position)
                    else
                        addChatLog(result.data, recyclerView!!, lifecycleScope!!)
                }
            }
        })
    }

    fun requestSetChatListListener(email: String, chatRecyclerView: RecyclerView, lifecycleScope : LifecycleCoroutineScope){
        setChatListListenerUseCase.execute(email, object  : LongTaskCallback<Chat> {
            override fun onResponse(result: Result<Chat>) {
                if(result is Result.Success) {
                    addChatLog(result.data, chatRecyclerView, lifecycleScope)
                }
            }
        })
    }

    fun requestRemoveChatList(context : Context, targetEmail : String, myEmail : String, myNickname : String, chat : Chat){
        val tempList = chatListLiveData.value?.toMutableList()
        tempList?.remove(chat)
        chatListLiveData.value = tempList?.toList()

        chat.content = String.format(context.getString(R.string.chat_remove_content), myNickname)
        chat.isExit = true
        deleteChatListUseCase.execute(targetEmail, myEmail, chat)
    }

    fun requestUpdateCheckDot(myEmail : String, targetEmail : String){
        chatCheckDotUseCase.execute(myEmail, targetEmail)
    }
}