package com.coheser.app.koin

import android.content.Context
import android.content.SharedPreferences
import com.coheser.app.repositories.AddressRepository
import com.coheser.app.repositories.ChatRepository
import com.coheser.app.repositories.NotificationRepository
import com.coheser.app.repositories.ReportRepository
import com.coheser.app.repositories.RoomRepository
import com.coheser.app.repositories.ShopRepository
import com.coheser.app.repositories.SplashRepository
import com.coheser.app.repositories.StripeRepository
import com.coheser.app.repositories.UserRepository
import com.coheser.app.repositories.VideosRepository
import com.coheser.app.repositories.WalletRepository
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.viewModels.AddPayoutViewModel
import com.coheser.app.viewModels.AddressViewModel
import com.coheser.app.viewModels.BlockUsersViewModel
import com.coheser.app.viewModels.DeleteAccountViewModel
import com.coheser.app.viewModels.DiscoverViewModel
import com.coheser.app.viewModels.EditProfileViewModel
import com.coheser.app.viewModels.FavouriteVideosViewModel
import com.coheser.app.viewModels.FollowersViewModel
import com.coheser.app.viewModels.HomeViewModel
import com.coheser.app.viewModels.InboxViewModel
import com.coheser.app.viewModels.LikedVideosViewModel
import com.coheser.app.viewModels.MainMenuViewModel
import com.coheser.app.viewModels.MainSearchViewModel
import com.coheser.app.viewModels.MyProfileViewModel
import com.coheser.app.viewModels.NearByVideoViewModel
import com.coheser.app.viewModels.NotificationViewModel
import com.coheser.app.viewModels.OthersProfileViewModel
import com.coheser.app.viewModels.PrivacyPolicyViewModel
import com.coheser.app.viewModels.PrivateVideosViewModel
import com.coheser.app.viewModels.ProfileVerificationViewModel
import com.coheser.app.viewModels.ProfileViewsViewModel
import com.coheser.app.viewModels.PushNotificationViewModel
import com.coheser.app.viewModels.ReportViewModel
import com.coheser.app.viewModels.RepostVideosViewModel
import com.coheser.app.viewModels.SearchAllUsersViewModel
import com.coheser.app.viewModels.ShowPayoutViewModel
import com.coheser.app.viewModels.SplashViewModel
import com.coheser.app.viewModels.StripeViewModel
import com.coheser.app.viewModels.TaggedVideoViewModel
import com.coheser.app.viewModels.UserVideosViewModel
import com.coheser.app.viewModels.VideoActionsViewModel
import com.coheser.app.viewModels.VideoPlayViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule= module {

    single<SharedPreferences> {
        Functions.getSharedPreference(get<Context>())
    }

    single { AddressRepository() }
    single { NotificationRepository() }
    single { ReportRepository() }
    single { ShopRepository() }
    single { RoomRepository() }
    single { StripeRepository() }
    single { UserRepository() }
    single { VideosRepository() }
    single { WalletRepository() }
    single { ChatRepository() }
    single { SplashRepository() }

    // Provide ViewModels (Each separately)

    viewModel { InboxViewModel(get<Context>()) }

    viewModel { MainMenuViewModel(get<SharedPreferences>(), get<AddressRepository>(), get<UserRepository>()) }
    viewModel { AddPayoutViewModel(get<Context>(), get<WalletRepository>()) }
    viewModel { AddressViewModel(get<Context>(), get<AddressRepository>()) }

    viewModel { BlockUsersViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { DeleteAccountViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { EditProfileViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { FollowersViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { PrivacyPolicyViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { ProfileVerificationViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { PushNotificationViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { SearchAllUsersViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { ProfileViewsViewModel(get<Context>(), get<UserRepository>()) }
    viewModel { OthersProfileViewModel(get<Context>(), get<UserRepository>()) }





    viewModel { NotificationViewModel(get(), get(),get()) }
    viewModel { HomeViewModel(get(), get(),get(),get()) }
    viewModel { DiscoverViewModel(get(), get(),get(),get()) }

    viewModel { MyProfileViewModel(get<Context>(), get<UserRepository>(),get<ChatRepository>()) }

    viewModel { UserVideosViewModel(get<Context>(), get<VideosRepository>()) }
    viewModel { TaggedVideoViewModel(get<Context>(), get<VideosRepository>()) }
    viewModel { LikedVideosViewModel(get<Context>(), get<VideosRepository>()) }
    viewModel { NearByVideoViewModel(get<Context>(), get<VideosRepository>()) }
    viewModel { RepostVideosViewModel(get<Context>(), get<VideosRepository>()) }
    viewModel { PrivateVideosViewModel(get<Context>(), get<VideosRepository>()) }
    viewModel { FavouriteVideosViewModel(get<Context>(), get<VideosRepository>()) }



    viewModel { VideoPlayViewModel(get<Context>(), get<UserRepository>(),get<VideosRepository>()) }
    viewModel { VideoActionsViewModel(get<Context>(), get<UserRepository>(),get<VideosRepository>()) }
    viewModel { MainSearchViewModel(get<Context>(), get<UserRepository>(),get<VideosRepository>()) }

    viewModel { SplashViewModel(get<Context>(), get<SplashRepository>(),get<AddressRepository>(),get<VideosRepository>()) }


    viewModel{ ReportViewModel(get<Context>(), get<ReportRepository>()) }

    viewModel{ ShowPayoutViewModel(get<Context>(), get<WalletRepository>()) }

    viewModel{ StripeViewModel(get<Context>(),get<StripeRepository>(), get<WalletRepository>()) }



}