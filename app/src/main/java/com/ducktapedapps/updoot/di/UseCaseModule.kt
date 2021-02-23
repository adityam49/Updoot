package com.ducktapedapps.updoot.di

import com.ducktapedapps.updoot.ui.navDrawer.GetUserSubscriptionsUseCase
import com.ducktapedapps.updoot.ui.navDrawer.GetUserSubscriptionsUseCaseImpl
import com.ducktapedapps.updoot.ui.search.SearchSubredditUseCase
import com.ducktapedapps.updoot.ui.search.SearchSubredditUseCaseImpl
import com.ducktapedapps.updoot.ui.subreddit.*
import com.ducktapedapps.updoot.ui.user.GetUserCommentsUseCase
import com.ducktapedapps.updoot.ui.user.GetUserCommentsUseCaseImpl
import com.ducktapedapps.updoot.ui.user.GetUserSectionsUseCase
import com.ducktapedapps.updoot.ui.user.GetUserSectionsUseCaseImpl
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

    @Binds
    abstract fun bindGetUserSectionsUseCase(useCase: GetUserSectionsUseCaseImpl): GetUserSectionsUseCase

    @Binds
    abstract fun bindEditSubredditSubscriptionUseCase(useCase: EditSubredditSubscriptionUseCaseImpl): EditSubredditSubscriptionUseCase

    @Binds
    abstract fun bindGetSubredditSubscriptionState(useCase: GetSubredditSubscriptionStateImpl): GetSubredditSubscriptionState

    @Binds
    abstract fun bindSearchSubredditUseCase(useCase: SearchSubredditUseCaseImpl): SearchSubredditUseCase
}
