package studio.seno.domain.model

data class Feed(
    val email : String,
    var nickname : String,
    var sort : String,
    var hashTags : List<String>?,
    var localUri : List<String>,
    var content : String?,
    var heart : Long,
    var comment : Long,
    var timestamp : Long,
    var remoteProfileUri : String?,
    var remoteUri : List<String>?
)