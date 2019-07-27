package com.ducktapedapps.updoot.utils.accountManagement;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ducktapedapps.updoot.api.authAPI;
import com.ducktapedapps.updoot.ui.LoginActivity;
import com.ducktapedapps.updoot.utils.constants;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;

public class Authenticator extends AbstractAccountAuthenticator {
    private static final String TAG = "Authenticator";

    @Inject
    authAPI authAPI;
    private Context mContext;

    Authenticator(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;

    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        AccountManager am = AccountManager.get(mContext);
        String authToken = am.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            authToken = authAPI
                    .getUserToken(constants.TOKEN_ACCESS_URL,
                            Credentials.basic(constants.client_id, ""),
                            constants.user_grantType,
                            options.getString("code"), constants.redirect_uri)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> Log.e(TAG, "getAuthToken: ", throwable))
                    .blockingGet()
                    .getAccess_token();
        }


        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If you reach here, person needs to login again. or sign up

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity which is the AccountsActivity in my case.
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(constants.ACCOUNT_TYPE, account.type);
        intent.putExtra("full_access", authTokenType);

        Bundle retBundle = new Bundle();
        retBundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return retBundle;
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        if (account.name.equals(constants.ANON_USER)) {
            Bundle result = new Bundle();
            //restrict anon account removal from settings
            //https://stackoverflow.com/questions/9841525/is-it-possible-to-override-the-accounts-sync-remove-account-functionality
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
            return result;
        }
        return super.getAccountRemovalAllowed(response, account);
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
        throw new UnsupportedOperationException();
    }
}
