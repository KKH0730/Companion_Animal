package studio.seno.companion_animal.ui.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.Chat

class ChatViewModel() : ViewModel(){
    private val chatLiveData = MutableLiveData<Chat>()


    fun getChatLiveData() : MutableLiveData<Chat> {
        return chatLiveData
    }

    fun setChatLiveData(chat : Chat) {
        chatLiveData.value = chat
    }

}