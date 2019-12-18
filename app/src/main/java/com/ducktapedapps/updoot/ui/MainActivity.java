package com.ducktapedapps.updoot.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.databinding.ActivityMainBinding;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.utils.Constants;
import com.ducktapedapps.updoot.utils.accountManagement.UserManager;
import com.ducktapedapps.updoot.utils.accountManagement.UserManager.AccountChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements AccountsBottomSheetDialogFragment.BottomSheetListener, AccountChangeListener {
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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()) {
                case R.id.SubredditDestination:
                    if (arguments != null) {
                        String title = arguments.getString("r/subreddit");
                        if (title != null) {
                            binding.toolbar.setTitle(title);
                        } else {
                            binding.toolbar.setTitle(getString(R.string.app_name));
                        }
                    }
                    break;
                case R.id.CommentsDestination:
                    if (arguments != null) {
                        LinkData data = (LinkData) arguments.getSerializable("SubmissionData");
                        if (data != null) {
                            if (data.getCommentsCount() != 0) {
                                if (data.getCommentsCount() <= 999)
                                    binding.toolbar.setTitle(data.getCommentsCount() + " comments");
                                else
                                    binding.toolbar.setTitle(data.getCommentsCount() / 1000 + "k comments");
                            }
                        }
                    } else
                        binding.toolbar.setTitle(getString(R.string.Comments));
                    break;
            }
        });

        BottomNavigationView bottomNavigationView = binding.bottomNavigationBar;
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.home:
                    navController.popBackStack(R.id.SubredditDestination, true);
                    return true;

                case R.id.accounts:
                    navController.navigate(R.id.AccountsBottomSheetDialog);
                    return false;
                case R.id.sort:
                case R.id.more:
                    navController.navigate(R.id.settingsFragment);
                    return true;
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
}
