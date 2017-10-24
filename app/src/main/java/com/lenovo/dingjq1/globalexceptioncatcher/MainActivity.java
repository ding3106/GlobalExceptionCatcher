package com.lenovo.dingjq1.globalexceptioncatcher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "dingjq1";
    private String mStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.print(mStr.equals("prit Anything"));

//        Thread.UncaughtExceptionHandler
    }
}
