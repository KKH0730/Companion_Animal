package studio.seno.companion_animal.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import studio.seno.datamodule.repository.remote.ChatRepositoryImpl
import studio.seno.datamodule.repository.remote.CommentRepositoryImpl
import studio.seno.datamodule.repository.remote.FeedRepositoryImpl
import studio.seno.datamodule.repository.remote.FollowRepositoryImpl
import studio.seno.datamodule.repository.remote.NotificationRepositoryImpl
import studio.seno.datamodule.repository.remote.ReportRepositoryImpl
import studio.seno.datamodule.repository.remote.SearchRepositoryImpl
import studio.seno.datamodule.repository.remote.UploadRepositoryImpl
import studio.seno.datamodule.repository.remote.UserManagerRepositoryImpl
import studio.seno.domain.repository.ChatRepository
import studio.seno.domain.repository.CommentRepository
import studio.seno.domain.repository.FeedRepository
import studio.seno.domain.repository.FollowRepository
import studio.seno.domain.repository.NotificationRepository
import studio.seno.domain.repository.ReportRepository
import studio.seno.domain.repository.SearchRepository
import studio.seno.domain.repository.UploadRepository
import studio.seno.domain.repository.UserManagerRepository

/**
 * 구현체 형태의 repository 객체들을 viewModel scope에 interface 형태로 binds 해준다.
 */
@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindChatRepository(chatRepositoryImpl: ChatRepositoryImpl): ChatRepository

    @Binds
    abstract fun bindCommentRepository(commentRepositoryImpl: CommentRepositoryImpl): CommentRepository

    @Binds
    abstract fun bindFeedRepository(feedRepositoryImpl: FeedRepositoryImpl): FeedRepository

    @Binds
    abstract fun bindFollowRepository(followRepositoryImpl: FollowRepositoryImpl): FollowRepository

    @Binds
    abstract fun bindNotificationRepository(notificationRepositoryImpl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindReportRepository(reportRepositoryImpl: ReportRepositoryImpl): ReportRepository

    @Binds
    abstract fun bindSearchRepository(searchRepositoryImpl: SearchRepositoryImpl): SearchRepository

    @Binds
    abstract fun bindUploadRepository(uploadRepositoryImpl: UploadRepositoryImpl): UploadRepository

    @Binds
    abstract fun bindUserManagerRepository(userManagerRepositoryImpl: UserManagerRepositoryImpl): UserManagerRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonRepositoryModule {

//    @Binds
//    abstract fun bindConfigRepository(configRepositoryImpl: ConfigRepositoryImpl): ConfigRepository
}