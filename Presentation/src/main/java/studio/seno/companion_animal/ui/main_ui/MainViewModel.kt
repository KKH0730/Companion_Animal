package studio.seno.companion_animal.ui.main_ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.Repository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.User

class MainViewModel : ViewModel() {
    private val userLiveData : MutableLiveData<User> = MutableLiveData()


    fun getUserLiveData() : MutableLiveData<User>{
        return userLiveData
    }

    fun requestUserData(email : String){
        Repository().loadUserInfo(email, object : LongTaskCallback<User>{
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success)
                    userLiveData.value = result.data
            }
        })
    }

}