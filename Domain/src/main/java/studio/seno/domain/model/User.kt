package studio.seno.domain.model

data class User(
    var email : String,
    var nickname : String,
    var follower : Long,
    var following : Long,
    var feedCount : Long
) {
}