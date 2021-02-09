package studio.seno.companion_animal.ui.feed

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.relex.circleindicator.CircleIndicator3
import studio.seno.domain.model.Feed

class FeedViewModel(
    lifecycle: Lifecycle,
    fm: FragmentManager,
    indicator: CircleIndicator3,
    lifecycleOwner: LifecycleOwner
) : ViewModel() {
    private var feedLiveData: MutableLiveData<Feed> = MutableLiveData()
    private var feedCommentData: MutableLiveData<String> = MutableLiveData()
    private var mLifecycle: Lifecycle = lifecycle
    private var mFm: FragmentManager = fm
    private var mIndicator = indicator
    private val mLifecycleOwner = lifecycleOwner


    fun setFeedLiveData(feed: Feed) {
        feedLiveData.value = feed
    }

    fun getFeedLiveData(): MutableLiveData<Feed> {
        return feedLiveData
    }

    fun setFeedCommentLiveData(str: String) {
        feedCommentData.value = str
    }

    fun getFeedCommentLiveData():MutableLiveData<String> {
        return feedCommentData
    }

    fun getLifecycle(): Lifecycle {
        return mLifecycle
    }

    fun getFragmentManager(): FragmentManager {
        return mFm
    }

    fun getIndicator(): CircleIndicator3 {
        return mIndicator
    }

    fun getLifeCycleOwner() : LifecycleOwner {
        return mLifecycleOwner
    }
}