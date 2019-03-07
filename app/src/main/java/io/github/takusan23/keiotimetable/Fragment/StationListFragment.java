package io.github.takusan23.keiotimetable.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.takusan23.keiotimetable.Adapter.ListAdapter;
import io.github.takusan23.keiotimetable.Adapter.ListItem;
import io.github.takusan23.keiotimetable.R;

public class StationListFragment extends Fragment {

    private ListView station_ListView;
    private String url = "https://keio.ekitan.com/pc/T5?dw=0&slCode=";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_station_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        station_ListView = view.findViewById(R.id.station_list_listview);

        getActivity().setTitle("駅一覧");

        //ListView
        ArrayList<ListItem> arrayList = new ArrayList<>();
        final ListAdapter adapter = new ListAdapter(getContext(), R.layout.listview_layout, arrayList);

        //駅一覧
        String[] a = getResources().getStringArray(R.array.keio_station);
        //for
        for (int i = 1; i < a.length; i++) {
            //Adapter用List
            ArrayList<String> item = new ArrayList<>();
            item.add("station_list");
            item.add(a[i - 1] + " / KO-" + String.valueOf((i)));
            item.add(a[i - 1]);
            item.add(urlGenerator(i));
            item.add("");
            item.add("");
            ListItem listItem = new ListItem(item);
            adapter.add(listItem);
        }

        // ListViewにArrayAdapter
        station_ListView.setAdapter(adapter);


    }

    //フラグメント
    private void changeFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_fragment, fragment);
        transaction.commit();
    }

    //URL生成
    private String urlGenerator(int station_number) {
        //上り
        String url = "https://keio.ekitan.com/pc/T5?slCode=";


        String d = "&d=1&dw=0";

        //初台
        if (station_number == 2 || station_number == 3) {
            url += "263-" + String.valueOf(station_number - 1) + d;
        } else if (station_number >= 35 && station_number <= 45) {
            //京王多摩川とか
            url += "261-" + String.valueOf(station_number - 34) + d;
        } else if (station_number == 46) {
            //府中競馬
            url += "265-1" + d;
        } else if (station_number == 47) {
            //動物公園
            url += "260-1" + d;
        } else if (station_number >= 48) {
            //京王片倉駅
            url += "264-" + String.valueOf(station_number - 47) + d;
        } else if (station_number == 1) {
            //新宿
            url += "262-0" + d;
        } else {
            //笹塚から京王八王子まで
            url += "262-" + String.valueOf(station_number - 3) + d;
        }
        //System.out.println("リンク : " + url);
        return url;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setTitle("駅一覧");
    }
}
