package studio.seno.datamodule.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import studio.seno.datamodule.model.NotificationModel

interface ApiInterface {

    @Headers("Authorization: key=" + "AAAAbIORjeo:APA91bHM2v0aCElUY-N5X2gaL_lgLxcTLFsKHbDZqdr4Zis_oEHQv6oSnTqrHjoaJyQ4wo6lMRKaD17LayynEBRhXDfM8yEDfwQTRcCm0Dn7uOIUuhBZnSDhbeVem2wD5aTaLq-rMB8N", "Content-Type:application/json")
    @POST("fcm/send")
    fun sendNotification(
        @Body notificationModel: NotificationModel
    ): Call<ResponseBody>


}