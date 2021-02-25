package studio.seno.companion_animal.service

import android.content.Intent
import android.util.Log
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
        val myPath = remoteMessage.data["myPath"].toString()

        if(remoteMessage.data["targetPath"] != "chat") {
            RemoteRepository.getInstance()!!.uploadNotificationInfo( //notification 저장경로
                NotificationData(
                    title,
                    body, //content
                    remoteMessage.data["timestamp"]?.toLong(),
                    myPath, //알림받은 피드의 경로
                    remoteMessage.data["targetPath"].toString(),
                    true
                )
            )
        }



        var notificationModel = NotificationModule(applicationContext, title)

        if(remoteMessage.data["myPath"] != "chat" && remoteMessage.data["targetPath"] != "chat") {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.setAction(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("from", "notification")
            intent.putExtra("target_path", remoteMessage.data["targetPath"].toString())
            notificationModel.makeNotification(title + applicationContext.getString(R.string.noti_title), body, intent, "noti")
        } else {
            val chatInfo : List<String> = myPath.split(" ")
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("from", "chat")
            intent.putExtra("targetRealEmail", chatInfo[0])
            intent.putExtra("targetEmail", chatInfo[1])
            intent.putExtra("targetNickname", chatInfo[2])
            intent.putExtra("targetProfileUri", chatInfo[3])
            notificationModel.makeNotification(title + applicationContext.getString(R.string.chat_title), body, intent, "chat")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}