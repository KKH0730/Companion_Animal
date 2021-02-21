package studio.seno.companion_animal.ui.search

import studio.seno.domain.model.Feed

interface OnSearchItemClickListener {
    fun onSearchItemClicked(feed : Feed, position : Int)
}