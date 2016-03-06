package com.arachnisapps.sars;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class PageFragment2 extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private String tableHTML = null;
    String id, pass;
    Boolean connected;
    private int mPage;
    View view=null;
    SharedPreferences sharedPreferences;

    public static PageFragment2 newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment2 fragment = new PageFragment2();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MYPREFERENCES",0);
        this.id = sharedPreferences.getString("username", null);
        this.pass = sharedPreferences.getString("password", null);
        this.connected = sharedPreferences.getBoolean("connection_status",false);
        new myAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragmentpage2, container, false);
        ((AppCompatActivity)this.getActivity()).getSupportActionBar().setBackgroundDrawable((Drawable)new ColorDrawable(Color.parseColor((String)"#0431B4")));
        return view;
    }

    private class myAsyncTask extends AsyncTask<Void, Void, Void> {

        Document page = null;

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
                page = Jsoup.connect("https://markattendance.webapps.snu.edu.in/public/application/index/summary")
                        .cookies(loginform.cookies())
                        .get();
                Log.d("Main", page.title());
                if (!(page.title().startsWith("Login"))) connected = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TextView naam = (TextView) view.findViewById(R.id.name);
            TextView roll = (TextView) view.findViewById(R.id.rno);
            WebView wv = (WebView) view.findViewById(R.id.webView1);
            wv.getSettings().setSupportZoom(true);
            wv.getSettings().setSaveFormData(true);
            wv.getSettings().setBuiltInZoomControls(true);
            //wv.getSettings().setJavaScriptEnabled(true);
            wv.setWebViewClient(new WebViewClient());
            wv.setInitialScale(140);
            if(connected)
            {
                try{
                    Element table = page.select("table[class=table table-bordered table-condensed table-striped]").first();
                    tableHTML = table.html();
                    tableHTML = "<table>" + tableHTML + "</table>";
                    wv.loadDataWithBaseURL(null, tableHTML, "text/html", "utf-8", null);
                    String nametable = page.select("div[class=container]").get(1).text();
                    String name = nametable.substring(nametable.indexOf(" ") + 1, nametable.indexOf("["));
                    String rno = nametable.substring(nametable.indexOf("["), nametable.indexOf("]") + 1);
                    naam.setText((CharSequence)"Name:- " + name);
                    roll.setText((CharSequence)"Roll Number:- " + rno);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(getContext(),(CharSequence)"(2)AN internal error has made the app to force exit...Please try again ", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getContext(),(CharSequence)"(2)Network Communication Issues...Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}