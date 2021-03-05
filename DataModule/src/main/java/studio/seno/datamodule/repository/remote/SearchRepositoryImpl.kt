package studio.seno.datamodule.repository.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import studio.seno.domain.repository.SearchRepository
import studio.seno.domain.model.LastSearch
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class SearchRepositoryImpl : SearchRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun setLastSearch(lastSearch: LastSearch) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .collection("search")
            .document(auth.currentUser?.email.toString() + lastSearch.timestamp)
            .set(lastSearch)
    }

    override fun getLastSearch(callback: LongTaskCallback<List<LastSearch>>) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
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

    override fun deleteLastSearch(lastSearch: LastSearch) {
        db.collection("user")
            .document(auth.currentUser?.email.toString())
            .collection("search")
            .document(auth.currentUser?.email.toString() + lastSearch.timestamp)
            .delete()
    }

}