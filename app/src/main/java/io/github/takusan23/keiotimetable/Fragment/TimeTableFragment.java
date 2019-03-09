package io.github.takusan23.keiotimetable.Fragment;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import java.util.ConcurrentModificationException;

import io.github.takusan23.keiotimetable.Adapter.ListAdapter;
import io.github.takusan23.keiotimetable.Adapter.ListItem;
import io.github.takusan23.keiotimetable.R;
import io.github.takusan23.keiotimetable.Utilities.ArrayListSharedPreferences;
import io.github.takusan23.keiotimetable.SQLiteTimeTable;

public class TimeTableFragment extends Fragment {

    private ListView listView;
    private String up_url;
    private String name;
    private ArrayList<ListItem> arrayList;
    private ListAdapter adapter;
    private SpeedDialView speedDialView;
    private SharedPreferences pref_setting;
    private String finalURL;

    SQLiteTimeTable helper;
    SQLiteDatabase sqLiteDatabase;

    ArrayList<String> text_ArrayList = new ArrayList<>();
    ArrayList<String> url_ArrayList = new ArrayList<>();
    ArrayList<String> css_1_ArrayList = new ArrayList<>();
    ArrayList<String> css_2_ArrayList = new ArrayList<>();
    ArrayList<String> hour_ArrayList = new ArrayList<>();
    ArrayList<String> minute_ArrayList = new ArrayList<>();

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

        up_url = getArguments().getString("URL");
        name = getArguments().getString("name");
        finalURL = up_url;

        if (helper == null) {
            helper = new SQLiteTimeTable(getContext());
        }
        if (sqLiteDatabase == null) {
            sqLiteDatabase = helper.getWritableDatabase();
        }

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
                            getHTMLAndPerse(up_url.replace("d=1", "d=2"));
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.up_down_menu, "京王八王子・高尾山口方面 → 新宿方面", R.drawable.ic_arrow_downward_white_24dp);
                        } else {
                            //URL
                            getHTMLAndPerse(up_url);
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.up_down_menu, "新宿方面 → 京王八王子・高尾山口方面", R.drawable.ic_arrow_upward_white_24dp);
                        }
                        break;
                    case R.id.day_menu:
                        if (actionItem.getLabel(getContext()).contains("平日 → 土日、休日")) {
                            //下りURL
                            getHTMLAndPerse(up_url.replace("dw=0", "dw=1"));
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.day_menu, "土日、休日 → 平日", R.drawable.ic_supervisor_account_white_24dp);
                        } else {
                            //URL
                            getHTMLAndPerse(up_url);
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
                        saveSQLite(up_url, "up");
                        break;
                }

                return false;
            }
        });

        //ListView
        arrayList = new ArrayList<>();
        adapter = new ListAdapter(getContext(), R.layout.listview_layout, arrayList);

        getHTMLAndPerse(up_url);

    }

    private void setTitleUIThread(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(message);
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
        text_ArrayList.clear();
        css_1_ArrayList.clear();
        css_2_ArrayList.clear();
        url_ArrayList.clear();
        hour_ArrayList.clear();
        minute_ArrayList.clear();
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
                                        listView.setAdapter(adapter);


                                    }

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
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
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
                        if (mode.contains("up")) {
                            Toast.makeText(getContext(), "上りダウンロード完了", Toast.LENGTH_SHORT).show();
                            saveSQLite(up_url.replace("d=1", "d=2"), "down");
                        } else {
                            Toast.makeText(getContext(), "下りダウンロード完了", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }

}
