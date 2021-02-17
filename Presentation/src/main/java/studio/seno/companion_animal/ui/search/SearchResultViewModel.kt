package studio.seno.companion_animal.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.Feed

class SearchResultViewModel : ViewModel() {
    private var feedLiveData: MutableLiveData<Feed> = MutableLiveData()

    fun setFeedLiveData(feed: Feed) {
        feedLiveData.value = feed
    }

    fun getFeedLiveData(): MutableLiveData<Feed> {
        return feedLiveData
    }

}