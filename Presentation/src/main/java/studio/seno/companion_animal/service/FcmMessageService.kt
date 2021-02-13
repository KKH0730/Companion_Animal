package studio.seno.companion_animal.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import studio.seno.companion_animal.module.NotificationModule
import studio.seno.companion_animal.ui.feed.MakeFeedActivity
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.datamodule.Repository
import studio.seno.domain.util.PrefereceManager

class FcmMessageService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            var notificationModel = NotificationModule(applicationContext)
            val intent = Intent(applicationContext, MakeFeedActivity::class.java)
            it.title?.let { it1 -> it.body?.let { it2 ->
                notificationModel.makeNotification(it1,
                    it2,
                    intent
                )
            } }
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PrefereceManager.setString(applicationContext, "token", token)
    }
}