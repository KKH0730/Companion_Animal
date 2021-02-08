package studio.seno.domain.database

import androidx.room.Database
import androidx.room.RoomDatabase
import studio.seno.domain.model.User

@Database(entities = arrayOf(User::class), version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
}