package com.ducktapedapps.updoot.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.databinding.ActivityMainBinding;
import com.ducktapedapps.updoot.ui.fragments.SubredditFragment;
import com.ducktapedapps.updoot.ui.fragments.accountsBottomSheet;
import com.ducktapedapps.updoot.utils.Constants;
import com.ducktapedapps.updoot.utils.accountManagement.UserManager;
import com.ducktapedapps.updoot.utils.accountManagement.UserManager.AccountChangeListener;
import com.ducktapedapps.updoot.viewModels.ActivityVM;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements accountsBottomSheet.BottomSheetListener, AccountChangeListener {
    private static final String TAG = "MainActivity";

    private ActivityVM viewModel;

    @Inject
    UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ((UpdootApplication) getApplication()).getUpdootComponent().inject(this);

        setSupportActionBar(binding.toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            // empty string for subreddit gives frontpage as defined bu reddit api
            fragment = SubredditFragment.newInstance("", true);
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, String.valueOf(0))
                    .commit();
        }

        BottomNavigationView bottomNavigationView = binding.bottomNavigationBar;
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.home:
                    if (bottomNavigationView.getSelectedItemId() == R.id.sort) {
                        return true;
                    } else {
                        getSupportFragmentManager()
                                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        return true;
                    }

                case R.id.accounts:
                    accountsBottomSheet bottomSheet = new accountsBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "accountsBottomSheet");
                    return false;

                case R.id.sort:
                    Fragment visibleFragment = getVisibleFragment();
                    if (visibleFragment instanceof SubredditFragment) {
                        ((SubredditFragment) visibleFragment).inflateSortPopup(findViewById(R.id.sort));
                    }
                case R.id.more:
                default:
                    return false;
            }
        });

        userManager.attachListener(this);

        viewModel = new ViewModelProvider(this).get(ActivityVM.class);
    }

    //for bottomSheet Account switching
    @Override
    public void onButtonClicked(String text) {
        if (text.equals("Add Account")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, Constants.ACCOUNT_LOGIN_REQUEST_CODE);
        } else {
            userManager.setCurrentUser(text, null);
            viewModel.setCurrentAccount(userManager.getCurrentUser().name);
        }
    }

    //Account switching after new login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ACCOUNT_LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            reloadContent();
        }
    }

    private void reloadContent() {
        viewModel.setCurrentAccount(userManager.getCurrentUser().name);
    }

    @Override
    public void onCurrentAccountRemoved() {
        reloadContent();
    }

    private Fragment getVisibleFragment() {
        String topFragmentTag = String.valueOf(getSupportFragmentManager().getBackStackEntryCount());
        return getSupportFragmentManager().findFragmentByTag(topFragmentTag);
    }
}
