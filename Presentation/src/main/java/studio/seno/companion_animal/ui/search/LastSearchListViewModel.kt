package studio.seno.companion_animal.ui.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import studio.seno.datamodule.Repository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.LastSearch

class LastSearchListViewModel : ViewModel() {
    val repository = Repository()
    val mapper = Mapper()

    private var lastSearchListLiveData  = MutableLiveData<List<LastSearch>>()

    fun setLastSearchLiveData(list : List<LastSearch>) {
        this.lastSearchListLiveData.value = list
    }

    fun getLastSearchLiveData() : MutableLiveData<List<LastSearch>>{
        return lastSearchListLiveData
    }

    fun requestUploadLastSearch(myEmail : String, content : String, timestamp : Long){

        val lastSearch = mapper.mapperToLastSearch(content , timestamp)
        repository.requestUploadLastSearch(myEmail, lastSearch)
    }

    fun requestLoadLastSearch(myEmail : String){
        repository.requestLoadLastSearch(myEmail, object : LongTaskCallback<List<LastSearch>>{
            override fun onResponse(result: Result<List<LastSearch>>) {
                if(result is Result.Success) {
                    lastSearchListLiveData.value = result.data
                }else if(result is Result.Error) {
                    Log.e("error","load LastSearch Error : ${result.exception}")
                }
            }
        })
    }

    fun requestDeleteLastSearch(myEmail : String, lastSearch: LastSearch){
        repository.requestDeleteLastSearch(myEmail, lastSearch)
    }

}