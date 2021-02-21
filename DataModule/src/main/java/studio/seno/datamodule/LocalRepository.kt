package studio.seno.datamodule

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.database.AppDatabase
import studio.seno.domain.model.User
import studio.seno.domain.usecase.local.LocalUserUseCase

class LocalRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val localUserUseCase = LocalUserUseCase()


    companion object{
        private var localRepository : LocalRepository? = null

        fun getInstance(context : Context) : LocalRepository? {
            if(localRepository == null) {
                synchronized(LocalRepository::class.java) {
                    localRepository = LocalRepository(context)
                }
            }
            return localRepository
        }

    }



    fun InsertUserInfo(lifecycleScope: LifecycleCoroutineScope, user : User) {
        localUserUseCase.InsertUserInfo(lifecycleScope, user, db!!)
    }

    fun updateUserInfo(lifecycleScope: LifecycleCoroutineScope, user: User) {
        localUserUseCase.updateUserInfo(lifecycleScope, user, db!!, null)
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

    fun updateProfileUri(lifecycleScope: LifecycleCoroutineScope, profileUri : String, callback: LongTaskCallback<User>) {
        localUserUseCase.updateProfileUri(lifecycleScope, profileUri, db!!, callback)
    }

    fun getUserInfo(lifecycleScope: LifecycleCoroutineScope, callback : LongTaskCallback<User>){
            localUserUseCase.getUserInfo(lifecycleScope, db!!, callback)
    }
}