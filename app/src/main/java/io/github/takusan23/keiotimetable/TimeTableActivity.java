package io.github.takusan23.keiotimetable;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.keiotimetable.Adapter.ListAdapter;
import io.github.takusan23.keiotimetable.Adapter.ListItem;
import io.github.takusan23.keiotimetable.Fragment.TimeTableFragment;
import io.github.takusan23.keiotimetable.Utilities.ArrayListSharedPreferences;

public class TimeTableActivity extends AppCompatActivity {

    private ListView listView;
    private String up_url;
    private String name;
    private ArrayList<ListItem> arrayList;
    private ListAdapter adapter;
    private SpeedDialView speedDialView;
    private SharedPreferences pref_setting;

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        up_url = getIntent().getStringExtra("URL");
        name = getIntent().getStringExtra("name");

        //Fragment設置
        Bundle bundle = new Bundle();
        bundle.putString("URL",up_url);
        bundle.putString("name",name);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TimeTableFragment timeTableFragment = new TimeTableFragment();
        timeTableFragment.setArguments(bundle);
        transaction.replace(R.id.activity_time_table_linearlayout, timeTableFragment);
        //戻れるようにする
        transaction.commit();

    }


}


