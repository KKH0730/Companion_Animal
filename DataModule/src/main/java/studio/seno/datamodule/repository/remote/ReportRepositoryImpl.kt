package studio.seno.datamodule.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.seno.domain.repository.ReportRepository
import studio.seno.domain.model.Feed

class ReportRepositoryImpl : ReportRepository {
    private val db = FirebaseFirestore.getInstance()

    override fun reportFeed(feed: Feed, number: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val map = mutableMapOf<String, Any>()
            map["path"] = feed.getEmail() + feed.getTimestamp()
            map["report"] = number

            db.collection("report")
                .add(map)
        }
    }
}
