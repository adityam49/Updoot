package com.ducktapedapps.updoot.di;

import android.content.SharedPreferences;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.api.RedditAPI;
import com.ducktapedapps.updoot.ui.LoginActivity;
import com.ducktapedapps.updoot.ui.MainActivity;
import com.ducktapedapps.updoot.ui.fragments.AccountsBottomSheetDialogFragment;
import com.ducktapedapps.updoot.ui.fragments.CommentsFragment;
import com.ducktapedapps.updoot.ui.fragments.SubredditFragment;

import javax.inject.Singleton;

import dagger.Component;
import io.reactivex.Single;

@Singleton
@Component(modules = {
        NetworkModule.class,
        ApiModule.class,
        ApplicationModule.class
})
public interface UpdootComponent {
    //Injectors
    void inject(UpdootApplication updootApp);

    void inject(LoginActivity loginActivity);

    void inject(SubredditFragment subredditFragment);

    void inject(CommentsFragment commentsFragment);

    void inject(AccountsBottomSheetDialogFragment accountsBottomSheetDialogFragment);

    void inject(MainActivity mainActivity);

    //dependencies
    SharedPreferences getSharedPreferences();

    Single<RedditAPI> getRedditAPI();
}

