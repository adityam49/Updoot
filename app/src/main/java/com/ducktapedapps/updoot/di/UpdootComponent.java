package com.ducktapedapps.updoot.di;

import android.content.SharedPreferences;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.api.RedditAPI;
import com.ducktapedapps.updoot.ui.LoginActivity;
import com.ducktapedapps.updoot.ui.MainActivity;
import com.ducktapedapps.updoot.ui.adapters.submissionsAdapter;
import com.ducktapedapps.updoot.ui.fragments.SubredditFragment;
import com.ducktapedapps.updoot.ui.fragments.accountsBottomSheet;

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

    void inject(accountsBottomSheet accountsBottomSheet);

    void inject(MainActivity mainActivity);

    void inject(submissionsAdapter adapter);
    //dependencies
    SharedPreferences getSharedPreferences();

    Single<RedditAPI> getRedditAPI();
}

