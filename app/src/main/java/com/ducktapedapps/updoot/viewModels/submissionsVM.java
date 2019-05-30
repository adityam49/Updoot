package com.ducktapedapps.updoot.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ducktapedapps.updoot.model.ListingData;
import com.ducktapedapps.updoot.model.thing;
import com.ducktapedapps.updoot.repository.submissionsRepo;
import com.ducktapedapps.updoot.utils.constants;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class submissionsVM extends AndroidViewModel {

    private CompositeDisposable disposable = new CompositeDisposable();
    private static final String TAG = "submissionsVM";
    private submissionsRepo frontPageRepo;
    private MutableLiveData<List<thing>> allSubmissions = new MutableLiveData<>();
    private MutableLiveData<constants.state> state = new MutableLiveData<>();

    public MutableLiveData<List<thing>> getAllSubmissions() {
        return allSubmissions;
    }

    public MutableLiveData<constants.state> getState() {
        return state;
    }

    public submissionsVM(Application application) {
        super(application);
        frontPageRepo = new submissionsRepo(application);

        disposable.add(frontPageRepo.fetchFrontPage(null)
                .map(thing -> {
                    if (thing.getData() instanceof ListingData) {
                        return ((ListingData) thing.getData()).getChildren();
                    } else throw new Exception("unsupported response");
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(__ -> state.postValue(constants.state.LOADING))
                .subscribe(submissions -> {
                            Log.i(TAG, "submissionsVM: " + submissions);
                            allSubmissions.postValue(submissions);
                            state.postValue(constants.state.SUCCESS);
                        }
                        , throwable -> {
                            Log.e(TAG, "submissionsVM: " + throwable.getMessage());
                            state.postValue(constants.state.ERROR);
                        }
                ));
    }

    @Override
    protected void onCleared() {
        if (!disposable.isDisposed()) {
            disposable.clear();
        }
        super.onCleared();
        Log.i(TAG, "onCleared: ");
    }
}
