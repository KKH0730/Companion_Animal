package studio.seno.domain.Repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

interface NotificationRepository {

    fun setNotificationInfo(
        notificationData : NotificationData
    )

    fun getNotification(callback : LongTaskCallback<List<NotificationData>>)

    fun setCheckDot(notificationData : NotificationData)

    fun deleteNotification(
        notificationData : NotificationData,
        callback: LongTaskCallback<Boolean>
    )
}