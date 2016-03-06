package com.arachnisapps.sars;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class PageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    private String tableTXT = null;
    String id, pass;
    Boolean connected;
    View view=null;
    TextView course1=null, venue1=null,start1=null,end1=null;
    Button btn;
    private Connection.Response loginform;
    private ValueAnimator animator = null;
    private TextView attstats,table;

    private ValueAnimator blink(TextView textView, ValueAnimator valueAnimator, boolean bl) {
        if(bl) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt((Object) textView, (String) "Visibility", (int[]) new int[]{0, 8});
            objectAnimator.setDuration(1800);
            objectAnimator.setEvaluator((TypeEvaluator) new ArgbEvaluator());
            objectAnimator.setRepeatCount(-1);
            objectAnimator.start();
            return objectAnimator;
        }
        return valueAnimator;
    }

    public void onInitiation(Boolean stat) {
        if(stat) {
            try {
                this.attstats.setTextColor(getResources().getColor(R.color.Blue));
                this.attstats.setText((CharSequence) "ATTENDANCE INITIATED");
                this.animator = this.blink(this.attstats, this.animator, true);
                table.setBackgroundColor(Color.parseColor("#58FA58"));
                ((AppCompatActivity) this.getActivity()).getSupportActionBar().setBackgroundDrawable((Drawable) new ColorDrawable(Color.parseColor((String) "#088A08")));
            } catch (NullPointerException np) {
                np.printStackTrace();
            }
        }
        else
        {
            this.attstats.setTextColor(getResources().getColor(R.color.OrangeRed));
            this.attstats.setText((CharSequence) "ATTENDANCE NOT INITIATED");
        }
    }

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new myAsyncTask().execute();
        View view = inflater.inflate(R.layout.fragmentpage, container, false);
        ((AppCompatActivity)this.getActivity()).getSupportActionBar().setBackgroundDrawable((Drawable)new ColorDrawable(Color.parseColor((String)"#0431B4")));
        this.attstats = (TextView) view.findViewById(R.id.attstatus);
        this.table = (TextView) view.findViewById(R.id.table_header);
        this.attstats.setTypeface(Typeface.MONOSPACE);
        try {
            course1 = (TextView) view.findViewById(R.id.textView1);
            venue1 = (TextView) view.findViewById(R.id.textView2);
            start1 = (TextView) view.findViewById(R.id.textView3);
            end1 = (TextView) view.findViewById(R.id.textView4);
            btn = (Button) view.findViewById(R.id.mark);
            btn.setVisibility(View.GONE);
            course1.setText((CharSequence) "Course:- NO CLASSES SCHEDULED");
            venue1.setText((CharSequence) "Venue:- N.A.");
            start1.setText((CharSequence) "Start time:- N.A.");
            end1.setText((CharSequence) "End time:- N.A.");
        }
        catch(NullPointerException np)
        {
            np.printStackTrace();
        }
        return view;
    }
    private class myAsyncTask extends AsyncTask<Void, Void, Void> {

        Document page = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                loginform = Jsoup.connect("https://markattendance.webapps.snu.edu.in/public/application/login/login")
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
                if (!(page.title().startsWith("Login")) && page.location().matches("https://markattendance.webapps.snu.edu.in/public/application/index/index")) connected = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            onInitiation(connected);
            final ProgressDialog dialog = new ProgressDialog(getContext());
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(connected)
                    {
                        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        dialog.setMessage("Please Wait...");
                        dialog.setIndeterminate(true);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        new markAttendance().execute();
                    }
                }
            });

            if(connected)
            {
                try{
                String course = page.select("td").get(0).text();
                String venue = page.select("td").get(1).text();
                String start = page.select("td").get(2).text();
                String end = page.select("td").get(3).text();
                    course1.setText((CharSequence) course);
                    venue1.setText((CharSequence) venue);
                    start1.setText((CharSequence)start);
                    end1.setText((CharSequence) end);
                    btn.setVisibility(View.VISIBLE);

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                //Toast.makeText(getContext(),"(1)Network Communication Issues...Please try again later.", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).selectFragment(1);
                course1.setText((CharSequence) "Course:- NO CLASSES SCHEDULED");
                venue1.setText((CharSequence) "Venue:- N.A.");
                start1.setText((CharSequence) "Start time:- N.A.");
                end1.setText((CharSequence) "End time:- N.A.");
            }
        }
    }

    public class markAttendance extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            /*try {
                Connection.Response connect = Jsoup.connect("https://markattendance.webapps.snu.edu.in/public/application/index/submit_attendance")
                        .method(Connection.Method.GET)
                        .validateTLSCertificates(false)
                        .cookies(loginform.cookies())
                        .execute();

            }
            catch(IOException io)
            {
                io.printStackTrace();
            }*/

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getContext(),"BUTTON NOT WORKING YET",Toast.LENGTH_SHORT).show();
        }
    }
}
