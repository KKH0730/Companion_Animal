package studio.seno.domain.model


data class Comment(
    val type : Long,
    val email : String,
    val nickname : String,
    val content : String,
    var profileUri : String?,
    val timestamp : Long
) {
    private var children : List<Comment>? = listOf()


    fun setChildren(list : List<Comment>) {
        children = list
    }
}
