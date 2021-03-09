package studio.seno.domain.model

data class NotificationData(
    val title: String,
    val body : String,
    val timestamp : Long?,
    val notificationPath : String?,
    val feedPath : String?,
    val check : Boolean = true,
    val email : String?,
    val chatPathEmail : String?,
    val nickname : String?,
    val profileUri : String?

) {
}