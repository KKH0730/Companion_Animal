package studio.seno.companion_animal.ui.notification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.NotificationData
import studio.seno.domain.usecase.feedUseCase.GetFeedUseCase
import studio.seno.domain.usecase.notificationUseCase.DeleteNotificationUseCase
import studio.seno.domain.usecase.notificationUseCase.GetNotificationUseCase
import studio.seno.domain.usecase.notificationUseCase.SetCheckDotUseCase

class NotificationListViewModel(
    private val getFeedUseCase: GetFeedUseCase,
    private val getNotificationUseCase: GetNotificationUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val checkDotUseCase: SetCheckDotUseCase
) : ViewModel() {
    private var notificationListLiveData : MutableLiveData<List<NotificationData>> = MutableLiveData()


    fun getNotificationListLiveData() : MutableLiveData<List<NotificationData>>{
        return notificationListLiveData
    }


    fun requestLoadNotification() {
        getNotificationUseCase.execute(object : LongTaskCallback<List<NotificationData>> {
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
        checkDotUseCase.execute(notificationData)
    }

    fun deleteNotification(notificationData : NotificationData){
        deleteNotificationUseCase.execute(notificationData, object :
            LongTaskCallback<Boolean> {
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


    fun getFeed(path : String, callback: LongTaskCallback<Feed>){
        getFeedUseCase.execute(path, callback)
    }
}
