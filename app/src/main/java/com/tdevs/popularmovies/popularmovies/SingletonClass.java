package com.tdevs.popularmovies.popularmovies;

import android.content.Context;


public class SingletonClass {
    private static SingletonClass ourInstance = new SingletonClass();
    private Context appContenxt;

    public static SingletonClass getInstance() {
        return ourInstance;
    }

    private SingletonClass() {
    }

    public void setContext(Context context)
    {
        appContenxt = context;
    }
    public Context getContext()
    {
        return appContenxt;
    }
}
