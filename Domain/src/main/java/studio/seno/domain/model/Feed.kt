package studio.seno.domain.model

import android.os.Parcel
import android.os.Parcelable



data class Feed(
    val email: String?,
    val nickname: String?,
    val sort: String?,
    val hashTags: List<String>?,
    val localUri: List<String>?,
    val content: String?,
    var heart: Long,
    val comment: Long,
    val timestamp: Long,
    var remoteProfileUri: String?,
    var remoteUri: List<String>?,
    var heartList: Map<String, String>?,
    var bookmarkList: Map<String, String>?,
    var followList: Map<String, String>?

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.readSerializable() as Map<String, String>,
        parcel.readSerializable() as Map<String, String>,
        parcel.readSerializable() as Map<String, String>

    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
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
        dest?.writeValue(heartList)
        dest?.writeValue(bookmarkList)
        dest?.writeValue(followList)
    }

    companion object CREATOR : Parcelable.Creator<Feed> {
        override fun createFromParcel(parcel: Parcel): Feed {
            return Feed(parcel)
        }

        override fun newArray(size: Int): Array<Feed?> {
            return arrayOfNulls(size)
        }
    }

}