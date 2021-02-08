package studio.seno.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id : Long,
    var email : String,
    var nickname : String,
    var follower : Long,
    var following : Long,
    var feedCount : Long
) {

}