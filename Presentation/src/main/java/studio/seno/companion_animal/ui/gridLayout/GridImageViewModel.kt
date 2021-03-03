package studio.seno.companion_animal.ui.gridLayout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.Feed

class GridImageViewModel : ViewModel() {
    private var feedLiveData: MutableLiveData<Feed> = MutableLiveData()

    fun setFeedLiveData(feed: Feed) {
        feedLiveData.value = feed
    }

    fun getFeedLiveData(): MutableLiveData<Feed> {
        return feedLiveData
    }

}