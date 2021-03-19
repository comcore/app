package com.gmail.comcorecrew.comcore.classes;

import android.content.Context;

import java.io.File;

/**
 * Singleton class to store data.
 */
public class AppData {

    public static File cacheDir;
    public static File filesDir;

    /**
     * Init method that should be run when app is opened.
     *
     * @param context App context
     */
    public static void init(Context context) {
        cacheDir = context.getCacheDir();
        filesDir = context.getFilesDir();
    }
}
