package com.urban.stockcount1.CustomClass;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Cache {

    private Map<String, String> dataList = new HashMap<>();
    private Map<String, Boolean> boolData=new HashMap<>();

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

    public Boolean isUpdate (){
        Boolean ex=false;
        if (this.dataList.get("UPDATE")!=null) {
            if (this.dataList.get("UPDATE").trim().contains("true")) {
                ex=true;
            } else {
                ex=false;
            }
        } else {
            ex=false;
        }
        return ex;
    }

    public Boolean getBoolData(String key){
        Boolean data=false;
        if (this.boolData.get(key)!=null) {
            data=this.boolData.get(key);
        }
        return data;
    }

    public void putBooldata(String key, Boolean data){
        if (this.boolData.get(key)!=null) {
            this.boolData.remove(key);
        }
        this.boolData.put(key, data);
    }

    public void replace (String key, String value) {
        this.dataList.replace(key,value);
    }

    public static Cache instance;
}