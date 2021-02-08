package studio.seno.domain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import studio.seno.domain.model.User


@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAll() : List<User>

    @Insert
    fun insert(userInfo: User)

    @Update
    fun update(userInfo: User)

    @Delete
    fun delete(userInfo: User)
}