package studio.seno.domain.usecase.remote

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Chat

class ChatUseCase {
    val CHAT_ROOT = "chat"

    fun loadChatLog(myEmail : String, targetEmail : String, mRTDB : FirebaseDatabase, callback : LongTaskCallback<List<Chat>>){
        mRTDB.reference.child(CHAT_ROOT).child(myEmail).child(myEmail + targetEmail).get()
            .addOnCompleteListener {

            val list = mutableListOf<Chat>()
            val size = it.result?.childrenCount

            if (it.result != null) {
                for(element in it.result?.children!!) {
                    element.getValue(Chat::class.java)?.let { it1 -> list.add(it1) }

                    if(list.size.toLong() == size) {
                        callback.onResponse(Result.Success(list))
                    }
                }
            }
        }.addOnFailureListener {
            Log.e("error", "ChatUseCase loadChatLog error : ${it.message}")
        }
    }

    fun addChat(myEmail : String, targetEmail : String, chat : Chat, mRTDB : FirebaseDatabase){
        mRTDB.reference.child(CHAT_ROOT).child(myEmail).child(myEmail + targetEmail).push().setValue(chat)
        mRTDB.reference.child(CHAT_ROOT).child(targetEmail).child(targetEmail + myEmail).push().setValue(chat)
    }

    fun loadChatList(myEmail : String, mRTDB : FirebaseDatabase, callback : LongTaskCallback<List<Chat>>){
        mRTDB.reference.child(CHAT_ROOT).child(myEmail).get()
            .addOnCompleteListener {
                val list = mutableListOf<Chat>()
                val size = it.result?.childrenCount

                for(element in it.result?.children!!) {

                    val chatList = element.children.toList()
                    chatList[chatList.size - 1].getValue(Chat::class.java)?.let { it1 -> list.add(it1) }

                    if(list.size.toLong() == size) {
                        callback.onResponse(Result.Success(list))
                    }
                }
            }
    }

    fun removeChatList(targetEmail : String, myEmail : String, chat : Chat, mRTDB : FirebaseDatabase){
        mRTDB.reference.child(CHAT_ROOT).child(myEmail).child(myEmail + targetEmail).removeValue()
        mRTDB.reference.child(CHAT_ROOT).child(targetEmail).child(targetEmail + myEmail).get().addOnCompleteListener {
            if(it.result?.exists() == true) {
                mRTDB.reference.child(CHAT_ROOT).child(targetEmail).child(targetEmail + myEmail).push().setValue(chat)
            } else
                return@addOnCompleteListener
        }
    }

}