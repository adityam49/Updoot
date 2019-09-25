package com.ducktapedapps.updoot.utils.accountManagement;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.utils.Constants;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class UserManager {
    private static final String TAG = "UserManager";
    private final AccountManager accountManager;
    private final SharedPreferences sharedPreferences;
    private final TokenInterceptor interceptor;
    private AccountChangeListener mListener;

    @Inject
    public UserManager(AccountManager accountManager, SharedPreferences sharedPreferences, TokenInterceptor interceptor) {
        this.accountManager = accountManager;
        this.sharedPreferences = sharedPreferences;
        this.interceptor = interceptor;
    }

    public AccountChangeListener getmListener() {
        return mListener;
    }

    public Account getCurrentUser() {
        String currentCachedUser = sharedPreferences.getString(Constants.LOGIN_STATE, null);
        if (currentCachedUser != null) {
            for (Account account : accountManager.getAccounts()) {
                if (account.name.equals(currentCachedUser)) {
                    return account;
                }
            }
        }
        return null;
    }

    public void setCurrentUser(String userName, Token token) {
        Log.i(TAG, "setCurrentUser: user name is " + userName + " token is " + token);
        sharedPreferences.edit().putString(Constants.LOGIN_STATE, userName).apply();
        for (Account account : accountManager.getAccounts()) {
            if (account.name.equals(userName)) {
                interceptor.setSessionToken(token);
                return;
            }
        }

        Log.i(TAG, "setCurrentUser: after fresh install");
        interceptor.setSessionToken(token);
        accountManager.addAccountExplicitly(new Account(Constants.ANON_USER, Constants.ACCOUNT_TYPE), null, null);
    }


    public void updateUserSessionData(Token token) {
        interceptor.setSessionToken(token);
    }

    public void attachListener(Context context) {
        mListener = (AccountChangeListener) context;
    }

    public interface AccountChangeListener {
        void onCurrentAccountRemoved();
    }
}
