package studio.seno.domain.Repository

import studio.seno.domain.model.Feed

interface ReportRepository {
    fun reportFeed(feed : Feed, number : Int)
}