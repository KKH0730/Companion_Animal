package studio.seno.datamodule

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.database.AppDatabase
import studio.seno.domain.model.User
import studio.seno.domain.usecase.local.LocalUserUseCase

class LocalRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val localUserUseCase = LocalUserUseCase()


    fun InsertUserInfo(lifecycleScope: LifecycleCoroutineScope, user : User) {
        localUserUseCase.InsertUserInfo(lifecycleScope, user, db!!)
    }

    fun updateUserInfo(lifecycleScope: LifecycleCoroutineScope, user: User) {
        localUserUseCase.updateUserInfo(lifecycleScope, user, db!!)
    }

    fun updateFollowing(lifecycleScope: LifecycleCoroutineScope, add : Boolean){
        localUserUseCase.updateFollowing(lifecycleScope, add, db!!)
    }

    fun updateFeedCount(lifecycleScope: LifecycleCoroutineScope, add : Boolean) {
        localUserUseCase.updateFeedCount(lifecycleScope, add, db!!)
    }

    fun updateNickname(lifecycleScope: LifecycleCoroutineScope, content : String){
        localUserUseCase.updateNickname(lifecycleScope, content, db!!)
    }

    fun updateProfileUri(lifecycleScope: LifecycleCoroutineScope, profileUri : String) {
        localUserUseCase.updateProfileUri(lifecycleScope, profileUri, db!!)
    }

    fun getUserInfo(lifecycleScope: LifecycleCoroutineScope, callback : LongTaskCallback<User>){
            localUserUseCase.getUserInfo(lifecycleScope, db!!, callback)
    }
}