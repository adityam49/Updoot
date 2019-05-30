package com.ducktapedapps.updoot.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.ui.adapters.submissionsAdapter;
import com.ducktapedapps.updoot.viewModels.submissionsVM;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private submissionsAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private submissionsVM viewModel;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = findViewById(R.id.progress_circular);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new submissionsAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = ViewModelProviders.of(this).get(submissionsVM.class);


        viewModel.getState().observe(this, state -> {
            switch (state) {
                case LOADING:
                    break;
                case ERROR:
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        });
        viewModel.getAllSubmissions().observe(this, things -> {
            Log.i(TAG, "onChanged: ");
            if (things != null)
                adapter.addSubmissions(things);
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
