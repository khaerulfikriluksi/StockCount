package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.opencsv.CSVWriter;
import com.urban.stockcount1.CustomClass.ListHistoryAdapter;
import com.urban.stockcount1.CustomClass.UploadCSV;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class FHistory_Counting extends AppCompatActivity {
    private ListView listView;
    private ListHistoryAdapter adapter;
    private AutoCompleteTextView eidscan,etglscan,ecabang,enamascan,elokasiscan;
    private String alias_user,kodecb, selectedindexcabang, id_user;
    private SQLiteDatabase db;
    private LottieAnimationView history_noata_anim;
    private Toolbar toolbar;
    private AlertDialog show;
    private FloatingActionButton ct_add;
    public static ActionMode actionMode=null;
    public static Boolean isActionMode = false;
    private StorageReference storageRef;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fhistory_counting);
        storageRef = FirebaseStorage.getInstance().getReference("csv");
        dbRef = FirebaseDatabase.getInstance("https://stockapp-4abf1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("csv_data");

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

        history_noata_anim = (LottieAnimationView) findViewById(R.id.history_noata_anim);

        Cursor ctr = db.rawQuery("SELECT * FROM tbl_user", null);
        ctr.moveToFirst();
        if (ctr.getCount()>0) {
            alias_user=ctr.getString(4);
            id_user = ctr.getString(0);
            kodecb = ctr.getString(5);
        } else {
            id_user = "";
            kodecb = "";
            alias_user="-";
        }
        ct_add=(FloatingActionButton) findViewById(R.id.ct_add);
        listView=findViewById(R.id.listhistory);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(modeListener);
        //Toolbar
        toolbar = findViewById(R.id.toolbarhistory);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ct_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int no = new Random().nextInt((9 - 0) + 1) + 0;
                String idrandom=getSaltString()+String.valueOf(no);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        FHistory_Counting.this, R.style.AppBottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(FHistory_Counting.this)
                        .inflate(
                                R.layout.bottom_sheet_newcounting,(LinearLayout)findViewById(R.id.bottompopup)
                        );
                eidscan= (AutoCompleteTextView) bottomSheetView.findViewById(R.id.eidscan);
                etglscan= (AutoCompleteTextView) bottomSheetView.findViewById(R.id.etglscan);
                ecabang= (AutoCompleteTextView) bottomSheetView.findViewById(R.id.ecabang);
                enamascan= (AutoCompleteTextView) bottomSheetView.findViewById(R.id.enamascan);
                elokasiscan= (AutoCompleteTextView) bottomSheetView.findViewById(R.id.elokasiscan);
                enamascan.setText(alias_user);

                eidscan.setText("CT-" + kodecb + idrandom);
                ecabang.setEnabled(true);
                etglscan.setText(getCurrentTime());
                bottomSheetView.findViewById(R.id.bnewscan).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TombolNewView
                        if ((TextUtils.isEmpty(enamascan.getText().toString())) || (TextUtils.isEmpty(elokasiscan.getText().toString()))){
                            ShowMessage(FHistory_Counting.this,"Data kosong tidak diperbolehkan","Pemberitahuan");
                        }
                        else
                        {
                            bottomSheetDialog.dismiss();
                            Cursor cursorc=db.rawQuery("SELECT  * FROM tbl_cabang where '['||`kode`||'] '||`nama` = '"+ecabang.getText().toString()+"'",null);
                            if (cursorc.getCount()>=1) {
                                cursorc.moveToFirst();
                                db.execSQL("INSERT INTO tbl_counting (no_doc,kode_cabang,petugas,id_user,lokasi_rak,tanggal) VALUES " +
                                        "('"+eidscan.getText().toString()+"','"+cursorc.getString(0)+"','"+enamascan.getText().toString()+"','"+id_user+"','"+elokasiscan.getText().toString()+"','"+etglscan.getText().toString()+"')");
                                Intent Formku = new Intent(FHistory_Counting.this, FCounting_Stock.class);
                                Formku.putExtra("id",eidscan.getText().toString());
                                Formku.putExtra("tanggal",etglscan.getText().toString());
                                Formku.putExtra("cabang",ecabang.getText().toString());
                                FCounting_Stock.setState(0);
                                startActivity(Formku);
                                finish();
                            } else {
                                ShowMessage(FHistory_Counting.this,"Cabang belum ada yang terpilih","Pemberitahuan");
                            }
                        }
                    }
                });
//                ecabang.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        String nilai = s.toString();
//                        namacabang=nilai;
//                        Cursor cursor = db.rawQuery("SELECT * FROM `tbl_cabang` where '['||kode||'] '||nama='" + nilai + "'", null);
//                        if (cursor.getCount()>=1) {
//                            cursor.moveToFirst();
//                            kodecb = cursor.getString(0);
//                            eidscan.setText("CT-" + cursor.getString(0) + idrandom);
//                        } else {
//
//                        }
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//
//                    }
//                });

                //
//                Cursor cr = db.rawQuery("select * from tbl_cabang where `kode`='"+kodecb+"'",null);
                Cursor cr = db.rawQuery("select * from tbl_cabang",null);
                if (cr.getCount()>=1) {
                    List<String> labels= getAllLabels();
                    ecabang.setText(selectedindexcabang);
                    ArrayAdapter<String> storeadapter = new ArrayAdapter<String>(FHistory_Counting.this,
                            R.layout.cust_dropdown_spinner, labels);
                    ecabang.setAdapter(storeadapter);
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            BottomSheetDialog d = (BottomSheetDialog) dialog;
                            FrameLayout bottomSheet = (FrameLayout) d.findViewById(R.id.design_bottom_sheet);
                            BottomSheetBehavior.from(bottomSheet)
                                    .setState(BottomSheetBehavior.STATE_EXPANDED);
                        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        }
                    });
                    bottomSheetDialog.show();
                } else {
                    Toast.makeText(FHistory_Counting.this,"There is no data", Toast.LENGTH_LONG).show();
                }
            }
        });
        getdata();
    }

    //ShowMessage
    public void ShowMessage(Context cont, String message, String title){
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(cont);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", null);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    //SetDataToSpiner
    public List<String> getAllLabels(){
        List<String>labels= new ArrayList<>();
//        Cursor cursor=db.rawQuery("SELECT  * FROM tbl_cabang where `kode`='"+kodecb+"'",null);
        Cursor cursor=db.rawQuery("SELECT  * FROM tbl_cabang",null);
        cursor.moveToFirst();
        selectedindexcabang="["+cursor.getString(0)+"] "+cursor.getString(1);
        for (int count = 0; count < cursor.getCount(); count++) {
            cursor.moveToPosition(count);
            labels.add("["+cursor.getString(0)+"] "+cursor.getString(1));
        }
        return labels;
    }

    public void getdata(){
        ArrayList<String> cabang = new ArrayList<>();
        ArrayList<String> idscan = new ArrayList<>();
        ArrayList<String> tglnya = new ArrayList<>();
        ArrayList<String> petugas = new ArrayList<>();
        ArrayList<String> rak = new ArrayList<>();
//        Cursor cursor = db.rawQuery("select `ct`.`no_doc`,'['||`ct`.`kode_cabang`||'] '||`cb`.`nama` as `cabang`,`ct`.`tanggal`,`ct`.`petugas`,`ct`.`lokasi_rak` from tbl_counting as `ct` left join `tbl_cabang` as `cb` on `cb`.`kode`=`ct`.`kode_cabang` where `ct`.`kode_cabang`='"+kodecb+"'", null);
        Cursor cursor = db.rawQuery("select `ct`.`no_doc`,'['||`ct`.`kode_cabang`||'] '||`cb`.`nama` as `cabang`,`ct`.`tanggal`,`ct`.`petugas`,`ct`.`lokasi_rak` from tbl_counting as `ct` left join `tbl_cabang` as `cb` on `cb`.`kode`=`ct`.`kode_cabang`", null);
        if (cursor.getCount()>0) {
            history_noata_anim.setVisibility(View.GONE);
        } else {
            history_noata_anim.setVisibility(View.VISIBLE);
        }
        cursor.moveToFirst();
        for (int count = 0; count < cursor.getCount(); count++) {
            cursor.moveToPosition(count);
            cabang.add(cursor.getString(1));
            idscan.add(cursor.getString(0));
            tglnya.add(cursor.getString(2));
            petugas.add(cursor.getString(3));
            rak.add(cursor.getString(4));
        }
        adapter = new ListHistoryAdapter(FHistory_Counting.this,cabang,idscan,tglnya,petugas,rak);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cd = db.rawQuery("SELECT * FROM tbl_barang",null);
                if (cd.getCount()>=0) {
//                    CoordinatorLayout bg;
//                    bg = (CoordinatorLayout) findViewById(R.id.history_count_parent);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        int cx = bg.getWidth();
//                        int cy = 0;
//                        float finalRadius = Math.max(bg.getWidth(), bg.getHeight());
//                        Animator circularReveal = ViewAnimationUtils.createCircularReveal(bg, cx, cy, finalRadius, 0);
//                        circularReveal.addListener(new Animator.AnimatorListener() {
//                            @Override
//                            public void onAnimationStart(Animator animator) {
//                                Intent Formku = new Intent(FHistory_Counting.this, FCounting_Stock.class);
//                                Formku.putExtra("id", adapter.getName(position));
//                                Formku.putExtra("tanggal", adapter.getTanggal(position));
//                                Formku.putExtra("cabang", adapter.getCabang(position));
//                                FCounting_Stock.setState(0);
//                                startActivity(Formku);
//                                finish();
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animator animator) {
//                                bg.setVisibility(View.INVISIBLE);
//                            }
//
//                            @Override
//                            public void onAnimationCancel(Animator animator) {
//
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animator animator) {
//
//                            }
//                        });
//                        circularReveal.setDuration(500);
//                        circularReveal.start();
//                    } else {
                        Intent Formku = new Intent(FHistory_Counting.this, FCounting_Stock.class);
                        Formku.putExtra("id", adapter.getName(position));
                        Formku.putExtra("tanggal", adapter.getTanggal(position));
                        Formku.putExtra("cabang", adapter.getCabang(position));
                        FCounting_Stock.setState(0);
                        startActivity(Formku);
                        finish();
//                    }
                } else
                {
                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Tidak ada data barang", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            }
        });
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Counting.this);
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
                    if (adapter.getCountSelectedCheckBoxes()>0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Counting.this);
                        LayoutInflater inflat = (LayoutInflater) FHistory_Counting.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                                    File folder = new File(getExternalFilesDir(null),"/CSV/STOCK COUNT");
                                    String nama = folder+"/"+share_name.getText().toString().trim()+".csv";
                                    File file = new File(nama);
                                    if(file.exists()) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Counting.this);
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
                } else if (id == R.id.bupload) {
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
            if(adapter.getCount() > 0){
                List<Boolean> checked = adapter.getCheckList();
                for(int i = checked.size() - 1; i >= 0; i--){
                    adapter.setChecked(i,false);
                }
            }
            isActionMode = false;
            actionMode=null;
        }
    };

    public void onBackPressed() {
//        CoordinatorLayout bg;
//        bg = (CoordinatorLayout) findViewById(R.id.history_count_parent);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            int cx = bg.getWidth();
//            int cy = 0;
//            float finalRadius = Math.max(bg.getWidth(), bg.getHeight());
//            Animator circularReveal = ViewAnimationUtils.createCircularReveal(bg, cx, cy, finalRadius, 0);
//
//            circularReveal.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animator) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animator) {
//                    bg.setVisibility(View.INVISIBLE);
//                    Intent Formku = new Intent(FHistory_Counting.this, FMenuList.class);
//                    startActivity(Formku);
//                    finish();
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animator) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animator) {
//
//                }
//            });
//            circularReveal.setDuration(500);
//            circularReveal.start();
//        } else {
            Intent Formku = new Intent(FHistory_Counting.this, FMenuList.class);
            startActivity(Formku);
            finish();
//        }
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

    public void SaveCSV(String namanya) {
        if(adapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = adapter.getCheckList();
            ArrayList<String>value=new ArrayList<>();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    value.add(adapter.getName(i));
                }
            }
            String val = value.toString().replace("[","'");
            String val2 = val.replace("]","'");
            String Valx = val2.replace(" ","");
            String val3 = Valx.replace(",","','");
            Cursor curCSV = db.rawQuery("select `det`.`no_doc` as `NO DOCUMENT`,`hd`.`tanggal` as `TANGGAL SCAN`,`hd`.`petugas` as `PETUGAS`,`hd`.`lokasi_rak` as `LOKASI RAK`,`hd`.`kode_cabang` as `KODE CABANG`,`hd`.`nama` as `CABANG`,`det`.`kode_barang` as `KODE BARANG`,`brg`.`nama_barang` as `NAMA BARANG`, `brg`.`harga_beli` as `HARGA BELI`, `brg`.`harga_jual` as `HARGA JUAL`, `det`.`qty` as `QTY` from " +
                    "tbl_counting_detail as `det` left join (select `ct`.`no_doc`,`ct`.`petugas`,`ct`.`lokasi_rak`,`ct`.`kode_cabang`,`cb`.`nama`,`ct`.`tanggal` from tbl_counting as `ct` left join tbl_cabang as `cb` on `cb`.`kode`=`ct`.`kode_cabang`) as `hd` on `hd`.`no_doc`=`det`.`no_doc` " +
                    "left join tbl_barang as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`no_doc` in (" + val3 + ")", null);
            if (curCSV.getCount() >= 1) {
                try {
                    File folder = new File(getExternalFilesDir(null),"/CSV/STOCK COUNT");
                    if (folder.exists()){
                    } else
                    {
                        folder.mkdirs();
                    }
                    String nama=null;
                    if (adapter.getCountSelectedCheckBoxes()>1){
                        nama = folder+"/"+namanya+".csv";
                    } else {
                        nama = folder+"/"+namanya+".csv";
                    }
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while (curCSV.moveToNext()) {
                        String arrStr[] = {curCSV.getString(0),curCSV.getString(1),curCSV.getString(2),curCSV.getString(3),
                                curCSV.getString(4),curCSV.getString(5),curCSV.getString(6),curCSV.getString(7),curCSV.getString(8),curCSV.getString(9),curCSV.getString(10)};
                        csvWrite.writeNext(arrStr);
                    }
                    csvWrite.close();
                    curCSV.close();
                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "File tersimpan : "+nama, Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                } catch (Exception sqlEx) {
                    Toast.makeText(FHistory_Counting.this,sqlEx.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Data tidak ada", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        }
    }

    public void csvshare() {
        if(adapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = adapter.getCheckList();
            ArrayList<String>value=new ArrayList<>();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    value.add(adapter.getName(i));
                }
            }
            String val = value.toString().replace("[","'");
            String val2 = val.replace("]","'");
            String Valx = val2.replace(" ","");
            String val3 = Valx.replace(",","','");
            Cursor curCSV = db.rawQuery("select `det`.`no_doc` as `NO DOCUMENT`,`hd`.`tanggal` as `TANGGAL SCAN`,`hd`.`petugas` as `PETUGAS`,`hd`.`lokasi_rak` as `LOKASI RAK`,`hd`.`kode_cabang` as `KODE CABANG`,`hd`.`nama` as `CABANG`,`det`.`kode_barang` as `KODE BARANG`,`brg`.`nama_barang` as `NAMA BARANG`, `brg`.`harga_beli` as `HARGA BELI`, `brg`.`harga_jual` as `HARGA JUAL`, `det`.`qty` as `QTY` from " +
                    "tbl_counting_detail as `det` left join (select `ct`.`no_doc`,`ct`.`petugas`,`ct`.`lokasi_rak`,`ct`.`kode_cabang`,`cb`.`nama`,`ct`.`tanggal` from tbl_counting as `ct` left join tbl_cabang as `cb` on `cb`.`kode`=`ct`.`kode_cabang`) as `hd` on `hd`.`no_doc`=`det`.`no_doc` " +
                    "left join tbl_barang as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`no_doc` in (" + val3 + ")", null);
            if (curCSV.getCount() >= 1) {
                try {
                    File folder = new File(getExternalFilesDir(null),"/CSV/BIN");
                    if (folder.exists()){
                    } else
                    {
                        folder.mkdirs();
                    }
                    String nama=null;
                    if (adapter.getCountSelectedCheckBoxes()>1){
                        nama = folder+"/CT-Batch-"+getSaltString()+".csv";
                    } else {
                        List<Boolean> checked1 = adapter.getCheckList();
                        for(int i = checked1.size() - 1; i >= 0; i--) {   //Rueckwaerts ausfuehren!
                            if (checked1.get(i)) {
                                nama = folder+"/"+adapter.getName(i)+getSaltString()+".csv";
                            }
                        }
                    }
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while (curCSV.moveToNext()) {
                        String arrStr[] = {curCSV.getString(0),curCSV.getString(1),curCSV.getString(2),curCSV.getString(3),
                                curCSV.getString(4),curCSV.getString(5),curCSV.getString(6),curCSV.getString(7),curCSV.getString(8),curCSV.getString(9),curCSV.getString(10)};
                        csvWrite.writeNext(arrStr);
                    }
                    csvWrite.close();
                    curCSV.close();
                    shareFile(nama);
                } catch (Exception sqlEx) {
                    Toast.makeText(FHistory_Counting.this,sqlEx.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Data tidak ada", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        }
    }



    public void deleteitem() {
        if(adapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = adapter.getCheckList();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    String name = adapter.getName(i);
                    adapter.delete(i);
                    db.execSQL("DELETE FROM tbl_counting_detail WHERE `no_doc`='"+name+"'");
                    db.execSQL("DELETE FROM tbl_counting WHERE `no_doc`='"+name+"'");
                    getdata();
                }
            }
            File folder2 = new File(getExternalFilesDir(null)+"/CSV/BIN");
            if (folder2.exists()){
                for(File tempFile2 : folder2.listFiles()) {
                    tempFile2.delete();
                }
            }
            else {
            }
        }else{
            Toast.makeText(FHistory_Counting.this,"Belum ada list yang terpilih", Toast.LENGTH_LONG).show();
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
        ProgressDialog mProgressDialog = new ProgressDialog(FHistory_Counting.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_Counting.this);
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
                                                                UploadCSV upl = new UploadCSV(name,uploadURL,key,alias_user,"","","counting");
                                                                String uploadid = dbRef.push().getKey();
                                                                dbRef.child(uploadid).setValue(upl);
                                                                Toast.makeText(FHistory_Counting.this,"Upload success", Toast.LENGTH_LONG).show();
                                                                AlertDialog.Builder buil = new AlertDialog.Builder(FHistory_Counting.this);
                                                                buil.setCancelable(false);
                                                                buil.setTitle(key);
                                                                buil.setMessage("Key will expired after 7 days");
                                                                buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                                        ClipData clip = ClipData.newPlainText("label", key);
                                                                        clipboard.setPrimaryClip(clip);
                                                                        Toast.makeText(FHistory_Counting.this,"Copied",Toast.LENGTH_LONG).show();
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
                                                                Toast.makeText(FHistory_Counting.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(FHistory_Counting.this,e.getMessage(), Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(FHistory_Counting.this,"Failed to overwrite to cloud : "+exception.getMessage(), Toast.LENGTH_LONG).show();
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
                                AlertDialog.Builder buil = new AlertDialog.Builder(FHistory_Counting.this);
                                buil.setCancelable(false);
                                buil.setTitle(finalKeynya);
                                buil.setMessage("Key will expired after 7 days");
                                buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("label", finalKeynya);
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(FHistory_Counting.this,"Copied",Toast.LENGTH_LONG).show();
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
                                            UploadCSV upl = new UploadCSV(name,uploadURL,key,alias_user,"","","counting");
                                            String uploadid = dbRef.push().getKey();
                                            dbRef.child(uploadid).setValue(upl);
                                            Toast.makeText(FHistory_Counting.this,"Upload success", Toast.LENGTH_LONG).show();
                                            AlertDialog.Builder buil = new AlertDialog.Builder(FHistory_Counting.this);
                                            buil.setCancelable(false);
                                            buil.setTitle(key);
                                            buil.setMessage("Key will expired after 7 days");
                                            buil.setNegativeButton("Copy Key", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("label", key);
                                                    clipboard.setPrimaryClip(clip);
                                                    Toast.makeText(FHistory_Counting.this,"Copied",Toast.LENGTH_LONG).show();
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
                                            Toast.makeText(FHistory_Counting.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(FHistory_Counting.this,e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(FHistory_Counting.this,error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void makecsvupload() {
        if(adapter.getCountSelectedCheckBoxes() > 0){
            List<Boolean> checked = adapter.getCheckList();
            ArrayList<String>value=new ArrayList<>();
            for(int i = checked.size() - 1; i >= 0; i--){
                if(checked.get(i)){
                    value.add(adapter.getName(i));
                }
            }
            String val = value.toString().replace("[","'");
            String val2 = val.replace("]","'");
            String Valx = val2.replace(" ","");
            String val3 = Valx.replace(",","','");
            Cursor curCSV = db.rawQuery("select `det`.`no_doc` as `NO DOCUMENT`,`hd`.`tanggal` as `TANGGAL SCAN`,`hd`.`petugas` as `PETUGAS`,`hd`.`lokasi_rak` as `LOKASI RAK`,`hd`.`kode_cabang` as `KODE CABANG`,`hd`.`nama` as `CABANG`,`det`.`kode_barang` as `KODE BARANG`,`brg`.`nama_barang` as `NAMA BARANG`, `brg`.`harga_beli` as `HARGA BELI`, `brg`.`harga_jual` as `HARGA JUAL`, `det`.`qty` as `QTY` from " +
                    "tbl_counting_detail as `det` left join (select `ct`.`no_doc`,`ct`.`petugas`,`ct`.`lokasi_rak`,`ct`.`kode_cabang`,`cb`.`nama`,`ct`.`tanggal` from tbl_counting as `ct` left join tbl_cabang as `cb` on `cb`.`kode`=`ct`.`kode_cabang`) as `hd` on `hd`.`no_doc`=`det`.`no_doc` " +
                    "left join tbl_barang as `brg` on `brg`.`kode_barang`=`det`.`kode_barang` where `det`.`no_doc` in (" + val3 + ")", null);
            if (curCSV.getCount() >= 1) {
                try {
                    File folder = new File(getExternalFilesDir(null),"/CSV/BIN");
                    if (folder.exists()){
                    } else
                    {
                        folder.mkdirs();
                    }
                    String file=null;
                    String nama=null;
                    if (adapter.getCountSelectedCheckBoxes()>1){
                        String salt = getSaltString();
                        nama = folder+"/CT-Batch-"+salt+".csv";
                        file = "CT-Batch-"+salt+".csv";
                    } else {
                        List<Boolean> checked1 = adapter.getCheckList();
                        for(int i = checked1.size() - 1; i >= 0; i--) {   //Rueckwaerts ausfuehren!
                            if (checked1.get(i)) {
                                nama = folder+"/"+adapter.getName(i)+".csv";
                                file = adapter.getName(i)+".csv";
                            }
                        }
                    }
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(nama));
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
                    Toast.makeText(FHistory_Counting.this,sqlEx.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Data tidak ada", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        }
    }
}