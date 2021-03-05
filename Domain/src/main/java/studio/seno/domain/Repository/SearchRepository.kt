package studio.seno.domain.Repository

import studio.seno.domain.model.LastSearch
import studio.seno.domain.util.LongTaskCallback

interface SearchRepository {
    fun setLastSearch(lastSearch: LastSearch)
    fun getLastSearch(callback: LongTaskCallback<List<LastSearch>>)
    fun deleteLastSearch(lastSearch: LastSearch)
}