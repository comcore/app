package com.gmail.comcorecrew.comcore.classes;

import android.app.Application;
import android.content.Context;

public class Comcore extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
