package io.github.takusan23.keiotimetable;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimeTableFragment extends Fragment {

    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_table, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        textView = view.findViewById(R.id.test_tv);

      new AsyncTask<Void,Void,Void>(){

          @Override
          protected Void doInBackground(Void... aVoid) {

              try {
                  //時刻表URL
                  Document doc = Jsoup.connect("").get();
                  //HTML Table
                  //なんか３個めで行けた
                  final Element tables = doc.select("table").get(3);
                  final Elements tr = tables.select("tr");
                  for(int i  = 1; i < tr.size(); i++) {
                      final int finalI = i;
                      //時間を出していく
                      Element tables_table = tr.select("td").select("table").get(0);
                      final Elements tables_table_tr = tables_table.select("tr");
                      getActivity().runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              textView.append(tables_table_tr.text() + "\n\n\n");
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
