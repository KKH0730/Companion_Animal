package studio.seno.companion_animal.ui.user_manage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.RemoteRepository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result

class UserViewModel() : ViewModel() {
    private val findPasswordListData : MutableLiveData<Boolean> = MutableLiveData()
    private val uploadLiveData : MutableLiveData<Boolean> = MutableLiveData()
    private val overLapLiveData : MutableLiveData<Boolean> = MutableLiveData()
    private val remoteRepository = RemoteRepository.getInstance()!!



    fun getFindPasswordListData() : MutableLiveData<Boolean> {
        return findPasswordListData
    }

    fun setFindPasswordListData(bool : Boolean) {
        this.findPasswordListData.value = bool
    }

    fun getUpLoadLiveData() : MutableLiveData<Boolean>{
        return uploadLiveData
    }

    fun getOverLapLiveData() : MutableLiveData<Boolean> {
        return overLapLiveData
    }

    fun requestCheckEnableLogin(email : String, password : String, callback : LongTaskCallback<Boolean>)  {
        remoteRepository.requestCheckEnableLogin(email, password, callback)
    }

    fun requestSendFindEmail(emailAddress : String){
        remoteRepository.requestSendFindEmail(emailAddress, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success)
                    findPasswordListData.value = result.data
                else if(result is Result.Error)
                    Log.e("error", "UserViewModel sendFindEmail error : ${result.exception}")
            }
        })
    }

    fun requestRegisterUser(email : String, password : String, callback: LongTaskCallback<Boolean>){
        remoteRepository.requestRegisterUser(email, password, callback)
    }

    fun requestLoadProfileUri(email: String, callback: LongTaskCallback<String>){
        remoteRepository.requestLoadProfileUri(email, callback)
    }

    fun requestUploadInItProfileImage(imageUri : Uri, callback: LongTaskCallback<Boolean>){
        remoteRepository.uploadInItProfileImage(imageUri, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    uploadLiveData.value = true
                    callback.onResponse(Result.Success(true))

                } else if(result is Result.Error) {
                    uploadLiveData.value = false
                    Log.e("error", "UserViewModel uploadInItProfileImage error: ${result.exception}")
                }
            }
        })
    }

    fun requestUploadUserInfo(id : Long, email: String, nickname: String, follower: Long,
                        following: Long, feedCount: Long, token : String, profileUri : String){
        val user = Mapper.getInstance()!!.mapperToUser(id, email, nickname, follower, following, feedCount, token, profileUri)
        remoteRepository.uploadUserInfo(user)
    }

    fun requestCheckOverlapEmail(email : String) {
        remoteRepository.requestCheckOverlapEmail(email, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success)
                    overLapLiveData.value = result.data

                else if(result is Result.Error)
                    Log.e("error", "UserViewModel checkOverlapEmail error : ${result.exception}")
            }
        })
    }
}