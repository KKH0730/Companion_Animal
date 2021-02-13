package studio.seno.companion_animal.ui.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.NotificationData

class NotificationListViewModel : ViewModel() {
    private var notificationListLiveData : MutableLiveData<List<NotificationData>> = MutableLiveData()

    fun getNotificationListLiveData() : MutableLiveData<List<NotificationData>>{
        return notificationListLiveData
    }



}