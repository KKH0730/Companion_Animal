package studio.seno.domain.usecase.searchUseCase

import studio.seno.domain.Repository.SearchRepository
import studio.seno.domain.model.LastSearch

class SetLastSearchUseCase(private val searchRepository: SearchRepository) {
    fun execute(content : String, timestamp : Long) {
        val lastSearch = studio.seno.domain.util.Mapper.getInstance()!!.mapperToLastSearch(content , timestamp)
        searchRepository.setLastSearch(lastSearch)
    }
}