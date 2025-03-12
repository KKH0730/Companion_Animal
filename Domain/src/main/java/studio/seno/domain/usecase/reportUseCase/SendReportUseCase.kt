package studio.seno.domain.usecase.reportUseCase

import studio.seno.domain.model.Feed
import studio.seno.domain.repository.ReportRepository
import javax.inject.Inject

class SendReportUseCase @Inject constructor(private val reportRepository: ReportRepository) {
    fun execute(feed : Feed, number : Int) {
        reportRepository.reportFeed(feed, number)
    }

}