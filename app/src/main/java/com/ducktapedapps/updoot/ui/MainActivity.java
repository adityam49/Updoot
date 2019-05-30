package com.ducktapedapps.updoot;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ducktapedapps.updoot.api.endPoint;
import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.model.tempModel;
import com.ducktapedapps.updoot.utils.auth;
import com.ducktapedapps.updoot.utils.retrofitClientGenerator;
import com.ducktapedapps.updoot.utils.tokenManager;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth
                .userLess(this)
                .doAfterSuccess(token -> tokenManager.saveToken(token, this))
                .flatMap(token -> getPosts(token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<tempModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(tempModel tempModel) {
                        Log.i(TAG, "onSuccess: " + tempModel);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                        Toast.makeText(MainActivity.this, "There seems to be some problem", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static Single<tempModel> getPosts(Token token) {
        Retrofit retrofit = retrofitClientGenerator.createForEndPoints(token);
        endPoint endPoint = retrofit.create(com.ducktapedapps.updoot.api.endPoint.class);
        return endPoint
                .getFrontPage();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
