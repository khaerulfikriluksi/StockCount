package com.urban.stockcount1.CustomClass;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Cache {

    private Map<String, String> dataList = new HashMap<>();

    public String getDataList(String key) {
        String datanya="";
        if (this.dataList.get(key)!=null) {
            datanya=this.dataList.get(key);
        } else {
            datanya="";
        }
        Log.v("Get : ",this.dataList.get(key));
        return datanya;
    }

    public void setDataList(String key, String value) {
        if (this.dataList.get(key)!=null) {
            this.dataList.remove(key);
            this.dataList.put(key,value);
        } else {
            this.dataList.put(key, value);
        }
    }

    public Cache() {}

    public static Cache getInstance() {
        if( instance == null ) {
            instance = new Cache();
        }
        return instance;
    }

    public static Cache instance;
}