package studio.seno.companion_animal.ui.feed

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.Repository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.model.Feed
import studio.seno.domain.Result
import studio.seno.domain.model.Comment

class FeedListViewModel() : ViewModel() {
    private var feedListLiveData = MutableLiveData<List<Feed>>()
    private var feedSaveStatus = MutableLiveData<Boolean>()
    private val mapper = Mapper()
    private val repository = Repository()

    fun getFeedListLiveData(): MutableLiveData<List<Feed>> {
        return feedListLiveData
    }

    fun getFeedListSaveStatus() : MutableLiveData<Boolean>{
        return feedSaveStatus
    }

    fun requestUploadFeed(context : Context, id : Long, email : String, nickname : String, sort : String, hashTags : List<String>,
        localUri : List<String>, content : String, timestamp : Long
    ) {

        var feed = mapper.mapperToFeed(
            id, email, nickname, sort, hashTags,
            localUri, content, timestamp
        )

        repository.uploadFeed(context, feed, object : LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if (result is Result.Success) {
                    feedSaveStatus.value = result.data
                } else if(result is Result.Error) {
                    feedSaveStatus.value = false
                    Log.e("error", "upload feed error : ${result.exception}")
                }
            }
        })
    }


    fun loadFeedList(callback : LongTaskCallback<List<Feed>>?){
        repository.loadFeedList(object : LongTaskCallback<List<Feed>> {
            override fun onResponse(result: Result<List<Feed>>) {
                if(result is Result.Success) {
                    var list = result.data
                    feedListLiveData.value = list

                    if(callback != null) {
                        callback.onResponse(result)
                    }
                }else if(result is Result.Error){
                    if(callback!= null)
                        callback.onResponse(Result.Error(result.exception))
                    Log.e("error", "load feed list error : ${result.exception}")
                }
            }
        })
    }


    fun requestDeleteFeed(feed: Feed){
        repository.deleteFeed(feed, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    if(result.data) {
                        loadFeedList(null)
                        feedSaveStatus.value = true
                    } else {
                        Log.e("error", "requestDeleteFeed fail")
                    }
                } else if(result is Result.Error) {
                    Log.e("error", "requestDeleteFeed : ${result.exception}")
                }
            }
        })
    }

    fun requestUpdateHeart(feed: Feed, count: Long, myEmail : String, flag : Boolean){
        repository.requestUpdateHeart(feed, count, myEmail, flag)
    }

    fun requestUpdateBookmark(feed: Feed, myEmail : String, flag : Boolean) {
        repository.requestUpdateBookmark(feed, myEmail, flag)
    }

    fun requestUpdateFollower(feed: Feed, myEmail : String, flag : Boolean) {
        repository.requestUpdateFollower(feed, myEmail, flag)
    }

}