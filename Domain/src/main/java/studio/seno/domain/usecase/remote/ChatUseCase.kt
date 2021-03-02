package studio.seno.domain.usecase.remote

import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Chat

class ChatUseCase {
    val CHAT_ROOT = "chat"

    fun loadChatLog(
        myEmail: String,
        targetEmail: String,
        realTimeDB: FirebaseDatabase,
        callback: LongTaskCallback<List<Chat>>
    ) {
        realTimeDB.reference.child(CHAT_ROOT).child(myEmail).child(myEmail + targetEmail).get()
            .addOnCompleteListener {

                val list = mutableListOf<Chat>()
                val size = it.result?.childrenCount

                if(size == 0L) {
                    callback.onResponse(Result.Success(null))
                    return@addOnCompleteListener
                }

                if (it.result != null) {
                    for (element in it.result?.children!!) {
                        element.getValue(Chat::class.java)?.let { it1 -> list.add(it1) }

                        if (list.size.toLong() == size) {
                            callback.onResponse(Result.Success(list))
                        }
                    }
                }
            }.addOnFailureListener {
                Log.e("error", "ChatUseCase loadChatLog error : ${it.message}")
            }
    }

    fun addChat(myEmail: String, targetEmail: String, chat: Chat, realTimeDB: FirebaseDatabase) {
        realTimeDB.reference.child(CHAT_ROOT).child(myEmail).child(myEmail + targetEmail).push()
            .setValue(chat)
        realTimeDB.reference.child(CHAT_ROOT).child(targetEmail).child(targetEmail + myEmail).push()
            .setValue(chat)
    }

    fun setAddedChatListener(email: String, targetEmail: String, realTimeDB: FirebaseDatabase, callback: LongTaskCallback<Chat>){
        realTimeDB.reference
            .child(CHAT_ROOT)
            .child(email)
            .child(email + targetEmail)
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val addedChat = snapshot.getValue(Chat::class.java)
                    if (addedChat != null) {
                        callback.onResponse(Result.Success(addedChat))
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    fun setChatListListener(realTimeDB: FirebaseDatabase, email: String, callback : LongTaskCallback<Chat>){
        realTimeDB.reference
            .child(CHAT_ROOT)
            .child(email)
            .orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatList = snapshot.children.toList()
                    val addedChat = chatList[chatList.size - 1].getValue(Chat::class.java)
                    if (addedChat != null) {
                        callback.onResponse(Result.Success(addedChat))
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    fun removeChatList(targetEmail: String, myEmail: String, chat: Chat, realTimeDB: FirebaseDatabase) {
        realTimeDB.reference.child(CHAT_ROOT).child(myEmail).child(myEmail + targetEmail).removeValue()
        realTimeDB.reference.child(CHAT_ROOT).child(targetEmail).child(targetEmail + myEmail).get()
            .addOnCompleteListener {
                if (it.result?.exists() == true) {
                    realTimeDB.reference.child(CHAT_ROOT).child(targetEmail).child(targetEmail + myEmail)
                        .push().setValue(chat)
                } else
                    return@addOnCompleteListener
            }
    }


    fun updateCheckDot(myEmail: String, targetEmail: String, realTimeDB: FirebaseDatabase) {
        realTimeDB.reference.child(CHAT_ROOT).child(myEmail).child(myEmail + targetEmail).get()
            .addOnCompleteListener {
                val childCount = it.result?.childrenCount
                var count = 0
                if (it.result != null) {
                    val chatList = it.result?.children!!.toList()
                    val map = mutableMapOf<String, Any>()
                    map["read"] = true
                    realTimeDB.reference.child(CHAT_ROOT).child(myEmail).child(myEmail + targetEmail)
                        .child(chatList[chatList.size - 1].key!!).updateChildren(map)

                }
            }
    }
}