package io.github.takusan23.keiotimetable.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.takusan23.keiotimetable.R;
import io.github.takusan23.keiotimetable.TimeTableActivity;

public class ListAdapter extends ArrayAdapter<ListItem> {

    private int mResource;
    private List<ListItem> mItems;
    private LayoutInflater mInflater;

    private ArrayList<String> listItem;
    private String memo;
    private String text;
    private String url;
    private String name;

    private TextView listview_textview;
    private LinearLayout listview_linearlayout;

    public ListAdapter(Context context, int resource, List<ListItem> items) {
        super(context, resource, items);
        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }

        //データを受け取る
        ListItem item = mItems.get(position);
        listItem = item.getList();

        //受け取ったデータを整理
        memo = listItem.get(0);
        text = listItem.get(1);
        final String name = listItem.get(2);
        final String url = listItem.get(3);
        final String css = listItem.get(4);
        final String css_2nd = listItem.get(5);

        //find(ry
        listview_textview = view.findViewById(R.id.listview_layout_textview);
        listview_linearlayout = view.findViewById(R.id.listview_layout_linearlayout);
        listview_textview.setText(text);


        //押す
        listview_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url.length() != 0) {
                    if (memo.contains("station_list")) {
                        Intent intent = new Intent(getContext(), TimeTableActivity.class);
                        intent.putExtra("URL", url);
                        intent.putExtra("name", name);
                        getContext().startActivity(intent);
                    } else if (memo.contains("time_table_list")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        getContext().startActivity(intent);
                    }

                }
            }
        });

        //CSSから各駅停車等取得
        if (css.length() != 0) {
            loadCSS(css);
        }
        //終点とか
        if (memo.contains("time_table_list")) {
            endPoint(css, css_2nd);
        }


        return view;
    }

    private void loadCSS(String css) {
        switch (css) {
            case "k_1301":
                listTextViewAppend("各駅停車");
                //背景色設定
                setBackgroundColor("#000000");
                break;
            case "k_1302":
                listTextViewAppend("快速");
                setBackgroundColor("#177EE6");
                break;
            case "k_1303":
                listTextViewAppend("通勤快速");
                setBackgroundColor("#177EE6");
                break;
            case "k_1304":
                listTextViewAppend("急行");
                setBackgroundColor("#00CC00");
                break;
            case "k_1305":
                listTextViewAppend("準特急");
                setBackgroundColor("#FF9900");
                break;
            case "k_1306":
                listTextViewAppend("特急");
                setBackgroundColor("#FF0033");
                break;
            case "k_1307":
                listTextViewAppend("京王ライナー");
                setBackgroundColor("#29417C");
                break;
            case "k_2905":
                listTextViewAppend("区間急行");
                setBackgroundColor("#00CC00");
                break;
        }
    }

    private void endPoint(String css, String css_2nd) {
        if (css_2nd.contains("k_1305")) {
            setEndStation("高新", "各駅停車　新宿ゆき。高幡不動から準特急　新宿ゆき");
            setEndStation("●", "北野駅で準特急新宿行に接続");
        } else if (css_2nd.contains("k_1306")) {
            setEndStation("高新", "各駅停車　新宿ゆき。高幡不動から特急　新宿ゆき");
            setEndStation("●", "北野駅で特急新宿行に接続");
        } else if (css.contains("k_1301") && listview_textview.getText().toString().contains("●")) {
            setEndStation("●", "当駅始発");
        } else if (css.contains("k_1306") && css_2nd.contains("k_1301")) {
            setEndStation("高八", "特急　京王八王子ゆき。高幡不動から各駅停車　京王八王子ゆき");
            setEndStation("高山", "特急　高尾山口ゆき。高幡不動から各駅停車　高尾山口ゆき");
            setEndStation("セ橋", "特急　橋本ゆき。京王多摩センターから各駅停車　橋本ゆき");
        } else if (css.contains("k_1305") && css_2nd.contains("k_1301")) {
            setEndStation("セ橋", "準特急　京王八王子ゆき。高幡不動から各駅停車　京王八王子ゆき");
        } else if (css.contains("k_1304") && css_2nd.contains("k_1301")) {
            setEndStation("セ橋", "急行　橋本ゆき。京王多摩センターから各駅停車　橋本ゆき");
            setEndStation("シン本", "急行　本八幡ゆき。新線新宿から各駅停車　本八幡ゆき");
        } else if (css.contains("k_1305") && listview_textview.getText().toString().contains("◆")) {
            setEndStation("◆", "準特急(不定期)");
        } else if (css.contains("k_1304") && listview_textview.getText().toString().contains("◆")) {
            setEndStation("◆", "急行(不定期)");
        } else if (listview_textview.getText().toString().contains("つ")) {
            setEndStation("つ", "つつじケ丘");
        } else if (listview_textview.getText().toString().contains("桜")) {
            setEndStation("桜", "桜上水");
        } else if (listview_textview.getText().toString().contains("シン")) {
            setEndStation("シン", "新線新宿");
        } else if (listview_textview.getText().toString().contains("瑞")) {
            setEndStation("瑞", "瑞江");
        } else if (listview_textview.getText().toString().contains("調")) {
            setEndStation("調", "調布");
        } else if (listview_textview.getText().toString().contains("府")) {
            setEndStation("府", "府中");
        } else if (listview_textview.getText().toString().contains("高")) {
            setEndStation("高", "高幡不動");
        } else if (listview_textview.getText().toString().contains("北")) {
            setEndStation("北", "北野");
        } else if (listview_textview.getText().toString().contains("本")) {
            setEndStation("本", "本八幡");
        } else if (listview_textview.getText().toString().contains("幡")) {
            setEndStation("幡", "八幡山");
        } else if (listview_textview.getText().toString().contains("本")) {
            setEndStation("本", "本八幡");
        } else if (listview_textview.getText().toString().contains("山")) {
            setEndStation("山", "高尾山口");
        } else if (listview_textview.getText().toString().contains("若")) {
            setEndStation("若", "若葉台");
        } else if (listview_textview.getText().toString().contains("橋")) {
            setEndStation("橋", "橋本");
        }
    }

    /**
     * 文字追加
     */
    private void listTextViewAppend(String message) {
        listview_textview.setText(text + "\n" + message);
    }

    /**
     * 背景色
     * 透明度設定済み
     */
    private void setBackgroundColor(String color_code) {
        //透明度設定時に＃邪魔なので置き換える
        listview_linearlayout.setBackgroundColor(Color.parseColor("#33" + color_code.replace("#", "")));
    }

    private void setEndStation(String shortCode, String name) {
        if (listview_textview.getText().toString().contains(shortCode)) {
            listview_textview.setText(listview_textview.getText().toString().replace(shortCode, ""));
            listview_textview.append("\n" + name);
        }
    }

}