package com.example.synchess;

import android.app.Application;
import androidx.lifecycle.ViewModelProvider;

public class ChessApplication extends Application {
    private ChessViewModel sharedViewModel;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedViewModel = new ViewModelProvider.AndroidViewModelFactory(this).create(ChessViewModel.class);
    }

    public ChessViewModel getSharedViewModel() {
        return sharedViewModel;
    }
}