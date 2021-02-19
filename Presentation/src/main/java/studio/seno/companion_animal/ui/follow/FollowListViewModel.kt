package studio.seno.companion_animal.ui.follow

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.Repository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Follow

class FollowListViewModel() : ViewModel() {
    private var followListLiveData : MutableLiveData<List<Follow>> = MutableLiveData()
    private val repository = Repository()


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

}