package studio.seno.companion_animal.ui.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import studio.seno.domain.model.LastSearch
import studio.seno.domain.usecase.searchUseCase.DeleteLastSearchUseCase
import studio.seno.domain.usecase.searchUseCase.GetLastSearchUseCase
import studio.seno.domain.usecase.searchUseCase.SetLastSearchUseCase
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import javax.inject.Inject

@HiltViewModel
class LastSearchListViewModel @Inject constructor(
    private val setLastSearchUseCase: SetLastSearchUseCase,
    private val getLastSearchUseCase: GetLastSearchUseCase,
    private val deleteLastSearchUseCase: DeleteLastSearchUseCase
) : ViewModel() {
    private var lastSearchListLiveData  = MutableLiveData<List<LastSearch>>()

    fun setLastSearchLiveData(list : List<LastSearch>) {
        this.lastSearchListLiveData.value = list
    }

    fun getLastSearchLiveData() : MutableLiveData<List<LastSearch>>{
        return lastSearchListLiveData
    }

    fun requestUploadLastSearch(content : String, timestamp : Long){
        val keywordList = lastSearchListLiveData.value?.toMutableList()

        if (keywordList != null) {
            for(element in keywordList)
                if(element.content == content)
                    return
        }

        setLastSearchUseCase.execute(content, timestamp)
    }

    fun requestLoadLastSearch(){
        getLastSearchUseCase.execute(object : LongTaskCallback<Any> {
            override fun onResponse(result: Result<Any>) {
                if(result is Result.Success) {
                    lastSearchListLiveData.value = result.data as List<LastSearch>
                }else if(result is Result.Error) {
                    Log.e("error","LastSearchListViewModel load LastSearch Error : ${result.exception}")
                }
            }
        })
    }

    fun requestDeleteLastSearch(lastSearch: LastSearch){
        deleteLastSearchUseCase.execute(lastSearch)
    }

}