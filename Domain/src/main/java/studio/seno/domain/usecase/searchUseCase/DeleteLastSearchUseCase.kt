package studio.seno.domain.usecase.searchUseCase

import studio.seno.domain.repository.SearchRepository
import studio.seno.domain.model.LastSearch
import javax.inject.Inject

class DeleteLastSearchUseCase @Inject constructor(private val searchRepository: SearchRepository) {
    fun execute(lastSearch: LastSearch) {
        searchRepository.deleteLastSearch(lastSearch)
    }
}