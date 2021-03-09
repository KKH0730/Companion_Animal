package studio.seno.domain.usecase.searchUseCase

import studio.seno.domain.repository.SearchRepository
import studio.seno.domain.model.LastSearch
import studio.seno.domain.util.LongTaskCallback

class GetLastSearchUseCase (private val searchRepository: SearchRepository) {
    fun execute(callback: LongTaskCallback<Any>) {
        searchRepository.getLastSearch(callback)
    }
}