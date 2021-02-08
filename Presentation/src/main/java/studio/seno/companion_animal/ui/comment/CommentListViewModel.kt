package studio.seno.companion_animal.ui.comment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.Repository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed

class CommentListViewModel : ViewModel() {
    private var commentListLiveData : MutableLiveData<List<Comment>> = MutableLiveData()
    private val repository = Repository()
    private var list = mutableListOf<Comment>()

    fun getCommentListLiveData() : MutableLiveData<List<Comment>> {
        return commentListLiveData
    }

    fun setCommentListLiveData(list : List<Comment>) {
        commentListLiveData.value = list
    }

    fun requestLoadComment(email : String, timestamp : Long){
        repository.loadComment(email, timestamp, object : LongTaskCallback<Comment>{
            override fun onResponse(result: Result<Comment>) {
                if(result is Result.Success) {
                    Log.d("hi", result.data.content)
                    list.add(result.data)
                    commentListLiveData.value = list
                }
            }
        })
    }
}