package studio.seno.domain.usecase

import com.google.firebase.firestore.FirebaseFirestore
import studio.seno.domain.model.NotificationData

class NotificationUseCase {

    fun uploadNotificationInfo(myEmail : String, notificationData : NotificationData, db : FirebaseFirestore){

        db.collection("user")
            .document(myEmail)
            .collection("notification")
           .document(notificationData.targetPath)
            .set(notificationData)
    }
}