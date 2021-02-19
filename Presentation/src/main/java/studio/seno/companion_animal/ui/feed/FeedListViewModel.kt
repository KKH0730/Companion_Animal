package studio.seno.companion_animal.ui.feed

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import studio.seno.datamodule.Repository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.model.Feed
import studio.seno.domain.Result

class FeedListViewModel() : ViewModel() {
    private var feedListLiveData = MutableLiveData<List<Feed>>()
    private var feedSaveStatus = MutableLiveData<Boolean>()
    private val mapper = Mapper()
    private val repository = Repository()

    fun getFeedListLiveData(): MutableLiveData<List<Feed>> {
        return feedListLiveData
    }

    fun setFeedListLiveData(list : List<Feed>) {
        feedListLiveData.value = null
        feedListLiveData.value = list
    }

    fun getFeedListSaveStatus() : MutableLiveData<Boolean>{
        return feedSaveStatus
    }



    fun requestUploadFeed(context : Context, id : Long, email : String, nickname : String, sort : String, hashTags : List<String>,
        localUri : List<String>, content : String, timestamp : Long, lifecycleCoroutineScope: LifecycleCoroutineScope
    ) {

        var feed = mapper.mapperToFeed(
            id, email, nickname, sort, hashTags,
            localUri, content, timestamp
        )

        repository.uploadFeed(context, feed, lifecycleCoroutineScope, object : LongTaskCallback<Feed> {
            override fun onResponse(result: Result<Feed>) {
                if (result is Result.Success) {
                    feedSaveStatus.value = true

                } else if(result is Result.Error) {
                    Log.e("error", "upload feed error : ${result.exception}")
                }
            }
        })
    }

    //피드를 페이징하여 로드
    fun requestLoadFeedList(keyword: String?, recyclerView: RecyclerView, callback: LongTaskCallback<List<Feed>>?){
        repository.requestLoadFeedList(keyword, recyclerView, object : LongTaskCallback<List<Feed>>{
            override fun onResponse(result: Result<List<Feed>>) {
                if(result is Result.Success) {
                    val list = result.data
                    if(list != null) {
                        var tempList : MutableList<Feed>? = feedListLiveData.value?.toMutableList()

                        if(tempList != null)
                            for(element in list)
                                tempList.add(element)
                        else
                            tempList = list.toMutableList()

                        if(recyclerView.layoutManager is LinearLayoutManager)
                            feedListLiveData.value = tempList
                        else if(recyclerView.layoutManager is StaggeredGridLayoutManager)
                            feedListLiveData.value = tempList
                    }
                    callback?.onResponse(result)

                }else if(result is Result.Error){
                    callback?.onResponse(Result.Error(result.exception))
                    Log.e("error", "load feed list error : ${result.exception}")
                }
            }
        })
    }

    fun clearFeedList(){
        feedListLiveData.value = null
    }


    /*
     fun loadFeedList(callback : LongTaskCallback<List<Feed>>?){
        repository.loadFeedList(object : LongTaskCallback<List<Feed>> {
            override fun onResponse(result: Result<List<Feed>>) {
                if(result is Result.Success) {
                    var list = result.data

                    feedListLiveData.value = list
                    callback?.onResponse(result)

                }else if(result is Result.Error){
                    callback?.onResponse(Result.Error(result.exception))
                    Log.e("error", "load feed list error : ${result.exception}")
                }
            }
        })
    }
     */

    fun requestDeleteFeed(feed: Feed){
        repository.deleteFeed(feed, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    if(result.data) {
                       // loadFeedList(null)
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

    fun requestCheckFollow(targetFeed: Feed, myEmail: String, callback: LongTaskCallback<Boolean>){
        repository.requestCheckFollow(targetFeed, myEmail, callback)
    }

    fun requestUpdateFollower(targetFeed: Feed, myEmail : String, flag : Boolean) {
        repository.requestUpdateFollower(targetFeed, myEmail, flag)
    }

}