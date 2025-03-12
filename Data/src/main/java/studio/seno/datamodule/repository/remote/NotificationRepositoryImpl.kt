package studio.seno.datamodule.repository.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.domain.model.Comment
import studio.seno.domain.repository.NotificationRepository
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun setNotificationInfo(notificationData: NotificationData) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .collection("notification")
                .document(notificationData.notificationPath!!)
                .set(notificationData)
        }
    }

    override fun getNotification(callback: LongTaskCallback<Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .collection("notification")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener {

                    if(it.isSuccessful && it.result != null) {
                        val size = it.result!!.size()
                        val resultList = mutableListOf<NotificationData>()
                        val documentList : MutableList<DocumentSnapshot> = it.result!!.documents
                        for(element in documentList) {
                            val notiData = NotificationData(
                                element.getString("title")!!,
                                element.getString("body")!!,
                                element.getLong("timestamp")!!,
                                element.getString("notificationPath")!!,
                                element.getString("feedPath")!!,
                                element.getBoolean("check")!!,
                                element.getString("email")!!,
                                element.getString("chatPathEmail")!!,
                                element.getString("nickname")!!,
                                element.getString("profileUri")
                            )
                            resultList.add(notiData)
                        }

                        if(size == resultList.size)
                            sendCallback(resultList.toList(), false, callback)
                    }
                }.addOnFailureListener{
                    sendCallback(it, true, callback)
                }
        }
    }

    override fun setCheckDot(notificationData: NotificationData) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .collection("notification")
                .document(notificationData.notificationPath!!)
                .update("check",false)
        }
    }

    override fun deleteNotification(
        notificationData: NotificationData,
        callback: LongTaskCallback<Any>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .collection("notification")
                .document(notificationData.notificationPath!!)
                .delete()
                .addOnCompleteListener {
                    sendCallback(true, false, callback)
                    callback.onResponse(Result.Success(true))
                }.addOnFailureListener {
                    sendCallback(it, true, callback)
                    callback.onResponse(Result.Error(it))
                }
        }
    }

    private fun sendCallback(any : Any, isError : Boolean, callback: LongTaskCallback<Any>?)  {
        CoroutineScope(Dispatchers.Main).launch {
            if(!isError)
                callback?.onResponse(Result.Success(any))
            else
                callback?.onResponse(Result.Error(any as java.lang.Exception))
        }
    }
}