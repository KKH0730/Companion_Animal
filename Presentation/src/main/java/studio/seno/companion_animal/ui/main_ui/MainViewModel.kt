package studio.seno.companion_animal.ui.main_ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.RemoteRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.model.User

class MainViewModel : ViewModel() {
    private val userLiveData : MutableLiveData<User> = MutableLiveData()
    private val repository = RemoteRepository.getInstance()!!

    fun getUserLiveData() : MutableLiveData<User>{
        return userLiveData
    }

    fun requestUserData(email : String, callback : LongTaskCallback<User>){
        repository.loadUserInfo(email, callback)
    }

    fun requestUpdateToken(token : String){
        repository.updateToken(token)
    }

    fun requestUpdateNickname(content: String) {
        repository.requestUpdateNickname(content)
    }


}