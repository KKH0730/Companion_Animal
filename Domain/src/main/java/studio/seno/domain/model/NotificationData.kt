package studio.seno.domain.model

data class NotificationData(
    val title: String,
    val body : String,
    val timestamp : Long?,
    val myPath : String?,
    val targetPath : String?,
    val check : Boolean = true,
    val myEmail : String?,
    val chatPathEmail : String?,
    val myNickname : String?,
    val myProfileUri : String?

) {
}