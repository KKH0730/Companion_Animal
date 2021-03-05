package studio.seno.domain.usecase.reportUseCase

import studio.seno.domain.model.Feed
import studio.seno.domain.Repository.ReportRepository

class SendReportUseCase(private val reportRepository: ReportRepository) {
    fun execute(feed : Feed, number : Int) {
        reportRepository.reportFeed(feed, number)
    }

}