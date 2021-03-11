package studio.seno.datamodule.repository.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.domain.repository.SearchRepository
import studio.seno.domain.model.LastSearch
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class SearchRepositoryImpl : SearchRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun setLastSearch(lastSearch: LastSearch) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .collection("search")
                .document(lastSearch.timestamp.toString())
                .set(lastSearch)
        }
    }

    override fun getLastSearch(callback: LongTaskCallback<Any>) {
        CoroutineScope(Dispatchers.IO).launch {
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
                            sendCallback(resultList.toList(), false, callback)
                    }
                }.addOnFailureListener {
                    sendCallback(it, true, callback)
                }
        }
    }

    override fun deleteLastSearch(lastSearch: LastSearch) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("user")
                .document(auth.currentUser?.email.toString())
                .collection("search")
                .document(auth.currentUser?.email.toString() + lastSearch.timestamp)
                .delete()
        }
    }

    private fun sendCallback(any : Any, isError : Boolean, callback: LongTaskCallback<Any>?)  {
        CoroutineScope(Dispatchers.Main).launch {
            if(!isError)
                callback?.onResponse(Result.Success(any))
            else
                callback?.onResponse(Result.Error(any as java.lang.Exception))
        }
    }
}