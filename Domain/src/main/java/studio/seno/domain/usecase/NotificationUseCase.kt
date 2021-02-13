package studio.seno.domain.usecase

import android.util.Log
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
           .document(notificationData.targetPath!!)
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
                            element.getString("targetPath")!!
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
}