package studio.seno.companion_animal.ui.feed

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import studio.seno.datamodule.RemoteRepository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.model.Feed
import studio.seno.domain.Result

class FeedListViewModel() : ViewModel() {
    private var feedListLiveData = MutableLiveData<List<Feed>>()
    private var feedSaveStatus = MutableLiveData<Boolean>()
    private val repository = RemoteRepository.getInstance()!!

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

    //피드를 페이징하여 로드
    fun requestLoadFeedList(keyword: String?, sort: String, myEmail: String?,
                            recyclerView: RecyclerView, callback: LongTaskCallback<List<Feed>>?){
        repository.requestLoadFeedList(keyword, sort, myEmail, recyclerView, object : LongTaskCallback<List<Feed>>{
            override fun onResponse(result: Result<List<Feed>>) {
                if(result is Result.Success) {
                    val list = result.data

                    if(list != null) {
                        Log.d("hi","list size -> ${list.size}")
                        var tempList : MutableList<Feed>? = feedListLiveData.value?.toMutableList()

                        if(tempList != null)
                            for(element in list)
                                tempList.add(element)
                        else
                            tempList = list.toMutableList()

                        if(recyclerView.layoutManager is LinearLayoutManager)
                            feedListLiveData.setValue(tempList)
                        else if(recyclerView.layoutManager is StaggeredGridLayoutManager)
                            feedListLiveData.setValue(tempList)
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



    fun requestUploadFeed(context : Context, id : Long, email : String, nickname : String, sort : String, hashTags : List<String>,
        localUri : List<String>, content : String, timestamp: Long, lifecycleCoroutineScope: LifecycleCoroutineScope
    ) {

        var feed = Mapper.getInstance()!!.mapperToFeed(
            id, email, nickname, sort, hashTags,
            localUri, content, timestamp
        )

        repository.uploadFeed(context, feed ,lifecycleCoroutineScope, object : LongTaskCallback<Feed> {
            override fun onResponse(result: Result<Feed>) {
                if (result is Result.Success) {
                    feedSaveStatus.value = true

                } else if(result is Result.Error) {
                    Log.e("error", "upload feed error : ${result.exception}")
                }
            }
        })
    }



    fun requestDeleteFeed(feed: Feed){
        repository.deleteFeed(feed, object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    if(result.data) {
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

    fun requestUpdateHeart(feed: Feed, count: Long, flag : Boolean){
        repository.requestUpdateHeart(feed, count, flag)
    }

    fun requestUpdateBookmark(feed: Feed, flag : Boolean) {
        repository.requestUpdateBookmark(feed, flag)
    }

    fun requestCheckFollow(targetEmail: String,  callback: LongTaskCallback<Boolean>){
        repository.requestCheckFollow(targetEmail, callback)
    }

    fun requestUpdateFollower(targetEmail : String, targetNickname: String, targetProfileUri : String, flag : Boolean, myNickName : String, myProfileUri : String) {
        val targetFollow = Mapper.getInstance()!!.mapperToFollow(targetEmail, targetNickname, targetProfileUri)
        val myFollow = Mapper.getInstance()!!.mapperToFollow(FirebaseAuth.getInstance().currentUser?.email.toString(), myNickName, myProfileUri)
        repository.requestUpdateFollower(targetEmail, flag, myFollow, targetFollow)
    }

}