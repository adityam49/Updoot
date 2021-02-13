package com.ducktapedapps.updoot.di

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
}