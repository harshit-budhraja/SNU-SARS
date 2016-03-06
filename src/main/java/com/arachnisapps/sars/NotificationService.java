package com.arachnisapps.sars;

import android.app.Service;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class NotificationService extends Service {


    String id, pass;
    private Boolean connected = false;

    public void MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        SharedPreferences sharedPreferences = getSharedPreferences("MYPREFERENCES", 0);
        try {
            id = sharedPreferences.getString("username", null);
            pass = sharedPreferences.getString("password", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        // Perform your long running operations here.
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
    }
}
