package com.ducktapedapps.updoot.utils.accountManagement;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.utils.constants;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class userManager {
    private static final String TAG = "userManager";
    private final AccountManager accountManager;
    private final SharedPreferences sharedPreferences;
    private final TokenInterceptor interceptor;
    private OnAccountListUpdated mListener;

    @Inject
    public userManager(AccountManager accountManager, SharedPreferences sharedPreferences, TokenInterceptor interceptor) {
        this.accountManager = accountManager;
        this.sharedPreferences = sharedPreferences;
        this.interceptor = interceptor;
    }

    public OnAccountListUpdated getmListener() {
        return mListener;
    }

    public Account getCurrentUser() {
        String currentCachedUser = sharedPreferences.getString(constants.LOGIN_STATE, null);
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
        sharedPreferences.edit().putString(constants.LOGIN_STATE, userName).apply();
        for (Account account : accountManager.getAccounts()) {
            if (account.name.equals(userName)) {
                interceptor.setSessionToken(token);
                return;
            }
        }

        Log.i(TAG, "setCurrentUser: after fresh install");
        interceptor.setSessionToken(token);
        accountManager.addAccountExplicitly(new Account(constants.ANON_USER, constants.ACCOUNT_TYPE), null, null);
    }

    public void updateUserSessionData(Token token) {
        interceptor.setSessionToken(token);
    }

    public void attachListener(Context context) {
        mListener = (OnAccountListUpdated) context;
    }

    public interface OnAccountListUpdated {
        void currentAccountRemoved();
    }
}
