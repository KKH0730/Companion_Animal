package studio.seno.domain.model


data class Comment (
    val type : Long,
    val email : String,
    val nickname : String,
    val content : String,
    var profileUri : String?,
    val timestamp : Long
) : Comparable<Comment> {
    private var children : List<Comment>? = listOf()


    fun setChildren(list : List<Comment>) {
        children = list
    }

    fun getChildren() : List<Comment>?{
        return children
    }

    fun initChildren() {
        children = listOf()
    }

    override fun compareTo(other: Comment): Int {
        return this.timestamp.compareTo(other.timestamp)
    }
}
