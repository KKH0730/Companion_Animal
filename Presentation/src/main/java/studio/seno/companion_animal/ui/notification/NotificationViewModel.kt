package studio.seno.companion_animal.ui.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.NotificationData

class NotificationViewModel : ViewModel() {
    private var notificationLiveData : MutableLiveData<NotificationData> = MutableLiveData()

    fun getNotificationLiveData() : MutableLiveData<NotificationData>{
        return notificationLiveData
    }

    fun setNotificationLiveData(notificationData: NotificationData){
        notificationLiveData.value = notificationData
    }
}