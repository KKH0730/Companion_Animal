package studio.seno.datamodule.repository.local

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.datamodule.database.AppDatabase
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class LocalRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)!!


    companion object {
        private var localRepository: LocalRepository? = null

        fun getInstance(context: Context): LocalRepository? {
            if (localRepository == null) {
                synchronized(LocalRepository::class.java) {
                    localRepository = LocalRepository(context)
                }
            }
            return localRepository
        }
    }


    fun insertUserInfo(lifecycleScope: LifecycleCoroutineScope, user: User) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.userDao().insert(user)
        }
    }

    fun updateUserInfo(
        lifecycleScope: LifecycleCoroutineScope,
        user: User,
        callback: LongTaskCallback<User>?
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.userDao().updateUserInfo(
                user.email,
                user.nickname,
                user.follower,
                user.following,
                user.feedCount,
                user.token,
                user.profileUri
            )

            lifecycleScope.launch(Dispatchers.Main) {
                callback?.onResponse(Result.Success(null))
            }
        }
    }

    fun updateFollowing(lifecycleScope: LifecycleCoroutineScope, add: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user: User = db.userDao().getAll()[0]
            val following = user.following

            if (add)
                user.following = following + 1
            else
                user.following = following - 1

            updateUserInfo(lifecycleScope, user, null)
        }
    }

    fun updateFeedCount(lifecycleScope: LifecycleCoroutineScope, add: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user: User = db.userDao().getAll()[0]
            val feedCount = user.feedCount

            if (add)
                user.feedCount = feedCount + 1
            else
                user.feedCount = feedCount - 1

            updateUserInfo(lifecycleScope, user, null)
        }
    }

    fun updateNickname(lifecycleScope: LifecycleCoroutineScope, content: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user: User = db.userDao().getAll()[0]
            user.nickname = content

            updateUserInfo(lifecycleScope, user, null)
        }
    }

    fun updateProfileUri(
        lifecycleScope: LifecycleCoroutineScope,
        profileUri: String,
        callback: LongTaskCallback<User>
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user: User = db.userDao().getAll()[0]
            user.profileUri = profileUri

            updateUserInfo(lifecycleScope, user, callback)
        }
    }

    fun getUserInfo(lifecycleScope: LifecycleCoroutineScope, callback: LongTaskCallback<User>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = db.userDao().getAll()

            if (list.isEmpty()) {
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
}