package studio.seno.domain.usecase.uploadUseCase

import studio.seno.domain.repository.UploadRepository
import studio.seno.domain.util.LongTaskCallback

class GetProfileImageUseCase(private val uploadRepository: UploadRepository) {
    fun execute(
        email : String,
        callback: LongTaskCallback<String>
    ) {

        uploadRepository.getRemoteProfileImage(email, callback)
    }
}