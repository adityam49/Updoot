package com.ducktapedapps.updoot.di

import android.content.Context
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.ui.LoginActivity
import com.ducktapedapps.updoot.ui.MainActivity
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.comments.CommentsVMFactory
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.explore.ExploreVMFactory
import com.ducktapedapps.updoot.ui.navDrawer.BottomNavDrawerFragment
import com.ducktapedapps.updoot.ui.subreddit.SubmissionsVMFactory
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import com.ducktapedapps.updoot.ui.subreddit.options.SubmissionOptionsBottomSheet
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, ApiModule::class, ApplicationModule::class, RoomModule::class])
interface UpdootComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): UpdootComponent
    }

    //Application Class
    fun inject(application: UpdootApplication)

    //Activity
    fun inject(mainActivity: MainActivity)
    fun inject(loginActivity: LoginActivity)

    //fragments
    fun inject(subredditFragment: SubredditFragment)
    fun inject(commentsFragment: CommentsFragment)
    fun inject(exploreFragment: ExploreFragment)
    fun inject(bottomNavDrawerFragment: BottomNavDrawerFragment)
    fun inject(submissionOptionsBottomSheet: SubmissionOptionsBottomSheet)

    //factories
    fun inject(submissionsVMFactory: SubmissionsVMFactory)
    fun inject(commentsVMFactory: CommentsVMFactory)
    fun inject(exploreVMFactory: ExploreVMFactory)
}