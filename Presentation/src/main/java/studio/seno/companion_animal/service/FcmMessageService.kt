package studio.seno.companion_animal.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import studio.seno.companion_animal.MainActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.datamodule.repository.remote.NotificationRepositoryImpl
import studio.seno.domain.model.NotificationData

class FcmMessageService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"].toString()
        val body = remoteMessage.data["body"].toString()
        val timestamp = remoteMessage.data["timestamp"]?.toLong()
        val notificationPath = remoteMessage.data["notificationPath"].toString()
        val feedPath = remoteMessage.data["feedPath"].toString()
        val email = remoteMessage.data["email"].toString()
        val chatPathEmail = remoteMessage.data["chatPathEmail"].toString()
        val nickname = remoteMessage.data["nickname"].toString()
        val profileUri = remoteMessage.data["profileUri"].toString()

        if(remoteMessage.data["feedPath"] != "chat") {
            NotificationRepositoryImpl().setNotificationInfo( //notification 저장경로
                NotificationData(
                    title,
                    body, //content
                    timestamp,
                    notificationPath, //알림받은 피드의 경로
                    feedPath,
                    true,
                    email,
                    chatPathEmail,
                    nickname,
                    profileUri
                )
            )
        }


        var notificationModel = NotificationModule(applicationContext, title)

        if(remoteMessage.data["feedPath"] != "chat" && remoteMessage.data["targetPath"] != "chat") {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("from", "notification")
            intent.putExtra("target_path", remoteMessage.data["feedPath"].toString())
            notificationModel.makeNotification(title + applicationContext.getString(R.string.noti_title), body, intent, "noti")
        } else {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("from", "chat")
            intent.putExtra("targetRealEmail", email)
            intent.putExtra("targetEmail", chatPathEmail)
            intent.putExtra("targetNickname", nickname)
            intent.putExtra("targetProfileUri", profileUri)
            notificationModel.makeNotification(title + applicationContext.getString(R.string.chat_title), body, intent, "chat")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}