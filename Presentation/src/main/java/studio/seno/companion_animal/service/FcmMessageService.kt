package studio.seno.companion_animal.service

import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import studio.seno.companion_animal.MainActivity
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.companion_animal.ui.feed.MakeFeedActivity
import studio.seno.datamodule.Repository
import studio.seno.domain.model.NotificationData
import studio.seno.domain.util.PrefereceManager
import java.sql.Timestamp

class FcmMessageService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"].toString()
        val body = remoteMessage.data["body"].toString()


        Repository().uploadNotificationInfo(
            FirebaseAuth.getInstance().currentUser?.email.toString(), //notification 저장경로
            NotificationData(
                remoteMessage.data["title"].toString(),
                remoteMessage.data["body"].toString(), //content
                remoteMessage.data["timestamp"]?.toLong(),
                remoteMessage.data["myPath"].toString(), //알림받은 피드의 경로
                remoteMessage.data["targetPath"].toString(),
                true
            )
        )

        remoteMessage.data.let {
            var notificationModel = NotificationModule(applicationContext)
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("from", "notification")
            intent.putExtra("target_path", remoteMessage.data["targetPath"].toString())
            notificationModel.makeNotification(title, body, intent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PrefereceManager.setString(applicationContext, "token", token)
    }
}