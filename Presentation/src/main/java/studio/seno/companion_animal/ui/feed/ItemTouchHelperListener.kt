package studio.seno.companion_animal.ui.feed

interface ItemTouchHelperListener {
    fun onItemMove(fromPosition : Int, toPosition : Int) : Boolean
    fun onItemSwipe(position : Int)
}