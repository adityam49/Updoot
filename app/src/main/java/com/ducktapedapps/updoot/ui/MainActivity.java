package com.ducktapedapps.updoot.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.ui.fragments.accountsBottomSheet;
import com.ducktapedapps.updoot.ui.fragments.homeFragment;
import com.ducktapedapps.updoot.utils.accountManagement.userManager;
import com.ducktapedapps.updoot.utils.constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements accountsBottomSheet.BottomSheetListener, userManager.OnAccountListUpdated {
    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bottom_navigation_bar)
    BottomNavigationView bottomNavigationView;

    @Inject
    userManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((UpdootApplication) getApplication()).getUpdootComponent().inject(this);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.home:
                    //??
                    break;
                case R.id.accounts:
                    accountsBottomSheet bottomSheet = new accountsBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "accountsBottomSheet");
                    return false;
            }
            return true;
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = homeFragment.newInstance();
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }

        userManager.attachListener(this);
    }


    //for bottomSheet account switching
    @Override
    public void onButtonClicked(String text) {
        if (text.equals("Add account")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, constants.ACCOUNT_LOGIN_REQUEST_CODE);
        } else {
            userManager.setCurrentUser(text, null);
            reloadContent();
        }
    }

    //account switching after new login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == constants.ACCOUNT_LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            reloadContent();
        }
    }

    private void reloadContent() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof homeFragment) {
            ((homeFragment) fragment).reload();
        }
    }

    @Override
    public void currentAccountRemoved() {
        reloadContent();
    }
}
