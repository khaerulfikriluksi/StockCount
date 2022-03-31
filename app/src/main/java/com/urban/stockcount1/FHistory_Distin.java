package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.ColorSpace;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
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
import com.bumptech.glide.load.model.Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.Result;
import com.opencsv.CSVWriter;
import com.urban.stockcount1.CustomClass.ListDistHistAdapter;
import com.urban.stockcount1.CustomClass.ListHistoryAdapter;
import com.urban.stockcount1.CustomClass.UploadCSV;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class FHistory_Distin extends AppCompatActivity {

    private Toolbar toolbar;
    private AlertDialog show,show2;
    private SQLiteDatabase db;
    private String API=null,usr_alias=null,Today=null, kodecb=null, id_user=null;
    private AutoCompleteTextView din_newnobukti,din_tglmasuk, din_keterangan;
    private FloatingActionButton din_add;
    private CircularProgressButton din_get;
    private LottieAnimationView din_nodata_anim;
    private ListDistHistAdapter listDistHistAdapter;
    private ListView din_historylist;
    private static ActionMode actionMode=null;
    private static Boolean isActionMode = false;
    private StorageReference storageRef;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fhistory_distin);
        storageRef = FirebaseStorage.getInstance().getReference("csv");
        dbRef = FirebaseDatabase.getInstance("https://stockapp-4abf1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("csv_data");
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
        Today=simpleDateFormat2.format(calendar.getTime());
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(this.getResources().getColor(R.color.unguatas));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }
        Cursor cursor2 = db.rawQuery("SELECT * FROM tbl_api", null);
        cursor2.moveToFirst();
        if (cursor2.getCount()>0) {
            API=cursor2.getString(1);
            Cursor cr = db.rawQuery("SELECT * FROM tbl_user", null);
            cr.moveToFirst();
            if (cr.getCount()>0) {
                id_user=cr.getString(0);
                kodecb=cr.getString(5);
                usr_alias=cr.getString(4);
                din_nodata_anim= (LottieAnimationView) findViewById(R.id.din_nodata_anim);
                toolbar = (Toolbar) findViewById(R.id.toolbarhistorydistin);
                din_add = (FloatingActionButton) findViewById(R.id.din_add);
                din_historylist = (ListView) findViewById(R.id.din_historylist);
                din_historylist.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
                din_historylist.setMultiChoiceModeListener(modeListener);
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                din_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder buildernewdis = new AlertDialog.Builder(FHistory_Distin.this);
                        LayoutInflater inflater = FHistory_Distin.this.getLayoutInflater();
                        View view = inflater.inflate(R.layout.cust_popup_newdistin, null);
                        TextInputLayout din_newnobukti_head = (TextInputLayout) view.findViewById(R.id.din_newnobukti_head),
                                din_tglmasuk_head = (TextInputLayout) view.findViewById(R.id.din_tglmasuk_head);
                        din_newnobukti = (AutoCompleteTextView) view.findViewById(R.id.din_newnobukti);
                        din_tglmasuk = (AutoCompleteTextView) view.findViewById(R.id.din_tglmasuk);
                        din_keterangan = (AutoCompleteTextView) view.findViewById(R.id.din_keterangan);
                        din_get = (CircularProgressButton) view.findViewById(R.id.din_get);
                        din_get.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                if (din_newnobukti.getText().toString().trim().length()>0){
                                    Cursor cr = db.rawQuery("SELECT * FROM tbl_distin WHERE `No_bukti`='"+din_newnobukti.getText().toString()+"'",null);
                                    cr.moveToFirst();
                                    if (cr.getCount()>0){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
                                        builder.setTitle("Data Already Exists");
                                        builder.setCancelable(false);
                                        builder.setMessage("Data sudah ada, apakah anda ingin mengunduh ulang?");
                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                        builder.setPositiveButton("Unduh", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                OpenDistin();
                                            }
                                        });
                                        builder.show();
                                    } else {
                                        OpenDistin();
                                    }
                                } else {
                                    din_newnobukti_head.setHelperText("*Required");
                                    din_newnobukti.requestFocus();
                                }
                            }
                        });
                        din_newnobukti.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (din_newnobukti.getText().toString().trim().length()>0){
                                    din_newnobukti_head.setHelperText(null);
                                } else {
                                    din_newnobukti_head.setHelperText("*Required");
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        din_tglmasuk.setText(Today);
                        din_tglmasuk_head.setEndIconDrawable(R.drawable.ico_calendar);
                        din_tglmasuk_head.setEndIconOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                din_tglmasuk.callOnClick();
                            }
                        });
                        din_tglmasuk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                String insertDate = din_tglmasuk.getText().toString();
                                String[] items1 = insertDate.split("-");
                                String d1=items1[2];
                                String m1=items1[1];
                                String y1=items1[0];
                                int datenow = Integer.parseInt(d1);
                                int monthnow = Integer.parseInt(m1)-1;
                                int yearnow = Integer.parseInt(y1);
                                final Calendar calendar = Calendar.getInstance();
                                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                DatePickerDialog datePickerDialog= new DatePickerDialog(FHistory_Distin.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        calendar.set(year,month,dayOfMonth);
                                        din_tglmasuk.setText(simpleDateFormat.format(calendar.getTime()));
                                        Today=din_tglmasuk.getText().toString();
                                    }
                                },yearnow,monthnow,datenow);
                                datePickerDialog.setTitle("Pilih Tanggal");
                                datePickerDialog.show();
                            }
                        });
                        din_newnobukti_head.setEndIconDrawable(R.drawable.ico_scan_wrntext);
                        din_newnobukti_head.setEndIconOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                AlertDialog.Builder builderscan = new AlertDialog.Builder(FHistory_Distin.this);
                                LayoutInflater inflater2 = FHistory_Distin.this.getLayoutInflater();
                                View view2 = inflater2.inflate(R.layout.cust_popup_scan, null);
                                ZXingScannerView camera = (ZXingScannerView) view2.findViewById(R.id.camera);
                                camera.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        camera.toggleFlash();
                                    }
                                });
                                camera.setResultHandler(new ZXingScannerView.ResultHandler() {
                                    @Override
                                    public void handleResult(Result result) {
                                        din_newnobukti_head.setHelperText(null);
                                        din_newnobukti.setText(result.getText());
                                        camera.stopCamera();
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                show2.dismiss();
                                            }
                                        }, 500);
                                    }
                                });
                                builderscan.setCancelable(false);
                                camera.startCamera();
                                builderscan.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        show2.dismiss();
                                    }
                                });
                                builderscan.setView(view2);
                                show2=builderscan.show();
                                show2.getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
                            }
                        });
                        buildernewdis.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                show.dismiss();
                            }
                        });
                        buildernewdis.setCancelable(false);
                        buildernewdis.setView(view);
                        show=buildernewdis.show();
                    }
                });
                getdata();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
                builder.setTitle("Session ended");
                builder.setCancelable(false);
                builder.setMessage("Sesi anda berakhir, mohon login kembali...");
                builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.execSQL("delete from tbl_user");
                        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                        prefs.edit().clear().commit();
                        Intent formku = new Intent(FHistory_Distin.this, Flogin.class);
                        startActivity(formku);
                        finish();
                    }
                });
                builder.show();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
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

    private void getdata(){
        ArrayList<String> noukti2 = new ArrayList<>();
        ArrayList<String> pengirim2 = new ArrayList<>();
        ArrayList<String> penerima2 = new ArrayList<>();
        ArrayList<String> qtydikirim2 = new ArrayList<>();
        ArrayList<String> qtyditerima2 = new ArrayList<>();
        ArrayList<String> tgldibuat2 = new ArrayList<>();
        ArrayList<String> tglditerima2 = new ArrayList<>();
        Cursor cursor = db.rawQuery("select `No_bukti`,'['||`Kode_Pengirim`||'] '||`nama_pengirim` as `pengirim`,'['||`Kode_Penerima`||'] '||`nama_penerima` as `penerima`,`Total_qty`||' Pcs' as `Total_qty`,`Total_qty_receive`||' Pcs' as `Total_qty_receive`,`tanggal_buat`,`tanggal_masuk` from `tbl_distin` where `kode_penerima`='"+kodecb+"'", null);
        if (cursor.getCount()>0) {
            din_nodata_anim.setVisibility(View.GONE);
            cursor.moveToFirst();
            for (int count = 0; count < cursor.getCount(); count++) {
                cursor.moveToPosition(count);
                noukti2.add(cursor.getString(0));
                pengirim2.add(cursor.getString(1));
                penerima2.add(cursor.getString(2));
                qtydikirim2.add(cursor.getString(3));
                qtyditerima2.add(cursor.getString(4));
                tgldibuat2.add(cursor.getString(5));
                tglditerima2.add(cursor.getString(6));
            }
            listDistHistAdapter = new ListDistHistAdapter(FHistory_Distin.this, noukti2,
                    pengirim2, penerima2, qtydikirim2,
                    qtyditerima2, tgldibuat2, tglditerima2);
            din_historylist.setAdapter(listDistHistAdapter);
        } else {
            din_nodata_anim.setVisibility(View.VISIBLE);
        }
        din_historylist.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cd = db.rawQuery("SELECT * FROM tbl_barang",null);
                if (cd.getCount()>=0) {
                    Intent Formku = new Intent(FHistory_Distin.this, FDistin.class);
                    Formku.putExtra("id", listDistHistAdapter.getName(position));
                    startActivity(Formku);
                    finish();
                } else {
                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Tidak ada data barang", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            }
        });
    }

    public void onBackPressed() {
        Intent Formku = new Intent(FHistory_Distin.this, FMenuList.class);
        startActivity(Formku);
        finish();
    }

    private void deleteitem() {
        if(listDistHistAdapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = listDistHistAdapter.getCheckList();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    String name = listDistHistAdapter.getName(i);
                    listDistHistAdapter.delete(i);
                    db.execSQL("DELETE FROM tbl_distin_detail WHERE `No_bukti`='"+name+"'");
                    db.execSQL("DELETE FROM tbl_distin WHERE `No_bukti`='"+name+"'");
                    getdata();
                }
            }
            //
            File folder2 = new File(getExternalFilesDir(null)+"/CSV/BIN");
            if (folder2.exists()){
                for(File tempFile2 : folder2.listFiles()) {
                    tempFile2.delete();
                }
            }
            else {
            }
        }else{
            Toast.makeText(FHistory_Distin.this,"Belum ada list yang terpilih", Toast.LENGTH_LONG).show();
        }
    }

    AbsListView.MultiChoiceModeListener modeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_history,menu);
            isActionMode = true;
            actionMode=mode;
            listDistHistAdapter.setVisible(true);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.bdeletecount)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
                builder.setTitle("Konfirmasi");
                builder.setMessage("Hapus data?");
                builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteitem();
                        mode.finish();
                    }
                });
                builder.show();
            }
            else
            if (id == R.id.bsharecount)
            {
                File folder = new File(getExternalFilesDir(null)+"/CSV/BIN");
                if (folder.exists()){
                    for(File tempFile : folder.listFiles()) {
                        tempFile.delete();
                    }
                }
                else {
                }
                csvshare();
                mode.finish();
            }
            else
            if (id == R.id.bsaveinternal)
            {
                if (listDistHistAdapter.getCountSelectedCheckBoxes()>0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
                    LayoutInflater inflat = (LayoutInflater) FHistory_Distin.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view2 = inflat.inflate(R.layout.cust_popup_nameshare, null);
                    TextInputLayout share_name_head = (TextInputLayout) view2.findViewById(R.id.share_name_head);
                    AutoCompleteTextView share_name = (AutoCompleteTextView) view2.findViewById(R.id.share_name);
                    share_name.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (share_name.getText().toString().trim().length()>0) {
                                share_name_head.setHelperText(null);
                            } else {
                                share_name_head.setHelperText("*Required");
                                share_name.requestFocus();
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                    builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (share_name.getText().toString().length()>0) {
                                File folder = new File(getExternalFilesDir(null),"/CSV/DISTRIBUSI(IN)");
                                String nama = folder+"/"+share_name.getText().toString().trim()+".csv";
                                File file = new File(nama);
                                if(file.exists()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
                                    builder.setTitle("Duplicated file");
                                    builder.setCancelable(false);
                                    builder.setMessage("File sudah ada, timpa data?");
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    builder.setPositiveButton("Timpa", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            file.delete();
                                            SaveCSV(share_name.getText().toString().trim());
                                            show.dismiss();
                                            mode.finish();
                                        }
                                    });
                                    builder.show();
                                } else {
                                    SaveCSV(share_name.getText().toString().trim());
                                    show.dismiss();
                                    mode.finish();
                                }
                            } else {
                                share_name_head.setHelperText("*Required");
                                share_name.requestFocus();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            show.dismiss();
                        }
                    });
                    builder.setCancelable(false);
                    builder.setView(view2);
                    show=builder.show();
                } else {
                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Nothing selected", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            }
            else
            if (id==R.id.bupload) {
                File folder = new File(getExternalFilesDir(null)+"/CSV/BIN");
                if (folder.exists()){
                    for(File tempFile : folder.listFiles()) {
                        tempFile.delete();
                    }
                }
                else {
                }
                makecsvupload();
                mode.finish();
            }
            return true;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
//            toolbar.setVisibility(View.VISIBLE);
            if(listDistHistAdapter.getCount() > 0){
                List<Boolean> checked = listDistHistAdapter.getCheckList();
                for(int i = checked.size() - 1; i >= 0; i--){
                    listDistHistAdapter.setChecked(i,false);
                }
            }
            listDistHistAdapter.setVisible(false);
            isActionMode = false;
            actionMode=null;
        }
    };

    public void SaveCSV(String namanya) {
        if(listDistHistAdapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = listDistHistAdapter.getCheckList();
            ArrayList<String>value=new ArrayList<>();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    value.add(listDistHistAdapter.getName(i));
                }
            }
            String val = value.toString().replace("[","'");
            String val2 = val.replace("]","'");
            String Valx = val2.replace(" ","");
            String val3 = Valx.replace(",","','");
            Cursor curCSV = db.rawQuery("select `det`.`No_bukti` as 'NO BUKTI',`det`.`Kode_Pengirim` as 'KODE PENGIRIM',`det`.`nama_pengirim` as 'NAMA PENGIRIM',`det`.`Kode_Penerima` as 'KODE PENERIMA',`det`.`nama_penerima` AS 'NAMA PENERIMA',`hd`.`kode_departemen` as 'KODE DEPARTEMEN',`hd`.`Total_qty` as 'TOTAL QTY',`hd`.`total_beli` as 'TOTAL BELI',`hd`.`total_jual` as 'TOTAL JUAL',`hd`.`kode_user` as 'KODE USER',`hd`.`tanggal_buat` as 'TANGGAL BUAT',`hd`.`tanggal_masuk` as 'TANGGAL MASUK',`hd`.`tgl_kirim` as 'TANGGAL KIRIM',`hd`.`No_SuratJalan` as 'NO SURAT JALAN',`hd`.`no_pol` as 'NO POLISI',`hd`.`expedisi` as  'EXPEDISI',`det`.`kode_barang` as 'KODE BARANG',`brg`.`nama_barang` as 'NAMA BARANG',`det`.`Qty` as 'TERKIRIM',`det`.`Qty_receive` as 'DITERIMA',`det`.`harga_beli` as 'HARGA BELI'," +
                    "`det`.`harga_jual` as 'HARGA JUAL',`hd`.`nama_penghitung` as 'NAMA PENGHITUNG',`hd`.`keterangan` as 'KETERANGAN' from `tbl_distin_detail` as `det`LEFT JOIN `tbl_distin` as `hd` on `hd`.`No_bukti`=`det`.`No_bukti` LEFT JOIN `tbl_barang` as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`No_bukti` in (" + val3 + ")", null);
            if (curCSV.getCount() >= 1) {
                try {
                    File folder = new File(getExternalFilesDir(null),"/CSV/DISTRIBUSI(IN)");
                    if (folder.exists()){
                    } else
                    {
                        folder.mkdirs();
                    }
                    String nama=null;
                    if (listDistHistAdapter.getCountSelectedCheckBoxes()>1){
                        nama = folder+"/"+namanya+".csv";
                    } else {
                        nama = folder+"/"+namanya+".csv";
                    }
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while (curCSV.moveToNext()) {
                        String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                                curCSV.getString(3),curCSV.getString(4),curCSV.getString(5),
                                curCSV.getString(6),curCSV.getString(7),curCSV.getString(8),
                                curCSV.getString(9),curCSV.getString(10),curCSV.getString(11),
                                curCSV.getString(12),curCSV.getString(13),curCSV.getString(14),
                                curCSV.getString(15),curCSV.getString(16),curCSV.getString(17),
                                curCSV.getString(18),curCSV.getString(19),curCSV.getString(20),
                                curCSV.getString(21),curCSV.getString(22),curCSV.getString(23)};
                        csvWrite.writeNext(arrStr);
                    }
                    csvWrite.close();
                    curCSV.close();
                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "File tersimpan : "+nama, Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                } catch (Exception sqlEx) {
                    Toast.makeText(FHistory_Distin.this,sqlEx.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Data tidak ada", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        }
    }

    private String generateKey() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    private void upload(String name,String filepath) {
        ProgressDialog mProgressDialog = new ProgressDialog(FHistory_Distin.this);
        mProgressDialog.setMessage("Uploading file");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();
        StorageReference fileref = storageRef.child(name);
        Query qry = dbRef.orderByChild("name").equalTo(name);
        qry.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mProgressDialog.dismiss();
                    String keynya="";
                    for (DataSnapshot snap: snapshot.getChildren()) {
                        keynya = snap.child("key").getValue(String.class);
                        Log.v("Key",snap.child("key").getValue(String.class));
                        AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
                        builder.setTitle("Duplicated data");
                        builder.setCancelable(false);
                        builder.setMessage("Data sudah ada, timpa data?");
                        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                snap.getRef().removeValue();
                                StorageReference desertRef = storageRef.child(name);
                                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProgressDialog.show();
                                        fileref.putFile(Uri.fromFile(new File(filepath)))
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                mProgressDialog.dismiss();
                                                                String key= id_user+kodecb+generateKey();
                                                                String uploadURL = uri.toString();
                                                                UploadCSV upl = new UploadCSV(name,uploadURL,key,usr_alias,"","","distin");
                                                                String uploadid = dbRef.push().getKey();
                                                                dbRef.child(uploadid).setValue(upl);
                                                                Toast.makeText(FHistory_Distin.this,"Upload success", Toast.LENGTH_LONG).show();
                                                                AlertDialog.Builder buil = new AlertDialog.Builder(FHistory_Distin.this);
                                                                buil.setCancelable(false);
                                                                buil.setTitle(key);
                                                                buil.setMessage("Key will expired after 7 days");
                                                                buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                                        ClipData clip = ClipData.newPlainText("label", key);
                                                                        clipboard.setPrimaryClip(clip);
                                                                        Toast.makeText(FHistory_Distin.this,"Copied",Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                                buil.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                    }
                                                                });
                                                                buil.show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                mProgressDialog.dismiss();
                                                                Toast.makeText(FHistory_Distin.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(FHistory_Distin.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                })
                                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                        double progres = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                                        mProgressDialog.setProgress((int) progres);
                                                    }
                                                });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(FHistory_Distin.this,"Failed to overwrite to cloud : "+exception.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        String finalKeynya = keynya;
                        builder.setNegativeButton("Show Key", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder buil = new AlertDialog.Builder(FHistory_Distin.this);
                                buil.setCancelable(false);
                                buil.setTitle(finalKeynya);
                                buil.setMessage("Key will expired after 7 days");
                                buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("label", finalKeynya);
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(FHistory_Distin.this,"Copied",Toast.LENGTH_LONG).show();
                                    }
                                });
                                buil.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                buil.show();
                            }
                        });
                        builder.show();
                        break;
                    }
                } else {
//                    mProgressDialog.dismiss();
//                    mProgressDialog.show();
                    fileref.putFile(Uri.fromFile(new File(filepath)))
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            mProgressDialog.dismiss();
                                            String key= id_user+kodecb+generateKey();
                                            String uploadURL = uri.toString();
                                            UploadCSV upl = new UploadCSV(name,uploadURL,key,usr_alias,"","","distin");
                                            String uploadid = dbRef.push().getKey();
                                            dbRef.child(uploadid).setValue(upl);
                                            Toast.makeText(FHistory_Distin.this,"Upload success", Toast.LENGTH_LONG).show();
                                            AlertDialog.Builder buil = new AlertDialog.Builder(FHistory_Distin.this);
                                            buil.setCancelable(false);
                                            buil.setTitle(key);
                                            buil.setMessage("Key will expired after 7 days");
                                            buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("label", key);
                                                    clipboard.setPrimaryClip(clip);
                                                    Toast.makeText(FHistory_Distin.this,"Copied",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            buil.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            buil.show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(FHistory_Distin.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(FHistory_Distin.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    double progres = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                    mProgressDialog.setProgress((int) progres);
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mProgressDialog.dismiss();
                Toast.makeText(FHistory_Distin.this,error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void makecsvupload() {
        if(listDistHistAdapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = listDistHistAdapter.getCheckList();
            ArrayList<String>value=new ArrayList<>();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    value.add(listDistHistAdapter.getName(i));
                }
            }
            String val = value.toString().replace("[","'");
            String val2 = val.replace("]","'");
            String Valx = val2.replace(" ","");
            String val3 = Valx.replace(",","','");
            Cursor curCSV = db.rawQuery("select `det`.`No_bukti` as 'NO BUKTI',`det`.`Kode_Pengirim` as 'KODE PENGIRIM',`det`.`nama_pengirim` as 'NAMA PENGIRIM',`det`.`Kode_Penerima` as 'KODE PENERIMA',`det`.`nama_penerima` AS 'NAMA PENERIMA',`hd`.`kode_departemen` as 'KODE DEPARTEMEN',`hd`.`Total_qty` as 'TOTAL QTY',`hd`.`total_beli` as 'TOTAL BELI',`hd`.`total_jual` as 'TOTAL JUAL',`hd`.`kode_user` as 'KODE USER',`hd`.`tanggal_buat` as 'TANGGAL BUAT',`hd`.`tanggal_masuk` as 'TANGGAL MASUK',`hd`.`tgl_kirim` as 'TANGGAL KIRIM',`hd`.`No_SuratJalan` as 'NO SURAT JALAN',`hd`.`no_pol` as 'NO POLISI',`hd`.`expedisi` as  'EXPEDISI',`det`.`kode_barang` as 'KODE BARANG',`brg`.`nama_barang` as 'NAMA BARANG',`det`.`Qty` as 'TERKIRIM',`det`.`Qty_receive` as 'DITERIMA',`det`.`harga_beli` as 'HARGA BELI'," +
                    "`det`.`harga_jual` as 'HARGA JUAL',`hd`.`nama_penghitung` as 'NAMA PENGHITUNG',`hd`.`keterangan` as 'KETERANGAN' from `tbl_distin_detail` as `det`LEFT JOIN `tbl_distin` as `hd` on `hd`.`No_bukti`=`det`.`No_bukti` LEFT JOIN `tbl_barang` as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`No_bukti` in (" + val3 + ")", null);
            if (curCSV.getCount() >= 1) {
                try {
                    File folder = new File(getExternalFilesDir(null),"/CSV/BIN");
                    if (folder.exists()){
                    } else
                    {
                        folder.mkdirs();
                    }
                    String nama=null;
                    String file=null;
                    if (listDistHistAdapter.getCountSelectedCheckBoxes()>1){
                        String salt = getSaltString();
                        nama = folder+"/DIN-Batch-"+salt+".csv";
                        file = "DIN-Batch-"+salt+".csv";
                    } else {
                        List<Boolean> checked1 = listDistHistAdapter.getCheckList();
                        for(int i = checked1.size() - 1; i >= 0; i--) {
                            if (checked1.get(i)) {
                                nama = folder+"/"+listDistHistAdapter.getName(i)+".csv";
                                file = listDistHistAdapter.getName(i)+".csv";
                            }
                        }
                    }
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while (curCSV.moveToNext()) {
                        String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                                curCSV.getString(3),curCSV.getString(4),curCSV.getString(5),
                                curCSV.getString(6),curCSV.getString(7),curCSV.getString(8),
                                curCSV.getString(9),curCSV.getString(10),curCSV.getString(11),
                                curCSV.getString(12),curCSV.getString(13),curCSV.getString(14),
                                curCSV.getString(15),curCSV.getString(16),curCSV.getString(17),
                                curCSV.getString(18),curCSV.getString(19),curCSV.getString(20),
                                curCSV.getString(21),curCSV.getString(22),curCSV.getString(23)};
                        csvWrite.writeNext(arrStr);
                    }
                    csvWrite.close();
                    curCSV.close();
                    upload(file,nama);
                } catch (Exception sqlEx) {
                    Toast.makeText(FHistory_Distin.this,sqlEx.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Data tidak ada", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        }
    }

    public void csvshare() {
        if(listDistHistAdapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = listDistHistAdapter.getCheckList();
            ArrayList<String>value=new ArrayList<>();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    value.add(listDistHistAdapter.getName(i));
                }
            }
            String val = value.toString().replace("[","'");
            String val2 = val.replace("]","'");
            String Valx = val2.replace(" ","");
            String val3 = Valx.replace(",","','");
            Cursor curCSV = db.rawQuery("select `det`.`No_bukti` as 'NO BUKTI',`det`.`Kode_Pengirim` as 'KODE PENGIRIM',`det`.`nama_pengirim` as 'NAMA PENGIRIM',`det`.`Kode_Penerima` as 'KODE PENERIMA',`det`.`nama_penerima` AS 'NAMA PENERIMA',`hd`.`kode_departemen` as 'KODE DEPARTEMEN',`hd`.`Total_qty` as 'TOTAL QTY',`hd`.`total_beli` as 'TOTAL BELI',`hd`.`total_jual` as 'TOTAL JUAL',`hd`.`kode_user` as 'KODE USER',`hd`.`tanggal_buat` as 'TANGGAL BUAT',`hd`.`tanggal_masuk` as 'TANGGAL MASUK',`hd`.`tgl_kirim` as 'TANGGAL KIRIM',`hd`.`No_SuratJalan` as 'NO SURAT JALAN',`hd`.`no_pol` as 'NO POLISI',`hd`.`expedisi` as  'EXPEDISI',`det`.`kode_barang` as 'KODE BARANG',`brg`.`nama_barang` as 'NAMA BARANG',`det`.`Qty` as 'TERKIRIM',`det`.`Qty_receive` as 'DITERIMA',`det`.`harga_beli` as 'HARGA BELI'," +
                    "`det`.`harga_jual` as 'HARGA JUAL',`hd`.`nama_penghitung` as 'NAMA PENGHITUNG',`hd`.`keterangan` as 'KETERANGAN' from `tbl_distin_detail` as `det`LEFT JOIN `tbl_distin` as `hd` on `hd`.`No_bukti`=`det`.`No_bukti` LEFT JOIN `tbl_barang` as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`No_bukti` in (" + val3 + ")", null);
            if (curCSV.getCount() >= 1) {
                try {
                    File folder = new File(getExternalFilesDir(null),"/CSV/BIN");
                    if (folder.exists()){
                    } else
                    {
                        folder.mkdirs();
                    }
                    String nama=null;
                    if (listDistHistAdapter.getCountSelectedCheckBoxes()>1){
                        nama = folder+"/DIN-Batch-"+getSaltString()+".csv";
                    } else {
                        List<Boolean> checked1 = listDistHistAdapter.getCheckList();
                        for(int i = checked1.size() - 1; i >= 0; i--) {
                            if (checked1.get(i)) {
                                nama = folder+"/"+listDistHistAdapter.getName(i)+getSaltString()+".csv";
                            }
                        }
                    }
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while (curCSV.moveToNext()) {
                        String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                                curCSV.getString(3),curCSV.getString(4),curCSV.getString(5),
                                curCSV.getString(6),curCSV.getString(7),curCSV.getString(8),
                                curCSV.getString(9),curCSV.getString(10),curCSV.getString(11),
                                curCSV.getString(12),curCSV.getString(13),curCSV.getString(14),
                                curCSV.getString(15),curCSV.getString(16),curCSV.getString(17),
                                curCSV.getString(18),curCSV.getString(19),curCSV.getString(20),
                                curCSV.getString(21),curCSV.getString(22),curCSV.getString(23)};
                        csvWrite.writeNext(arrStr);
                    }
                    csvWrite.close();
                    curCSV.close();
                    shareFile(nama);
                } catch (Exception sqlEx) {
                    Toast.makeText(FHistory_Distin.this,sqlEx.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Data tidak ada", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        }
    }

    private void shareFile(String filePath) {
        File f = new File(filePath);
        File fileWithinMyDir = new File(filePath);
        if (fileWithinMyDir.exists()) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName(), fileWithinMyDir);
            Intent intent = ShareCompat.IntentBuilder.from(this)
                    .setStream(uri)
                    .setType("text/csv")
                    .getIntent()
                    .setAction(Intent.ACTION_SEND)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 3) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    private void OpenDistin() {
        din_get.startAnimation();
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='distribusiout'",null);
        cr.moveToFirst();
//        String keterangan = din_keterangan.getText().toString();
        String tglmasuk = din_tglmasuk.getText().toString();
        String nobukti = din_newnobukti.getText().toString();
        String jsonurl = API+cr.getString(1)+din_newnobukti.getText().toString()+"/penerima/"+kodecb;
        Log.v("OpenDistin",jsonurl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, jsonurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("OpenDistin-response",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status==200) {
                                db.execSQL("delete from tbl_distin where `No_bukti`='"+nobukti+"'");
                                db.execSQL("delete from tbl_distin_detail where `No_bukti`='"+nobukti+"'");
                                String NoBuk=null, keterangan=null, Kode_departemen=null, expedisi=null, no_pol=null, No_SuratJalan=null, tgl_kirim=null, Kode_user=null, Total_jual=null, Total_beli=null,kd_penerima=null,Penerima=null,kd_pengirim=null,Pengirim=null,TotalQTYHead=null,tglbuat=null;
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int position = 0; position < jsonArray.length(); position++) {
                                    JSONObject row = jsonArray.getJSONObject(position);
                                    NoBuk = row.getString("No_bukti");
                                    kd_penerima = row.getString("Kode_Penerima");
                                    Penerima = row.getString("nama_penerima");
                                    kd_pengirim = row.getString("Kode_Pengirim");
                                    Pengirim = row.getString("nama_pengirim");
                                    TotalQTYHead = row.getString("Total_qty");
                                    tglbuat = row.getString("tgl_buat");
                                    Kode_departemen = row.getString("Kode_Departemen");
                                    Total_beli= row.getString("Total_Beli");
                                    Total_jual=row.getString("Total_Jual");
                                    Kode_user=row.getString("kode_user");
                                    tgl_kirim=row.getString("Tgl_Kirim");
                                    No_SuratJalan=row.getString("No_SuratJalan");
                                    no_pol=row.getString("no_pol");
                                    expedisi=row.getString("Expedisi");
                                    String No_bukti = row.getString("No_bukti");
                                    String harga_beli = row.getString("Harga_beli");
                                    String harga_jual = row.getString("Harga_jual");
                                    String Kode_Penerima = row.getString("Kode_Penerima");
                                    String Kode_Pengirim = row.getString("Kode_Pengirim");
                                    String Kode_barang = row.getString("Kode_barang");
                                    String Qty = row.getString("Qty");
                                    String nama_penerima = row.getString("nama_penerima");
                                    String nama_pengirim = row.getString("nama_pengirim");
                                    keterangan = row.getString("Keterangan");
                                    db.execSQL("INSERT INTO tbl_distin_detail (`No_bukti`,`Kode_Penerima`,`nama_penerima`,`Kode_Pengirim`,`nama_pengirim`,`Kode_barang`,`Qty`,`Qty_receive`,`harga_beli`,`harga_jual`) " +
                                            "VALUES('"+No_bukti+"','"+Kode_Penerima+"','"+nama_penerima+"','"+Kode_Pengirim+"','"+nama_pengirim+"','"+Kode_barang+"','"+Qty+"','0','"+harga_beli+"','"+harga_jual+"');");
                                }
                                db.execSQL("INSERT INTO tbl_distin (`No_bukti`,`Kode_Penerima`,`nama_penerima`,`Kode_Pengirim`,`nama_pengirim`,`Kode_departemen`,`Total_qty`,`Total_qty_receive`,`Total_beli`,`Total_jual`,`nama_penghitung`,`id_user`,`Kode_user`,`tgl_kirim`,`keterangan`,`tanggal_masuk`,`tanggal_buat`,`No_SuratJalan`,`no_pol`,`expedisi`) " +
                                        "VALUES('"+NoBuk+"','"+kd_penerima+"','"+Penerima+"','"+kd_pengirim+"','"+Pengirim+"','"+Kode_departemen+"','"+TotalQTYHead+"','0','"+Total_beli+"','"+Total_jual+"','"+usr_alias+"','"+id_user+"','"+Kode_user+"','"+tgl_kirim+"','"+keterangan+"','"+tglmasuk+"','"+tglbuat+"','"+No_SuratJalan+"','"+no_pol+"','"+expedisi+"');");
                                Intent Formku = new Intent(FHistory_Distin.this, FDistin.class);
                                Formku.putExtra("id", NoBuk);
                                startActivity(Formku);
                                finish();
                            } else {
                                String errorname = jsonObject.getString("message");
                                Log.v("Return-400",errorname);
                                Toast.makeText(FHistory_Distin.this,errorname, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.v("ParseException",e.getMessage());
                            AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
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
                        din_get.revertAnimation();
                        din_get.setBackgroundResource(R.drawable.round_ungu);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        din_get.revertAnimation();
                        din_get.setBackgroundResource(R.drawable.round_ungu);
                        String message = null; // error message, show it in toast or dialog, whatever you want
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                            message = "Cannot connect to Internet";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again later";
                        }  else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again later";
                        }
                        Log.v("Err",message);
                        AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Distin.this);
                        builder.setTitle("Connection problem");
                        builder.setCancelable(false);
                        builder.setMessage("Koneksi server bermasalah, Mohon cek koneksi anda kemudian refresh kembali");
                        builder.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                OpenDistin();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
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
        RequestQueue requestQueue = Volley.newRequestQueue(FHistory_Distin.this);
        requestQueue.add(stringRequest);
    }
}