package studio.seno.companion_animal.service

import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.companion_animal.ui.feed.MakeFeedActivity
import studio.seno.datamodule.Repository
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.PrefereceManager
import java.sql.Timestamp

class FcmMessageService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        Log.d("hi","onMessageReceived")
        if (body?.isNotEmpty()!!) {
            val array : List<String> = body.split(" ")
            Repository().uploadNotificationInfo(
                FirebaseAuth.getInstance().currentUser?.email.toString(),
                NotificationData(
                    title!!,
                    array.get(1),
                    Timestamp(System.currentTimeMillis()).time,
                    array.get(0))
            )
        }


        remoteMessage.notification?.let {
            var notificationModel = NotificationModule(applicationContext)
            val intent = Intent(applicationContext, MakeFeedActivity::class.java)

            if (title != null && body != null) {
                notificationModel.makeNotification(title, body, intent)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PrefereceManager.setString(applicationContext, "token", token)
    }
}