package io.github.takusan23.keiotimetable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteTimeTable extends SQLiteOpenHelper {
    // データーベースのバージョン
    private static final int DATABASE_VERSION = 1;

    // データーベース名
    private static final String DATABASE_NAME = "StationListDB.db";
    private static final String TABLE_NAME = "stationdb";
    private static final String TITLE = "station";
    private static final String MEMO = "memo";
    private static final String UP_DOWN = "up";
    //URLとか
    private static final String URL = "url";
    private static final String CSS_1 = "css_1";
    private static final String CSS_2 = "css_2";
    //朝四時～夜一時
    private static final String TIMETABLE = "time";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    TITLE + " TEXT," +
                    MEMO + " TEXT," +
                    UP_DOWN + " TEXT," +
                    URL + " TEXT," +
                    CSS_1 + " TEXT," +
                    CSS_2 + " TEXT," +
                    HOUR + " TEXT," +
                    MINUTE + " TEXT," +
                    TIMETABLE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    SQLiteTimeTable(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        db.execSQL(
                SQL_CREATE_ENTRIES
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // アップデートの判別
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}