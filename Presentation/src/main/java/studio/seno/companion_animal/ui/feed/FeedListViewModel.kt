package studio.seno.companion_animal.ui.feed

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.usecase.feedUseCase.*
import studio.seno.domain.usecase.followUseCase.CheckFollowUseCase
import studio.seno.domain.usecase.followUseCase.SetFollowUseCase

class FeedListViewModel(
    private val setFeedUseCase: SetFeedUseCase,
    private val deleteFeedUseCase: DeleteFeedUseCase,
    private val updateHeartUseCase: UpdateHeartUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val getPagingFeedUseCase: GetPagingFeedUseCase,
    private val checkFollowUseCase: CheckFollowUseCase,
    private val setFollowUseCase: SetFollowUseCase
) : ViewModel() {
    private var feedListLiveData = MutableLiveData<List<Feed>>()
    private var feedSaveStatus = MutableLiveData<Boolean>()

    fun getFeedListLiveData(): MutableLiveData<List<Feed>> {
        return feedListLiveData
    }

    fun setFeedListLiveData(list : List<Feed>) {
        feedListLiveData.value = list

    }

    fun getFeedListSaveStatus() : MutableLiveData<Boolean>{
        return feedSaveStatus
    }


    //피드를 페이징하여 로드
    fun getPagingFeed(f1 : Boolean?, f2 : Boolean?, f3: Boolean?, keyword: String?, sort: String, myEmail: String?,
                            recyclerView: RecyclerView, callback: LongTaskCallback<List<Feed>>?){

        getPagingFeedUseCase.execute(f1, f2, f3, keyword, sort, myEmail, recyclerView, object :
            LongTaskCallback<List<Feed>> {
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



    fun requestUploadFeed(email : String, nickname: String, sort:String, hashTags : List<String>,
                          localUri: List<String>, content: String, timestamp: Long, callback : LongTaskCallback<Feed>
    ) {
        setFeedUseCase.execute(
            email, nickname, sort, hashTags, localUri, content, timestamp, object :
                LongTaskCallback<Feed> {
                override fun onResponse(result: Result<Feed>) {
                    if (result is Result.Success) {
                        feedSaveStatus.value = true

                        callback.onResponse(Result.Success(result.data))
                    } else if(result is Result.Error) {
                        Log.e("error", "upload feed error : ${result.exception}")
                    }
                }

            })
    }



    fun requestDeleteFeed(feed: Feed){
        deleteFeedUseCase.execute(feed, object : LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    if(result.data) {
                        feedSaveStatus.value = true
                    } else {
                        Log.e("error", "requestDeleteFeed fail")
                    }
                } else if(result is Result.Error)
                    Log.e("error", "requestDeleteFeed : ${result.exception}")
            }
        })
    }

    fun updateHeart(feed: Feed, count: Long, flag : Boolean){
        updateHeartUseCase.execute(feed, count, flag)
    }

    fun updateBookmark(feed: Feed, flag : Boolean) {
        updateBookmarkUseCase.execute(feed, flag)
    }

    fun requestCheckFollow(targetEmail: String,  callback: LongTaskCallback<Boolean>){
        checkFollowUseCase.execute(targetEmail, callback)
    }

    fun requestUpdateFollow(targetEmail : String, targetNickname: String, targetProfileUri : String, flag : Boolean, myNickName : String, myProfileUri : String) {
        setFollowUseCase.execute(
            targetEmail,
            targetNickname,
            targetProfileUri,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            myNickName,
            myProfileUri,
            flag
        )
    }
}