package studio.seno.companion_animal.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import studio.seno.companion_animal.MainActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.datamodule.RemoteRepository
import studio.seno.domain.model.NotificationData

class FcmMessageService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"].toString()
        val body = remoteMessage.data["body"].toString()


        if(remoteMessage.data["myPath"] != "chat" && remoteMessage.data["targetPath"] != "chat") {
            RemoteRepository.getInstance()!!.uploadNotificationInfo( //notification 저장경로
                NotificationData(
                    remoteMessage.data["title"].toString(),
                    remoteMessage.data["body"].toString(), //content
                    remoteMessage.data["timestamp"]?.toLong(),
                    remoteMessage.data["myPath"].toString(), //알림받은 피드의 경로
                    remoteMessage.data["targetPath"].toString(),
                    true
                )
            )
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        var notificationModel = NotificationModule(applicationContext)

        if(remoteMessage.data["myPath"] != "chat" && remoteMessage.data["targetPath"] != "chat") {
            intent.putExtra("from", "notification")
            intent.putExtra("target_path", remoteMessage.data["targetPath"].toString())
            notificationModel.makeNotification(title + applicationContext.getString(R.string.noti_title), body, intent, "noti")
        } else {
            intent.putExtra("from", "chat")
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            notificationModel.makeNotification(title + applicationContext.getString(R.string.chat_title), body, intent, "chat")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}