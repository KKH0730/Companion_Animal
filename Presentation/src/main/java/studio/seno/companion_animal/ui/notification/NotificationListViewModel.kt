package studio.seno.companion_animal.ui.notification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.Repository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.NotificationData

class NotificationListViewModel : ViewModel() {
    private var notificationListLiveData : MutableLiveData<List<NotificationData>> = MutableLiveData()
    private val repository = Repository()

    fun getNotificationListLiveData() : MutableLiveData<List<NotificationData>>{
        return notificationListLiveData
    }


    fun requestLoadNotification(myEmail : String) {
        repository.requestLoadNotification(myEmail, object : LongTaskCallback<List<NotificationData>>{
            override fun onResponse(result: Result<List<NotificationData>>) {
                if(result is Result.Success) {
                    notificationListLiveData.value = result.data
                }else if(result is Result.Error) {
                    Log.e("error","load notification : ${result.exception}")
                }
            }
        })
    }

    fun requestUpdateCheckDot(myEmail : String, notificationData : NotificationData){
        repository.requestUpdateCheckDot(myEmail, notificationData)

    }
}
