package studio.seno.companion_animal.ui.follow

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import studio.seno.datamodule.RemoteRepository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Follow

class FollowListViewModel() : ViewModel() {
    private var followListLiveData : MutableLiveData<List<Follow>> = MutableLiveData()
    private val repository = RemoteRepository.getInstance()!!


    fun getFollowListLiveData() : MutableLiveData<List<Follow>> {
        return followListLiveData
    }



    fun requestLoadFollower(){
        repository.loadFollower("follower", object : LongTaskCallback<List<Follow>>{
            override fun onResponse(result: Result<List<Follow>>) {
                if(result is Result.Success){
                    followListLiveData.value = result.data
                } else if(result is Result.Error){
                    Log.e("error", "FollowListViewModel load Follower Error : ${result.exception}")
                }
            }
        })
    }

    fun requestLoadFollowing(){
        repository.loadFollower("following", object : LongTaskCallback<List<Follow>>{
            override fun onResponse(result: Result<List<Follow>>) {
                if(result is Result.Success){
                    followListLiveData.value = result.data
                } else if(result is Result.Error){
                    Log.e("error", "FollowListViewModel load Following Error : ${result.exception}")
                }
            }
        })
    }

    fun requestUpdateFollower(follow : Follow, flag : Boolean, myNickName : String, myProfileUri : String, isDelete : Boolean) {
        val targetFollow = Mapper.getInstance()!!.mapperToFollow(follow.email, follow.nickname, follow.profileUri)
        val myFollow = Mapper.getInstance()!!.mapperToFollow(FirebaseAuth.getInstance().currentUser?.email.toString(), myNickName, myProfileUri)
        repository.requestUpdateFollower(follow.email, flag, myFollow, targetFollow)

        if(isDelete){
            val tempList = followListLiveData.value?.toMutableList()
            tempList?.remove(follow)
            followListLiveData.value = tempList?.toList()
        }
    }

    fun requestCheckFollow(targetEmail : String ,callback : LongTaskCallback<Boolean>){
        repository.requestCheckFollow(targetEmail, callback)
    }
}