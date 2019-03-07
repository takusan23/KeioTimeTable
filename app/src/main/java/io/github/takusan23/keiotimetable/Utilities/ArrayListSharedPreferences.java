package io.github.takusan23.keiotimetable.Utilities;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ArrayListSharedPreferences {
    /**
     * ArrayListをJSONArrayにして保存する
     *
     * @param arrayList 配列
     * @param name      Preferenceのキー
     */
    public static void saveArrayListSharedPreferences(ArrayList arrayList, String name, SharedPreferences pref_setting) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < arrayList.size(); i++) {
            jsonArray.put(arrayList.get(i));
        }
        SharedPreferences.Editor editor = pref_setting.edit();
        editor.putString(name, jsonArray.toString());
        editor.apply();
    }

    /**
     * ArrayListをJSONArrayにして保存する
     *
     * @param name Preferenceのキー
     */
    public static ArrayList<String> loadSharedPreferencesArrayList(String name, SharedPreferences pref_setting) {
        ArrayList<String> arrayList = new ArrayList<>();
        String json = pref_setting.getString(name, "");
        //要素０回避
        if (json.length() != 0) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    arrayList.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

}
