package com.ducktapedapps.updoot.di

import android.content.SharedPreferences
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.api.RedditAPI
import com.ducktapedapps.updoot.ui.AccountsBottomSheetDialogFragment
import com.ducktapedapps.updoot.ui.LoginActivity
import com.ducktapedapps.updoot.ui.MainActivity
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import dagger.Component
import io.reactivex.Single
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

    //dependencies
    val sharedPreferences: SharedPreferences
    val redditAPI: Single<RedditAPI?>
}