package studio.seno.companion_animal.ui.notification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.RemoteRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.NotificationData

class NotificationListViewModel : ViewModel() {
    private var notificationListLiveData : MutableLiveData<List<NotificationData>> = MutableLiveData()
    private val remoteRepository = RemoteRepository.getInstance()!!

    fun getNotificationListLiveData() : MutableLiveData<List<NotificationData>>{
        return notificationListLiveData
    }


    fun requestLoadNotification() {
        remoteRepository.requestLoadNotification(object : LongTaskCallback<List<NotificationData>>{
            override fun onResponse(result: Result<List<NotificationData>>) {
                if(result is Result.Success) {
                    notificationListLiveData.value = result.data
                }else if(result is Result.Error) {
                    Log.e("error","load notification : ${result.exception}")
                }
            }
        })
    }

    fun requestUpdateCheckDot(notificationData : NotificationData){
        remoteRepository.requestUpdateCheckDot(notificationData)
    }

    fun deleteNotification(notificationData : NotificationData){
        remoteRepository.requestDeleteNotification(notificationData, object : LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    val list = notificationListLiveData.value?.toMutableList()
                    if(list != null) {
                        list.remove(notificationData)
                        list.toList()
                    }
                    notificationListLiveData.value = list

                } else if(result is Result.Error) {
                    Log.e("error", "deleteNotification error : ${result.exception}")
                }
            }
        })
    }
}
