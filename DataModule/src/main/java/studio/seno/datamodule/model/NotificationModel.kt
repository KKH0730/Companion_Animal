package studio.seno.datamodule.model

import com.google.gson.annotations.SerializedName
import studio.seno.domain.model.NotificationData

class NotificationModel {
    @SerializedName("to")
    private var token : String? = null

    @SerializedName("notification")
    private var notificationData : NotificationData? = null

    constructor(token : String, notificationData: NotificationData) {
        this.token = token
        this.notificationData = notificationData
    }

    fun getToken() : String? {
        return token
    }

    fun setToken(token : String){
        this.token = token
    }

    fun getNotificationData() : NotificationData? {
        return notificationData
    }

    fun setNotificationData(notificationData: NotificationData){
        this.notificationData = notificationData
    }

}