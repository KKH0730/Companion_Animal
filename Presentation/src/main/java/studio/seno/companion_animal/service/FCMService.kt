package studio.seno.companion_animal.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.activity.viewModels
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.domain.util.PrefereceManager

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            Log.d("hi", "title : ${it.title}")
            Log.d("hi", "body : ${it.body}")
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("hi", "onNewToken")
    }
}