package studio.seno.datamodule.mapper

import studio.seno.domain.model.*

object Mapper {
    private var mapper : Mapper? = null

    fun getInstance() : Mapper? {
        if(mapper == null) {
            synchronized(Mapper::class.java) {
                mapper = this
            }
        }
        return mapper
    }

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
        token : String,
        profileUri : String
    ) : User {
        return User(id, email, nickname, follower, following, feedCount, token, profileUri)
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

    fun mapperToFollow(
        email: String,
        nickname : String,
        profileUri : String
    ) : Follow {
        return Follow(email, nickname, profileUri)
    }

    fun mapperToChat(
        email : String,
        realEmail : String,
        targetEmail : String,
        targetRealEmail : String,
        nickname : String,
        targetNickname : String,
        content : String,
        profileUri : String,
        targetProfileUri : String,
        timestamp : Long,
        isExit : Boolean,
        isRead : Boolean
    ) : Chat {
        return Chat(email, realEmail, targetEmail, targetRealEmail, nickname, targetNickname, content, profileUri, targetProfileUri, timestamp, isExit, isRead)
    }

}