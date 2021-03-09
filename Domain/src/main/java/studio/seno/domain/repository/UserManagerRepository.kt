package studio.seno.domain.repository

import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback

interface UserManagerRepository {
    fun checkEnableLogin(
        email: String,
        password: String,
        callback : LongTaskCallback<Any>
    )

    fun sendFindEmail(
        emailAddress: String,
        callback : LongTaskCallback<Any>
    )

    fun registerUser(
        email: String,
        password: String,
        callback: LongTaskCallback<Any>
    )


    fun setUserInfo(
        user: User
    )

    fun getUserInfo(
        email : String,
        callback: LongTaskCallback<Any>
    )

    fun setNickname(
        content: String
    )

    fun setToken(
        token : String
    )

    fun checkOverlapUser(
        email : String,
        callback : LongTaskCallback<Any>
    )


    fun setProfileUri(
        profileUri : String
    )
}