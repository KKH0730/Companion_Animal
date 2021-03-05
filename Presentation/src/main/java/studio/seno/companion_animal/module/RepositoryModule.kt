package studio.seno.companion_animal.module

import org.koin.core.module.Module
import org.koin.dsl.module
import studio.seno.datamodule.repository.remote.*
import studio.seno.domain.Repository.ReportRepository
import studio.seno.domain.Repository.*

val repositoryModule: Module = module {
    single<FeedRepository> { FeedRepositoryImpl() }
    single<UploadRepository> { UploadRepositoryImpl() }
    single<UserManagerRepository> { UserManagerRepositoryImpl() }
    single<NotificationRepository> { NotificationRepositoryImpl() }
    single<SearchRepository> { SearchRepositoryImpl() }
    single<CommentRepository> { CommentRepositoryImpl() }
    single<FollowRepository> { FollowRepositoryImpl() }
    single<ReportRepository> { ReportRepositoryImpl() }
    single<ChatRepository> { ChatRepositoryImpl() }
}