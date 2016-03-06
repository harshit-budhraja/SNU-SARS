package com.arachnisapps.sars;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class MainActivity extends AppCompatActivity {

    public Toolbar toolbar;
    String scr = "Main Activity";
    String id, pass,name=null;
    Boolean connected=false,url=false,notif;
    ViewPager viewPager=null;
    SharedPreferences sharedPreferences;
    NotificationManager manager;
    Notification.Builder builder;
    Notification mynotif;
    private Integer tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        builder = new Notification.Builder(MainActivity.this);
        sharedPreferences= getSharedPreferences("MYPREFERENCES", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("connection_status", true);
        editor.apply();

        //NEW ASYNC TASK
        try {
            new myAsyncTask().execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



        setContentView(R.layout.activity_main);
        Intent intent=getIntent();
        try
        {
            id = intent.getStringExtra("username");
            pass = intent.getStringExtra("password");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //NOTIFICATIONS
        manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));


        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        //To build the SHARE BUTTON
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share_intent = new Intent("android.intent.action.SEND");
                share_intent.setType("text/plain");
                share_intent.putExtra("android.intent.extra.SUBJECT", "Hey! Check this out");
                share_intent.putExtra("android.intent.extra.TEXT", "Hey! Marking online attendance has never been so easy for me." +
                        " I'm sure you would find it interesting and easy too. Check out on Google Play:- https://play.google.com/store/apps/details?id=com.arachnisapps.sars ");
                startActivity(Intent.createChooser(share_intent, "Complete action using"));
            }
        });


        /*Reference to the switch to start the service...
        Switch myswitch = (Switch) findViewById(R.id.switch1);
        myswitch.setChecked(false);
        myswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    startService();
                }
                else
                {
                    stopService();
                }
            }
        });
        */
    }

    private class myAsyncTask extends AsyncTask<Void, Void, Void> {

        Document page=null;
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Connection.Response loginform = Jsoup.connect("https://markattendance.webapps.snu.edu.in/public/application/login/login")
                        .method(Connection.Method.GET)
                        .validateTLSCertificates(false)
                        .execute();
               page = Jsoup.connect("https://markattendance.webapps.snu.edu.in/public/application/login/loginAuthSubmit")
                        .data("cookieexists", "false")
                        .data("login_user_name", id)
                        .data("login_password", pass)
                        .validateTLSCertificates(false)
                        .cookies(loginform.cookies())
                        .post();
                if (!(page.title().startsWith("Login"))) connected = true;
                else Toast.makeText(getApplicationContext(),"Network Communication Issues...",Toast.LENGTH_SHORT).show();
                Log.d("Main", page.baseUri());
                if (page.baseUri().equals("https://markattendance.webapps.snu.edu.in/public/application/index/index"))  url=true;
                sharedPreferences= getSharedPreferences("MYPREFERENCES", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("connection_status",(connected && url));
                editor.apply();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if(connected)
                {
                    if(url)
                    {
                        viewPager.setCurrentItem(0);
                        tab=0;
                        Toast.makeText(getApplicationContext(),"Attendance has been initiated. Press MARK MY ATTENDANCE button to mark your attendance.",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        viewPager.setCurrentItem(1);
                        tab=1;
                        Toast.makeText(getApplicationContext(),"No class scheduled for this time-slot / Faculty has not initiated attendance for the class.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch(NullPointerException np)
            {
                np.printStackTrace();
            }
            try {
                String nametable = page.select("div[class=container]").get(1).text();
                name = nametable.substring(nametable.indexOf(" ") + 1, nametable.indexOf("["));
            }
            catch(NullPointerException np)
            {
                np.printStackTrace();
            }


            try {
                //App Notifications
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(),MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                builder.setAutoCancel(true);
                builder.setTicker("You have a new notification.");
                builder.setContentTitle("SARS");
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLargeIcon(largeIcon);
                builder.setContentIntent(pendingIntent);
                builder.setOngoing(false);
                builder.setDefaults(-1);
                builder.setSubText("Mark your course attendance.");
                builder.setNumber(1);
                builder.build();
                if (name != null) builder.setContentText("Hey," + name);
                mynotif = builder.getNotification();
            }
            catch (Exception e)
            {
                    e.printStackTrace();
            }

            try {
                //SHARED PREFERENCES FOR NOTIFICATIONS
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                notif = sharedpreferences.getBoolean("notifications", true);
                if (notif && tab == 0) {
                    manager.notify(11, mynotif);
                }
            }
            catch(NullPointerException np)
            {
                np.printStackTrace();
            }
            checkFirstRun();
        }
    }

    public void checkFirstRun()
    {
        boolean isFirstRun = this.getSharedPreferences("MYPREFERENCES", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun){
            // Place your dialog code here to display the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            try {
                builder.setMessage("Welcome, " + name)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                            }
                        });
            }
            catch(NullPointerException np)
            {
                np.printStackTrace();
            }
            AlertDialog alert = builder.create();
            alert.show();

            this.getSharedPreferences("MYPREFERENCES", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent intent = new Intent(MainActivity.this,AboutActivity.class);
            startActivity(intent);
            return true;
        }

        else if (id == R.id.action_forget)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            SharedPreferences sharedPreferences = getSharedPreferences("MYPREFERENCES",0);
                            SharedPreferences.Editor editor= sharedPreferences.edit();
                            editor.remove("username");
                            editor.remove("password");
                            editor.remove("isFirstRun");
                            editor.remove("connection_status");
                            editor.remove("notifications");
                            editor.apply();
                            Toast.makeText(getApplicationContext(),(CharSequence)"You are logged out successfully!!",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            dialog.dismiss();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage((CharSequence)"Clear all app data and Logout ?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
            return true;
        }

        else if(id==R.id.action_refresh)
        {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            return true;
        }
        else if(id==R.id.action_settings)
        {
            Intent intent = new Intent(MainActivity.this,Prefer.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

    public void selectFragment(int position){
        viewPager.setCurrentItem(position, true);
        // true is to animate the transaction
    }

    // Start the service
    public void startService() {
        startService(new Intent(this, NotificationService.class));
    }

    // Stop the service
    public void stopService() {
        stopService(new Intent(this, NotificationService.class));
    }

}
