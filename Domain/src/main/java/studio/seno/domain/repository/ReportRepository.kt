package studio.seno.domain.repository

import studio.seno.domain.model.Feed

interface ReportRepository {
    fun reportFeed(feed : Feed, number : Int)
}