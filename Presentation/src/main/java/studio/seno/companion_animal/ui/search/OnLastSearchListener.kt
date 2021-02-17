package studio.seno.companion_animal.ui.search

import studio.seno.domain.model.LastSearch

interface OnLastSearchListener {
    fun onItemClicked(content: String)
    fun onDeleteClicked(timestamp : Long, lastSearch : LastSearch)
}