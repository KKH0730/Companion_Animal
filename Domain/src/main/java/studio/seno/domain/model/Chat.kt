package studio.seno.domain.model

data class Chat(
    var email : String ? = null,
    var realEmail : String? = null,
    var targetEmail : String? = null,
    var targetRealEmail : String?  = null,
    var nickname : String ? = null,
    var targetNickname : String? = null,
    var content: String ? = null,
    var profileUri : String ? = null,
    var targetProfileUri : String? = null,
    var timestamp: Long = 0L,
    var isExit : Boolean = false,
    var isRead : Boolean = false
) {


}