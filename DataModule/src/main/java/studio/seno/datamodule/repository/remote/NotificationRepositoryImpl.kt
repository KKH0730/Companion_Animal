package studio.seno.datamodule.repository.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import studio.seno.domain.Repository.NotificationRepository
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class NotificationRepositoryImpl : NotificationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun setNotificationInfo(notificationData: NotificationData) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .collection("notification")
            .document(notificationData.myPath!!)
            .set(notificationData)
    }

    override fun getNotification(callback: LongTaskCallback<List<NotificationData>>) {
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
                            element.getString("myPath")!!,
                            element.getString("targetPath")!!,
                            element.getBoolean("check")!!,
                            element.getString("myEmail")!!,
                            element.getString("chatPathEmail")!!,
                            element.getString("myNickname")!!,
                            element.getString("myProfileUri")
                        )
                        resultList.add(notiData)
                    }

                    if(size == resultList.size)
                        callback.onResponse(Result.Success(resultList.toList()))
                }
            }.addOnFailureListener{
                callback.onResponse(Result.Error(it))
            }
    }

    override fun setCheckDot(notificationData: NotificationData) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .collection("notification")
            .document(notificationData.myPath!!)
            .update("check",false)
    }

    override fun deleteNotification(
        notificationData: NotificationData,
        callback: LongTaskCallback<Boolean>
    ) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .collection("notification")
            .document(notificationData.myPath!!)
            .delete()
            .addOnCompleteListener {
                callback.onResponse(Result.Success(true))
            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }
}