package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FRegister extends AppCompatActivity {

    private String API;
    SQLiteDatabase db;
    private ProgressDialog progressDialog;
    private TextInputLayout reg_password_head, reg_repassword_head,reg_username_head,reg_email_head,reg_alias_head,reg_cabang_head;
    private String selectedindexcabang, namacabang, kodecb;
    private CircularProgressButton btn_register;
    private AutoCompleteTextView reg_alias,reg_email,reg_username,reg_password,reg_repassword,reg_cabang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fregister);
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
        cursor.moveToFirst();
        if (cursor.getCount()>0) {
            API=cursor.getString(1);
            reg_password_head=(TextInputLayout) findViewById(R.id.reg_password_head);
            reg_repassword_head=(TextInputLayout) findViewById(R.id.reg_repassword_head);
            reg_username_head=(TextInputLayout) findViewById(R.id.reg_username_head);
            reg_email_head=(TextInputLayout) findViewById(R.id.reg_email_head);
            reg_alias_head=(TextInputLayout) findViewById(R.id.reg_alias_head);
            reg_cabang_head=(TextInputLayout) findViewById(R.id.reg_cabang_head);
            reg_alias=(AutoCompleteTextView) findViewById(R.id.reg_alias);
            reg_email=(AutoCompleteTextView) findViewById(R.id.reg_email);
            reg_username=(AutoCompleteTextView) findViewById(R.id.reg_username);
            reg_password=(AutoCompleteTextView) findViewById(R.id.reg_password);
            reg_repassword=(AutoCompleteTextView) findViewById(R.id.reg_repassword);
            btn_register=(CircularProgressButton) findViewById(R.id.btn_register);
            reg_cabang=(AutoCompleteTextView) findViewById(R.id.reg_cabang);

            Cursor cr = db.rawQuery("select * from tbl_cabang",null);
            if (cr.getCount()>=1) {
                List<String> labels= getAllLabels();
                reg_cabang.setText(selectedindexcabang);
                ArrayAdapter<String> storeadapter = new ArrayAdapter<String>(FRegister.this,
                        R.layout.cust_dropdown_spinner, labels);
                reg_cabang.setAdapter(storeadapter);
                reg_repassword.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        validatepass();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                reg_cabang.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String nilai = s.toString();
                        namacabang=nilai;
                        Cursor cursor = db.rawQuery("SELECT * FROM `tbl_cabang` where '['||kode||'] '||nama='" + nilai + "'", null);
                        if (cursor.getCount()>=1) {
                            cursor.moveToFirst();
                            kodecb = cursor.getString(0);
                        } else {

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                reg_email.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        validateemail();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                btn_register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (reg_alias.getText().toString().trim().length()>0){
                            reg_alias_head.setHelperText(null);
                            if (reg_email.getText().toString().trim().length()>0){
                                reg_email_head.setHelperText(null);
                                if (reg_username.getText().toString().trim().length()>0){
                                    reg_username_head.setHelperText(null);
                                    if (reg_password.getText().toString().trim().length()>0){
                                        reg_password_head.setHelperText(null);
                                        if (validateemail()==true) {
                                            if (validatepass()==true) {
                                                Map<String, String> param = new HashMap<String, String>();
                                                param.put("username", reg_username.getText().toString().toLowerCase());
                                                param.put("password", reg_password.getText().toString());
                                                param.put("alias", reg_alias.getText().toString().toUpperCase());
                                                param.put("kode_cabang", kodecb);
                                                param.put("email", reg_email.getText().toString());
                                                register(param);
                                            }
                                        }
                                    } else {
                                        reg_password_head.setHelperText("*Required");
                                        reg_password.setText("");
                                        reg_password.requestFocus();
                                    }
                                } else {
                                    reg_username_head.setHelperText("*Required");
                                    reg_username.setText("");
                                    reg_username.requestFocus();
                                }
                            } else {
                                reg_email_head.setHelperText("*Required");
                                reg_email.setText("");
                                reg_email.requestFocus();
                            }
                        } else {
                            reg_alias_head.setHelperText("*Required");
                            reg_alias.setText("");
                            reg_alias.requestFocus();
                        }
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(FRegister.this);
                builder.setTitle("Data cabang belum ada, unduh dulu?");
                builder.setCancelable(false);
                builder.setMessage("API tidak ada, silahkan kontak developer...");
                builder.setPositiveButton("Unduh", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
        } else{
            AlertDialog.Builder builder = new AlertDialog.Builder(FRegister.this);
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

    private void download_cabang() {
        progressDialog = new ProgressDialog(FRegister.this);
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
                        Log.v("GET_CABANG Response",response);
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
                                Cursor cr = db.rawQuery("select * from tbl_cabang",null);
                                if (cr.getCount()>=1) {
                                    List<String> labels = getAllLabels();
                                    reg_cabang.setText(selectedindexcabang);
                                    ArrayAdapter<String> storeadapter = new ArrayAdapter<String>(FRegister.this,
                                            R.layout.cust_dropdown_spinner, labels);
                                    reg_cabang.setAdapter(storeadapter);
                                }
                            } else {
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(FRegister.this);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(FRegister.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(FRegister.this);
                        builder.setTitle("Connection problem");
                        builder.setCancelable(false);
                        builder.setMessage("Koneksi server bermasalah, Mohon hubungi IT kemudian refresh kembali");
                        builder.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor cursor = db.rawQuery("SELECT * FROM tbl_api", null);
                                cursor.moveToFirst();
                                if (cursor.getCount()>0) {
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
        RequestQueue requestQueue = Volley.newRequestQueue(FRegister.this);
        requestQueue.add(stringRequest);
    }

    public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9+._%-+]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" +
                    "(" +
                    "." +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" +
                    ")+"
    );

    private List<String> getAllLabels(){
        List<String>labels= new ArrayList<>();
        Cursor cursor=db.rawQuery("SELECT  * FROM tbl_cabang",null);
        cursor.moveToFirst();
        kodecb=cursor.getString(0);
        selectedindexcabang="["+cursor.getString(0)+"] "+cursor.getString(1);
        for (int count = 0; count < cursor.getCount(); count++) {
            cursor.moveToPosition(count);
            labels.add("["+cursor.getString(0)+"] "+cursor.getString(1));
        }
        return labels;
    }

    private boolean validateemail() {
        boolean temp=false;
        String checkemail = reg_email.getText().toString();
        if(checkemail.isEmpty()){
            reg_email_head.setHelperText("Email mohon di isi");
            temp=false;
        }
        else if(!EMAIL_ADDRESS_PATTERN.matcher(checkemail).matches()){
            reg_email_head.setHelperText("Mohon masukkan email valid");
            temp=false;
        } else {
            reg_email_head.setHelperText(null);
            temp=true;
        }
        return temp;
    }

    private boolean validatepass() {
        boolean temp=false;
        String pass=reg_password.getText().toString();
        String cpass=reg_repassword.getText().toString();
        if (pass.isEmpty()){
            reg_password_head.setHelperText("Password mohon di isi");
            reg_password.requestFocus();
            temp=false;
        } else if (cpass.isEmpty()){
            reg_repassword_head.setHelperText("Mohon di isi");
            reg_repassword.requestFocus();
            temp=false;
        } else if(!pass.equals(cpass)){
            reg_repassword_head.setHelperText("Password tidak sama");
            temp=false;
        } else {
            reg_password_head.setHelperText(null);
            reg_repassword_head.setHelperText(null);
            temp=true;
        }
        return temp;
    }

    private void register (Map<String,String> paramlogin) {
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='register'",null);
        cr.moveToFirst();
        String URL = API+cr.getString(1);
        Log.v("Login",URL);
        btn_register.startAnimation();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status==200) {
                                //BerhasilRegist
                                AlertDialog.Builder builder = new AlertDialog.Builder(FRegister.this);
                                builder.setTitle("Registrasi Berhasil");
                                builder.setMessage("Registrasi selesai, mohon hubungi admin untuk aktivasi akun");
                                builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onBackPressed();
                                    }
                                });
                                builder.show();
                            } else {
                                String errorname = jsonObject.getString("message");
                                Toast.makeText(FRegister.this,errorname, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v("JsonObjectError",e.getMessage());
                        }
                        btn_register.revertAnimation();
                        btn_register.setBackgroundResource(R.drawable.round_ungu);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.v("VolleyPostError",error.getMessage());
                        String message = null;
                        if (error instanceof NetworkError) {
                            message = "Cannot connect to Server...Please check your connection!";
                            Toast.makeText(FRegister.this,message, Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again after some time!!";
                            Toast.makeText(FRegister.this,message, Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            message = "Cannot connect to Server...Please check your connection!";
                            Toast.makeText(FRegister.this,message, Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again after some time!!";
                            Toast.makeText(FRegister.this,message, Toast.LENGTH_LONG).show();
                        } else if (error instanceof NoConnectionError) {
                            message = "Cannot connect to Server...Please check your connection!";
                            Toast.makeText(FRegister.this,message, Toast.LENGTH_LONG).show();
                        } else if (error instanceof TimeoutError) {
                            message = "Connection TimeOut! Please check your internet connection.";
                            Toast.makeText(FRegister.this,message, Toast.LENGTH_LONG).show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FRegister.this);
                            builder.setTitle("Data corrupt");
                            builder.setCancelable(false);
                            builder.setMessage("Data server bermasalah, Mohon hubungi IT...");
                            builder.setNegativeButton("Keluar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    System.exit(0);
                                }
                            });
                            builder.show();
                        }
                        btn_register.revertAnimation();
                        btn_register.setBackgroundResource(R.drawable.round_ungu);
                    }
                })
        {
            @NonNull
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = paramlogin;
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("API_KEY", "53713");
                return headers;
            }

            @Override
            public Response<String> parseNetworkResponse(NetworkResponse response) {
                String statusCode = String.valueOf(response.statusCode);
                Log.d("parseNetworkResponse",statusCode);
                if (response.statusCode!=200) {
                    Toast.makeText(FRegister.this,"Lost connection from server", Toast.LENGTH_LONG).show();
                }
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(FRegister.this);
        requestQueue.add(stringRequest);
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

    public void onBackPressed() {
//        RelativeLayout bg;
//        bg = (RelativeLayout) findViewById(R.id.reg_parent);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            int cx = bg.getWidth();
//            int cy = 0;
//            float finalRadius = Math.max(bg.getWidth(), bg.getHeight());
//            Animator circularReveal = ViewAnimationUtils.createCircularReveal(bg, cx, cy, finalRadius, 0);
//            circularReveal.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animator) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animator) {
//                    bg.setVisibility(View.INVISIBLE);
//                    Intent formku = new Intent(FRegister.this, Flogin.class);
//                    formku.putExtra("start",0);
//                    startActivity(formku);
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
            Intent formku = new Intent(FRegister.this, Flogin.class);
            formku.putExtra("start",0);
            startActivity(formku);
            finish();
//        }
        return;
    }

    public void kembali (View v) {
        onBackPressed();
    }
}