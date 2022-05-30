package com.urban.stockcount1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FMasterData extends AppCompatActivity {
    //Var
    ListView listView;
    Context context = this;
    String API = null;//JsonBarang
    SQLiteDatabase db;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fmasterdata);
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.unguatas));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }

        Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
        cursor.moveToFirst();
        if (cursor.getCount()>0) {
            API=cursor.getString(1);//APIOnline
            //
            listView = (ListView) findViewById(R.id.list);
            //BackNavbarButton
            Toolbar toolbar = findViewById(R.id.toolBar2);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent Formku = new Intent(FMasterData.this, FMenuList.class);
                    startActivity(Formku);
                    finish();
                }
            });
            //
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            if (sdkVersion <= 28) {
                requestPermissions28();
            } else {
                requestPermissions();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(FMasterData.this);
            builder.setTitle("Application Error");
            builder.setCancelable(false);
            builder.setMessage("API tidak ada, silahkan kontak developer...");
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

    private void requestPermissions28() {
        Dexter.withActivity(FMasterData.this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            getDatabrg();
                        }
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            String toas = multiplePermissionsReport.getDeniedPermissionResponses().get(0).getPermissionName();
                            Toast.makeText(FMasterData.this,"Not Granted : "+toas, Toast.LENGTH_LONG).show();
                            showSettingsDialog();
                        }
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
            }
        }).onSameThread().check();
    }
    private void requestPermissions() {
        Dexter.withActivity(FMasterData.this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            getDatabrg();
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
                Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
            }
        }).onSameThread().check();
    }
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FMasterData.this);
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

    private void getDatabrg(){
        HashMap<String, String> nameaddress = new HashMap<>();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_barang LIMIT 1000", null);
        cursor.moveToFirst();
        for (int count = 0; count < cursor.getCount(); count++) {
            cursor.moveToPosition(count);
            nameaddress.put(cursor.getString(1), cursor.getString(0));
        }
        List<HashMap<String, String>> listitems = new ArrayList<>();
        SimpleAdapter adapter2 = new SimpleAdapter(FMasterData.this, listitems, R.layout.cus_list_masterdata,
                new String[]{"First", "Second"},
                new int[]{R.id.textter2, R.id.textter1});
        Iterator it = nameaddress.entrySet().iterator();
        while (it.hasNext()) {
            HashMap<String, String> resultmap = new HashMap<>();
            Map.Entry pair = (Map.Entry) it.next();
            resultmap.put("First", pair.getKey().toString());
            resultmap.put("Second", pair.getValue().toString());
            listitems.add(resultmap);
        }
        listView.setAdapter(adapter2);
    }




    //SearchText
    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.csvmenu,menu);
        MenuItem menuItem = menu.findItem(R.id.ecari);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Cari nama barang...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Toast.makeText(import_csv.this,"Text on Submit", Toast.LENGTH_LONG).show();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                //Onchanged
                HashMap<String, String> nameaddress = new HashMap<>();
                Cursor cursor = db.rawQuery("SELECT * FROM tbl_barang WHERE `nama_barang` LIKE '%"+newText+"%' LIMIT 1000", null);
                cursor.moveToFirst();
                for (int count = 0; count < cursor.getCount(); count++) {
                    cursor.moveToPosition(count);
                    nameaddress.put(cursor.getString(1), cursor.getString(0));
                }
                List<HashMap<String, String>> listitems = new ArrayList<>();
                SimpleAdapter adapter2 = new SimpleAdapter(FMasterData.this, listitems, R.layout.cus_list_masterdata,
                        new String[]{"First", "Second"},
                        new int[]{R.id.textter2, R.id.textter1});
                Iterator it = nameaddress.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap<String, String> resultmap = new HashMap<>();
                    Map.Entry pair = (Map.Entry) it.next();
                    resultmap.put("First", pair.getKey().toString());
                    resultmap.put("Second", pair.getValue().toString());
                    listitems.add(resultmap);
                }
                listView.setAdapter(adapter2);
                return false;
            }
        });
        return true;
    }

    //Download-CLearData
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.updateonline){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Konfirmasi");
            builder.setMessage("Apakah anda ingin mengunduh data kembali?");
            // Membuat tombol negative
            builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            //Membuat tombol positif
            builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    db.execSQL("DELETE FROM tbl_cabang");
                    db.execSQL("DELETE FROM tbl_barang");
                    download_cabang();
                }
            });
            builder.show();
        }
        else if (id == R.id.bersihbrg)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Konfirmasi");
            builder.setMessage("Apakah anda ingin menghapus semua data?");
            // Membuat tombol negative
            builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                //BatalHapus
                }
            });
            //Membuat tombol positif
            builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    db.execSQL("DELETE FROM tbl_barang");
                    getDatabrg();
                }
            });
            builder.show();
        }
        return true;
    }

    private void download_cabang() {
        progressDialog = new ProgressDialog(FMasterData.this);
        progressDialog.setTitle("Preparing First Run");
        progressDialog.setMessage("Mengunduh data cabang, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='cabang'",null);
        cr.moveToFirst();
        String jsonurl = API+cr.getString(1);
        Log.v("API-GET_CABANG",jsonurl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, jsonurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Log.v("GET_CABANG Response",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status==200) {
                                db.execSQL("delete from tbl_cabang");
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int position = 0; position < jsonArray.length(); position++) {
                                    JSONObject row = jsonArray.getJSONObject(position);
                                    String kode = row.getString("kode");
                                    String nama = row.getString("nama");
                                    String alamat = row.getString("alamat");
                                    String telepon = row.getString("telepon");
                                    String foto_cabang = row.getString("foto_cabang");
                                    String SQLiteDataBaseQueryHolder = "INSERT INTO tbl_cabang (kode,nama,alamat,latlong,telepon,foto_cabang) VALUES('"+kode+"','"+nama+"','"+alamat+"','-','"+telepon+"','"+foto_cabang+"');";
                                    db.execSQL(SQLiteDataBaseQueryHolder);
                                }
                                if (progressDialog.isShowing()) progressDialog.dismiss();
//                                final Handler handler = new Handler();
//                                handler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
                                        download_items();
//                                    }
//                                }, 800);
                            } else {
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(FMasterData.this);
                                builder.setTitle("Application error");
                                builder.setCancelable(false);
                                builder.setMessage("Respond Server error (GetCabang Result 400), Mohon kontak developer...");
                                builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        System.exit(0);
                                    }
                                });
                                builder.show();
                            }
                        } catch (JSONException e) {
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(FMasterData.this);
                            builder.setTitle("Application error");
                            builder.setCancelable(false);
                            builder.setMessage("Data Server error, Mohon kontak developer...");
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
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        String message = null; // error message, show it in toast or dialog, whatever you want
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                            message = "Cannot connect to Internet";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again later";
                        }  else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again later";
                        }
                        Log.v("Err",message);
                        AlertDialog.Builder builder = new AlertDialog.Builder(FMasterData.this);
                        builder.setTitle("Connection problem");
                        builder.setCancelable(false);
                        builder.setMessage("Koneksi server bermasalah, Mohon hubungi IT kemudian refresh kembali");
                        builder.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
                                cursor.moveToFirst();
                                if (cursor.getCount()>0) {
                                    API=cursor.getString(2);
                                    API=cursor.getString(1);
                                }
                                download_cabang();
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
        RequestQueue requestQueue = Volley.newRequestQueue(FMasterData.this);
        requestQueue.add(stringRequest);
    }

    private void download_items() {
        progressDialog = new ProgressDialog(FMasterData.this);
        progressDialog.setTitle("Preparing First Run");
        progressDialog.setMessage("Mengunduh data barang, ini memakan waktu beberapa menit, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='item'",null);
        cr.moveToFirst();
        String jsonurl = API+cr.getString(1);
        Log.v("API-GET_BARANG",jsonurl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, jsonurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    int status = jsonObject.getInt("status");
                                    if (status == 200) {
                                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                                        for (int position = 0; position < jsonArray.length(); position++) {
                                            JSONObject row = jsonArray.getJSONObject(position);
                                            String kdbrg = row.getString("kode_barang");
                                            String nmbrg = row.getString("nama_barang");
                                            String harga_beli = row.getString("harga_beli");
                                            String harga_jual = row.getString("harga_jual");
                                            String SQLiteDataBaseQueryHolder = "INSERT INTO tbl_barang (`kode_barang`,`nama_barang`,`harga_beli`,`harga_jual`) VALUES('" + kdbrg + "','" + nmbrg + "','"+harga_beli+"','"+harga_jual+"');";
                                            db.execSQL(SQLiteDataBaseQueryHolder);
                                        }
                                        if (progressDialog.isShowing()) progressDialog.dismiss();
                                        Intent Formku = new Intent(FMasterData.this, FMasterData.class);
                                        startActivity(Formku);
                                        finish();
                                    } else {
                                        if (progressDialog.isShowing()) progressDialog.dismiss();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(FMasterData.this);
                                        builder.setTitle("Application error");
                                        builder.setCancelable(false);
                                        builder.setMessage("Respond Server error (GetCabang Result 400), Mohon kontak developer...");
                                        builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.execSQL("delete from tbl_barang");
                                                dialog.cancel();
                                                System.exit(0);
                                            }
                                        });
                                        builder.show();
                                    }
                                } catch (JSONException e) {
                                    if (progressDialog.isShowing()) progressDialog.dismiss();
                                    Log.v("JsonObject",e.getMessage());
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FMasterData.this);
                                    builder.setTitle("Application error");
                                    builder.setCancelable(false);
                                    builder.setMessage("Data Server error, Mohon kontak developer...");
                                    builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            db.execSQL("delete from tbl_barang");
                                            dialog.cancel();
                                            System.exit(0);
                                        }
                                    });
                                    builder.show();
                                }
                            }
                        }).start();
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        String message = null; // error message, show it in toast or dialog, whatever you want
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                            message = "Cannot connect to Internet";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again later";
                        }  else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again later";
                        }
                        Log.v("Err",message);
                        AlertDialog.Builder builder = new AlertDialog.Builder(FMasterData.this);
                        builder.setTitle("Connection problem");
                        builder.setCancelable(false);
                        builder.setMessage("Koneksi server bermasalah, Mohon hubungi IT kemudian refresh kembali");
                        builder.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
                                cursor.moveToFirst();
                                if (cursor.getCount()>0) {
                                    API=cursor.getString(2);
                                    API=cursor.getString(1);
                                }
                                db.execSQL("delete from tbl_barang");
                                download_items();
                            }
                        });
                        builder.setNegativeButton("Keluar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.execSQL("delete from tbl_barang");
                                dialog.cancel();
                                System.exit(0);
                            }
                        });
                        builder.show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(FMasterData.this);
//        request.setRetryPolicy(new DefaultRetryPolicy(0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    //HardBackButton
    public void onBackPressed() {
        Intent Formku = new Intent(FMasterData.this, FMenuList.class);
        startActivity(Formku);
        finish();
    }

    //HideKeyboarOnLOstFocus
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }
}