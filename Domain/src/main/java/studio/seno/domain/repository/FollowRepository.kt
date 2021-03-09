package studio.seno.domain.repository

import studio.seno.domain.model.Follow
import studio.seno.domain.util.LongTaskCallback

interface FollowRepository {
    fun checkFollow(
        targetEmail: String,
        callback: LongTaskCallback<Any>
    )

    fun getFollow(
        fieldName: String,
        callback: LongTaskCallback<Any>
    )

    fun setFollower(
        targetEmail : String,
        flag: Boolean,
        myFollow: Follow,
        targetFollow: Follow
    )
}