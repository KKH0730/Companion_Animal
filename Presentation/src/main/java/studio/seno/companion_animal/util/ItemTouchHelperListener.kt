package studio.seno.companion_animal.util

interface ItemTouchHelperListener {
    fun onItemMove(fromPosition : Int, toPosition : Int) : Boolean
    fun onItemSwipe(position : Int)
}