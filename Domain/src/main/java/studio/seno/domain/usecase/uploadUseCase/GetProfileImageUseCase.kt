package studio.seno.domain.usecase.uploadUseCase

import studio.seno.domain.repository.UploadRepository
import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class GetProfileImageUseCase @Inject constructor(private val uploadRepository: UploadRepository) {
    fun execute(
        email : String,
        callback: LongTaskCallback<Any>
    ) {

        uploadRepository.getRemoteProfileImage(email, callback)
    }
}