package com.urban.stockcount1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    // static variable
    private static final int DATABASE_VERSION = 1;
    // Database name
    private static final String DATABASE_NAME = "stockapp.db";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    //Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String Qtblbarang = "CREATE TABLE tbl_barang (kode_barang varchar(255) PRIMARY KEY, nama_barang VARCHAR(255), harga_beli varchar(255), harga_jual varchar(255))";
        db.execSQL(Qtblbarang);
        String Qtblcabang = "CREATE TABLE tbl_cabang (kode varchar(255) PRIMARY KEY, nama VARCHAR(255), alamat text, latlong VARCHAR(255), telepon VARCHAR(255), foto_cabang VARCHAR(255))";
        db.execSQL(Qtblcabang);
        String Qtblcounting = "CREATE TABLE tbl_counting (no_doc varchar(255) PRIMARY KEY, kode_cabang varchar(255), petugas varchar(255), id_user varchar(255), lokasi_rak VARCHAR(255), tanggal DATE)";
        db.execSQL(Qtblcounting);
        String Qtblcountingdet = "CREATE TABLE tbl_counting_detail (id INTEGER PRIMARY KEY AUTOINCREMENT, no_doc varchar(255), kode_barang varchar(255), qty INTEGER)";
        db.execSQL(Qtblcountingdet);
        String Qtbldistin= "CREATE TABLE tbl_distin (No_bukti varchar(255) PRIMARY KEY,Kode_Penerima varchar(255),Kode_Pengirim varchar(255),nama_penerima varchar(255),nama_pengirim varchar(255),Kode_departemen varchar(255),Total_qty INTEGER,Total_qty_receive INTEGER,Total_beli varchar(255),Total_jual varchar(255),nama_penghitung varchar(255),id_user varchar(255),Kode_user varchar(255),keterangan TEXT,tgl_kirim DATE,tanggal_masuk DATE,tanggal_buat DATE,No_SuratJalan varchar(255),no_pol varchar(255),expedisi varchar(255));";
        db.execSQL(Qtbldistin);
        String Qtbldistindet = "CREATE TABLE tbl_distin_detail (id INTEGER PRIMARY KEY AUTOINCREMENT, No_bukti varchar(255), Kode_Penerima varchar(255), nama_penerima varchar(255), Kode_Pengirim varchar(255), nama_pengirim varchar(255), Kode_barang varchar(255), Qty INTEGER, Qty_receive INTEGER, harga_beli varchar(255), harga_jual varchar(255))";
        db.execSQL(Qtbldistindet);
        String Qapi = "CREATE TABLE tbl_api (id INTEGER PRIMARY KEY AUTOINCREMENT, api_online varchar(255), api_offline varchar(255))";
        db.execSQL(Qapi);
        String Quser = "CREATE TABLE tbl_user (id INTEGER PRIMARY KEY AUTOINCREMENT, username varchar(255), password varchar(255), email varchar(255), alias varchar(255), kode_cabang varchar(255))";
        db.execSQL(Quser);
        String APIPath = "CREATE TABLE tbl_api_path (name varchar(255) PRIMARY KEY, path varchar(255))";
        db.execSQL(APIPath);
    }

    // on Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tbl_barang");
        db.execSQL("DROP TABLE IF EXISTS tbl_cabang");
        db.execSQL("DROP TABLE IF EXISTS tbl_counting");
        db.execSQL("DROP TABLE IF EXISTS tbl_counting_detail");
        db.execSQL("DROP TABLE IF EXISTS tbl_distin");
        db.execSQL("DROP TABLE IF EXISTS tbl_distin_detail");
        db.execSQL("DROP TABLE IF EXISTS tbl_api");
        db.execSQL("DROP TABLE IF EXISTS tbl_user");
        db.execSQL("DROP TABLE IF EXISTS tbl_api_path");
        onCreate(db);
    }
}
