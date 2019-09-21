package com.ducktapedapps.updoot.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.ui.fragments.accountsBottomSheet;
import com.ducktapedapps.updoot.ui.fragments.subredditFragment;
import com.ducktapedapps.updoot.utils.accountManagement.userManager;
import com.ducktapedapps.updoot.utils.accountManagement.userManager.AccountChangeListener;
import com.ducktapedapps.updoot.utils.constants;
import com.ducktapedapps.updoot.viewModels.ActivityVM;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements accountsBottomSheet.BottomSheetListener, AccountChangeListener {
    private static final String TAG = "MainActivity";

    private ActivityVM viewModel;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bottom_navigation_bar)
    BottomNavigationView bottomNavigationView;

    @Inject
    userManager userManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: ");
        ((UpdootApplication) getApplication()).getUpdootComponent().inject(this);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            // empty string for subreddit gives frontpage as defined bu reddit api
            fragment = subredditFragment.newInstance("", true);
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragmentContainer, fragment, String.valueOf(0))
                    .commit();
        }

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
                    if (visibleFragment instanceof subredditFragment) {
                        ((subredditFragment) visibleFragment).inflateSortPopup(findViewById(R.id.sort));
                    }
                case R.id.more:
                default:
                    return false;
            }
        });

        userManager.attachListener(this);

        viewModel = new ViewModelProvider(this).get(ActivityVM.class);
    }


    //for bottomSheet account switching
    @Override
    public void onButtonClicked(String text) {
        if (text.equals("Add account")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, constants.ACCOUNT_LOGIN_REQUEST_CODE);
        } else {
            userManager.setCurrentUser(text, null);
            viewModel.getCurrentAccount().setValue(userManager.getCurrentUser().name);
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
        viewModel.getCurrentAccount().setValue(userManager.getCurrentUser().name);
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
