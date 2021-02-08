package studio.seno.domain.model

data class Feed(
    val email : String,
    val nickname : String,
    val sort : String,
    val hashTags : List<String>?,
    val localUri : List<String>,
    val content : String?,
    val heart : Long,
    val comment : Long,
    val timestamp : Long,
    var remoteProfileUri : String?,
    var remoteUri : List<String>?
)