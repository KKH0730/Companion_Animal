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
    val heart: Long,
    val comment: Long,
    val timestamp: Long,
    var remoteProfileUri: String?,
    var remoteUri: List<String>?
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
        parcel.createStringArrayList()
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