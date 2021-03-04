package studio.seno.companion_animal.ui.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.RemoteRepository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.LastSearch

class LastSearchListViewModel : ViewModel() {
    private val remoteRepository = RemoteRepository.getInstance()!!
    private var lastSearchListLiveData  = MutableLiveData<List<LastSearch>>()

    fun setLastSearchLiveData(list : List<LastSearch>) {
        this.lastSearchListLiveData.value = list
    }

    fun getLastSearchLiveData() : MutableLiveData<List<LastSearch>>{
        return lastSearchListLiveData
    }

    fun requestUploadLastSearch(content : String, timestamp : Long){
        val lastSearch = Mapper.getInstance()!!.mapperToLastSearch(content , timestamp)
        val keywordList = lastSearchListLiveData.value?.toMutableList()

        if (keywordList != null) {
            for(element in keywordList)
                if(element.content == content)
                    return
        }

        remoteRepository.requestUploadLastSearch(lastSearch)
    }

    fun requestLoadLastSearch(){
        remoteRepository.requestLoadLastSearch(object : LongTaskCallback<List<LastSearch>>{
            override fun onResponse(result: Result<List<LastSearch>>) {
                if(result is Result.Success) {
                    lastSearchListLiveData.value = result.data
                }else if(result is Result.Error) {
                    Log.e("error","LastSearchListViewModel load LastSearch Error : ${result.exception}")
                }
            }
        })
    }

    fun requestDeleteLastSearch(lastSearch: LastSearch){
        remoteRepository.requestDeleteLastSearch(lastSearch)
    }

}