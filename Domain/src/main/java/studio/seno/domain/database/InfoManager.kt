package studio.seno.domain.database

import android.content.Context
import android.content.SharedPreferences

object InfoManager {
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("userData", Context.MODE_PRIVATE)
    }

    fun setString(context: Context, key: String?, value: String?) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(context: Context, key: String?): String? {
        val sharedPreferences = getPreferences(context)
        return sharedPreferences.getString(key, "isEmpty")
    }

    fun setBoolean(context: Context, key: String?, value: Boolean) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(context: Context, key: String?): Boolean {
        val sharedPreferences = getPreferences(context)
        return sharedPreferences.getBoolean(key, false)
    }

    fun setLong(context: Context, key: String?, value: Long) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(context: Context, key: String?): Long {
        val sharedPreferences = getPreferences(context)
        return sharedPreferences.getLong(key, 0)
    }


    fun setUserInfo(
        context: Context,
        email: String?,
        nickName: String?,
        follower: Long,
        following: Long,
        feedCount: Long
    ) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.putString("nickName", nickName)
        editor.putLong("follower", follower)
        editor.putLong("following", following)
        editor.putLong("feedCount", feedCount)
        editor.apply()
    }
}