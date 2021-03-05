package studio.seno.domain.Repository

import com.google.firebase.firestore.FirebaseFirestore
import studio.seno.domain.model.Follow
import studio.seno.domain.util.LongTaskCallback

interface FollowRepository {
    fun checkFollow(
        targetEmail: String,
        callback: LongTaskCallback<Boolean>
    )

    fun getFollow(
        fieldName: String,
        callback: LongTaskCallback<List<Follow>>
    )

    fun setFollower(
        targetEmail : String,
        flag: Boolean,
        myFollow: Follow,
        targetFollow: Follow
    )
}