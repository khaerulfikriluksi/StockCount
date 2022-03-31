package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.urban.stockcount1.CustomClass.ListAdapter;
import com.urban.stockcount1.CustomClass.ListItemDistinAdapter;
import com.urban.stockcount1.CustomClass.UploadCSV;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FDistin extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ToneGenerator toneGen1;
    private SQLiteDatabase db;
    private ZXingScannerView din_camera;
    private TextView din_evalue,din_nobukti;
    private LinearLayout din_laybarcode, din_laymanual;
    private AutoCompleteTextView din_eidmanual;
    private Button din_checkall,din_delselected;
    private String qrysearch;
    private AlertDialog show;
    private ListView din_listitem;
    private ListItemDistinAdapter adapter;
    private ArrayList<String> valu = new ArrayList<>();
    private StorageReference storageRef;
    private DatabaseReference dbRef;
    private String id_user,kodecb,usr_alias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fdistin);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(this.getResources().getColor(R.color.unguatas));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        storageRef = FirebaseStorage.getInstance().getReference("csv");
        dbRef = FirebaseDatabase.getInstance("https://stockapp-4abf1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("csv_data");
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        Cursor cc = db.rawQuery("SELECT * FROM `tbl_user`",null);
        cc.moveToFirst();
        if (cc.getCount()>0) {
            id_user = cc.getString(0);
            kodecb = cc.getString(5);
            usr_alias= cc.getString(4);
        } else {
            id_user = "00";
            kodecb = "UNK00";
            usr_alias= "ANONYMOUS";
        }
        din_camera = (ZXingScannerView) findViewById(R.id.din_camera);
        din_evalue = (TextView) findViewById(R.id.din_evalue);
        din_laybarcode = (LinearLayout) findViewById(R.id.din_laybarcode);
        din_laymanual = (LinearLayout) findViewById(R.id.din_laymanual);
        din_eidmanual = (AutoCompleteTextView) findViewById(R.id.din_eidmanual);
        Toolbar toolBar_distin = findViewById(R.id.toolBar_distin);
        setSupportActionBar(toolBar_distin);
        din_checkall = (Button) findViewById(R.id.din_checkall);
        din_delselected = (Button) findViewById(R.id.din_delselected);
        din_nobukti = (TextView) findViewById(R.id.din_nobukti);
        din_listitem = (ListView) findViewById(R.id.din_listitem);
        //
        toolBar_distin.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ico_menu_white));
        din_nobukti.setText(getIntent().getExtras().getString("id"));
        din_camera.setResultHandler(FDistin.this);
        din_camera.startCamera();
        new AdapterHelp().execute();
        //
        din_eidmanual.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (din_laymanual.getVisibility()==View.VISIBLE) {
                    String[] separated = s.toString().split(" : ");
                    Cursor cur = db.rawQuery("SELECT * FROM tbl_barang WHERE kode_barang='" + separated[0] + "'", null);
                    cur.moveToFirst();
                    if (cur.getCount() >= 1) {
                        String harga_beli=cur.getString(2);
                        String harga_jual=cur.getString(3);
                        db.execSQL("UPDATE tbl_distin_detail SET `Qty_receive`=`Qty_receive`+1 WHERE `kode_barang`='" + separated[0] + "' AND `No_bukti`='" + din_nobukti.getText().toString() + "'");
                        db.execSQL("INSERT INTO tbl_distin_detail (`No_bukti`,`kode_penerima`,`nama_penerima`,`kode_pengirim`,`nama_pengirim`,`kode_barang`,`qty`,`qty_receive`,`harga_beli`,`harga_jual`) SELECT * FROM (" +
                                "SELECT `No_bukti`,`kode_penerima`,`nama_penerima`,`kode_pengirim`,`nama_pengirim`,'"+separated[0]+"' as `kode_barang`,'0' as `qty`,'1' as `receive`,'"+harga_beli+"' as `harga_beli`,'"+harga_jual+"' as `harga_jual` FROM `tbl_distin` where `No_bukti`='"+ din_nobukti.getText().toString() +"') AS A WHERE NOT EXISTS (SELECT * FROM tbl_distin_detail WHERE `kode_barang`='" + separated[0] + "' AND `No_bukti`='" + din_nobukti.getText().toString() + "')");
                        int nilai = 0;
                        Cursor cr = db.rawQuery("select SUM(`Qty_receive`) as `receive` from `tbl_distin_detail` WHERE `No_bukti`='"+din_nobukti.getText().toString()+"' GROUP by `No_bukti`",null);
                        cr.moveToFirst();
                        if (cr.getCount()>0) {
                            nilai = cr.getInt(0);
                        } else {
                            nilai=0;
                        }
                        db.execSQL("UPDATE tbl_distin SET `Total_qty_receive`='"+nilai+"' WHERE `No_bukti`='"+din_nobukti.getText().toString()+"'");
                        din_eidmanual.setText("");
                        getbarang();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        din_checkall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.getCount() > 0){
                    Boolean set;
                    List<Boolean> checked = adapter.getCheckList();
                    if (adapter.getCount() == adapter.getCountSelectedCheckBoxes()){
                        set=false;
                    } else
                    {
                        set=true;
                    }
                    for(int i = checked.size() - 1; i >= 0; i--){
                        adapter.setChecked(i,set);
                    }
                }else{
                    Toast.makeText(FDistin.this,"List kosong", Toast.LENGTH_LONG).show();
                }
            }
        });
        din_delselected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.getCountSelectedCheckBoxes() > 0){
                    List<Boolean> checked = adapter.getCheckList();
                    for(int i = checked.size() - 1; i >= 0; i--){
                        if(checked.get(i)){
                            String name = adapter.getName(i);
                            adapter.delete(i);
                            db.execSQL("DELETE FROM tbl_distin_detail WHERE `kode_barang`='" + name + "' AND `No_bukti`='" + din_nobukti.getText().toString() + "'");
//                            db.execSQL("UPDATE tbl_distin_detail SET `Qty_receive`=0 WHERE `kode_barang`='" + name + "' AND `No_bukti`='" + din_nobukti.getText().toString() + "' AND `Qty`>0");
                        }
                    }
                    int nilai = 0;
                    Cursor cr = db.rawQuery("select SUM(`Qty_receive`) as `receive` from `tbl_distin_detail` WHERE `No_bukti`='"+din_nobukti.getText().toString()+"' GROUP by `No_bukti`",null);
                    cr.moveToFirst();
                    if (cr.getCount()>0) {
                        nilai = cr.getInt(0);
                    } else {
                        nilai=0;
                    }
                    db.execSQL("UPDATE tbl_distin SET `Total_qty_receive`='"+nilai+"' WHERE `No_bukti`='"+din_nobukti.getText().toString()+"'");
                    getbarang();
                }else{
                    Toast.makeText(FDistin.this,"Belum ada list yang terpilih", Toast.LENGTH_LONG).show();
                }
            }
        });
        toolBar_distin.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kembali();
            }
        });
        din_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                din_camera.toggleFlash();
            }
        });
        qrysearch = "select `det`.`kode_barang`,`brg`.`nama_barang`,`det`.`qty`||' Pcs' as `qty`,`det`.`Qty_receive` from `tbl_distin_detail` as `det` left join `tbl_barang` as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`No_bukti`='"+din_nobukti.getText().toString()+"'";
        getbarang();
    }

    private class AdapterHelp extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            valu.clear();
            Cursor cr = db.rawQuery("select * from tbl_barang",null);
            cr.moveToFirst();
            for (int count = 0; count < cr.getCount(); count++) {
                cr.moveToPosition(count);
                valu.add(cr.getString(0)+" : "+cr.getString(1));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            din_eidmanual.setAdapter(new LimitArrayAdapter<String>(FDistin.this,
                    android.R.layout.simple_dropdown_item_1line,
                    valu));
        }
    }

    public class LimitArrayAdapter<T> extends ArrayAdapter<T> {

        final int LIMIT = 10;
        //overload other constructors you're using
        public LimitArrayAdapter(Context context, int textViewResourceId,
                                 List<T> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public int getCount() {
            return Math.min(LIMIT, super.getCount());
        }

    }

    public void getbarang(){
        ArrayList<String> namabrg = new ArrayList<>();
        ArrayList<String> kodebrg = new ArrayList<>();
        ArrayList<String> qtysj = new ArrayList<>();
        ArrayList<String>qtyditerima = new ArrayList<>();
        Cursor cursor = db.rawQuery(qrysearch, null);
        cursor.moveToFirst();
        for (int count = 0; count < cursor.getCount(); count++) {
            cursor.moveToPosition(count);
            kodebrg.add(cursor.getString(0));
            namabrg.add(cursor.getString(1));
            qtysj.add(cursor.getString(2));
            qtyditerima.add(cursor.getString(3));
        }
        adapter = new ListItemDistinAdapter(FDistin.this,namabrg,kodebrg,qtysj,qtyditerima,din_nobukti.getText().toString());
        din_listitem.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        kembali();
    }

    @Override
    public void handleResult(Result result) {
        toneGen1.startTone(ToneGenerator.TONE_DTMF_0,150);
        din_evalue.setText(result.getText());
        din_camera.stopCamera();
        Cursor cur = db.rawQuery("SELECT * FROM tbl_barang WHERE kode_barang='"+result.getText()+"'",null);
        cur.moveToFirst();
        if (cur.getCount()>=1){
            String harga_beli=cur.getString(2);
            String harga_jual=cur.getString(3);
            db.execSQL("UPDATE tbl_distin_detail SET `Qty_receive`=`Qty_receive`+1 WHERE `kode_barang`='" + result.getText() + "' AND `No_bukti`='" + din_nobukti.getText().toString() + "'");
            db.execSQL("INSERT INTO tbl_distin_detail (`No_bukti`,`kode_penerima`,`nama_penerima`,`kode_pengirim`,`nama_pengirim`,`kode_barang`,`qty`,`qty_receive`,`harga_beli`,`harga_jual`) SELECT * FROM (" +
                    "SELECT `No_bukti`,`kode_penerima`,`nama_penerima`,`kode_pengirim`,`nama_pengirim`,'"+result.getText()+"' as `kode_barang`,'0' as `qty`,'1' as `receive`,'"+harga_beli+"' as `harga_beli`, '"+harga_jual+"' as `harga_jual` FROM `tbl_distin` where `No_bukti`='"+ din_nobukti.getText().toString() +"') AS A WHERE NOT EXISTS (SELECT * FROM tbl_distin_detail WHERE `kode_barang`='" + result.getText() + "' AND `No_bukti`='" + din_nobukti.getText().toString() + "')");
            int nilai = 0;
            Cursor cr = db.rawQuery("select SUM(`Qty_receive`) as `receive` from `tbl_distin_detail` WHERE `No_bukti`='"+din_nobukti.getText().toString()+"' GROUP by `No_bukti`",null);
            cr.moveToFirst();
            if (cr.getCount()>0) {
                nilai = cr.getInt(0);
            } else {
                nilai=0;
            }
            db.execSQL("UPDATE tbl_distin SET `Total_qty_receive`='"+nilai+"' WHERE `No_bukti`='"+din_nobukti.getText().toString()+"'");
            getbarang();
        }
        else {
            Toast.makeText(FDistin.this,"Kode barang belum terdaftar", Toast.LENGTH_LONG).show();
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                din_evalue.setText("Turn on Flash : Tap Cam Box");
                din_camera.startCamera();
                din_camera.resumeCameraPreview(FDistin.this);
            }
        }, 500);
    }
    //OnResumeBarcode
    @Override
    protected void onResume(){
        super.onResume();
        din_camera.setResultHandler(FDistin.this);
        din_camera.startCamera();
    }
    //onPauseBarcode
    @Override
    protected void onPause(){
        super.onPause();
        din_camera.stopCamera();
    }

    //ToolbarMenuButton
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.act_counting_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.bscan){
            if (din_laybarcode.getVisibility()==View.VISIBLE)
            {
                item.setIcon(R.drawable.ico_scan);
                din_camera.stopCamera();
                din_laybarcode.setVisibility(View.INVISIBLE);
                din_laymanual.setVisibility(View.VISIBLE);
                din_eidmanual.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(din_eidmanual, InputMethodManager.SHOW_IMPLICIT);
            }
            else
            {
                try {
                    hideSoftKeyboard(this);
                } catch (Exception e){

                }
                item.setIcon(R.drawable.ico_manual);
                din_laybarcode.setVisibility(View.VISIBLE);
                din_laymanual.setVisibility(View.INVISIBLE);
                din_camera.setResultHandler(FDistin.this);
                din_camera.startCamera();
            }
        } else
        if (id == R.id.bvoidcount)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(FDistin.this);
            builder.setTitle("Konfirmasi");
            builder.setMessage("Hapus data ini?");
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
                    db.execSQL("DELETE FROM tbl_distin where `No_bukti`='" + din_nobukti.getText().toString() + "'");
                    db.execSQL("DELETE FROM tbl_distin_detail where `No_bukti`='" + din_nobukti.getText().toString() + "'");
                    File folder = new File(getExternalFilesDir(null) + "/CSV/DISTRIBUSI(IN)");
                    if (folder.exists()) {
                        for (File tempFile : folder.listFiles()) {
                            tempFile.delete();
                        }
                    } else {
                    }
                    kembali();
                }
            });
            builder.show();
        } else
        if (id == R.id.bexportcsv){
            File folder = new File(getExternalFilesDir(null)+"/CSV/BIN");
            if (folder.exists()){
                for(File tempFile : folder.listFiles()) {
                    tempFile.delete();
                }
            }
            else {
            }
            Exportcsv();
        } else if (id == R.id.bsavecsv) {
            if (adapter.getCount()>0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FDistin.this);
                LayoutInflater inflat = (LayoutInflater) FDistin.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view2 = inflat.inflate(R.layout.cust_popup_nameshare, null);
                TextInputLayout share_name_head = (TextInputLayout) view2.findViewById(R.id.share_name_head);
                AutoCompleteTextView share_name = (AutoCompleteTextView) view2.findViewById(R.id.share_name);
                share_name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (share_name.getText().toString().trim().length() > 0) {
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
                        if (share_name.getText().toString().length() > 0) {
                            File folder = new File(getExternalFilesDir(null), "/CSV/DISTRIBUSI(IN)");
                            String nama = folder + "/" + share_name.getText().toString().trim() + ".csv";
                            File file = new File(nama);
                            if (file.exists()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(FDistin.this);
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
                                        SaveCsv(share_name.getText().toString().trim());
                                        show.dismiss();
                                    }
                                });
                                builder.show();
                            } else {
                                SaveCsv(share_name.getText().toString().trim());
                                show.dismiss();
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
                show = builder.show();
            } else {
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "List tidak ada", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        } else if (id==R.id.buploadcsv) {
            File folder = new File(getExternalFilesDir(null)+"/CSV/BIN");
            if (folder.exists()){
                for(File tempFile : folder.listFiles()) {
                    tempFile.delete();
                }
            }
            else {
            }
            makecsvupload();
        }
        return true;
    }

    private void kembali(){
        Intent Formku = new Intent(FDistin.this, FHistory_Distin.class);
        startActivity(Formku);
        finish();
    }

    public void SaveCsv(String namanya) {
        Cursor cd = db.rawQuery("SELECT * FROM tbl_distin_detail where No_bukti='" + din_nobukti.getText().toString() + "'", null);
        if (cd.getCount() >= 1) {
            try {
                File folder = new File(getExternalFilesDir(null), "/CSV/DISTRIBUSI(IN)");
                if (folder.exists()) {
                } else {
                    folder.mkdirs();
                }
                String nama = folder + "/" + namanya + ".csv";
                CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                Cursor curCSV = db.rawQuery("select `det`.`No_bukti` as 'NO BUKTI',`det`.`Kode_Pengirim` as 'KODE PENGIRIM',`det`.`nama_pengirim` as 'NAMA PENGIRIM',`det`.`Kode_Penerima` as 'KODE PENERIMA',`det`.`nama_penerima` AS 'NAMA PENERIMA',`hd`.`kode_departemen` as 'KODE DEPARTEMEN',`hd`.`Total_qty` as 'TOTAL QTY',`hd`.`total_beli` as 'TOTAL BELI',`hd`.`total_jual` as 'TOTAL JUAL',`hd`.`kode_user` as 'KODE USER',`hd`.`tanggal_buat` as 'TANGGAL BUAT',`hd`.`tanggal_masuk` as 'TANGGAL MASUK',`hd`.`tgl_kirim` as 'TANGGAL KIRIM',`hd`.`No_SuratJalan` as 'NO SURAT JALAN',`hd`.`no_pol` as 'NO POLISI',`hd`.`expedisi` as  'EXPEDISI',`det`.`kode_barang` as 'KODE BARANG',`brg`.`nama_barang` as 'NAMA BARANG',`det`.`Qty` as 'TERKIRIM',`det`.`Qty_receive` as 'DITERIMA',`det`.`harga_beli` as 'HARGA BELI'," +
                        "`det`.`harga_jual` as 'HARGA JUAL',`hd`.`nama_penghitung` as 'NAMA PENGHITUNG',`hd`.`keterangan` as 'KETERANGAN' from `tbl_distin_detail` as `det`LEFT JOIN `tbl_distin` as `hd` on `hd`.`No_bukti`=`det`.`No_bukti` LEFT JOIN `tbl_barang` as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`No_bukti` ='"+din_nobukti.getText().toString()+"'", null);
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
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Saved to : "+nama, Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            } catch (Exception sqlEx) {
                Toast.makeText(FDistin.this, sqlEx.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "List tidak ada", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
    }

    public void Exportcsv() {
        Cursor cd = db.rawQuery("SELECT * FROM tbl_distin_detail where No_bukti='" + din_nobukti.getText().toString() + "'", null);
        if (cd.getCount() >= 1) {
            try {
                File folder = new File(getExternalFilesDir(null), "/CSV/BIN");
                if (folder.exists()) {
                } else {
                    folder.mkdirs();
                }
                String nama = folder + "/" + din_nobukti.getText().toString() + ".csv";
                CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                Cursor curCSV = db.rawQuery("select `det`.`No_bukti` as 'NO BUKTI',`det`.`Kode_Pengirim` as 'KODE PENGIRIM',`det`.`nama_pengirim` as 'NAMA PENGIRIM',`det`.`Kode_Penerima` as 'KODE PENERIMA',`det`.`nama_penerima` AS 'NAMA PENERIMA',`hd`.`kode_departemen` as 'KODE DEPARTEMEN',`hd`.`Total_qty` as 'TOTAL QTY',`hd`.`total_beli` as 'TOTAL BELI',`hd`.`total_jual` as 'TOTAL JUAL',`hd`.`kode_user` as 'KODE USER',`hd`.`tanggal_buat` as 'TANGGAL BUAT',`hd`.`tanggal_masuk` as 'TANGGAL MASUK',`hd`.`tgl_kirim` as 'TANGGAL KIRIM',`hd`.`No_SuratJalan` as 'NO SURAT JALAN',`hd`.`no_pol` as 'NO POLISI',`hd`.`expedisi` as  'EXPEDISI',`det`.`kode_barang` as 'KODE BARANG',`brg`.`nama_barang` as 'NAMA BARANG',`det`.`Qty` as 'TERKIRIM',`det`.`Qty_receive` as 'DITERIMA',`det`.`harga_beli` as 'HARGA BELI'," +
                        "`det`.`harga_jual` as 'HARGA JUAL',`hd`.`nama_penghitung` as 'NAMA PENGHITUNG',`hd`.`keterangan` as 'KETERANGAN' from `tbl_distin_detail` as `det`LEFT JOIN `tbl_distin` as `hd` on `hd`.`No_bukti`=`det`.`No_bukti` LEFT JOIN `tbl_barang` as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`No_bukti` ='"+din_nobukti.getText().toString()+"'", null);
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
                Toast.makeText(FDistin.this, sqlEx.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "List tidak ada", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
    }

    private void shareFile(String filePath) {
        File f = new File(filePath);
        File fileWithinMyDir = new File(filePath);
        if (fileWithinMyDir.exists()) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName(), fileWithinMyDir);
            Intent intent = ShareCompat.IntentBuilder.from(this)
                    .setStream(uri) // uri from FileProvider
                    .setType("text/csv")
                    .getIntent()
                    .setAction(Intent.ACTION_SEND) //Change if needed
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
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
        ProgressDialog mProgressDialog = new ProgressDialog(FDistin.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(FDistin.this);
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
                                                                Toast.makeText(FDistin.this,"Upload success", Toast.LENGTH_LONG).show();
                                                                AlertDialog.Builder buil = new AlertDialog.Builder(FDistin.this);
                                                                buil.setCancelable(false);
                                                                buil.setTitle(key);
                                                                buil.setMessage("Key will expired after 7 days");
                                                                buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                                        ClipData clip = ClipData.newPlainText("label", key);
                                                                        clipboard.setPrimaryClip(clip);
                                                                        Toast.makeText(FDistin.this,"Copied",Toast.LENGTH_LONG).show();
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
                                                                Toast.makeText(FDistin.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(FDistin.this,e.getMessage(), Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(FDistin.this,"Failed to overwrite to cloud : "+exception.getMessage(), Toast.LENGTH_LONG).show();
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
                                AlertDialog.Builder buil = new AlertDialog.Builder(FDistin.this);
                                buil.setCancelable(false);
                                buil.setTitle(finalKeynya);
                                buil.setMessage("Key will expired after 7 days");
                                buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("label", finalKeynya);
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(FDistin.this,"Copied",Toast.LENGTH_LONG).show();
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
                                            Toast.makeText(FDistin.this,"Upload success", Toast.LENGTH_LONG).show();
                                            AlertDialog.Builder buil = new AlertDialog.Builder(FDistin.this);
                                            buil.setCancelable(false);
                                            buil.setTitle(key);
                                            buil.setMessage("Key will expired after 7 days");
                                            buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("label", key);
                                                    clipboard.setPrimaryClip(clip);
                                                    Toast.makeText(FDistin.this,"Copied",Toast.LENGTH_LONG).show();
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
                                            Toast.makeText(FDistin.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(FDistin.this,e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(FDistin.this,error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void makecsvupload() {
        Cursor cd = db.rawQuery("SELECT * FROM tbl_distin_detail where No_bukti='" + din_nobukti.getText().toString() + "'", null);
        if (cd.getCount() >= 1) {
            try {
                File folder = new File(getExternalFilesDir(null), "/CSV/BIN");
                if (folder.exists()) {
                } else {
                    folder.mkdirs();
                }
                String nama = folder + "/" + din_nobukti.getText().toString() + ".csv";
                String file=din_nobukti.getText().toString() + ".csv";
                CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                Cursor curCSV = db.rawQuery("select `det`.`No_bukti` as 'NO BUKTI',`det`.`Kode_Pengirim` as 'KODE PENGIRIM',`det`.`nama_pengirim` as 'NAMA PENGIRIM',`det`.`Kode_Penerima` as 'KODE PENERIMA',`det`.`nama_penerima` AS 'NAMA PENERIMA',`hd`.`kode_departemen` as 'KODE DEPARTEMEN',`hd`.`Total_qty` as 'TOTAL QTY',`hd`.`total_beli` as 'TOTAL BELI',`hd`.`total_jual` as 'TOTAL JUAL',`hd`.`kode_user` as 'KODE USER',`hd`.`tanggal_buat` as 'TANGGAL BUAT',`hd`.`tanggal_masuk` as 'TANGGAL MASUK',`hd`.`tgl_kirim` as 'TANGGAL KIRIM',`hd`.`No_SuratJalan` as 'NO SURAT JALAN',`hd`.`no_pol` as 'NO POLISI',`hd`.`expedisi` as  'EXPEDISI',`det`.`kode_barang` as 'KODE BARANG',`brg`.`nama_barang` as 'NAMA BARANG',`det`.`Qty` as 'TERKIRIM',`det`.`Qty_receive` as 'DITERIMA',`det`.`harga_beli` as 'HARGA BELI'," +
                        "`det`.`harga_jual` as 'HARGA JUAL',`hd`.`nama_penghitung` as 'NAMA PENGHITUNG',`hd`.`keterangan` as 'KETERANGAN' from `tbl_distin_detail` as `det`LEFT JOIN `tbl_distin` as `hd` on `hd`.`No_bukti`=`det`.`No_bukti` LEFT JOIN `tbl_barang` as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`No_bukti` ='"+din_nobukti.getText().toString()+"'", null);
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
                Toast.makeText(FDistin.this, sqlEx.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "List tidak ada", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
    }
}