package studio.seno.companion_animal.ui.follow

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import studio.seno.domain.model.Follow
import studio.seno.domain.usecase.followUseCase.GetFollowUseCase
import studio.seno.domain.usecase.followUseCase.SetFollowUseCase
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class FollowListViewModel(
    private val setFollowUseCase: SetFollowUseCase,
    private val getFollowUseCase: GetFollowUseCase
) : ViewModel() {
    private var followListLiveData : MutableLiveData<List<Follow>> = MutableLiveData()


    fun getFollowListLiveData() : MutableLiveData<List<Follow>> {
        return followListLiveData
    }


    fun requestLoadFollower(fieldName : String){
        getFollowUseCase.execute(fieldName, object : LongTaskCallback<Any> {
            override fun onResponse(result: Result<Any>) {
                if(result is Result.Success){
                    followListLiveData.value = result.data as List<Follow>
                } else if(result is Result.Error){
                    Log.e("error", "FollowListViewModel load Follower Error : ${result.exception}")
                }
            }
        })
    }


    fun requestUpdateFollow(follow : Follow, flag : Boolean, myNickName : String, myProfileUri : String, isDelete : Boolean) {
        setFollowUseCase.execute(
            follow.email,
            follow.nickname,
            follow.profileUri,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            myNickName,
            myProfileUri,
            flag
        )

        if(isDelete){
            val tempList = followListLiveData.value?.toMutableList()
            tempList?.remove(follow)
            followListLiveData.value = tempList?.toList()
        }
    }
}