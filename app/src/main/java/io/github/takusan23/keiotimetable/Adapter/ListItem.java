package io.github.takusan23.keiotimetable.Adapter;

import java.util.ArrayList;

public class ListItem {
    private ArrayList<String> list = null;

    public ListItem(ArrayList<String> list){
        this.list = list;
    }

    public void  setList(ArrayList<String> list){
        this.list = list;
    }

    public ArrayList<String> getList(){
        return list;
    }
}
