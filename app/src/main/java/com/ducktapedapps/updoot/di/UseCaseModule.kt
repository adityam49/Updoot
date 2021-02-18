package com.ducktapedapps.updoot.di

import com.ducktapedapps.updoot.ui.navDrawer.GetUserSubscriptionsUseCase
import com.ducktapedapps.updoot.ui.navDrawer.GetUserSubscriptionsUseCaseImpl
import com.ducktapedapps.updoot.ui.subreddit.*
import com.ducktapedapps.updoot.ui.user.GetUserCommentsUseCase
import com.ducktapedapps.updoot.ui.user.GetUserCommentsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class UseCaseModule {
    @Binds
    abstract fun bindGetUserCommentsUseCase(useCase: GetUserCommentsUseCaseImpl): GetUserCommentsUseCase

    @Binds
    abstract fun bindGetSubredditPrefsUseCase(useCase: GetSubredditPreferencesUseCaseImpl): GetSubredditPreferencesUseCase

    @Binds
    abstract fun bindGetSubredditInfoUseCase(useCase: GetSubredditInfoUseCaseImpl): GetSubredditInfoUseCase

    @Binds
    abstract fun bindGetSubredditPostsUseCase(useCase: GetSubredditPostsUseCaseImpl): GetSubredditPostsUseCase

    @Binds
    abstract fun bindSetSubredditPostViewTypeUseCase(useCase: SetSubredditPostViewTypeUseCaseImpl): SetSubredditPostViewTypeUseCase

    @Binds
    abstract fun bindGetUserSubscriptionsUseCase(useCase: GetUserSubscriptionsUseCaseImpl): GetUserSubscriptionsUseCase
}
