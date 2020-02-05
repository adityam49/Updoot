package com.ducktapedapps.updoot.di

import android.content.SharedPreferences
import com.ducktapedapps.updoot.ui.AccountsBottomSheetDialogFragment
import com.ducktapedapps.updoot.ui.LoginActivity
import com.ducktapedapps.updoot.ui.MainActivity
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.comments.CommentsVMFactory
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.explore.ExploreVMFactory
import com.ducktapedapps.updoot.ui.subreddit.SubmissionsVMFactory
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, ApiModule::class, ApplicationModule::class])
interface UpdootComponent {
    //Injectors
    fun inject(loginActivity: LoginActivity)
    fun inject(subredditFragment: SubredditFragment)
    fun inject(commentsFragment: CommentsFragment)
    fun inject(accountsBottomSheetDialogFragment: AccountsBottomSheetDialogFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(exploreFragment: ExploreFragment)
    fun inject(submissionsVMFactory: SubmissionsVMFactory)
    fun inject(commentsVMFactory: CommentsVMFactory)
    fun inject(exploreVMFactory: ExploreVMFactory)

    //dependencies
    val sharedPreferences: SharedPreferences
}