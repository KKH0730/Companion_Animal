package studio.seno.domain.usecase.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.NotificationData

class NotificationUseCase {

    fun uploadNotificationInfo(myEmail : String, notificationData : NotificationData, db : FirebaseFirestore){
        db.collection("user")
            .document(myEmail)
            .collection("notification")
           .document(notificationData.myPath!!)
            .set(notificationData)
    }
    
    fun loadNotification(myEmail : String, db : FirebaseFirestore, callback : LongTaskCallback<List<NotificationData>>) {
        db.collection("user")
            .document(myEmail)
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

    fun updateCheckDot(myEmail : String, notificationData : NotificationData, db : FirebaseFirestore){
        db.collection("user")
            .document(myEmail)
            .collection("notification")
            .document(notificationData.myPath!!)
            .update("check",false)
    }

    fun deleteNotification(myEmail: String, notificationData : NotificationData, db : FirebaseFirestore, callback: LongTaskCallback<Boolean>){
        db.collection("user")
            .document(myEmail)
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