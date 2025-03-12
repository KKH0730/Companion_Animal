package studio.seno.domain.usecase.searchUseCase

import studio.seno.domain.repository.SearchRepository
import javax.inject.Inject

class SetLastSearchUseCase @Inject constructor(private val searchRepository: SearchRepository) {
    fun execute(content : String, timestamp : Long) {
        val lastSearch = studio.seno.domain.util.Mapper.getInstance()!!.mapperToLastSearch(content , timestamp)
        searchRepository.setLastSearch(lastSearch)
    }
}