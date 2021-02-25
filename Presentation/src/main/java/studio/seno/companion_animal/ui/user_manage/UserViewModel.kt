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

    fun requestCheckEnbleLogin(email : String, password : String, callback : LongTaskCallback<Boolean>)  {
        remoteRepository.requestCheckEnableLogin(email, password, callback)
    }

    fun sendFindEmail(emailAddress : String){
        remoteRepository.requestSendFindEmail(emailAddress, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    if(result.data)
                        findPasswordListData.value = true
                    else
                        findPasswordListData.value = false
                }
            }
        })
    }

    fun registerUser(email : String, password : String, callback: LongTaskCallback<Boolean>){
        remoteRepository.requestRegisterUser(email, password, callback)
    }


    fun requestLoadProfileUri(email: String, callback: LongTaskCallback<String>){
        remoteRepository.loadRemoteProfileImage(email, callback)
    }

    fun requestUploadInItProfileImage( imageUri : Uri, callback: LongTaskCallback<Boolean>){
        remoteRepository.uploadInItProfileImage(imageUri, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    uploadLiveData.value = true
                    callback.onResponse(Result.Success(true))

                } else if(result is Result.Error) {
                    uploadLiveData.value = false
                    Log.e("error", "uploadInItProfileImage : ${result.exception}")
                }

            }
        })
    }

    fun uploadUserInfo(id : Long, email: String, nickname: String, follower: Long,
                        following: Long, feedCount: Long, token : String, profileUri : String){
        var user = Mapper.getInstance()!!.mapperToUser(id, email, nickname, follower, following, feedCount, token, profileUri)
        remoteRepository.uploadUserInfo(user)
    }

    fun checkOverlapEmail(email : String) {
        remoteRepository.checkOverlapEmail(email, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    if(result.data) {
                        overLapLiveData.value = true
                    } else {
                        overLapLiveData.value = false
                    }
                } else if(result is Result.Error){
                    Log.e("error", "checkOverlapEmail : ${result.exception}")
                }
            }
        })
    }
}