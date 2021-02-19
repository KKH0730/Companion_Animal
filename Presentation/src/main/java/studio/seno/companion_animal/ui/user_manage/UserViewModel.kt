package studio.seno.companion_animal.ui.user_manage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.companion_animal.module.UserViewModelModule
import studio.seno.datamodule.Repository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result

class UserViewModel() : ViewModel() {
    private val memberViewModelModule = UserViewModelModule(this)
    private val loginLiveData : MutableLiveData<Boolean> = MutableLiveData()
    private val findPasswordListData : MutableLiveData<Boolean> = MutableLiveData()
    private val registerLiveData : MutableLiveData<Boolean> = MutableLiveData()
    private val uploadLiveData : MutableLiveData<Boolean> = MutableLiveData()
    private val overLapLiveData : MutableLiveData<Boolean> = MutableLiveData()
    private val mapper = Mapper()
    private val repository = Repository()

    fun getLoginLiveDate() : MutableLiveData<Boolean>{
        return loginLiveData
    }

    fun setLoginLiveData(bool : Boolean){
        this.loginLiveData.value = bool
    }

    fun getFindPasswordListData() : MutableLiveData<Boolean> {
        return findPasswordListData
    }

    fun setFindPasswordListData(bool : Boolean) {
        this.findPasswordListData.value = bool
    }

    fun getRegisterLiveData() : MutableLiveData<Boolean> {
        return registerLiveData
    }

    fun setRegisterLiveData(bool : Boolean) {
        this.registerLiveData.value = bool
    }

    fun enableLogin(email : String, password : String)  {
        memberViewModelModule.enableLogin(email, password)
    }

    fun sendFindEmail(emailAddress : String){
        memberViewModelModule.sendFindEmail(emailAddress)
    }

    fun registerUser(email : String, password : String){
        memberViewModelModule.registerUser(email, password)
    }

    fun getUpLoadLiveData() : MutableLiveData<Boolean>{
        return uploadLiveData
    }

    fun getOverLapLiveData() : MutableLiveData<Boolean> {
        return overLapLiveData
    }

    fun requestLoadProfileUri(callback: LongTaskCallback<String>){
        repository.loadRemoteProfileImage(callback)
    }

    fun requestUpload( imageUri : Uri, callback: LongTaskCallback<Boolean>){
        repository.uploadInItProfileImage(imageUri, object : LongTaskCallback<Boolean>{
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
        var user = mapper.mapperToUser(id, email, nickname, follower, following, feedCount, token, profileUri)
        repository.uploadUserInfo(user)
    }

    fun checkOverlapEmail(email : String) {
        repository.checkOverlapEmail(email, object : LongTaskCallback<Boolean>{
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