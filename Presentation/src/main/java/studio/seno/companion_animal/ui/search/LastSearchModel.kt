package studio.seno.companion_animal.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.LastSearch

class LastSearchModel : ViewModel() {
    private var lastSearchLiveData  = MutableLiveData<LastSearch>()

    fun setLastSearchLiveData(item : LastSearch) {
        this.lastSearchLiveData.value = item
    }

    fun getLastSearchLiveData() : MutableLiveData<LastSearch> {
        return lastSearchLiveData
    }
}