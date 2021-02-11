package studio.seno.companion_animal.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            Log.d("hi", "title : ${it.title}")

            Log.d("hi", "body : ${it.body}")
        }

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("hi", "onNewToken")
    }
}