package studio.seno.domain.usecase.uploadUseCase

import android.net.Uri
import studio.seno.domain.repository.UploadRepository
import studio.seno.domain.util.LongTaskCallback

class SetProfileImageUseCase(private val uploadRepository: UploadRepository) {
    fun execute(
        imageUri : Uri,
        callback: LongTaskCallback<Any>
    ) {

        uploadRepository.setRemoteProfileImage(imageUri, callback)
    }
}