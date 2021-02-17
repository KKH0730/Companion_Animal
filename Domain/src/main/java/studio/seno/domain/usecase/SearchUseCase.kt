package studio.seno.domain.usecase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.LastSearch
import studio.seno.domain.model.NotificationData

class SearchUseCase {
    fun uploadLastSearch(myEmail : String, lastSearch: LastSearch, db : FirebaseFirestore){
        db.collection("user")
            .document(myEmail)
            .collection("search")
            .document(myEmail + lastSearch.timestamp)
            .set(lastSearch)
    }

    fun loadLastSearch(myEmail: String,  db: FirebaseFirestore, callback : LongTaskCallback<List<LastSearch>>){
        db.collection("user")
            .document(myEmail)
            .collection("search")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful && it.result != null) {
                    val size = it.result!!.size()
                    val resultList = mutableListOf<LastSearch>()
                    val documentList : MutableList<DocumentSnapshot> = it.result!!.documents
                    for(element in documentList) {
                        val lastSearch = LastSearch(
                            element.getString("content")!!, element.getLong("timestamp")!!
                        )
                        resultList.add(lastSearch)
                    }
                    if(size == resultList.size)
                        callback.onResponse(Result.Success(resultList.toList()))
                }
            }.addOnFailureListener{
                callback.onResponse(Result.Error(it))
            }
    }

    fun deleteLastSearch(myEmail : String, lastSearch: LastSearch, db : FirebaseFirestore){
        db.collection("user")
            .document(myEmail)
            .collection("search")
            .document(myEmail + lastSearch.timestamp)
            .delete()
    }
}