package com.coheser.app.activitesfragments.argear.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.coheser.app.Constants;
import com.coheser.app.activitesfragments.argear.api.ContentsResponse;

public class ContentsViewModel extends AndroidViewModel {

    private final MutableLiveData<ContentsResponse> mutableLiveData;
    private final ContentsRepository contentsRepository;

    public ContentsViewModel(Application application) {
        super(application);
        contentsRepository = ContentsRepository.getInstance();
        mutableLiveData = contentsRepository.getContents(Constants.API_KEY_ARGEAR);
    }

    public LiveData<ContentsResponse> getContents() {
        return mutableLiveData;
    }
}
