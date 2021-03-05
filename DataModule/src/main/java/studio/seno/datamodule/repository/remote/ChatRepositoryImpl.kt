package studio.seno.datamodule.repository.remote

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import studio.seno.domain.Repository.ChatRepository
import studio.seno.domain.model.Chat
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class ChatRepositoryImpl : ChatRepository {
    private val realTimeDB = FirebaseDatabase.getInstance()
    private val CHATROOT = "chat"

    override fun addChat(
        myEmail: String,
        targetEmail: String,
        chat: Chat
    ) {
        realTimeDB.reference.child(CHATROOT).child(myEmail).child(myEmail + targetEmail).push()
            .setValue(chat)
        realTimeDB.reference.child(CHATROOT).child(targetEmail).child(targetEmail + myEmail).push()
            .setValue(chat)
    }

    override fun setAddedChatListener(
        email: String,
        targetEmail: String,
        callback: LongTaskCallback<Chat>
    ) {
        realTimeDB.reference
            .child(CHATROOT)
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

    override fun setChatListListener(email: String, callback: LongTaskCallback<Chat>) {
        realTimeDB.reference
            .child(CHATROOT)
            .child(email)
            .orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener {
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

    override fun deleteChatList(targetEmail: String, myEmail: String, chat: Chat) {
        realTimeDB.reference.child(CHATROOT).child(myEmail).child(myEmail + targetEmail).removeValue()
        realTimeDB.reference.child(CHATROOT).child(targetEmail).child(targetEmail + myEmail).get()
            .addOnCompleteListener {
                if (it.result?.exists() == true) {
                    realTimeDB.reference.child(CHATROOT).child(targetEmail).child(targetEmail + myEmail)
                        .push().setValue(chat)
                } else
                    return@addOnCompleteListener
            }
    }

    override fun setCheckDot(myEmail: String, targetEmail: String) {
        realTimeDB.reference.child(CHATROOT).child(myEmail).child(myEmail + targetEmail).get()
            .addOnCompleteListener {
                if (it.result != null) {
                    val chatList = it.result?.children!!.toList()
                    val map = mutableMapOf<String, Any>()
                    map["read"] = true

                    realTimeDB.reference.child(CHATROOT).child(myEmail).child(myEmail + targetEmail)
                        .child(chatList[chatList.size - 1].key!!).updateChildren(map)

                }
            }
    }
}