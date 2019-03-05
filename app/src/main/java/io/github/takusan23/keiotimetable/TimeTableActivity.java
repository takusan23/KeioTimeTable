package io.github.takusan23.keiotimetable;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import io.github.takusan23.keiotimetable.Adapter.ListItem;

public class TimeTableActivity extends AppCompatActivity {

    private TextView textView;
    private String up_url;
    private TextView train;

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        textView = findViewById(R.id.test_tv);
        train = findViewById(R.id.train_select_textview);


        up_url = getIntent().getStringExtra("URL");

        getHTMLAndPerse(up_url);


        //ポップアップメニュー作成
        final MenuBuilder menuBuilder = new MenuBuilder(TimeTableActivity.this);
        MenuInflater inflater = new MenuInflater(TimeTableActivity.this);
        inflater.inflate(R.menu.train_menu, menuBuilder);
        final MenuPopupHelper optionsMenu = new MenuPopupHelper(TimeTableActivity.this, menuBuilder, train);
        optionsMenu.setForceShowIcon(true);
        // ポップアップメニューのメニュー項目のクリック処理
        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ポップアップメニューを表示
                optionsMenu.show();
                //反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.sinzyuku:
                                train.setText("方面 :新宿方面");
                                train.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_arrow_upward_black_24dp, null), null, null, null);
                                up_url = "https://keio.ekitan.com/pc/T5?dw=0&slCode=";
                                getHTMLAndPerse(up_url);
                                break;
                            case R.id.hatiouzi:
                                train.setText("方面 : 京王八王子・高尾山口方面");
                                train.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_arrow_downward_black_24dp, null), null, null, null);
                                //最後変える
                                getHTMLAndPerse(up_url.replace("d=1", "d=2"));
                                break;
                        }

                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });
            }
        });


    }

    private void setTitleUIThread(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setTitle(message);
            }
        });
    }

    private void getHTMLAndPerse(final String url) {
        textView.setText("");
        setTitleUIThread("🚃🕗読み込み中");
        //サブタイトル設定
        if (url.contains("&d=1")) {
            getSupportActionBar().setSubtitle("方面 : 新宿方面");
        } else {
            getSupportActionBar().setSubtitle("方面 : 京王八王子・高尾山口方面");
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
                        final Elements time = tr.get(finalI).getElementsByClass("weekday");
                        //到着取り出し
                        final Elements time_td = tr.get(finalI).getElementsByClass("jikokuhyo");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //余分にforが回っている
                                //時刻表最後まで終わったら終了するようにする
                                if (time.text().length() > 0) {
                                    textView.append(time.text() + "時" + "\n");
                                    //到着
                                    for (int train = 0; train < time_td.size(); train++) {
                                        textView.append(time_td.get(train).text());
                                        //区切りを作る
                                        if (time_td.get(train).text().length() != 0) {
                                            textView.append(" / ");
                                        }
                                    }
                                    //区切り
                                    textView.append("\n----------\n");
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


