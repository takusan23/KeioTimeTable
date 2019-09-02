package io.github.takusan23.keiotimetable;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.takusan23.keiotimetable.Fragment.StationListFragment;
import io.github.takusan23.keiotimetable.Fragment.TimeTableFragment;
import io.github.takusan23.keiotimetable.Utilities.ArrayListSharedPreferences;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences pref_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //お気に入りメニュー
        loadMenu();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_fragment, new StationListFragment());
        transaction.commit();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.station_list_menu) {
            changeFragment(new StationListFragment());
        } else if (id == R.id.favourite_clear) {
            //削除コード
            new AlertDialog.Builder(this)
                    .setTitle("お気に入り駅全削除")
                    .setMessage("お気に入り登録した駅が全て削除されます。")
                    .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayListSharedPreferences.saveArrayListSharedPreferences(new ArrayList<String>(), "favourite_name", pref_setting);
                            ArrayListSharedPreferences.saveArrayListSharedPreferences(new ArrayList<String>(), "favourite_url", pref_setting);
                            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                            navigationView.getMenu().clear();
                            navigationView.inflateMenu(R.menu.activity_main_drawer);
                            Toast.makeText(MainActivity.this, "削除しました", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("キャンセル", null)
                    .show();
        } else if (id == R.id.station_dl_menu) {
            changeFragment(new SQLiteView());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //フラグメント
    private void changeFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_fragment, fragment);
        transaction.commit();
    }

    //フラグメント２
    private void changeTimeTableFragment(String name, String url) {
        //URL と 名前
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        bundle.putString("name", name);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TimeTableFragment timeTableFragment = new TimeTableFragment();
        timeTableFragment.setArguments(bundle);
        transaction.replace(R.id.home_fragment, timeTableFragment);
        //戻れるようにする
        transaction.addToBackStack("");
        transaction.commit();
    }

    //お気に入りロード
    private void loadMenu() {
        final ArrayList<String> name = ArrayListSharedPreferences.loadSharedPreferencesArrayList("favourite_name", pref_setting);
        final ArrayList<String> url = ArrayListSharedPreferences.loadSharedPreferencesArrayList("favourite_url", pref_setting);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < name.size(); i++) {
            //なにもないとGroupが出てこない（要検証）
            final int finalI = i;
            //クリックイベントも同時に実装
            menu.add(0, i, 0, name.get(i)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    //Fragment
                    changeTimeTableFragment(name.get(finalI), url.get(finalI));
                    return false;
                }
            });
        }
    }
}
