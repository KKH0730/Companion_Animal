package studio.seno.companion_animal.ui.main_ui

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.usecase.feedUseCase.GetFeedUseCase
import studio.seno.domain.usecase.followUseCase.CheckFollowUseCase
import studio.seno.domain.usecase.followUseCase.SetFollowUseCase
import studio.seno.domain.usecase.userMangerUseCase.*

class MainViewModel(
    private val getFeedUseCase: GetFeedUseCase,
    private val GetUserInfoUseCase: GetUserInfoUseCase,
    private val setNicknameUseCase: SetNicknameUseCase,
    private val setTokenUseCase: SetTokenUseCase,
    private val checkFollowUseCase: CheckFollowUseCase,
    private val setFollowUseCase: SetFollowUseCase
    ) : ViewModel() {
    private val userLiveData : MutableLiveData<User> = MutableLiveData()

    fun getUserLiveData() : MutableLiveData<User>{
        return userLiveData
    }

    fun requestUserInfo(email : String, callback : LongTaskCallback<Any>){
        GetUserInfoUseCase.execute(email, callback)
    }

    fun requestUpdateToken(token : String){
        setTokenUseCase.execute(token)
    }

    fun requestUpdateNickname(content: String) {
        setNicknameUseCase.execute(content)
    }


    fun getFeed(path : String, callback: LongTaskCallback<Any>){
        getFeedUseCase.execute(path, callback)
    }

    fun checkFollow(targetEmail: String, callback : LongTaskCallback<Any>){
        checkFollowUseCase.execute(targetEmail, callback)
    }

    fun requestUpdateFollow(targetEmail : String, targetNickname: String, targetProfileUri : String, flag : Boolean, myNickName : String, myProfileUri : String) {
        setFollowUseCase.execute(
            targetEmail,
            targetNickname,
            targetProfileUri,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            myNickName,
            myProfileUri,
            flag
        )
    }

}