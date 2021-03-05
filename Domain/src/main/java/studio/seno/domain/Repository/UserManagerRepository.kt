package studio.seno.domain.Repository

import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback

interface UserManagerRepository {
    fun checkEnableLogin(
        email: String,
        password: String,
        callback : LongTaskCallback<Boolean>
    )

    fun sendFindEmail(
        emailAddress: String,
        callback : LongTaskCallback<Boolean>
    )

    fun registerUser(
        email: String,
        password: String,
        callback: LongTaskCallback<Boolean>
    )


    fun setUserInfo(
        user: User
    )

    fun getUserInfo(
        email : String,
        callback: LongTaskCallback<User>
    )

    fun setNickname(
        content: String
    )

    fun setToken(
        token : String
    )

    fun checkOverlapUser(
        email : String,
        callback : LongTaskCallback<Boolean>
    )


    fun setProfileUri(
        profileUri : String
    )
}