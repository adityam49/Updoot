package com.ducktapedapps.updoot.di;

import android.accounts.AccountManager;
import android.content.SharedPreferences;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.api.redditAPI;
import com.ducktapedapps.updoot.ui.LoginActivity;
import com.ducktapedapps.updoot.ui.MainActivity;
import com.ducktapedapps.updoot.ui.fragments.accountsBottomSheet;
import com.ducktapedapps.updoot.ui.fragments.homeFragment;
import com.ducktapedapps.updoot.utils.TokenInterceptor;

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

    void inject(homeFragment homeFragment);

    void inject(accountsBottomSheet accountsBottomSheet);

    void inject(MainActivity mainActivity);

    //dependencies
    AccountManager provideAccountManager();

    SharedPreferences getSharedPreferences();

    TokenInterceptor getTokenInterceptor();

    Single<redditAPI> getRedditAPI();


}

