package io.github.takusan23.keiotimetable;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SQLiteView extends Fragment {

    private TextView textView;

    SQLiteTimeTable helper;
    SQLiteDatabase sqLiteDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sqlite_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        textView = view.findViewById(R.id.sqlite_tv);

        //DB
        if (helper == null) {
            helper = new SQLiteTimeTable(getContext());
        }
        if (sqLiteDatabase == null) {
            sqLiteDatabase = helper.getWritableDatabase();
        }

        //データ取り出し
        Cursor cursor = sqLiteDatabase.query(
                "stationdb",
                new String[]{"station", "memo", "up_down", "url", "css_1", "css_2", "time", "hour", "minute"},
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            textView.append(cursor.getString(0) + " / " + cursor.getString(2) + "\n");
            //次の項目へ
            cursor.moveToNext();
        }

        cursor.close();


    }

}
