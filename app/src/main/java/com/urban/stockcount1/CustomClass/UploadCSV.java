package com.urban.stockcount1.CustomClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import androidx.viewpager.widget.PagerAdapter;

public class UploadCSV {
    private String name;
    private String fileurl;
    private String key;
    private String user;
    private String date;
    private String time;
    private String data;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public UploadCSV() {

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public UploadCSV(String name, String fileurl, String key, String user, String date, String time, String data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());
        if (name.trim().equals("")) {
            this.name=getSaltString();
        } else {
            this.name = name;
        }
        this.fileurl=fileurl;
        this.key=key;
        this.user=user;
        if (date.trim().equals("")) {
            this.date = currentDate;
        } else {
            this.date = date;
        }
        if (time.trim().equals("")) {
            this.time = currentTime;
        } else {
            this.time = time;
        }
        this.data=data;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String n){
        this.name=n;
    }

    public String getFileurl() {
        return this.fileurl;
    }

    public void setFileurl(String n) {
        this.fileurl=n;
    }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
}
