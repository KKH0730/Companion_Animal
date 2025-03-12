package studio.seno.companion_animal.ui.user_manage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import studio.seno.domain.usecase.uploadUseCase.GetProfileImageUseCase
import studio.seno.domain.usecase.uploadUseCase.SetProfileImageUseCase
import studio.seno.domain.usecase.userMangerUseCase.*
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
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

    fun checkEnableLogin(email : String, password : String, callback : LongTaskCallback<Any>)  {
        checkEnableLoginUseCase.execute(email, password, callback)
    }

    fun requestSendFindEmail(emailAddress : String){
        findPasswordUseCase.execute(emailAddress, object : LongTaskCallback<Any> {
            override fun onResponse(result: Result<Any>) {
                if(result is Result.Success)
                    findPasswordListData.value = result.data as Boolean
                else if(result is Result.Error)
                    Log.e("error", "UserViewModel sendFindEmail error : ${result.exception}")
            }
        })
    }

    fun registerUser(email : String, password : String, callback: LongTaskCallback<Any>){
        registerUserUseCase.execute(email, password, callback)
    }

    fun loadProfileUri(email: String, callback: LongTaskCallback<Any>){
        getProfileImageUseCase.execute(email, object  : LongTaskCallback<Any>{
            override fun onResponse(result: Result<Any>) {
                if(result is Result.Success)
                    callback.onResponse(Result.Success(result.data as String))
                else if(result is Result.Error) {
                    Log.e("error", "UserViewModel loadProfileUri error: ${result.exception}")
                }
            }
        })
    }

    fun uploadProfileImage(imageUri : Uri, callback: LongTaskCallback<Boolean>){
        setProfileImageUseCase.execute(imageUri, object : LongTaskCallback<Any> {
            override fun onResponse(result: Result<Any  >) {
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

    fun requestCheckOverlapEmail(email: String, callback: LongTaskCallback<Any>) {
        checkOverlapUserUseCase.execute(email, object : LongTaskCallback<Any> {
            override fun onResponse(result: Result<Any>) {
                if(result is Result.Success) {
                    overLapLiveData.value = result.data as Boolean
                    callback.onResponse(Result.Success(result.data as Boolean))
                }

                else if(result is Result.Error)
                    Log.e("error", "UserViewModel checkOverlapEmail error : ${result.exception}")
            }
        })
    }

    fun updateRemoteProfileUri(profileUri : String){
        setProfileUriUseCase.execute(profileUri)
    }

}