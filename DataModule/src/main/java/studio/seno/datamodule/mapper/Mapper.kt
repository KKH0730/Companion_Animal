package studio.seno.datamodule.mapper

import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User

class Mapper {
    fun mapperToFeed(
        id : Long,
        email : String,
        nickname : String,
        sort : String,
        hashTags : List<String>,
        localUri : List<String>,
        content : String,
        timestamp: Long
    ) : Feed {
        return Feed(email, nickname, sort, hashTags, localUri, content,
            0, 0, timestamp, null, null)
    }

    fun mapperToUser(
        id : Long,
        email : String,
        nickname : String,
        follower : Long,
        following : Long,
        feedCount : Long
    ) : User {
        return User(id, email, nickname, follower, following, feedCount)
    }

    fun mapperToComment(
        type : Long,
        email: String,
        nickname: String,
        content: String,
        profileUri: String?,
        timestamp: Long
    ): Comment {
        return Comment(type, email, nickname, content, profileUri, timestamp)
    }
}