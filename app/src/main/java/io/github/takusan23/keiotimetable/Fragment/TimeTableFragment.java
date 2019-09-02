package io.github.takusan23.keiotimetable.Fragment;

import android.app.LauncherActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;

import io.github.takusan23.keiotimetable.Adapter.ListAdapter;
import io.github.takusan23.keiotimetable.Adapter.ListItem;
import io.github.takusan23.keiotimetable.R;
import io.github.takusan23.keiotimetable.TimeTableActivity;
import io.github.takusan23.keiotimetable.Utilities.ArrayListSharedPreferences;
import io.github.takusan23.keiotimetable.SQLiteTimeTable;

public class TimeTableFragment extends Fragment {

    private ListView listView;
    private TabLayout tablayout;
    private String up_url;
    private String name;
    private ArrayList<ListItem> arrayList;
    private ListAdapter adapter;
    private SpeedDialView speedDialView;
    private SharedPreferences pref_setting;
    private String finalURL;

    private SQLiteTimeTable helper;
    private SQLiteDatabase sqLiteDatabase;

    private ArrayList<String> text_ArrayList = new ArrayList<>();
    private ArrayList<String> url_ArrayList = new ArrayList<>();
    private ArrayList<String> css_1_ArrayList = new ArrayList<>();
    private ArrayList<String> css_2_ArrayList = new ArrayList<>();
    private ArrayList<String> hour_ArrayList = new ArrayList<>();
    private ArrayList<String> minute_ArrayList = new ArrayList<>();

    private boolean offline_mode = false;
    //上り？
    private boolean up_train = true;
    //休み？
    private boolean weak_train = false;

    //時間だけのリスト
    private ArrayList<String> hourList = new ArrayList<String>();
    //その他の
    private ArrayList<String> textList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_table, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {

        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());

        listView = view.findViewById(R.id.time_table_listview);
        speedDialView = view.findViewById(R.id.speedDial);
        tablayout = view.findViewById(R.id.timetable_tablayout);

        up_url = getArguments().getString("URL");
        name = getArguments().getString("name");
        finalURL = up_url;

        if (helper == null) {
            helper = new SQLiteTimeTable(getContext());
        }
        if (sqLiteDatabase == null) {
            sqLiteDatabase = helper.getWritableDatabase();
        }


        //TabLayoutを選んだときとか
        tablayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //時刻表の時刻が選べるように
                ArrayList<ListItem> timelineItemList = new ArrayList<>();
                for (int i = 0; i < hourList.size(); i++) {
                    String text = hourList.get(i).trim();
                    if (text.equals(tab.getText().toString())) {
                        timelineItemList.add(adapter.getItem(i));
                    }
                }
                ListAdapter tmpAadapter = new ListAdapter(getContext(), R.layout.listview_layout, timelineItemList);
                listView.setAdapter(tmpAadapter);
                tmpAadapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //SpeedDialとか
        speedDialView.setMainFabClosedBackgroundColor(Color.parseColor("#64c1ff"));
        speedDialView.setMainFabOpenedBackgroundColor(Color.parseColor("#0064b7"));
        //speedDialView.inflate(R.menu.speed_dial_menu);
        //IDは不明　speed_dial_menu.xmlで作ったのをこっちで使ってるだけ
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.up_down_menu, R.drawable.ic_arrow_upward_white_24dp)
                .setFabBackgroundColor(colorCodeToInt("#001064"))
                .setLabel("新宿方面 → 京王八王子・高尾山口方面")
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.day_menu, R.drawable.ic_work_white_24dp)
                .setFabBackgroundColor(colorCodeToInt("#ff9e40"))
                .setLabel("平日 → 土日、休日")
                .create());
        //登録
        //配列取得
        ArrayList<String> station_name = ArrayListSharedPreferences.loadSharedPreferencesArrayList("favourite_name", pref_setting);
        //同じものがあったら削除
        if (station_name.contains(name)) {
            speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.station_add_menu, R.drawable.ic_remove_white_24dp)
                    .setFabBackgroundColor(colorCodeToInt("#8e0000"))
                    .setLabel("駅をお気に入り解除")
                    .create());
        } else {
            speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.station_add_menu, R.drawable.ic_favorite_border_white_24dp)
                    .setFabBackgroundColor(colorCodeToInt("#8e0000"))
                    .setLabel("駅をお気に入り登録")
                    .create());
        }
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.download_menu, R.drawable.ic_file_download_white_24dp)
                .setFabBackgroundColor(colorCodeToInt("#005005"))
                .setLabel("ダウンロード")
                .create());


        //クリックイベント
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.up_down_menu:
                        if (actionItem.getLabel(getContext()).contains("新宿方面 → 京王八王子・高尾山口方面")) {
                            //下りURL
                            up_url = up_url.replace("d=1", "d=2");
                            if (offline_mode) {
                                loadSQLiteTimeTable("down");
                            } else {
                                getHTMLAndPerse(up_url);
                            }
                            up_train = false;
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.up_down_menu, "京王八王子・高尾山口方面 → 新宿方面", R.drawable.ic_arrow_downward_white_24dp);
                        } else {
                            //URL
                            up_url = up_url.replace("d=2", "d=1");
                            if (offline_mode) {
                                loadSQLiteTimeTable("up");
                            } else {
                                getHTMLAndPerse(up_url);
                            }
                            up_train = true;
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.up_down_menu, "新宿方面 → 京王八王子・高尾山口方面", R.drawable.ic_arrow_upward_white_24dp);
                        }
                        break;
                    case R.id.day_menu:
                        if (actionItem.getLabel(getContext()).contains("平日 → 土日、休日")) {
                            //下りURL
                            up_url = up_url.replace("dw=0", "dw=1");
                            if (offline_mode) {
                                //上りの休日
                                if (up_train) {
                                    loadSQLiteTimeTable("up_holiday");
                                } else {
                                    loadSQLiteTimeTable("down_holiday");
                                }
                            } else {
                                getHTMLAndPerse(up_url);
                            }
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.day_menu, "土日、休日 → 平日", R.drawable.ic_supervisor_account_white_24dp);
                        } else {
                            //URL
                            up_url = up_url.replace("dw=1", "dw=0");
                            if (offline_mode) {
                                //上りの休日
                                if (up_train) {
                                    loadSQLiteTimeTable("up");
                                } else {
                                    loadSQLiteTimeTable("down");
                                }
                            } else {
                                getHTMLAndPerse(up_url);
                            }
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.day_menu, "平日 → 土日、休日", R.drawable.ic_work_white_24dp);
                        }
                        break;
                    case R.id.station_add_menu:
                        //登録
                        //配列取得
                        ArrayList<String> station_name = ArrayListSharedPreferences.loadSharedPreferencesArrayList("favourite_name", pref_setting);
                        ArrayList<String> station_url = ArrayListSharedPreferences.loadSharedPreferencesArrayList("favourite_url", pref_setting);
                        //配列追加
                        //同じものがあったら削除
                        if (station_name.contains(name)) {
                            station_name.remove(name);
                            station_url.remove(up_url);
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.station_add_menu, "駅をお気に入り解除", R.drawable.ic_remove_white_24dp);
                        } else {
                            station_name.add(name);
                            station_url.add(up_url);
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.station_add_menu, "駅をお気に入り登録", R.drawable.ic_favorite_border_white_24dp);
                        }
                        //保存
                        ArrayListSharedPreferences.saveArrayListSharedPreferences(station_name, "favourite_name", pref_setting);
                        ArrayListSharedPreferences.saveArrayListSharedPreferences(station_url, "favourite_url", pref_setting);
                        break;
                    case R.id.download_menu:
                        Toast.makeText(getContext(), "ダウンロード開始", Toast.LENGTH_SHORT).show();
                        //平日の上り、下りのみ対応させる
                        saveSQLite(getArguments().getString("URL"), "up");
                        break;
                }

                return false;
            }
        });

        //ListView
        arrayList = new ArrayList<>();
        adapter = new ListAdapter(getContext(), R.layout.listview_layout, arrayList);

        //データ取得
        if (checkSQLiteTimeTableData(this.name) != 0) {
            offline_mode = true;
            up_train = true;
            loadSQLiteTimeTable("up");
            setDownloadSpeedDial();
        } else {
            getHTMLAndPerse(up_url);
            up_train = true;
        }

        listView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //２回送ってくる
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    //選択ボタン投下時
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER){
                        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.activity_time_table_linearlayout);
                        if(fragment instanceof TimeTableFragment){
                            ((TimeTableFragment) fragment).okKeyDown();
                        }
                    }
                }
                return false;
            }
        });

    }

    private void setTitleUIThread(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(message);
            }
        });
    }

    private void setSubTitleUIThread(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(message);
            }
        });
    }

    private int colorCodeToInt(String colorCode) {
        return Color.parseColor(colorCode);
    }

    private void setItemTitleIcon(SpeedDialActionItem item, int id, String title, int drawable) {
        //タイトル切り替え
        speedDialView.replaceActionItem(item,
                new SpeedDialActionItem.Builder(id, drawable)
                        .setFabBackgroundColor(item.getFabBackgroundColor())
                        .setLabel(title)
                        .create());
    }

    private void getHTMLAndPerse(final String url) {
        finalURL = url;
        adapter.clear();
        setTitleUIThread("🚃🕗読み込み中");
        //サブタイトル設定
        String title = "";
        if (url.contains("&d=1")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("方面 : 新宿方面");
            title = "方面 : 新宿方面";
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("方面 : 京王八王子・高尾山口方面");
            title = "方面 : 京王八王子・高尾山口方面";
        }
        //平日　休日
        if (url.contains("&dw=0")) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(title + " / 曜日 : 平日");
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(title + " / 曜日 : 休日");
        }
        //ネットワークは非同期処理
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... aVoid) {

                try {
                    //時刻表URL
                    Document doc = Jsoup.connect(url).get();
                    //HTML Table
                    //なんか３個めで行けた
                    final Element tables = doc.select("table").get(3);
                    final Elements tr = tables.select("tr");
                    //タイトル用
                    setTitleUIThread(tr.get(0).select("td").text());
                    for (int i = 3; i < tr.size(); i++) {
                        final int finalI = i;
                        //Tableの要素
                        final Elements td = tr.get(finalI).select("td");
                        //時間取り出し
                        //Class
                        //平日　weekday
                        //休日　holiday
                        String class_name = "weekday";
                        if (url.contains("&dw=0")) {
                            class_name = "weekday";
                        } else {
                            class_name = "holiday";
                        }
                        final Elements time = tr.get(finalI).getElementsByClass(class_name);

                        //到着取り出し
                        final Elements time_td = tr.get(finalI).getElementsByClass("jikokuhyo");

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String hour = "";
                                String minute = "";

                                //余分にforが回っている
                                //時刻表最後まで終わったら終了するようにする
                                if (time.text().length() > 0) {
                                    hour = time.text() + "時 ";
                                    //到着
                                    for (int train = 0; train < time_td.size(); train++) {
                                        hourList.add(hour);
                                        minute = time_td.get(train).text() + "分";
                                        //Class(CSS)取得→各駅、区間急行等
                                        String css = time_td.get(train).select("a").get(0).select("span").get(0).className();
                                        String css_2nd = time_td.get(train).select("a").get(0).select("span").select("span").get(1).className();
                                        //電車URL
                                        String train_info = time_td.get(train).select("a").attr("href");
                                        //いろいろ
                                        String text = hour + minute.replace("(", "").replace(")", "");

                                        //Adapter用List
                                        ArrayList<String> item = new ArrayList<>();
                                        item.add("time_table_list");
                                        item.add(text);
                                        item.add("");
                                        item.add("https://keio.ekitan.com/sp/" + train_info);
                                        item.add(css);
                                        item.add(css_2nd);
                                        ListItem listItem = new ListItem(item);
                                        adapter.add(listItem);
                                        //listView.setAdapter(adapter);
                                    }
                                }
                                //今の時間の時刻表を出す
                                Calendar calendar = Calendar.getInstance();
                                int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
                                if (nowHour >= 4) {
                                    //時刻表の時刻が選べるように
                                    ArrayList<ListItem> timelineItemList = new ArrayList<>();
                                    for (int i = 0; i < hourList.size(); i++) {
                                        String text = hourList.get(i).trim();
                                        if (text.equals(String.valueOf(nowHour) + "時")) {
                                            timelineItemList.add(adapter.getItem(i));
                                        }
                                    }
                                    ListAdapter tmpAadapter = new ListAdapter(getContext(), R.layout.listview_layout, timelineItemList);
                                    listView.setAdapter(tmpAadapter);
                                    tmpAadapter.notifyDataSetChanged();
                                    //TabLayoutのItemのいち
                                    tablayout.getTabAt(nowHour - 4).select();
                                } else {
                                    listView.setAdapter(adapter);
                                }
                            }
                        });

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    //SQLite
    private void saveSQLite(final String url, final String mode) {
        text_ArrayList.clear();
        url_ArrayList.clear();
        css_1_ArrayList.clear();
        css_2_ArrayList.clear();
        hour_ArrayList.clear();
        minute_ArrayList.clear();
        //ネットワークは非同期処理
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... aVoid) {

                try {
                    //時刻表URL
                    Document doc = Jsoup.connect(url).get();
                    //HTML Table
                    //なんか３個めで行けた
                    final Element tables = doc.select("table").get(3);
                    final Elements tr = tables.select("tr");
                    for (int i = 3; i < tr.size(); i++) {
                        final int finalI = i;
                        //Tableの要素
                        final Elements td = tr.get(finalI).select("td");
                        //時間取り出し
                        //Class
                        //平日　weekday
                        //休日　holiday
                        String class_name = "weekday";
                        if (url.contains("&dw=0")) {
                            class_name = "weekday";
                        } else {
                            class_name = "holiday";
                        }
                        final Elements time = tr.get(finalI).getElementsByClass(class_name);

                        //到着取り出し
                        final Elements time_td = tr.get(finalI).getElementsByClass("jikokuhyo");

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String hour = "";
                                String minute = "";

                                //余分にforが回っている
                                //時刻表最後まで終わったら終了するようにする
                                if (time.text().length() > 0) {
                                    hour = time.text() + "時 ";
                                    //到着
                                    for (int train = 0; train < time_td.size(); train++) {
                                        minute = time_td.get(train).text() + "分";
                                        //Class(CSS)取得→各駅、区間急行等
                                        String css = time_td.get(train).select("a").get(0).select("span").get(0).className();
                                        String css_2nd = time_td.get(train).select("a").get(0).select("span").select("span").get(1).className();
                                        //電車URL
                                        String train_info = time_td.get(train).select("a").attr("href");
                                        //いろいろ
                                        String text = hour + minute.replace("(", "").replace(")", "");

                                        //SQLite準備
                                        text_ArrayList.add(text);
                                        css_1_ArrayList.add(css);
                                        css_2_ArrayList.add(css_2nd);
                                        url_ArrayList.add(train_info);
                                        hour_ArrayList.add(hour);
                                        minute_ArrayList.add(minute);
                                    }

                                }
                            }
                        });

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }


                return null;
            }

            @Override
            protected void onPostExecute(final Void aVoid) {
                super.onPostExecute(aVoid);

                //ArrayListを変換
                //DB保存
                ContentValues values = new ContentValues();
                values.put("station", TimeTableFragment.this.name + "-" + mode);
                values.put("memo", "");
                values.put("up_down", mode);
                values.put("url", ArrayListSharedPreferences.setArrayListToJSONArray(url_ArrayList).toString());
                values.put("css_1", ArrayListSharedPreferences.setArrayListToJSONArray(css_1_ArrayList).toString());
                values.put("css_2", ArrayListSharedPreferences.setArrayListToJSONArray(css_2_ArrayList).toString());
                values.put("time", ArrayListSharedPreferences.setArrayListToJSONArray(text_ArrayList).toString());
                values.put("hour", ArrayListSharedPreferences.setArrayListToJSONArray(hour_ArrayList).toString());
                values.put("minute", ArrayListSharedPreferences.setArrayListToJSONArray(minute_ArrayList).toString());

                //すでにある場合は削除する？
                sqLiteDatabase.delete("stationdb", "station=?", new String[]{TimeTableFragment.this.name + "-" + mode});
                //登録
                sqLiteDatabase.insert("stationdb", null, values);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mode.contains("up") && !mode.contains("up_holiday")) {
                            Toast.makeText(getContext(), "上りダウンロード完了", Toast.LENGTH_SHORT).show();
                            saveSQLite(getArguments().getString("URL").replace("d=1", "d=2"), "down");
                        }
                        if (mode.contains("down") && !mode.contains("down_holiday")) {
                            Toast.makeText(getContext(), "下りダウンロード完了", Toast.LENGTH_SHORT).show();
                            String getURL = getArguments().getString("URL");
                            getURL = getURL.replace("dw=0", "dw=1");
                            getURL = getURL.replace("d=2", "d=1");
                            saveSQLite(getURL, "up_holiday");
                        }
                        if (mode.contains("up_holiday")) {
                            Toast.makeText(getContext(), "休日上りダウンロード完了", Toast.LENGTH_SHORT).show();
                            String getURL = getArguments().getString("URL");
                            getURL = getURL.replace("dw=0", "dw=1");
                            getURL = getURL.replace("d=1", "d=2");
                            saveSQLite(getURL, "down_holiday");
                        }
                        if (mode.contains("down_holiday")) {
                            Toast.makeText(getContext(), "休日下りダウンロード完了", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * SQLiteからデータを読み込む
     */
    private void loadSQLiteTimeTable(final String mode) {
        adapter.clear();
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... aVoid) {
                //データベース表示
                //データ取り出し
                Cursor cursor = sqLiteDatabase.query(
                        "stationdb",
                        new String[]{"station", "memo", "up_down", "url", "css_1", "css_2", "time", "hour", "minute"},
                        "station=?",
                        new String[]{TimeTableFragment.this.name + "-" + mode},
                        null,
                        null,
                        null
                );
                //はじめに移動
                cursor.moveToFirst();

                //取り出し
                for (int i = 0; i < cursor.getCount(); i++) {
                    //のぼり・くだり
                    String up_down = "";
                    if (cursor.getString(2).contains("up")) {
                        up_down = "新宿方面";
                    } else {
                        up_down = "京王八王子・高尾山口方面";
                    }
                    if (cursor.getString(0).contains("holiday")) {
                        setSubTitleUIThread("方面 : " + up_down + " / " + "曜日 : " + "休日");
                    } else {
                        setSubTitleUIThread("方面 : " + up_down + " / " + "曜日 : " + "平日");
                    }
                    String str = ("-" + mode);
                    setTitleUIThread(cursor.getString(0).replace(str, "") + " (ダウンロードデータ)");

                    try {
                        JSONArray text_JsonArray = new JSONArray(cursor.getString(6));
                        JSONArray url_JsonArray = new JSONArray(cursor.getString(3));
                        JSONArray css_1_JsonArray = new JSONArray(cursor.getString(4));
                        JSONArray css_2_JsonArray = new JSONArray(cursor.getString(5));
                        JSONArray hour_JSONArray = new JSONArray(cursor.getString(7));
                        for (int json_count = 0; json_count < text_JsonArray.length(); json_count++) {
                            hourList.add(hour_JSONArray.getString(json_count));
                            ArrayList<String> item = new ArrayList<>();
                            item.add("time_table_list");
                            item.add((String) text_JsonArray.get(json_count));
                            item.add("");
                            item.add("https://keio.ekitan.com/sp/" + url_JsonArray.get(json_count));
                            item.add((String) css_1_JsonArray.get(json_count));
                            item.add((String) css_2_JsonArray.get(json_count));
                            final ListItem listItem = new ListItem(item);
                            //UIスレッド限定な模様
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(listItem);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    //次の項目へ
                    cursor.moveToNext();
                }

                //最後
                cursor.close();


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Toast.makeText(getContext(), "ダウンロード済みのデータを表示中です", Toast.LENGTH_SHORT).show();
                listView.setAdapter(adapter);
                super.onPostExecute(aVoid);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * SQLiteに駅の時刻表データがあるか確認
     *
     * @return 0か1を返します。0のときは無く、1のときは有る状態です
     */
    private int checkSQLiteTimeTableData(String name) {
        int return_int = 0;
        //データ取り出し
        Cursor cursor = sqLiteDatabase.query(
                "stationdb",
                new String[]{"station", "memo", "up_down", "url", "css_1", "css_2", "time", "hour", "minute"},
                "station=?",
                new String[]{TimeTableFragment.this.name + "-up"},
                null,
                null,
                null
        );
        //はじめに移動
        cursor.moveToFirst();
        //取り出し
        for (int i = 0; i < cursor.getCount(); i++) {
            //確認
            if (cursor.getString(0).contains(name)) {
                return_int = 1;
            }
            //次の項目へ
            cursor.moveToNext();
        }
        //最後
        cursor.close();
        return return_int;
    }

    //SpeedDialのメッセージ変更
    private void setDownloadSpeedDial() {
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.download_menu, R.drawable.ic_autorenew_white_24dp)
                .setFabBackgroundColor(colorCodeToInt("#005005"))
                .setLabel("データ更新")
                .create()).setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                saveSQLite(up_url, "up");
                return false;
            }
        });

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.online_menu, R.drawable.ic_language_while_24dp)
                .setFabBackgroundColor(colorCodeToInt("#1b0000"))
                .setLabel("ウェブから取得")
                .create()).setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                getHTMLAndPerse(up_url);
                return false;
            }
        });

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.download_delete_menu, R.drawable.ic_delete_white_24dp)
                .setFabBackgroundColor(colorCodeToInt("#000a12"))
                .setLabel("DLデータ削除")
                .create()).setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                sqLiteDatabase.delete("stationdb", "station=?", new String[]{TimeTableFragment.this.name + "-" + "up"});
                sqLiteDatabase.delete("stationdb", "station=?", new String[]{TimeTableFragment.this.name + "-" + "down"});
                sqLiteDatabase.delete("stationdb", "station=?", new String[]{TimeTableFragment.this.name + "-" + "up_holiday"});
                sqLiteDatabase.delete("stationdb", "station=?", new String[]{TimeTableFragment.this.name + "-" + "down_holiday"});
                return false;
            }
        });
    }

    public void okKeyDown() {
        //決定ボタン押したとき
        int pos = listView.getSelectedItemPosition();
        ArrayList<String> item = adapter.getItem(pos).getList();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.get(3)));
        startActivity(intent);
    }


}
