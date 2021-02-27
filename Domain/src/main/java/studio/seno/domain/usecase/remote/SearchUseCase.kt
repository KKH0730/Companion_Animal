package studio.seno.domain.usecase.remote

import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.LastSearch

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
            .limit(5)
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
}