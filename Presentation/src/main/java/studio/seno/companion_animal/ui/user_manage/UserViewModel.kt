package studio.seno.companion_animal.ui.user_manage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.usecase.uploadUseCase.GetProfileImageUseCase
import studio.seno.domain.usecase.uploadUseCase.SetProfileImageUseCase
import studio.seno.domain.usecase.userMangerUseCase.*
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class UserViewModel(
    private val checkEnableLoginUseCase: CheckEnableLoginUseCase,
    private val findPasswordUseCase: FindPasswordUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val setProfileImageUseCase: SetProfileImageUseCase,
    private val getProfileImageUseCase: GetProfileImageUseCase,
    private val setUserInfoUseCase: SetUserInfoUseCase,
    private val checkOverlapUserUseCase: CheckOverlapUserUseCase,
    private val setProfileUriUseCase: SetProfileUriUseCase
) : ViewModel() {
    private val findPasswordListData : MutableLiveData<Boolean> = MutableLiveData()
    private val uploadLiveData : MutableLiveData<Boolean> = MutableLiveData()
    private val overLapLiveData : MutableLiveData<Boolean> = MutableLiveData()



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

    fun checkEnableLogin(email : String, password : String, callback : LongTaskCallback<Boolean>)  {
        checkEnableLoginUseCase.execute(email, password, callback)
    }

    fun requestSendFindEmail(emailAddress : String){
        findPasswordUseCase.execute(emailAddress, object : LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success)
                    findPasswordListData.value = result.data
                else if(result is Result.Error)
                    Log.e("error", "UserViewModel sendFindEmail error : ${result.exception}")
            }
        })
    }

    fun registerUser(email : String, password : String, callback: LongTaskCallback<Boolean>){
        registerUserUseCase.execute(email, password, callback)
    }

    fun loadProfileUri(email: String, callback: LongTaskCallback<String>){
        getProfileImageUseCase.execute(email, object  : LongTaskCallback<String>{
            override fun onResponse(result: Result<String>) {
                if(result is Result.Success)
                    callback.onResponse(Result.Success(result.data))
                else if(result is Result.Error) {
                    Log.e("error", "UserViewModel loadProfileUri error: ${result.exception}")
                }
            }
        })
    }

    fun uploadProfileImage(imageUri : Uri, callback: LongTaskCallback<Boolean>){
        setProfileImageUseCase.execute(imageUri, object : LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    uploadLiveData.value = true
                    callback.onResponse(Result.Success(true))

                } else if(result is Result.Error) {
                    uploadLiveData.value = false
                    Log.e("error", "UserViewModel uploadProfileImage error: ${result.exception}")
                }
            }
        })
    }

    fun requestUploadUserInfo(id : Long, email: String, nickname: String, follower: Long,
                        following: Long, feedCount: Long, token : String, profileUri : String){
        setUserInfoUseCase.execute(id, email, nickname, follower, following, feedCount, token, profileUri)
    }

    fun requestCheckOverlapEmail(email: String) {
        checkOverlapUserUseCase.execute(email, object : LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success)
                    overLapLiveData.value = result.data

                else if(result is Result.Error)
                    Log.e("error", "UserViewModel checkOverlapEmail error : ${result.exception}")
            }
        })
    }

    fun updateRemoteProfileUri(profileUri : String){
        setProfileUriUseCase.execute(profileUri)
    }

}