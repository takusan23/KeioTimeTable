package io.github.takusan23.keiotimetable.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

    private TextView listview_textview;

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
        final String url = listItem.get(2);

        //find(ry
        listview_textview = view.findViewById(R.id.listview_layout_textview);
        listview_textview.setText(text);


        //押す
        listview_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url.length() != 0){
                    Intent intent = new Intent(getContext(), TimeTableActivity.class);
                    intent.putExtra("URL",url);
                    getContext().startActivity(intent);
                }
            }
        });


        return view;
    }
}