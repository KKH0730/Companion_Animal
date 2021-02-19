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
import com.google.firebase.auth.FirebaseAuth
import okhttp3.ResponseBody
import studio.seno.companion_animal.R
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.api.ApiClient
import studio.seno.datamodule.api.ApiInterface
import studio.seno.datamodule.model.NotificationModel
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.NotificationData
import studio.seno.domain.model.User
import studio.seno.domain.util.PrefereceManager


class NotificationModule(context: Context, mainViewModel: MainViewModel?) {
    private var notificationManager: NotificationManager? = null
    private val myEmail  = FirebaseAuth.getInstance().currentUser?.email.toString()
    private val myNickname =  PrefereceManager.getString(context, "nickName")!!
    private var mContext = context
    private val mMainViewModel = mainViewModel

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


    fun sendNotification(targetEmail: String, content: String, currentTimestamp: Long, feed : Feed) {
        //댓글을 작성하면 notification 알림이 전송
        if(targetEmail == myEmail)
            return

        Log.d("hi", "send")

        mMainViewModel?.requestUserData(targetEmail, object : LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if (result is Result.Success) {

                    val notificationModel = NotificationModel(
                        result.data.token,
                        NotificationData(
                            myNickname,
                            content,
                            currentTimestamp,
                            feed.email + currentTimestamp,
                            feed.email + feed.timestamp,
                            true
                        )
                    )

                    var apiService = ApiClient.getClient().create(ApiInterface::class.java)
                    var responseBodyCall: retrofit2.Call<ResponseBody> =
                        apiService.sendNotification(notificationModel)
                    responseBodyCall.enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(call: retrofit2.Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {}

                        override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {}

                    })
                }
            }
        })
    }
}
