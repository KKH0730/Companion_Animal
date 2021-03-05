package studio.seno.companion_animal.module

import org.koin.core.module.Module
import org.koin.dsl.module
import studio.seno.domain.usecase.chatUseCase.*
import studio.seno.domain.usecase.commentUseCase.*
import studio.seno.domain.usecase.feedUseCase.*
import studio.seno.domain.usecase.followUseCase.CheckFollowUseCase
import studio.seno.domain.usecase.followUseCase.GetFollowUseCase
import studio.seno.domain.usecase.followUseCase.SetFollowUseCase
import studio.seno.domain.usecase.notificationUseCase.DeleteNotificationUseCase
import studio.seno.domain.usecase.notificationUseCase.GetNotificationUseCase
import studio.seno.domain.usecase.notificationUseCase.SetCheckDotUseCase
import studio.seno.domain.usecase.reportUseCase.SendReportUseCase
import studio.seno.domain.usecase.searchUseCase.DeleteLastSearchUseCase
import studio.seno.domain.usecase.searchUseCase.GetLastSearchUseCase
import studio.seno.domain.usecase.searchUseCase.SetLastSearchUseCase
import studio.seno.domain.usecase.uploadUseCase.GetProfileImageUseCase
import studio.seno.domain.usecase.uploadUseCase.SetProfileImageUseCase
import studio.seno.domain.usecase.userMangerUseCase.*

val useCaseModule: Module = module {
    single { SetFeedUseCase(get()) }
    single { DeleteFeedUseCase(get()) }
    single { GetFeedUseCase(get()) }
    single { UpdateHeartUseCase(get()) }
    single { UpdateBookmarkUseCase(get()) }
    single { GetPagingFeedUseCase(get()) }
    single { CheckEnableLoginUseCase(get()) }
    single { FindPasswordUseCase(get()) }
    single { RegisterUserUseCase(get()) }
    single { SetProfileImageUseCase(get()) }
    single { GetProfileImageUseCase(get()) }
    single { SetUserInfoUseCase(get()) }
    single { GetUserInfoUseCase(get()) }
    single { SetNicknameUseCase(get()) }
    single { SetTokenUseCase(get()) }
    single { CheckOverlapUserUseCase(get()) }
    single { SetProfileUriUseCase(get()) }
    single { SetLastSearchUseCase(get()) }
    single { GetLastSearchUseCase(get()) }
    single { DeleteLastSearchUseCase(get()) }
    single { SetCommentCountUseCase(get())}
    single { GetCommentAnswerUseCase(get()) }
    single { SetCommentUseCase(get()) }
    single { GetCommentUseCase(get()) }
    single { DeleteCommentUseCase(get())}
    single { GetNotificationUseCase(get()) }
    single { SetCheckDotUseCase(get()) }
    single { DeleteNotificationUseCase(get()) }
    single { CheckFollowUseCase(get())}
    single { GetFollowUseCase(get())}
    single { SetFollowUseCase(get())}
    single { SendReportUseCase(get())}
    single { AddChatUseCase(get())}
    single { SetAddedChatListenerUseCase(get())}
    single { SetChatListListenerUseCase(get())}
    single { SetChatCheckDotUseCase(get())}
    single { DeleteChatListUseCase(get())}
}