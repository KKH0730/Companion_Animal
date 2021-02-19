package studio.seno.companion_animal.ui.follow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.Follow
import studio.seno.domain.model.NotificationData

class FollowViewModel(category : String) : ViewModel() {
    private var followLiveData : MutableLiveData<Follow> = MutableLiveData()
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

}