package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.animation.Animator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteProgram;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jaredrummler.ktsh.Shell;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.urban.stockcount1.CustomClass.Cache;
import com.urban.stockcount1.CustomClass.DownloadService;
import com.urban.stockcount1.CustomClass.UpdateHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FSplash extends AppCompatActivity implements UpdateHelper.onUpdateCheckListener {
    //Var
    private ProgressDialog progressDialog;
    private DatabaseHelper module;
    private Animation TextAnim;
    private TextView textnya;
    private String ApiLocal,urldatabase,ApiOnline,TokenIDFCM;
    private ProgressBar progres;
    private TextView spls_status;
    private SQLiteDatabase db,dbonline;
    private FirebaseDatabase database;
    private DatabaseReference myRef,dbRef;
    private String device_id, ipfirebase,
            idlogin,userlogin,passlogin,aliaslogin
            ,deviceidlogin,devicelogin,emaillogin,kodecabang;
    private Boolean needlogin=true, load_done=false;
    private StorageReference storageRef;
    private Intent permissionIntent;
    private ProgressDialog mProgressDialog;
    private int count=0;
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //RemoveHeader(FullScreen)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fsplash);
        progressDialog = new ProgressDialog(FSplash.this);
        storageRef = FirebaseStorage.getInstance().getReference("csv");
        database = FirebaseDatabase.getInstance("https://stockapp-4abf1-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference("settings");
        dbRef = FirebaseDatabase.getInstance("https://stockapp-4abf1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("csv_data");
        module = new DatabaseHelper(this);
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        ApiOnline="";
        ApiLocal="";

        device_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(this.getResources().getColor(R.color.unguatas));
            window.setNavigationBarColor(this.getResources().getColor(R.color.ungubawah));
        }
        //Animasi Splash
        TextAnim = AnimationUtils.loadAnimation(FSplash.this,R.anim.splash_anim);
        spls_status = (TextView) findViewById(R.id.spls_status);
        textnya = (TextView) findViewById(R.id.TAni);
        textnya.startAnimation(TextAnim);
        //VisibleProgress
        progres = findViewById(R.id.Progres1);
        progres.setVisibility(View.VISIBLE);
//        db.execSQL("DELETE FROM tbl_counting");
//        db.execSQL("DELETE FROM tbl_counting_detail");
//        db.execSQL("DELETE FROM tbl_distin");
//        db.execSQL("DELETE FROM tbl_distin_detail");
        db.execSQL("DELETE FROM tbl_api_path");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('login','/api/auth/login')");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('register','/api/auth/register')");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('logout','/api/auth/logout')");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('login-status','/api/auth/login-status')");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('forgot','/api/auth/forgot')");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('distribusiout','/api/distribusi/history/')");//http://103.112.139.155:9000/api/distribusi/history/POS4UC000UE5300222-1239/penerima/UE530
        db.execSQL("INSERT INTO tbl_api_path VALUES ('item','/api/item')");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('cabang','/api/cabang')");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('notif','/api/item/notif')");//?setting=&ip_local=&ip_online
        db.execSQL("INSERT INTO tbl_api_path VALUES ('history_request','/api/stock/request')");
        db.execSQL("INSERT INTO tbl_api_path VALUES ('detail_request','/api/stock/request/')");//NomerRequest
        db.execSQL("INSERT INTO tbl_api_path VALUES ('edit_user','/api/auth/update/')");
        requestPermissions();
    }

    private void getValueFirebase() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                spls_status.setText("Checking connection...");
            }
        });
        myRef.child("ipconfig").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    db.execSQL("DELETE FROM tbl_api");
                    spls_status.setText("Checking Update...");
                    ipfirebase=String.valueOf(task.getResult().getValue());
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UpdateHelper.with(FSplash.this)
                                    .onUpdateCheck(FSplash.this)
                                    .check();
                        }
                    }, 1000);
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                if (prefs.contains("id")) {
                    Log.v("TimeoutPosition","Firebase");
                    Toast.makeText(FSplash.this,"Lost connection, Starting with offline mode",Toast.LENGTH_LONG).show();
                    Intent formku = new Intent(FSplash.this, FMenuUtama.class);
                    startActivity(formku);
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                    builder.setTitle("Lost Connection");
                    builder.setCancelable(false);
                    builder.setMessage("Device baru terdeteksi dan butuh login, mohon cek koneksi anda, Coba lagi?");
                    builder.setPositiveButton("Coba lagi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getValueFirebase();
                        }
                    });
                    builder.setNegativeButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            System.exit(0);
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    public void aWait() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                Log.v("Running Check Update :","Waiting");
                if (Environment.isExternalStorageManager()) {
                    Log.v("Running Check Update :","True");
                    permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    getValueFirebase();
                    Thread.currentThread().interrupt();
                } else {
                    aWait();
                    Thread.currentThread().interrupt();
                }
//                if (Environment.isExternalStorageManager()) {
//
//                    Thread.currentThread().interrupt();
//                } else {
//                    final Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            aWait();
//                        }
//                    }, 1000);
//                    Thread.currentThread().interrupt();
//                }
            }
        }).start();
    }


    private void continueafterdel(String ip){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (load_done==true) {
                    if (ip.trim().length() > 0) {
                        Log.v("FirebaseConfigResponse", "Value fetch : " + ip);
                        ApiOnline=ip;
                        ApiLocal=ip;
                        db.execSQL("INSERT INTO tbl_api (`api_online`,`api_offline`) " +
                                "values ('"+ip+"','"+ip+"')");
                        Cursor cursor=db.rawQuery("SELECT  * FROM tbl_barang",null);
                        if (cursor.getCount()==0){
                            download_cabang();
                        } else {
                            Cursor cr=db.rawQuery("SELECT  * FROM tbl_cabang",null);
                            if (cr.getCount()==0){
                                download_cabang();
                            } else {
                                Gettoken();
                            }
                        }
                    } else {
                        Log.v("FirebaseConfigResponse", "Task not successfully");
                        AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                        builder.setTitle("Server not found");
                        builder.setCancelable(false);
                        builder.setMessage("Server tidak ditemukan, silahkan kontak developer...");
                        builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                System.exit(0);
                            }
                        });
                        builder.show();
                    }
                    Thread.currentThread().interrupt();
                } else {
                    continueafterdel(ip);
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void requestPermissions() {
        Dexter.withActivity(FSplash.this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                if (Environment.isExternalStorageManager()) {
                                    getValueFirebase();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                                    builder.setTitle("Need Permissions");
                                    builder.setCancelable(false);
                                    builder.setMessage("Android 9 ke atas butuh akses tertentu, mohon izinkan");
                                    builder.setPositiveButton("Buka Pengaturan", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            permissionIntent = new Intent();
                                            permissionIntent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                            Uri uri = Uri.fromParts("package", FSplash.this.getPackageName(), null);
                                            permissionIntent.setData(uri);
                                            startActivity(permissionIntent);
                                            aWait();
                                        }
                                    });
                                    builder.show();
                                }
                            } else {
                                getValueFirebase();
                            }
                        }
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
//                Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
            }
        }).onSameThread().check();
    }
    private void showSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("Aplikasi ini butuh akses. Anda dapat mengubah kembali akses di pengaturan");
        builder.setPositiveButton("Buka Pengaturan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                System.exit(0);
            }
        });
        builder.show();
    }

    private void Gettoken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FAIL-GET-TOKEN", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
                        cursor.moveToFirst();
                        if (cursor.getCount()>0) {
                            ApiLocal=cursor.getString(2);
                            ApiOnline=cursor.getString(1);
                        }
                        TokenIDFCM=task.getResult();
                        Cache.getInstance().setDataList("TOKENFCM",TokenIDFCM);
                        Cache.getInstance().setDataList("DEVICEID",device_id);
                        Log.w("TOKEN", TokenIDFCM);
                        CekLoginStatus();
                    }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                            builder.setTitle("Application error");
                            builder.setCancelable(false);
                            builder.setMessage("Gagal terhubung ke API Google, Mohon kontak developer...");
                            builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    System.exit(0);
                                }
                            });
                            builder.show();
                        }
                    });
    }



    private void CekLoginStatus() {
        spls_status.setText("Checking login...");
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='login-status'",null);
        cr.moveToFirst();
        needlogin=true;
        String jsonurl = ApiOnline+cr.getString(1)+"?device_id="+device_id+"&token_id="+TokenIDFCM;
        Log.v("Login",jsonurl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, jsonurl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.v("LoginCheckResponse",response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        int status = jsonObject.getInt("status");
                        if (status==200) {
                            spls_status.setText("Welcome...");
                            db.execSQL("delete from tbl_user");
                            needlogin=false;
                            JSONObject obj = jsonObject.getJSONObject("data");
                            idlogin=obj.getString("id");
                            userlogin=obj.getString("username");
                            passlogin=obj.getString("password");
                            aliaslogin=obj.getString("alias");
                            deviceidlogin=obj.getString("device_id");
                            devicelogin=obj.getString("device");
                            emaillogin=obj.getString("email");
                            kodecabang=obj.getString("kode_cabang");
                            db.execSQL("insert into tbl_user (`id`,`username`,`password`,`email`,`alias`,`kode_cabang`) values " +
                                    "('"+idlogin+"','"+userlogin+"','"+passlogin+"','"+emaillogin+"','"+aliaslogin+"','"+kodecabang+"')");
                            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                            prefs.edit().clear().commit();
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("id", idlogin);
                            editor.putString("username", userlogin);
                            editor.putString("password", passlogin);
                            editor.putString("alias", aliaslogin);
                            editor.putString("device_id", deviceidlogin);
                            editor.putString("device", devicelogin);
                            editor.putString("email", emaillogin);
                            editor.putString("kode_cabang", kodecabang);
                            editor.commit();
                        } else {
                            needlogin=true;
                            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                            prefs.edit().clear().commit();
                        }
                        if (needlogin==true) {
                            Intent formku = new Intent(FSplash.this, Flogin.class);
                            formku.putExtra("start",1);
                            startActivity(formku);
                            finish();
                        } else {
                            Intent formku = new Intent(FSplash.this, FMenuUtama.class);
                            startActivity(formku);
                            finish();
                        }
                    } catch (JSONException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                        builder.setTitle("Application error");
                        builder.setCancelable(false);
                        builder.setMessage("Aplikasi error, Mohon kontak developer...");
                        builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                System.exit(0);
                            }
                        });
                        builder.show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String message = null; // error message, show it in toast or dialog, whatever you want
                    if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                        message = "Cannot connect to Internet";
                    } else if (error instanceof ServerError) {
                        message = "The server could not be found. Please try again later";
                    }  else if (error instanceof ParseError) {
                        message = "Parsing error! Please try again later";
                    }
                    Log.v("Err",message);
                    SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                    if (prefs.contains("id")) {
                        Log.v("TimeoutPosition","CheckLogin");
                        spls_status.setText("Welcome...");
                        Toast.makeText(FSplash.this,"Lost connection, Starting with offline mode",Toast.LENGTH_LONG).show();
                        Intent formku = new Intent(FSplash.this, FMenuUtama.class);
                        startActivity(formku);
                        finish();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                        builder.setTitle("Connection problem");
                        builder.setCancelable(false);
                        builder.setMessage("Butuh login terlebihdahulu, Koneksi tidak stabil");
                        builder.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
                                cursor.moveToFirst();
                                if (cursor.getCount()>0) {
                                    ApiLocal=cursor.getString(2);
                                    ApiOnline=cursor.getString(1);
                                }
                                Gettoken();
                            }
                        });
                        builder.setNegativeButton("Keluar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                System.exit(0);
                            }
                        });
                        builder.show();
                    }
                }
            })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("API_KEY", "53713");
                return headers;
            }

            @Override
            public Response<String> parseNetworkResponse(NetworkResponse response) {
                String statusCode = String.valueOf(response.statusCode);
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(FSplash.this);
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

//    private void download_cabang() {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//             @Override
//             public void run() {
//                 spls_status.setText("Downloading branch data...");
//             }
//         });
//        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='cabang'",null);
//        cr.moveToFirst();
//        String jsonurl = ApiOnline+cr.getString(1);
//        Log.v("API-GET_CABANG",jsonurl);
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, jsonurl,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
////                        Log.v("GET_CABANG Response",response);
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//                            int status = jsonObject.getInt("status");
//                            if (status==200) {
//                                db.execSQL("delete from tbl_cabang");
//                                JSONArray jsonArray = jsonObject.getJSONArray("data");
//                                for (int position = 0; position < jsonArray.length(); position++) {
//                                    JSONObject row = jsonArray.getJSONObject(position);
//                                    String kode = row.getString("kode");
//                                    String nama = row.getString("nama");
//                                    String alamat = row.getString("alamat");
//                                    String telepon = row.getString("telepon");
//                                    String foto_cabang = row.getString("foto_cabang");
//                                    String SQLiteDataBaseQueryHolder = "INSERT INTO tbl_cabang (kode,nama,alamat,latlong,telepon,foto_cabang) VALUES('"+kode+"','"+nama+"','"+alamat+"','-','"+telepon+"','"+foto_cabang+"');";
//                                    db.execSQL(SQLiteDataBaseQueryHolder);
//                                }
//                                if (progressDialog.isShowing()) progressDialog.dismiss();
//                                final Handler handler = new Handler();
//                                handler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        download_items();
//                                    }
//                                }, 1000);
//                            } else {
//                                if (progressDialog.isShowing()) progressDialog.dismiss();
//                                AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
//                                builder.setTitle("Application error");
//                                builder.setCancelable(false);
//                                builder.setMessage("Respond Server error (GetCabang Result 400), Mohon kontak developer...");
//                                builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.cancel();
//                                        System.exit(0);
//                                    }
//                                });
//                                builder.show();
//                            }
//                        } catch (JSONException e) {
//                            Log.v("C",e.getMessage());
//                            if (progressDialog.isShowing()) progressDialog.dismiss();
//                            AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
//                            builder.setTitle("Application error");
//                            builder.setCancelable(false);
//                            builder.setMessage("Data Server error, Mohon kontak developer...");
//                            builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();
//                                    System.exit(0);
//                                }
//                            });
//                            builder.show();
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        if (progressDialog.isShowing()) progressDialog.dismiss();
//                        String message = "Something wrong, please contact developer"; // error message, show it in toast or dialog, whatever you want
//                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
//                            message = "Cannot connect to Internet";
//                        } else if (error instanceof ServerError) {
//                            message = "The server could not be found. Please try again later";
//                        }  else if (error instanceof ParseError) {
//                            message = "Parsing error! Please try again later";
//                        }
//                        Log.v("Err",message);
//                        AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
//                        builder.setTitle("Connection problem");
//                        builder.setCancelable(false);
//                        builder.setMessage("Koneksi server bermasalah, Mohon hubungi IT kemudian refresh kembali");
//                        builder.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
//                                cursor.moveToFirst();
//                                if (cursor.getCount()>0) {
//                                    ApiLocal=cursor.getString(2);
//                                    ApiOnline=cursor.getString(1);
//                                }
//                                download_cabang();
//                            }
//                        });
//                        builder.setNegativeButton("Keluar", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                                System.exit(0);
//                            }
//                        });
//                        builder.show();
//                    }
//                })
//        {
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("API_KEY", "53713");
//                return headers;
//            }
//
//            @Override
//            public Response<String> parseNetworkResponse(NetworkResponse response) {
//                String statusCode = String.valueOf(response.statusCode);
//                return super.parseNetworkResponse(response);
//            }
//        };
//        RequestQueue requestQueue = Volley.newRequestQueue(FSplash.this);
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        requestQueue.add(stringRequest);
//    }

    private void download_cabang() {
        myRef.child("db_url").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> taskx) {
                urldatabase=String.valueOf(taskx.getResult().getValue());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        spls_status.setText("Downloading Barcode...");
                        new DownloadDB().execute(urldatabase);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                builder.setTitle("Cloud Not Responding");
                builder.setCancelable(false);
                builder.setMessage("Cloud Offline...");
                builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        });
    }

//    private void download_items() {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                spls_status.setText("Downloading Barcode...");
//                new DownloadBarangCSV().execute("https://firebasestorage.googleapis.com/v0/b/stockapp-4abf1.appspot.com/o/csv_barang%2Fv_barang.csv?alt=media&token=87b46d62-dc9c-4f01-992a-d9c099fd0381");
//            }
//        });
//    }

//    private void download_items() {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                spls_status.setText("Downloading barcode data...");
//            }
//        });
//        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='item'",null);
//        cr.moveToFirst();
//        String jsonurl = ApiOnline+cr.getString(1);
//        Log.v("API-GET_BARANG",jsonurl);
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, jsonurl,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        JSONObject jsonObject = new JSONObject(response);
//                                        int status = jsonObject.getInt("status");
//                                        if (status == 200) {
//                                            JSONArray jsonArray = jsonObject.getJSONArray("data");
//                                            for (int position = 0; position < jsonArray.length(); position++) {
//                                                JSONObject row = jsonArray.getJSONObject(position);
//                                                String kdbrg = row.getString("kode_barang");
//                                                String nmbrg = row.getString("nama_barang");
//                                                String harga_beli = row.getString("harga_beli");
//                                                String harga_jual = row.getString("harga_jual");
//                                                String SQLiteDataBaseQueryHolder = "INSERT INTO tbl_barang (`kode_barang`,`nama_barang`,`harga_beli`,`harga_jual`) VALUES('" + kdbrg + "','" + nmbrg + "','"+harga_beli+"','"+harga_jual+"');";
//                                                db.execSQL(SQLiteDataBaseQueryHolder);
//                                            }
//                                            if (progressDialog.isShowing()) progressDialog.dismiss();
//                                            Gettoken();
//                                        } else {
//                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    if (progressDialog.isShowing()) progressDialog.dismiss();
//                                                    AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
//                                                    builder.setTitle("Application error");
//                                                    builder.setCancelable(false);
//                                                    builder.setMessage("Respond Server error (GetCabang Result 400), Mohon kontak developer...");
//                                                    builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//                                                            db.execSQL("delete from tbl_barang");
//                                                            dialog.cancel();
//                                                            System.exit(0);
//                                                        }
//                                                    });
//                                                    builder.show();
//                                                }
//                                            });
//                                        }
//                                    } catch (JSONException e) {
//                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                if (progressDialog.isShowing()) progressDialog.dismiss();
//                                                Log.v("JsonObject",e.getMessage());
//                                                AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
//                                                builder.setTitle("Application error");
//                                                builder.setCancelable(false);
//                                                builder.setMessage("Data Server error, Mohon kontak developer...");
//                                                builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        db.execSQL("delete from tbl_barang");
//                                                        dialog.cancel();
//                                                        System.exit(0);
//                                                    }
//                                                });
//                                                builder.show();
//                                            }
//                                        });
//                                    }
//                                }
//                            }).start();
//                    }
//
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        if (progressDialog.isShowing()) progressDialog.dismiss();
//                        String message = ""; // error message, show it in toast or dialog, whatever you want
//                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
//                            message = "Cannot connect to Internet";
//                        } else if (error instanceof ServerError) {
//                            message = "The server could not be found. Please try again later";
//                        }  else if (error instanceof ParseError) {
//                            message = "Parsing error! Please try again later";
//                        }
//                        Log.v("Err",message);
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
//                                builder.setTitle("Connection problem");
//                                builder.setCancelable(false);
//                                builder.setMessage("Koneksi server bermasalah, Mohon hubungi IT kemudian refresh kembali");
//                                builder.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
//                                        cursor.moveToFirst();
//                                        if (cursor.getCount()>0) {
//                                            ApiLocal=cursor.getString(2);
//                                            ApiOnline=cursor.getString(1);
//                                        }
//                                        db.execSQL("delete from tbl_barang");
//                                        download_items();
//                                    }
//                                });
//                                builder.setNegativeButton("Keluar", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        db.execSQL("delete from tbl_barang");
//                                        dialog.cancel();
//                                        System.exit(0);
//                                    }
//                                });
//                                builder.show();
//                            }
//                        });
//                    }
//                })
//        {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("API_KEY", "53713");
//                return headers;
//            }
//
//            @Override
//            public Response<String> parseNetworkResponse(NetworkResponse response) {
//                String statusCode = String.valueOf(response.statusCode);
//                return super.parseNetworkResponse(response);
//            }
//        };
//        RequestQueue requestQueue = Volley.newRequestQueue(FSplash.this);
////        request.setRetryPolicy(new DefaultRetryPolicy(0,
////                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
////        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
////                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        requestQueue.add(stringRequest);
//    }

    @Override
    public void onUpdateCheckListener(String url) {
        if (url.trim().contains("true")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
            builder.setTitle("Update avaible");
            builder.setCancelable(false);
            builder.setMessage("Versi terbaru tersedia, mohon update untuk melanjutkan...");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    myRef.child("url_stock_app").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (Cache.getInstance().isUpdate()) {
                                    Cache.getInstance().replace("UPDATE", "true");
                                } else {
                                    Cache.getInstance().setDataList("UPDATE", "true");
                                }
                                String ur = String.valueOf(task.getResult().getValue());
                                if (ur.trim().length() > 0) {
                                    mProgressDialog = new ProgressDialog(FSplash.this);
                                    mProgressDialog.setMessage("Downloading update");
                                    mProgressDialog.setIndeterminate(true);
                                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    mProgressDialog.setCancelable(false);
                                    mProgressDialog.show();
                                    Intent intent = new Intent(FSplash.this, DownloadService.class);
                                    intent.putExtra("url", ur);
                                    intent.putExtra("receiver", new DownloadReceiver(new Handler()));
                                    startService(intent);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                                    builder.setTitle("Application error");
                                    builder.setCancelable(false);
                                    builder.setMessage("Tidak ada URL update, Mohon kontak developer...");
                                    builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            System.exit(0);
                                        }
                                    });
                                    builder.show();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FSplash.this);
                            builder.setTitle("Application error");
                            builder.setCancelable(false);
                            builder.setMessage("Gagal mengambil URL Update, Mohon kontak developer...");
                            builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    System.exit(0);
                                }
                            });
                            builder.show();
                        }
                    });

                }
            });
            builder.show();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            Date newDate = calendar.getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = dateFormat.format(newDate);
            Query qry1 = dbRef.orderByChild("date").startAt("2000-01-01").endAt(date);
            qry1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        load_done=false;
                        for (DataSnapshot snap: snapshot.getChildren()) {
                            load_done=false;
                            String filenya = snap.child("name").getValue(String.class);
                            StorageReference rref = storageRef.child(filenya);
                            rref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    snap.getRef().removeValue();
                                    load_done=true;
                                }
                            });
                        }
                        continueafterdel(ipfirebase);
                    } else {
                        load_done=true;
                        continueafterdel(ipfirebase);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private class DownloadReceiver extends ResultReceiver {

        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                mProgressDialog.setIndeterminate(false);
                int progress = resultData.getInt("progress"); //get the progress
//                Log.v("DownloadProgress",String.valueOf(progress));
                mProgressDialog.setProgress(progress);
                if (progress == 100) {
                    mProgressDialog.dismiss();
                    File toInstall = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StockApp" + ".apk");
                    Intent intent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        count++;
                        if (count ==1) {
                            Uri apkUri = FileProvider.getUriForFile(FSplash.this, BuildConfig.APPLICATION_ID, toInstall);
                            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(apkUri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                            finish();
                        } else {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    count = 0;
                                }
                            }, 2000);
                        }
                    } else {
                        count++;
                        if (count ==1) {
                            Uri apkUri = Uri.fromFile(toInstall);
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    count = 0;
                                }
                            }, 2000);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    private class DownloadDB extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int lenghtOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);
                OutputStream output = new FileOutputStream(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/stockapp.db" );

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);
            spls_status.setText("Fetching Barcode. Please wait...");
            dbonline=SQLiteDatabase.openDatabase(Environment.
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/stockapp.db", null,
                    SQLiteDatabase.OPEN_READWRITE);

            dbonline.execSQL("DELETE FROM tbl_counting");
            dbonline.execSQL("DELETE FROM tbl_counting_detail");
            dbonline.execSQL("DELETE FROM tbl_distin");
            dbonline.execSQL("DELETE FROM tbl_distin_detail");
            dbonline.execSQL("DELETE FROM tbl_api");
            dbonline.execSQL("DELETE FROM tbl_user");
            dbonline.execSQL("DELETE FROM tbl_api_path");

            Cursor cr1 = db.rawQuery("SELECT `no_doc`,`kode_cabang`,`petugas`,`id_user`,`lokasi_rak`,`tanggal` FROM tbl_counting",null);
            for (int count = 0; count < cr1.getCount(); count++) {
                cr1.moveToPosition(count);
                dbonline.execSQL("INSERT INTO tbl_counting (`no_doc`,`kode_cabang`,`petugas`,`id_user`,`lokasi_rak`,`tanggal`) VALUES" +
                        "('"+cr1.getString(0)+"','"+cr1.getString(1)+"','"+cr1.getString(2)+"','"+cr1.getString(3)+"','"+cr1.getString(4)+"','"+cr1.getString(5)+"');");
            }

            Cursor cr2 = db.rawQuery("SELECT `id`,`no_doc`,`kode_barang`,`qty` FROM tbl_counting_detail",null);
            for (int count = 0; count < cr2.getCount(); count++) {
                cr2.moveToPosition(count);
                dbonline.execSQL("INSERT INTO tbl_counting_detail (`id`,`no_doc`,`kode_barang`,`qty`) VALUES" +
                        "('"+cr2.getString(0)+"','"+cr2.getString(1)+"','"+cr2.getString(2)+"','"+cr2.getString(3)+"');");
            }

            Cursor cr3 = db.rawQuery("SELECT `No_bukti`,`Kode_Penerima`,`Kode_Pengirim`,`nama_penerima`,`nama_pengirim`,`Kode_departemen`,`Total_qty`,`Total_qty_receive`,`Total_beli`,`Total_jual`,`nama_penghitung`,`id_user`,`Kode_user`,`keterangan`,`tgl_kirim`,`tanggal_masuk`,`tanggal_buat`,`No_SuratJalan`,`no_pol`,`expedisi` FROM tbl_distin",null);
            for (int count = 0; count < cr3.getCount(); count++) {
                cr3.moveToPosition(count);
                dbonline.execSQL("INSERT INTO tbl_distin (`No_bukti`,`Kode_Penerima`,`Kode_Pengirim`,`nama_penerima`,`nama_pengirim`,`Kode_departemen`,`Total_qty`,`Total_qty_receive`,`Total_beli`,`Total_jual`,`nama_penghitung`,`id_user`,`Kode_user`,`keterangan`,`tgl_kirim`,`tanggal_masuk`,`tanggal_buat`,`No_SuratJalan`,`no_pol`,`expedisi`) VALUES" +
                        "('"+cr3.getString(0)+"','"+cr3.getString(1)+"','"+cr3.getString(2)+"','"+cr3.getString(3)+"','"+cr3.getString(4)+"','"+cr3.getString(5)+"','"+cr3.getString(6)+"','"+cr3.getString(7)+"','"+cr3.getString(8)+"','"+cr3.getString(9)+"','"+cr3.getString(10)+"','"+cr3.getString(11)+"','"+cr3.getString(12)+"','"+cr3.getString(13)+"','"+cr3.getString(14)+"','"+cr3.getString(15)+"','"+cr3.getString(16)+"','"+cr3.getString(17)+"',,'"+cr3.getString(18)+"','"+cr3.getString(19)+"');");
            }

            Cursor cr4 = db.rawQuery("SELECT `id`,`No_bukti`,`Kode_Penerima`,`nama_penerima`,`Kode_Pengirim`,`nama_pengirim`,`Kode_barang`,`Qty`,`Qty_receive`,`harga_beli`,`harga_jual` FROM tbl_distin_detail",null);
            for (int count = 0; count < cr4.getCount(); count++) {
                cr4.moveToPosition(count);
                dbonline.execSQL("INSERT INTO tbl_distin_detail (`id`,`No_bukti`,`Kode_Penerima`,`nama_penerima`,`Kode_Pengirim`,`nama_pengirim`,`Kode_barang`,`Qty`,`Qty_receive`,`harga_beli`,`harga_jual`) VALUES" +
                        "('"+cr4.getString(0)+"','"+cr4.getString(1)+"','"+cr4.getString(2)+"','"+cr4.getString(3)+"','"+cr4.getString(4)+"','"+cr4.getString(5)+"','"+cr4.getString(6)+"','"+cr4.getString(7)+"','"+cr4.getString(8)+"','"+cr4.getString(9)+"','"+cr4.getString(10)+"');");
            }

            Cursor cr5 = db.rawQuery("SELECT `id`,`api_online`,`api_offline` FROM tbl_api",null);
            for (int count = 0; count < cr5.getCount(); count++) {
                cr5.moveToPosition(count);
                dbonline.execSQL("INSERT INTO tbl_api (`id`,`api_online`,`api_offline`) VALUES" +
                        "('"+cr5.getString(0)+"','"+cr5.getString(1)+"','"+cr5.getString(2)+"');");
            }

            Cursor cr6 = db.rawQuery("SELECT `id`, `username`, `password`, `email`, `alias`, `kode_cabang` FROM tbl_user",null);
            for (int count = 0; count < cr6.getCount(); count++) {
                cr6.moveToPosition(count);
                dbonline.execSQL("INSERT INTO tbl_user (`id`, `username`, `password`, `email`, `alias`, `kode_cabang`) VALUES" +
                        "('"+cr6.getString(0)+"','"+cr6.getString(1)+"','"+cr6.getString(2)+"','"+cr6.getString(3)+"','"+cr6.getString(4)+"','"+cr6.getString(5)+"');");
            }

            Cursor cr7 = db.rawQuery("SELECT `name`, `path` FROM tbl_api_path",null);
            for (int count = 0; count < cr7.getCount(); count++) {
                cr7.moveToPosition(count);
                dbonline.execSQL("INSERT INTO tbl_api_path (`name`, `path`) VALUES" +
                        "('"+cr7.getString(0)+"','"+cr7.getString(1)+"');");
            }

            try {
                importDatabase(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/stockapp.db");
                Log.v("Success","Replace Done");
                Gettoken();
            } catch (IOException e) {
                e.printStackTrace();
                Log.v("ErrMoving",e.getMessage());
            }
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    FileReader file = null;
//                    try {
//                        file = new FileReader(Environment.
//                                getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/barang.csv");
//                        BufferedReader buffer = new BufferedReader(file);
//                        db.execSQL("DELETE FROM tbl_barang");
//                        String line = "";
//                        String tableName ="tbl_barang";
//                        String columns = "kode_barang, nama_barang, harga_beli, harga_jual";
//                        String str1 = "INSERT INTO " + tableName + " (" + columns + ") values (";
//                        String str2 = ");";
//                        db.beginTransaction();
//                        while (true) {
//                            try {
//                                if (!((line = buffer.readLine()) != null)) break;
//                                StringBuilder sb = new StringBuilder(str1);
//                                String[] str = line.split(",");
//                                sb.append("'" + str[0].replaceAll("\"", "") + "','");
//                                sb.append(str[1].replaceAll("\"", "") + "','");
//                                sb.append(str[2].replaceAll("\"", "") + "','");
//                                sb.append(str[3].replaceAll("\"", "") + "'");
//                                sb.append(str2);
//                                db.execSQL(sb.toString());
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Log.v("Error Read CSV",e.getMessage());
//                            }
//                        }
//
//                        db.setTransactionSuccessful();
//                        db.endTransaction();
//                        Process p=null;
//                        try {
//                            p = new ProcessBuilder()
//                                    .command("PathToYourScript")
//                                    .start();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        } finally {
//                            if(p!=null) p.destroy();
//                        }
//                        Gettoken();
////                        handler.removeCallbacks(this);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                        Log.v("File Not Found", e.getMessage());
////                        Gettoken();
////                        handler.removeCallbacks(this);
//                    }
//                }
//            }, 1000);
        }

    }
    private void importDatabase(String inputFileName) throws IOException
    {
        InputStream mInput = new FileInputStream(inputFileName);
        String outFileName = "data/data/com.urban.stockapp/databases/stockapp.db";
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }
}