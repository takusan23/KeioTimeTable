package io.github.takusan23.keiotimetable;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

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

public class TimeTableActivity extends AppCompatActivity {

    private ListView listView;
    private String up_url;
    private ArrayList<ListItem> arrayList;
    private ListAdapter adapter;
    private SpeedDialView speedDialView;

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        listView = findViewById(R.id.time_table_listview);
        speedDialView = findViewById(R.id.speedDial);

        up_url = getIntent().getStringExtra("URL");

        //SpeedDialとか
        speedDialView.setMainFabClosedBackgroundColor(Color.parseColor("#64c1ff"));
        speedDialView.setMainFabOpenedBackgroundColor(Color.parseColor("#0064b7"));
        //speedDialView.inflate(R.menu.speed_dial_menu);
        //IDは不明　speed_dial_menu.xmlで作ったのをこっちで使ってるだけ
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.up_down_menu, R.drawable.ic_arrow_upward_white_24dp)
                .setFabBackgroundColor(colorCodeToInt("#001064"))
                .setLabel("方面 : 新宿方面 → 京王八王子・高尾山口方面")
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.day_menu, R.drawable.ic_work_white_24dp)
                .setFabBackgroundColor(colorCodeToInt("#ff9e40"))
                .setLabel("曜日 : 平日 → 土日、休日")
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.station_add_menu, R.drawable.ic_add_white_24dp)
                .setFabBackgroundColor(colorCodeToInt("#8e0000"))
                .setLabel("駅を登録する")
                .create());
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
                        if (actionItem.getLabel(TimeTableActivity.this).contains("方面 : 新宿方面 → 京王八王子・高尾山口方面")) {
                            //下りURL
                            getHTMLAndPerse(up_url.replace("d=1", "d=2"));
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.up_down_menu, "方面 : 京王八王子・高尾山口方面 → 新宿方面", R.drawable.ic_arrow_downward_white_24dp);
                        } else {
                            //URL
                            getHTMLAndPerse(up_url);
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.up_down_menu, "方面 : 新宿方面 → 京王八王子・高尾山口方面", R.drawable.ic_arrow_upward_white_24dp);
                        }
                        break;
                    case R.id.day_menu:
                        if (actionItem.getLabel(TimeTableActivity.this).contains("曜日 : 平日 → 土日、休日")) {
                            //下りURL
                            getHTMLAndPerse(up_url.replace("dw=0", "dw=1"));
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.day_menu, "曜日 : 土日、休日 → 平日", R.drawable.ic_supervisor_account_white_24dp);
                        } else {
                            //URL
                            getHTMLAndPerse(up_url);
                            //タイトル切り替え
                            setItemTitleIcon(actionItem, R.id.day_menu, "曜日 : 平日 → 土日、休日", R.drawable.ic_work_white_24dp);
                        }

                }

                return false;
            }
        });

        //ListView
        arrayList = new ArrayList<>();
        adapter = new ListAdapter(TimeTableActivity.this, R.layout.listview_layout, arrayList);

        getHTMLAndPerse(up_url);

    }

    private void setTitleUIThread(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setTitle(message);
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
        adapter.clear();
        setTitleUIThread("🚃🕗読み込み中");
        //サブタイトル設定
        String title = "";
        if (url.contains("&d=1")) {
            getSupportActionBar().setSubtitle("方面 : 新宿方面");
            title = "方面 : 新宿方面";
        } else {
            getSupportActionBar().setSubtitle("方面 : 京王八王子・高尾山口方面");
            title = "方面 : 京王八王子・高尾山口方面";
        }
        //平日　休日
        if (url.contains("&dw=0")) {
            getSupportActionBar().setSubtitle(title + " / 曜日 : 平日");
        } else {
            getSupportActionBar().setSubtitle(title + " / 曜日 : 休日");
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

                        runOnUiThread(new Runnable() {
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
                                        //Adapter用List
                                        ArrayList<String> item = new ArrayList<>();
                                        item.add("time_table_list");
                                        item.add(hour + minute);
                                        item.add("");
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

}


