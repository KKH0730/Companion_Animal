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
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import studio.seno.companion_animal.R
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.LocalRepository
import studio.seno.datamodule.RemoteRepository
import studio.seno.datamodule.api.ApiClient
import studio.seno.datamodule.api.ApiInterface
import studio.seno.datamodule.model.NotificationModel
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.NotificationData
import studio.seno.domain.model.User
import studio.seno.domain.util.PreferenceManager


class NotificationModule(context: Context, title: String) {
    private var notificationManager: NotificationManager? = null
    private val myNickname = title
    private var mContext = context

    fun getNotificationManager(): NotificationManager {
        if (notificationManager == null) {
            notificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return notificationManager!!
    }

    fun makeNotificationChannel(sort : String) {
        var channel : NotificationChannel? = null

        if(sort == "noti") {
            channel = NotificationChannel(
                Constants.NOTI_CHANNEL1,
                mContext.getString(R.string.noti_name1),
                NotificationManager.IMPORTANCE_HIGH
            )
        } else if(sort == "chat") {
            channel = NotificationChannel(
                Constants.NOTI_CHANNEL2,
                mContext.getString(R.string.noti_name2),
                NotificationManager.IMPORTANCE_HIGH
            )
        }

        if (channel != null) {
            getNotificationManager().createNotificationChannel(channel)
        }
    }

    fun makeNotification(title: String, content: String, intent: Intent, sort : String) {
        var pendingIntent = PendingIntent.getActivity(
            mContext,
            Constants.NOTI_REQUEST,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )


        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var notificationBuilder : Notification.Builder? = null

        if(sort == "noti")
            notificationBuilder = Notification.Builder(mContext, Constants.NOTI_CHANNEL1)
        else if(sort == "chat")
            notificationBuilder = Notification.Builder(mContext, Constants.NOTI_CHANNEL2)

        if (notificationBuilder != null) {
            notificationBuilder
                .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, R.drawable.logo))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(sort)
        }

        getNotificationManager().notify(Constants.NOTI_ID, notificationBuilder?.build())
    }


    fun sendNotification(
        targetEmail: String,
        myProfileUri : String?,
        content: String,
        currentTimestamp: Long,
        feed: Feed?,
        lifecycleCoroutineScope: LifecycleCoroutineScope
    ) {
        //댓글을 작성하면 notification 알림이 전송
        if (targetEmail == FirebaseAuth.getInstance().currentUser?.email.toString()) {
            return
        }

        RemoteRepository.getInstance()!!.loadUserInfo(targetEmail, object : LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if (result is Result.Success) {
                    var notificationModel: NotificationModel? = null

                    if (feed != null) {
                        notificationModel = NotificationModel(
                            result.data.token,
                            NotificationData(
                                myNickname,
                                content,
                                currentTimestamp,
                                feed.getEmail() + currentTimestamp,
                                feed.getEmail() + feed.getTimestamp(),
                                true,
                                FirebaseAuth.getInstance().currentUser?.email.toString(),
                                CommonFunction.getInstance()!!.makeChatPath(FirebaseAuth.getInstance().currentUser?.email.toString()),
                                myNickname,
                                myProfileUri
                            )
                        )
                    } else {
                        notificationModel = NotificationModel(
                            result.data.token,
                            NotificationData(
                                myNickname,
                                content,
                                currentTimestamp,
                                null,
                                "chat",
                                true,
                                FirebaseAuth.getInstance().currentUser?.email.toString() ,
                                CommonFunction.getInstance()!!.makeChatPath(FirebaseAuth.getInstance().currentUser?.email.toString()),
                                myNickname,
                                myProfileUri,
                            )
                        )
                    }

                    lifecycleCoroutineScope.launch(Dispatchers.IO){
                        var apiService = ApiClient.getClient().create(ApiInterface::class.java)
                        var responseBodyCall: retrofit2.Call<ResponseBody> =
                            apiService.sendNotification(notificationModel)

                        responseBodyCall.enqueue(object : retrofit2.Callback<ResponseBody> {
                            override fun onResponse(
                                call: retrofit2.Call<ResponseBody>,
                                response: retrofit2.Response<ResponseBody>
                            ) {
                            }
                            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                            }

                        })
                    }
                }
            }
        })

    }
}
