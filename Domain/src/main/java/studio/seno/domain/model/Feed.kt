package studio.seno.domain.model


import android.os.Parcel
import android.os.Parcelable


class Feed : Parcelable {
    private var email: String? = ""
    private var nickname: String? = ""
    private var sort: String = ""
    private var hashTags: List<String> = listOf()
    private var localUri: List<String> = listOf()
    private var content: String = ""
    private var heart: Long = 0
    private var comment: Long = 0
    private var timestamp: Long = 0
    private var remoteProfileUri: String = ""
    private var remoteUri: List<String> = listOf()
    private var heartList: Map<String, String> = mapOf()
    private var bookmarkList: Map<String, String> = mapOf()

    constructor(
        email: String?,
        nickname: String?,
        sort: String,
        hashTags: List<String>,
        localUri: List<String>,
        content: String,
        heart: Long,
        comment: Long,
        timestamp: Long,
        remoteProfileUri: String,
        remoteUri: List<String>,
        heartList: Map<String, String>,
        bookmarkList: Map<String, String>,
    ) {
        this.email = email
        this.nickname = nickname
        this.sort = sort
        this.hashTags = hashTags
        this.localUri = localUri
        this.content = content
        this.heart = heart
        this.comment = comment
        this.timestamp = timestamp
        this.remoteProfileUri = remoteProfileUri
        this.remoteUri = remoteUri
        this.heartList = heartList
        this.bookmarkList = bookmarkList
    }

    constructor(parcel: Parcel) {
        this.email = parcel.readString().toString()
        this.nickname = parcel.readString().toString()
        this.sort = parcel.readString().toString()
        this.hashTags = parcel.createStringArrayList()!!
        this.localUri = parcel.createStringArrayList()!!
        this.content = parcel.readString().toString()
        this.heart = parcel.readLong()
        this.comment = parcel.readLong()
        this.timestamp = parcel.readLong()
        this.remoteProfileUri = parcel.readString()!!
        this.remoteUri = parcel.createStringArrayList()!!
        this.heartList = buildTheMap(parcel)
        this.bookmarkList = buildTheMap(parcel)
    }


    override fun describeContents(): Int {
        return 0
    }


    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest?.writeString(email)
        dest?.writeString(nickname)
        dest?.writeString(sort)
        dest?.writeStringList(hashTags)
        dest?.writeStringList(localUri)
        dest?.writeString(content)
        dest?.writeLong(heart)
        dest?.writeLong(comment)
        dest?.writeLong(timestamp)
        dest?.writeString(remoteProfileUri)
        dest?.writeStringList(remoteUri)
        writeToMap(heartList, dest)
        writeToMap(bookmarkList, dest)
    }

    companion object CREATOR : Parcelable.Creator<Feed> {
        override fun createFromParcel(parcel: Parcel): Feed {
            return Feed(parcel)
        }

        override fun newArray(size: Int): Array<Feed?> {
            return arrayOfNulls(size)
        }
    }

    fun buildTheMap(parcel: Parcel): Map<String, String> {
        val size = parcel.readInt()
        val map = HashMap<String, String>()

        for (i in 1..size) {
            val key = parcel.readString().toString()
            val value = parcel.readString().toString()
            map[key] = value
        }
        return map
    }


    fun writeToMap(map: Map<String, String>, parcel: Parcel?) {
        if (parcel != null) {
            parcel.writeInt(map.size)

            for ((key, value) in map) {
                parcel.writeString(key)
                parcel.writeString(value)
            }
        }
    }


    fun getEmail() : String?{
        return email
    }


    fun getNickname() : String? {
        return nickname
    }


    fun getSort() : String {
        return sort
    }

    fun setSort(sort : String) {
        this.sort = sort
    }


    fun getHashTags() : List<String> {
        return hashTags
    }

    fun setHashTags(hashTags: List<String>) {
        this.hashTags = hashTags
    }


    fun getLocalUri() : List<String> {
        return  localUri
    }

    fun setLocalUri(localUri : List<String>){
        this.localUri = localUri
    }

    fun getContent() : String {
        return content
    }

    fun setContent(content: String) {
        this.content = content
    }

    fun getHeart() : Long {
        return heart
    }

    fun setHeart(heart: Long) {
        this.heart = heart
    }

    fun getComment(): Long{
        return comment
    }

    fun setComment(comment: Long) {
        this.comment = comment
    }

    fun getTimestamp() : Long {
        return timestamp
    }

    fun getRemoteProfileUri() : String {
        return remoteProfileUri
    }

    fun setRemoteProfileUri(remoteProfileUri :String) {
        this.remoteProfileUri = remoteProfileUri
    }

    fun getRemoteUri(): List<String> {
        return remoteUri
    }

    fun setRemoteUri(remoteUri : List<String>) {
        this.remoteUri = remoteUri
    }

    fun getHeartList(): Map<String, String> {
        return heartList
    }

    fun setHeartList(heartList: Map<String, String>) {
        this.heartList = heartList
    }


    fun getBookmarkList(): Map<String, String> {
        return bookmarkList
    }

    fun setBookmarkList(bookmarkList: Map<String, String>) {
        this.bookmarkList = bookmarkList
    }

}