package studio.seno.domain.usecase.local

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.database.AppDatabase
import studio.seno.domain.model.User

class LocalUserUseCase {
    fun getUserInfo(lifecycleScope: LifecycleCoroutineScope, db: AppDatabase, callback : LongTaskCallback<User>){
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.userDao().getAll()
            Log.d("hi", "size : ${list.size}")
            if(list.isEmpty()){
                lifecycleScope.launch(Dispatchers.Main) {
                    callback.onResponse(Result.Success(null))
                }
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    callback.onResponse(Result.Success(list[0]))
                }
            }
        }
    }

    //유저정보 insert
    fun InsertUserInfo(lifecycleScope: LifecycleCoroutineScope, user: User, db : AppDatabase) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.userDao().insert(user)
        }
    }

    //유저정보 update
    fun updateUserInfo(lifecycleScope: LifecycleCoroutineScope, user: User, db : AppDatabase) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.userDao().updateUserInfo(user.email, user.nickname, user.follower, user.following, user.feedCount, user.token, user.profileUri)
        }
    }


    //following 개수 update
    fun updateFollowing(lifecycleScope: LifecycleCoroutineScope, add : Boolean, db : AppDatabase){
        lifecycleScope.launch(Dispatchers.IO) {
            val user : User = db.userDao().getAll()[0]
            val following = user.following

            if(add)
                user.following = following + 1
            else
                user.following = following - 1

            updateUserInfo(lifecycleScope, user, db)
        }
    }

    //feed 개수 update
    fun updateFeedCount(lifecycleScope: LifecycleCoroutineScope, add : Boolean, db : AppDatabase) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user : User = db.userDao().getAll()[0]
            val feedCount = user.feedCount

            if(add)
                user.feedCount = feedCount + 1
            else
                user.feedCount = feedCount - 1

            updateUserInfo(lifecycleScope, user, db)
        }
    }

    //nickname 업데이트
    fun updateNickname(lifecycleScope: LifecycleCoroutineScope, content : String, db : AppDatabase){
        lifecycleScope.launch(Dispatchers.IO) {
            val user : User = db.userDao().getAll()[0]
            user.nickname = content

            updateUserInfo(lifecycleScope, user, db)
        }
    }

    //profileUri 업데이트
    fun updateProfileUri(lifecycleScope: LifecycleCoroutineScope, profileUri : String, db : AppDatabase) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user : User = db.userDao().getAll()[0]
            user.profileUri = profileUri

            updateUserInfo(lifecycleScope, user, db)
        }
    }

}