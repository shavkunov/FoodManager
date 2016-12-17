package ru.spbau.cloudmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivityTAG";
    public final static String successfulMessage = "Upload is successful";
    public final static String failMessage = "Upload failed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Log.d(TAG, "Started to setup cloud source");
            CloudManager manager = new CloudManager(this);
            manager.setupCloudServices();

            Log.d(TAG, successfulMessage);
            Toast.makeText(this, successfulMessage, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            Log.d(TAG, failMessage);
            Toast.makeText(this, failMessage, Toast.LENGTH_LONG).show();
        }
    }
}