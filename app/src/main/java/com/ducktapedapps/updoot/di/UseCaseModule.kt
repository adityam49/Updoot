package com.ducktapedapps.updoot.di

import com.ducktapedapps.updoot.navDrawer.*
import com.ducktapedapps.updoot.search.SearchSubredditUseCase
import com.ducktapedapps.updoot.search.SearchSubredditUseCaseImpl
import com.ducktapedapps.updoot.subreddit.*
import com.ducktapedapps.updoot.user.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
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
    abstract fun bindUpdateUserSubscriptionUseCase(useCase: UpdateUserSubscriptionUseCaseImpl): UpdateUserSubscriptionUseCase

    @Binds
    abstract fun bindEditSubredditSubscriptionUseCase(useCase: EditSubredditSubscriptionUseCaseImpl): EditSubredditSubscriptionUseCase

    @Binds
    abstract fun bindGetSubredditSubscriptionState(useCase: GetSubredditSubscriptionStateImpl): GetSubredditSubscriptionState

    @Binds
    abstract fun bindSearchSubredditUseCase(useCase: SearchSubredditUseCaseImpl): SearchSubredditUseCase

    @Binds
    abstract fun bindGetUserPostsUseCase(useCase: GetUserPostsUseCaseImpl): GetUserPostsUseCase

    @Binds
    abstract fun bindGetUserOverViewUseCase(useCase: GetUserOverviewUseCaseImpl): GetUserOverviewUseCase

    @Binds
    abstract fun bindGetUserUpVotedUseCase(useCase: GetUserUpVotedUseCaseImpl): GetUserUpVotedUseCase

    @Binds
    abstract fun bindGetUserDownVotedUseCase(useCase: GetUserDownVotedUseCaseImpl): GetUserDownVotedUseCase

    @Binds
    abstract fun bindGetUserSavedUseCase(useCase: GetUserSavedUseCaseImpl): GetUserSavedUseCase

    @Binds
    abstract fun bindGetUserGildedUseCase(useCase: GetUserGildedUseCaseImpl): GetUserGildedUseCase

    @Binds
    abstract fun bindGetUserTrophiesUseCase(useCase: GetUserTrophiesUseCaseImpl): GetUserTrophiesUseCase

    @Binds
    abstract fun bindGetUserMultiRedditsUseCase(useCase: GetUserMultiRedditsUseCaseImpl): GetUserMultiRedditsUseCase
}
