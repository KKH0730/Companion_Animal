package studio.seno.domain.usecase

import android.util.Log
import android.widget.AbsListView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.firestore.*
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.LastSearch
import java.lang.Exception

class SearchUseCase {
    private var lastVisible: DocumentSnapshot? = null
    private var isScrolling = false
    private var isLastItemReached = false

    fun uploadLastSearch(myEmail: String, lastSearch: LastSearch, db: FirebaseFirestore) {
        db.collection("user")
            .document(myEmail)
            .collection("search")
            .document(myEmail + lastSearch.timestamp)
            .set(lastSearch)
    }

    fun loadLastSearch(
        myEmail: String,
        db: FirebaseFirestore,
        callback: LongTaskCallback<List<LastSearch>>
    ) {
        db.collection("user")
            .document(myEmail)
            .collection("search")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null) {
                    val size = it.result!!.size()
                    val resultList = mutableListOf<LastSearch>()
                    val documentList: MutableList<DocumentSnapshot> = it.result!!.documents
                    for (element in documentList) {
                        val lastSearch = LastSearch(
                            element.getString("content")!!, element.getLong("timestamp")!!
                        )
                        resultList.add(lastSearch)
                    }
                    if (size == resultList.size)
                        callback.onResponse(Result.Success(resultList.toList()))
                }
            }.addOnFailureListener {
                callback.onResponse(Result.Error(it))
            }
    }

    fun deleteLastSearch(myEmail: String, lastSearch: LastSearch, db: FirebaseFirestore) {
        db.collection("user")
            .document(myEmail)
            .collection("search")
            .document(myEmail + lastSearch.timestamp)
            .delete()
    }

    fun searchFeed(
        keyword: String?,
        recyclerView: RecyclerView,
        db: FirebaseFirestore,
        callback: LongTaskCallback<List<Feed>>
    ) {
        PagingModule().pagingFeed(keyword, recyclerView, db, callback)
    }
}