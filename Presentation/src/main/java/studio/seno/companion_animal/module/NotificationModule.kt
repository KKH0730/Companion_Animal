package studio.seno.companion_animal.module

import android.app.Notification
import android.app.Notification.PRIORITY_MAX
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import studio.seno.companion_animal.R
import studio.seno.companion_animal.util.Constants


class NotificationModule(context: Context) {
    private var notificationManager: NotificationManager? = null
    private var mContext = context

    fun getNotificationManager(): NotificationManager {
        if (notificationManager == null) {
            notificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return notificationManager!!
    }

    fun makeNotificationChannel() {
        var channel = NotificationChannel(
            Constants.NOTI_CHANNEL,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_HIGH
        )
        getNotificationManager().createNotificationChannel(channel)
    }

    fun makeNotification(title: String, content: String, intent: Intent) {
        var pendingIntent = PendingIntent.getActivity(
            mContext,
            Constants.NOTI_REQUEST,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationBuilder = Notification.Builder(mContext, Constants.NOTI_CHANNEL)
            .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, R.drawable.logo))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title + mContext.getString(R.string.noti_title))
            .setContentText(content)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setContentIntent(pendingIntent)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel()
        }

        getNotificationManager().notify(Constants.NOTI_ID, notificationBuilder.build())
    }



}
