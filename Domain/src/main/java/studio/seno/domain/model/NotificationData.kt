package studio.seno.domain.model

data class NotificationData(
    val title: String,
    val body : String,
    val timestamp : Long?,
    val targetPath : String?,
    val check : Boolean = true
) {
}