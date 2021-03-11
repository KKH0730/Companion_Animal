package studio.seno.companion_animal.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import studio.seno.companion_animal.ui.chat.ChatListVIewModel
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.follow.FollowListViewModel
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.companion_animal.ui.notification.NotificationListViewModel
import studio.seno.companion_animal.ui.search.LastSearchListViewModel
import studio.seno.companion_animal.ui.user_manage.UserViewModel

val viewModelModule: Module = module {
    viewModel { FeedListViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { NotificationListViewModel(get(), get(), get(), get()) }
    viewModel { UserViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CommentListViewModel(get(), get(), get(), get(), get(), get())}
    viewModel { LastSearchListViewModel(get(), get(), get()) }
    viewModel { FollowListViewModel(get(), get()) }
    viewModel { ChatListVIewModel(get(), get(), get(), get(), get())}
}