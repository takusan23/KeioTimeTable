package io.github.takusan23.keiotimetable;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class TimeTableActivity extends AppCompatActivity {

    private TextView textView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        textView = findViewById(R.id.test_tv);

        url = getIntent().getStringExtra("URL");

        setTitleUIThread("🚃🕗読み込み中");

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

    private void setTitleUIThread(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(message);
            }
        });
    }
}


