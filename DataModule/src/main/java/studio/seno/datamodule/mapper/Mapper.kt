package studio.seno.datamodule.mapper

import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed
import studio.seno.domain.model.LastSearch
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
            0, 0, timestamp, "", listOf(), mapOf(),   mapOf())
    }

    fun mapperToUser(
        id : Long,
        email : String,
        nickname : String,
        follower : Long,
        following : Long,
        feedCount : Long,
        token : String
    ) : User {
        return User(id, email, nickname, follower, following, feedCount, token)
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

    fun mapperToLastSearch(
        content : String,
        timestamp : Long
    ) : LastSearch{
        return LastSearch(content, timestamp)
    }
}