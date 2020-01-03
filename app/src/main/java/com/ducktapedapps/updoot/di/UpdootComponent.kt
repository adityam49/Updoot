package com.ducktapedapps.updoot.di

import android.content.SharedPreferences
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.ui.AccountsBottomSheetDialogFragment
import com.ducktapedapps.updoot.ui.LoginActivity
import com.ducktapedapps.updoot.ui.MainActivity
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.comments.CommentsRepo
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.explore.ExploreRepo
import com.ducktapedapps.updoot.ui.subreddit.SubmissionRepo
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, ApiModule::class, ApplicationModule::class])
interface UpdootComponent {
    //Injectors
    fun inject(updootApp: UpdootApplication)

    fun inject(loginActivity: LoginActivity)
    fun inject(subredditFragment: SubredditFragment)
    fun inject(commentsFragment: CommentsFragment)
    fun inject(accountsBottomSheetDialogFragment: AccountsBottomSheetDialogFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(commentsRepo: CommentsRepo)
    fun inject(submissionRepo: SubmissionRepo)
    fun inject(exploreRepo: ExploreRepo)
    fun inject(exploreFragment: ExploreFragment)

    //dependencies
    val sharedPreferences: SharedPreferences
}