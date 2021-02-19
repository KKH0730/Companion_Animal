package studio.seno.companion_animal.ui.feed

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import me.relex.circleindicator.CircleIndicator3
import studio.seno.domain.model.Feed

class FeedViewModel(
    lifecycle: Lifecycle,
    fm: FragmentManager,
    indicator: CircleIndicator3?,
    lifecycleCoroutineScope: LifecycleCoroutineScope
) : ViewModel() {
    private val feedLiveData: MutableLiveData<Feed> = MutableLiveData()
    private val mLifecycle: Lifecycle = lifecycle
    private val mFm: FragmentManager = fm
    private val mIndicator = indicator
    private val lifecycleScope = lifecycleCoroutineScope


    fun setFeedLiveData(feed: Feed) {
        feedLiveData.value = feed
    }

    fun getFeedLiveData(): MutableLiveData<Feed> {
        return feedLiveData
    }



    fun getLifecycle(): Lifecycle {
        return mLifecycle
    }

    fun getFragmentManager(): FragmentManager {
        return mFm
    }

    fun getIndicator(): CircleIndicator3 {
        return mIndicator!!
    }

    fun getLifecycleScope() : LifecycleCoroutineScope{
        return lifecycleScope
    }

}