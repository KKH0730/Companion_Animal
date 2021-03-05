package studio.seno.datamodule.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import studio.seno.domain.model.User


@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getLiveData() : LiveData<List<User>>

    @Query("SELECT * FROM User")
    fun getAll() : List<User>

    @Insert
    fun insert(userInfo: User)

    @Update
    fun update(userInfo: User)

    @Delete
    fun delete(userInfo: User)

    @Query("UPDATE user SET email = :email , nickname = :nickname , follower = :follower , following = :following , feedCount = :feedCount,  token = :token , profileUri = :profileUri where id = 1")
    fun updateUserInfo(email : String, nickname : String, follower : Long, following : Long, feedCount : Long, token : String, profileUri : String)



}