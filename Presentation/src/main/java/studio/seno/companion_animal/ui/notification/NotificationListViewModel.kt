package studio.seno.companion_animal.ui.notification

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.NotificationData
import studio.seno.domain.usecase.feedUseCase.GetFeedUseCase
import studio.seno.domain.usecase.notificationUseCase.DeleteNotificationUseCase
import studio.seno.domain.usecase.notificationUseCase.GetNotificationUseCase
import studio.seno.domain.usecase.notificationUseCase.SetCheckDotUseCase
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
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
        getNotificationUseCase.execute(object : LongTaskCallback<Any> {
            override fun onResponse(result: Result<Any>) {
                if(result is Result.Success) {
                    notificationListLiveData.value = result.data as List<NotificationData>
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
            LongTaskCallback<Any> {
            override fun onResponse(result: Result<Any>) {
                if(result is Result.Success) {
                    val list = notificationListLiveData.value?.toMutableList()
                    if(list != null) {
                        list.remove(notificationData)
                        list.toList()
                    }
                    notificationListLiveData.value = list ?: listOf()

                } else if(result is Result.Error) {
                    Log.e("error", "deleteNotification error : ${result.exception}")
                }
            }
        })
    }


    fun getFeed(path : String, callback: LongTaskCallback<Any>){
        getFeedUseCase.execute(path, callback)
    }
}
