package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.Manifest;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.opencsv.CSVWriter;
import com.urban.stockcount1.CustomClass.ListAdapter;
import com.urban.stockcount1.CustomClass.UploadCSV;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FCounting_Stock extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static int state;
    //Var
    private ToneGenerator toneGen1;
    private ZXingScannerView cam;
    private TextView camvalue;
    private AlertDialog show;
    private Context context = this;
    private SQLiteDatabase db;
    private LinearLayout layman,laybar;
    private AutoCompleteTextView emanual;
    private ListAdapter myadapter;
    private ListView listView;
    private String qrysearch,id_user,kodecb,usr_alias;
    private SearchView scanitemsearch;
    private TextView lid,lkdcb,ltgl;
    private ArrayList<String> valu = new ArrayList<>();
    private StorageReference storageRef;
    private DatabaseReference dbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fcounting_stock);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(this.getResources().getColor(R.color.unguatas));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }
        dbRef = FirebaseDatabase.getInstance("https://stockapp-4abf1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("csv_data");
        storageRef = FirebaseStorage.getInstance().getReference("csv");
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        //
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

        cam=findViewById(R.id.camera);
        camvalue=findViewById(R.id.evalue);
        layman=findViewById(R.id.laymanual);
        laybar=findViewById(R.id.laybarcode);
        emanual=findViewById(R.id.eidmanual);
        new AdapterHelp().execute();
        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cam.toggleFlash();
            }
        });

        //
        scanitemsearch=findViewById(R.id.scanitemsearch);
        scanitemsearch.setQueryHint("Cari nama barang...");
        lid=findViewById(R.id.lid);
        lid.setText(getIntent().getExtras().getString("id"));
        lkdcb=findViewById(R.id.lkdcb);
        lkdcb.setText(getIntent().getExtras().getString("cabang"));
        ltgl=findViewById(R.id.ltgl);
        ltgl.setText(getIntent().getExtras().getString("tanggal"));
        //ListView
        listView = findViewById(R.id.listitemscan);
        //BackNavbarButton
        Toolbar toolbar = findViewById(R.id.toolBar_counting);
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ico_menu_white));
//        toolbar.setOverflowIcon(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ico_menu));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kembali();
            }
        });
        emanual.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               if (layman.getVisibility()==View.VISIBLE) {
                   String[] separated = s.toString().split(" : ");
                   Cursor cur = db.rawQuery("SELECT * FROM tbl_barang WHERE kode_barang='" + separated[0] + "'", null);
                   if (cur.getCount() >= 1) {
                       db.execSQL("UPDATE tbl_counting_detail SET `qty`=`qty`+1 WHERE `kode_barang`='" + separated[0] + "' AND `no_doc`='" + lid.getText().toString() + "'");
                       db.execSQL("INSERT INTO tbl_counting_detail (`no_doc`,`kode_barang`,`qty`) SELECT * FROM (" +
                               "SELECT '" + lid.getText().toString() + "','" + separated[0] + "','1') AS A WHERE NOT EXISTS (SELECT * FROM tbl_counting_detail WHERE `kode_barang`='" + separated[0] + "' AND `no_doc`='" + lid.getText().toString() + "')");
                       emanual.setText("");
                       getbarang();
                   }
               }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        cam.setResultHandler(FCounting_Stock.this);
                        cam.startCamera();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(FCounting_Stock.this,"Requesting permission", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                    }
                }).check();
        scanitemsearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
                //
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    qrysearch = "SELECT `ct`.`kode_barang` as `kodebrg`,`ct`.`qty` as `qty`,`brg`.`nama_barang` as `nama`,`ct`.`no_doc` FROM `tbl_counting_detail` as `ct` left join `tbl_barang` as `brg` on `brg`.`kode_barang`=`ct`.`kode_barang` WHERE `ct`.`no_doc`='" + lid.getText().toString() + "' and `brg`.`nama_barang` LIKE '%%' ORDER BY id";
                } else {
                    qrysearch = "SELECT `ct`.`kode_barang` as `kodebrg`,`ct`.`qty` as `qty`,`brg`.`nama_barang` as `nama`,`ct`.`no_doc` FROM `tbl_counting_detail` as `ct` left join `tbl_barang` as `brg` on `brg`.`kode_barang`=`ct`.`kode_barang` WHERE `ct`.`no_doc`='" + lid.getText().toString() + "' and `brg`.`nama_barang` LIKE '%" + newText + "%' ORDER BY id";
                }
                getbarang();
                return true;
            }
        });
        qrysearch = "SELECT `ct`.`kode_barang` as `kodebrg`,`ct`.`qty` as `qty`,`brg`.`nama_barang` as `nama`,`ct`.`no_doc` FROM `tbl_counting_detail` as `ct` left join `tbl_barang` as `brg` on `brg`.`kode_barang`=`ct`.`kode_barang` WHERE `ct`.`no_doc`='" + lid.getText().toString() + "' and `brg`.`nama_barang` LIKE '%%' ORDER BY id";
        getbarang();
    }

    public void checkall(View v){
        if(myadapter.getCount() > 0){
            Boolean set;
            List<Boolean> checked = myadapter.getCheckList();
                if (myadapter.getCount() == myadapter.getCountSelectedCheckBoxes()){
                    set=false;
                } else
                {
                    set=true;
                }
            for(int i = checked.size() - 1; i >= 0; i--){
                myadapter.setChecked(i,set);
            }
        }else{
            Toast.makeText(FCounting_Stock.this,"List kosong", Toast.LENGTH_LONG).show();
        }
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
            emanual.setAdapter(new LimitArrayAdapter<String>(FCounting_Stock.this,
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
        ArrayList<String> listkode = new ArrayList<>();
        ArrayList<String> listnama = new ArrayList<>();
        ArrayList<String> qty = new ArrayList<>();
        Cursor cursor = db.rawQuery(qrysearch, null);
        cursor.moveToFirst();
        for (int count = 0; count < cursor.getCount(); count++) {
            cursor.moveToPosition(count);
            listkode.add(cursor.getString(0));
            listnama.add(cursor.getString(2));
            qty.add(cursor.getString(1));
        }
        myadapter =new ListAdapter(FCounting_Stock.this,listkode,listnama,qty,lid.getText().toString());
        listView.setAdapter(myadapter);
    }

    //DeleteButton
    public void deleteitem(View v) {
        if(myadapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = myadapter.getCheckList();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    String name = myadapter.getName(i);
                    myadapter.delete(i);
                    db.execSQL("DELETE FROM tbl_counting_detail WHERE `kode_barang`='" + name + "' AND `no_doc`='" + lid.getText().toString() + "'");
                    getbarang();
                }
            }
        }else{
            Toast.makeText(FCounting_Stock.this,"Belum ada list yang terpilih", Toast.LENGTH_LONG).show();
        }
    }

    //ResultBarcode
    @Override
    public void handleResult(Result result) {
        toneGen1.startTone(ToneGenerator.TONE_DTMF_0,150);
        camvalue.setText(result.getText());
        cam.stopCamera();
        Cursor cur = db.rawQuery("SELECT * FROM tbl_barang WHERE kode_barang='"+result.getText()+"'",null);
        if (cur.getCount()>=1){
            db.execSQL("UPDATE tbl_counting_detail SET `qty`=`qty`+1 WHERE `kode_barang`='" + result.getText() + "' AND `no_doc`='" + lid.getText().toString() + "'");
            db.execSQL("INSERT INTO tbl_counting_detail (`no_doc`,`kode_barang`,`qty`) SELECT * FROM (" +
                    "SELECT '" + lid.getText().toString() + "','" + result.getText() + "','1') AS A WHERE NOT EXISTS (SELECT * FROM tbl_counting_detail WHERE `kode_barang`='" + result.getText() + "' AND `no_doc`='" + lid.getText().toString() + "')");
            getbarang();
        }
        else
        {
            Toast.makeText(FCounting_Stock.this,"Kode barang belum terdaftar", Toast.LENGTH_LONG).show();
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                camvalue.setText("Turn on Flash : Tap Cam Box");
                cam.startCamera();
                cam.resumeCameraPreview(FCounting_Stock.this);
            }
        }, 500);
    }
    //OnResumeBarcode
    @Override
    protected void onResume(){
        super.onResume();
        cam.setResultHandler(FCounting_Stock.this);
        cam.startCamera();
    }
    //onPauseBarcode
    @Override
    protected void onPause(){
        super.onPause();
        cam.stopCamera();
    }


    //HardBackButton
    public void onBackPressed() {
        kembali();
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
            if (laybar.getVisibility()==View.VISIBLE)
            {
                item.setIcon(R.drawable.ico_scan);
                cam.stopCamera();
                laybar.setVisibility(View.INVISIBLE);
                layman.setVisibility(View.VISIBLE);
                emanual.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(emanual, InputMethodManager.SHOW_IMPLICIT);
            }
            else
            {
                try {
                    hideSoftKeyboard(this);
                } catch (Exception e){

                }
                item.setIcon(R.drawable.ico_manual);
                laybar.setVisibility(View.VISIBLE);
                layman.setVisibility(View.INVISIBLE);
                cam.setResultHandler(FCounting_Stock.this);
                cam.startCamera();
            }
        } else
        if (id == R.id.bvoidcount)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Konfirmasi");
            builder.setMessage("Batalkan scan ini?");
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
                    db.execSQL("DELETE FROM tbl_counting where no_doc='" + lid.getText().toString() + "'");
                    db.execSQL("DELETE FROM tbl_counting_detail where no_doc='" + lid.getText().toString() + "'");
                    File folder = new File(getExternalFilesDir(null) + "/CSV");
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
            if (myadapter.getCount()>0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FCounting_Stock.this);
                LayoutInflater inflat = (LayoutInflater) FCounting_Stock.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                            File folder = new File(getExternalFilesDir(null), "/CSV/STOCK COUNT");
                            String nama = folder + "/" + share_name.getText().toString().trim() + ".csv";
                            File file = new File(nama);
                            if (file.exists()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(FCounting_Stock.this);
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
        } else if (id == R.id.buploadcsv) {
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

    public void kembali(){
        Intent Formku = new Intent(FCounting_Stock.this, FHistory_Counting.class);
        startActivity(Formku);
        finish();
    }

    public static void setState(int i){
        state=i;
    }

    public void SaveCsv(String namanya) {
        Cursor cd = db.rawQuery("SELECT * FROM tbl_counting_detail where no_doc='" + lid.getText().toString() + "'", null);
        if (cd.getCount() >= 1) {
            try {
                File folder = new File(getExternalFilesDir(null), "/CSV/STOCK COUNT");
                if (folder.exists()) {
                } else {
                    folder.mkdirs();
                }
                String nama = folder + "/" + namanya + ".csv";
                CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                Cursor curCSV = db.rawQuery("select `det`.`no_doc` as `NO DOCUMENT`,`hd`.`tanggal` as `TANGGAL SCAN`,`hd`.`petugas` as `PETUGAS`,`hd`.`lokasi_rak` as `LOKASI RAK`,`hd`.`kode_cabang` as `KODE CABANG`,`hd`.`nama` as `CABANG`,`det`.`kode_barang` as `KODE BARANG`,`brg`.`nama_barang` as `NAMA BARANG`, `brg`.`harga_beli` as `HARGA BELI`, `brg`.`harga_jual` as `HARGA JUAL`, `det`.`qty` as `QTY` from " +
                        "tbl_counting_detail as `det` left join (select `ct`.`no_doc`,`ct`.`petugas`,`ct`.`lokasi_rak`,`ct`.`kode_cabang`,`cb`.`nama`,`ct`.`tanggal` from tbl_counting as `ct` left join tbl_cabang as `cb` on `cb`.`kode`=`ct`.`kode_cabang`) as `hd` on `hd`.`no_doc`=`det`.`no_doc` " +
                        "left join tbl_barang as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`no_doc`='" + lid.getText().toString() + "'", null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3),
                            curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7), curCSV.getString(8), curCSV.getString(9), curCSV.getString(10)};
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Saved to : "+nama, Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            } catch (Exception sqlEx) {
                Toast.makeText(FCounting_Stock.this, sqlEx.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "List tidak ada", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
    }

    public void Exportcsv() {
        Cursor cd = db.rawQuery("SELECT * FROM tbl_counting_detail where no_doc='" + lid.getText().toString() + "'", null);
        if (cd.getCount() >= 1) {
            try {
                File folder = new File(getExternalFilesDir(null), "/CSV/BIN");
                if (folder.exists()) {
                } else {
                    folder.mkdirs();
                }
                String nama = folder + "/" + lid.getText().toString() + ".csv";
                CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                Cursor curCSV = db.rawQuery("select `det`.`no_doc` as `NO DOCUMENT`,`hd`.`tanggal` as `TANGGAL SCAN`,`hd`.`petugas` as `PETUGAS`,`hd`.`lokasi_rak` as `LOKASI RAK`,`hd`.`kode_cabang` as `KODE CABANG`,`hd`.`nama` as `CABANG`,`det`.`kode_barang` as `KODE BARANG`,`brg`.`nama_barang` as `NAMA BARANG`, `brg`.`harga_beli` as `HARGA BELI`, `brg`.`harga_jual` as `HARGA JUAL`, `det`.`qty` as `QTY` from " +
                        "tbl_counting_detail as `det` left join (select `ct`.`no_doc`,`ct`.`petugas`,`ct`.`lokasi_rak`,`ct`.`kode_cabang`,`cb`.`nama`,`ct`.`tanggal` from tbl_counting as `ct` left join tbl_cabang as `cb` on `cb`.`kode`=`ct`.`kode_cabang`) as `hd` on `hd`.`no_doc`=`det`.`no_doc` " +
                        "left join tbl_barang as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`no_doc`='" + lid.getText().toString() + "'", null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3),
                            curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7), curCSV.getString(8), curCSV.getString(9), curCSV.getString(10)};
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
                shareFile(nama);
            } catch (Exception sqlEx) {
                Toast.makeText(FCounting_Stock.this, sqlEx.toString(), Toast.LENGTH_LONG).show();
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
        ProgressDialog mProgressDialog = new ProgressDialog(FCounting_Stock.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(FCounting_Stock.this);
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
                                                                UploadCSV upl = new UploadCSV(name,uploadURL,key,usr_alias,"","","counting");
                                                                String uploadid = dbRef.push().getKey();
                                                                dbRef.child(uploadid).setValue(upl);
                                                                Toast.makeText(FCounting_Stock.this,"Upload success", Toast.LENGTH_LONG).show();
                                                                AlertDialog.Builder buil = new AlertDialog.Builder(FCounting_Stock.this);
                                                                buil.setCancelable(false);
                                                                buil.setTitle(key);
                                                                buil.setMessage("Key will expired after 7 days");
                                                                buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                                        ClipData clip = ClipData.newPlainText("label", key);
                                                                        clipboard.setPrimaryClip(clip);
                                                                        Toast.makeText(FCounting_Stock.this,"Copied",Toast.LENGTH_LONG).show();
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
                                                                Toast.makeText(FCounting_Stock.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(FCounting_Stock.this,e.getMessage(), Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(FCounting_Stock.this,"Failed to overwrite to cloud : "+exception.getMessage(), Toast.LENGTH_LONG).show();
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
                                AlertDialog.Builder buil = new AlertDialog.Builder(FCounting_Stock.this);
                                buil.setCancelable(false);
                                buil.setTitle(finalKeynya);
                                buil.setMessage("Key will expired after 7 days");
                                buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("label", finalKeynya);
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(FCounting_Stock.this,"Copied",Toast.LENGTH_LONG).show();
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
                                            UploadCSV upl = new UploadCSV(name,uploadURL,key,usr_alias,"","","counting");
                                            String uploadid = dbRef.push().getKey();
                                            dbRef.child(uploadid).setValue(upl);
                                            Toast.makeText(FCounting_Stock.this,"Upload success", Toast.LENGTH_LONG).show();
                                            AlertDialog.Builder buil = new AlertDialog.Builder(FCounting_Stock.this);
                                            buil.setCancelable(false);
                                            buil.setTitle(key);
                                            buil.setMessage("Key will expired after 7 days");
                                            buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("label", key);
                                                    clipboard.setPrimaryClip(clip);
                                                    Toast.makeText(FCounting_Stock.this,"Copied",Toast.LENGTH_LONG).show();
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
                                            Toast.makeText(FCounting_Stock.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(FCounting_Stock.this,e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(FCounting_Stock.this,error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void makecsvupload() {
        Cursor cd = db.rawQuery("SELECT * FROM tbl_counting_detail where no_doc='" + lid.getText().toString() + "'", null);
        if (cd.getCount() >= 1) {
            try {
                File folder = new File(getExternalFilesDir(null), "/CSV/BIN");
                if (folder.exists()) {
                } else {
                    folder.mkdirs();
                }
                String file=lid.getText().toString() + ".csv";
                String nama = folder + "/" + lid.getText().toString() + ".csv";
                CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                Cursor curCSV = db.rawQuery("select `det`.`no_doc` as `NO DOCUMENT`,`hd`.`tanggal` as `TANGGAL SCAN`,`hd`.`petugas` as `PETUGAS`,`hd`.`lokasi_rak` as `LOKASI RAK`,`hd`.`kode_cabang` as `KODE CABANG`,`hd`.`nama` as `CABANG`,`det`.`kode_barang` as `KODE BARANG`,`brg`.`nama_barang` as `NAMA BARANG`, `brg`.`harga_beli` as `HARGA BELI`, `brg`.`harga_jual` as `HARGA JUAL`, `det`.`qty` as `QTY` from " +
                        "tbl_counting_detail as `det` left join (select `ct`.`no_doc`,`ct`.`petugas`,`ct`.`lokasi_rak`,`ct`.`kode_cabang`,`cb`.`nama`,`ct`.`tanggal` from tbl_counting as `ct` left join tbl_cabang as `cb` on `cb`.`kode`=`ct`.`kode_cabang`) as `hd` on `hd`.`no_doc`=`det`.`no_doc` " +
                        "left join tbl_barang as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`no_doc`='" + lid.getText().toString() + "'", null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    String arrStr[] = {curCSV.getString(0),curCSV.getString(1),curCSV.getString(2),curCSV.getString(3),
                            curCSV.getString(4),curCSV.getString(5),curCSV.getString(6),curCSV.getString(7),curCSV.getString(8),curCSV.getString(9),curCSV.getString(10)};
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
                upload(file,nama);
            } catch (Exception sqlEx) {
                Log.v("INI",sqlEx.getMessage());
                Toast.makeText(FCounting_Stock.this, sqlEx.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "List tidak ada", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
    }

}