package studio.seno.datamodule.repository.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.domain.repository.CommentRepository
import studio.seno.domain.model.Comment
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import java.util.*
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor() : CommentRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storageRef: StorageReference = FirebaseStorage.getInstance()
        .getReferenceFromUrl("gs://companion-animal-f0bfa.appspot.com/")

    override fun setComment(
        targetEmail: String,
        targetTimestamp: Long,
        myComment: Comment
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var remoteProfilePath = auth.currentUser?.email + "/profile/profileImage"
            storageRef.child(remoteProfilePath).downloadUrl.addOnSuccessListener { it1 ->
                myComment.profileUri = it1.toString()

                db.collection("feed")
                    .document(targetEmail + targetTimestamp)
                    .collection("comment")
                    .document(myComment.email + myComment.timestamp)
                    .set(myComment)

            }
        }
    }


    override fun getCommentAnswer(
        feedEmail: String,
        feedTimeStamp: Long,
        targetEmail: String,
        targetTimestamp: Long,
        myCommentAnswer: Comment
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var remoteProfilePath = auth.currentUser?.email + "/profile/profileImage"
            storageRef.child(remoteProfilePath).downloadUrl.addOnSuccessListener {
                myCommentAnswer.profileUri = it.toString()

                db.collection("feed")
                    .document(feedEmail + feedTimeStamp)
                    .collection("comment")
                    .document(targetEmail + targetTimestamp)
                    .collection("comment_answer")
                    .document(myCommentAnswer.email + myCommentAnswer.timestamp)
                    .set(myCommentAnswer)
            }
        }

    }

    override fun setCommentCount(
        targetEmail: String,
        targetTimestamp: Long,
        commentCount: Long,
        flag: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (flag) {
                db.collection("feed")
                    .document(targetEmail + targetTimestamp)
                    .update("comment", commentCount + 1)
            } else {
                db.collection("feed")
                    .document(targetEmail + targetTimestamp)
                    .update("comment", commentCount - 1)
            }
        }
    }

    override fun getComment(
        email: String,
        timestamp: Long,
        callback: LongTaskCallback<List<Comment>>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("feed")
                .document(email + timestamp)
                .collection("comment")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener {

                    var list: MutableList<Comment> = mutableListOf()
                    var size: Int? = null
                    if (it.result != null) {
                        size = it.result!!.size()

                        for (i in 0 until it.result!!.size()) {
                            var document: DocumentSnapshot = it.result!!.documents[i]
                            var loadComment = Comment(
                                document.getLong("type")!!,
                                document.getString("email")!!,
                                document.getString("nickname")!!,
                                document.getString("content")!!,
                                document.getString("profileUri"),
                                document.getLong("timestamp")!!
                            )
                            list.add(loadComment)
                        }
                    }
                    if (list.size == size) {
                        loadAnswerComment(
                            email,
                            timestamp,
                            list,
                            db,
                            callback
                        )
                    }
                }
        }
    }

    override fun deleteComment(
        feedEmail: String,
        feedTimestamp: Long,
        parentComment: Comment,
        childComment: Comment?,
        type: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val myEmail = auth.currentUser?.email.toString()

            if (type == "parent") {
                db.collection("feed")
                    .document(feedEmail + feedTimestamp)
                    .collection("comment")
                    .document(parentComment.email + parentComment.timestamp)
                    .collection("comment_answer")
                    .get()
                    .addOnCompleteListener {
                        var list: QuerySnapshot? = it.result

                        if (list != null)
                            for (element in list)
                                element.reference.delete()
                    }

                db.collection("feed")
                    .document(feedEmail + feedTimestamp)
                    .collection("comment")
                    .document(parentComment.email + parentComment.timestamp)
                    .delete()
            } else {
                db.collection("feed")
                    .document(feedEmail + feedTimestamp)
                    .collection("comment")
                    .document(parentComment.email + parentComment.timestamp)
                    .collection("comment_answer")
                    .document(childComment?.email + childComment?.timestamp)
                    .delete()
            }
        }
    }

    fun loadAnswerComment(
        email: String, timestamp: Long, commentList: MutableList<Comment>,
        db: FirebaseFirestore, callback: LongTaskCallback<List<Comment>>
    ) {
        var totalList = mutableListOf<Comment>()
        var size = commentList.size

        if (size == 0) {
            var result = Result.Success(totalList.toList())
            callback.onResponse(result)
        } else {
            for (i in 0 until size) {
                var loadComment = commentList[i]

                db.collection("feed")
                    .document(email + timestamp)
                    .collection("comment")
                    .document(loadComment.email + loadComment.timestamp)
                    .collection("comment_answer")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener {
                        var tempList = mutableListOf<Comment>()
                        if (it.result != null) {
                            for (i in 0 until it.result!!.size()) {
                                var document: DocumentSnapshot = it.result!!.documents[i]
                                var commentAnswerItem = Comment(
                                    document.getLong("type")!!,
                                    document.getString("email")!!,
                                    document.getString("nickname")!!,
                                    document.getString("content")!!,
                                    document.getString("profileUri"),
                                    document.getLong("timestamp")!!
                                )
                                tempList.add(commentAnswerItem)
                            }
                            loadComment.setChildren(tempList.toList())
                            totalList.add(loadComment)
                        }

                        if (totalList.size == size) {
                            Collections.sort(totalList)
                            sendCallback(totalList.toList(), false, callback)
                            var result = Result.Success(totalList.toList())
                            callback.onResponse(result)
                        }
                    }.addOnFailureListener {
                        sendCallback(it, true, callback)
                    }
            }
        }
    }

    private fun sendCallback(any : Any, isError : Boolean, callback: LongTaskCallback<List<Comment>>?)  {
        CoroutineScope(Dispatchers.Main).launch {
            if(!isError)
                callback?.onResponse(Result.Success(any as List<Comment>))
            else
                callback?.onResponse(Result.Error(any as java.lang.Exception))
        }
    }
}


