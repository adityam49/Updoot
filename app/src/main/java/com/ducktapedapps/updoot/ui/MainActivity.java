package com.ducktapedapps.updoot.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.ui.fragments.accountsBottomSheet;
import com.ducktapedapps.updoot.ui.fragments.homeFragment;
import com.ducktapedapps.updoot.utils.TokenInterceptor;
import com.ducktapedapps.updoot.utils.constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements accountsBottomSheet.BottomSheetListener {
    private static final String TAG = "MainActivity";

    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    TokenInterceptor interceptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ((UpdootApplication) getApplication()).getUpdootComponent().inject(this);
        //toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //bottom bar setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.home:
                    //??
                    break;
                case R.id.accounts:
                    accountsBottomSheet bottomSheet = new accountsBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "accountsBottomSheet");
                    break;
            }
            return false;
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            Log.i(TAG, "onCreate: fragment null");
            fragment = homeFragment.newInstance();

            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, "home_fragment")
                    .commit();
        }

    }

    @Override
    public void onButtonClicked(String text) {
        switch (text) {
            case "Add account":
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, constants.ACCOUNT_LOGIN_REQUEST_CODE);
                break;
            default:
                sharedPreferences.edit().putString(constants.LOGIN_STATE, text).apply();
                interceptor.setAccountChanged();
                reloadContent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case constants.ACCOUNT_LOGIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "onActivityResult: new Login");
                    reloadContent();
                }
        }
    }

    private void reloadContent() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("home_fragment");
        if (fragment instanceof homeFragment) {
            ((homeFragment) fragment).reload();
        }
    }
}
