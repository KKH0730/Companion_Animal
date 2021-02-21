package studio.seno.companion_animal.ui.follow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.Follow
import studio.seno.domain.model.NotificationData

class FollowViewModel(category : String, targetEmail : String) : ViewModel() {
    private var followLiveData : MutableLiveData<Follow> = MutableLiveData()
    private var mTargetEmail = targetEmail
    private val mCategory = category

    fun getFollowLiveData() : MutableLiveData<Follow> {
        return followLiveData
    }

    fun setFollowLiveData(follow: Follow){
        followLiveData.value = follow
    }

    fun getCategory(): String{
        return mCategory
    }

    fun getTargetEmail() : String{
        return mTargetEmail
    }

}