package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.urban.stockcount1.CustomClass.Cache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Fuser extends AppCompatActivity {

    BottomNavigationView hm_navbar;
    private SQLiteDatabase db;
    private String API=null,emaillogin,usernamelogin,passwordlogin,id_user;
    private TextView usr_alias;
    private AlertDialog show;
    private int count = 0;
    private CardView usr_beditaccount;
    private CircularProgressButton usr_bloginlogout, updt_breset;
    private AlertDialog.Builder builderupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fuser);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(this.getResources().getColor(R.color.ungu_header_user));
            window.setNavigationBarColor(this.getResources().getColor(R.color.white));
        }
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);

        Cursor cursor2 = db.rawQuery("SELECT * FROM tbl_api", null);
        cursor2.moveToFirst();
        if (cursor2.getCount()>0) {
            API=cursor2.getString(1);
            usr_alias=(TextView) findViewById(R.id.usr_alias);
            usr_bloginlogout=(CircularProgressButton) findViewById(R.id.usr_bloginlogout);
            usr_beditaccount = (CardView) findViewById(R.id.usr_beditaccount);
            Cursor cursor = db.rawQuery("SELECT * FROM tbl_user", null);
            cursor.moveToFirst();
            if (cursor.getCount()>0) {
                emaillogin=cursor.getString(3);
                id_user=cursor.getString(0);
                usr_alias.setText(cursor.getString(4));
                usernamelogin=cursor.getString(1);
                passwordlogin=cursor.getString(2);

            } else {
                emaillogin=null;
                id_user=null;
                usr_alias.setText("-");
                usernamelogin=null;
                passwordlogin=null;
            }

            usr_beditaccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    count++;
                    if (count == 1) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                count = 0;
                            }
                        }, 2000);
                        if (emaillogin == null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Fuser.this);
                            builder.setTitle("Session ended");
                            builder.setCancelable(false);
                            builder.setMessage("Sesi anda berakhir, mohon login kembali...");
                            builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.execSQL("delete from tbl_user");
                                    SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                    prefs.edit().clear().commit();
                                    Intent formku = new Intent(Fuser.this, Flogin.class);
                                    startActivity(formku);
                                    finish();
                                }
                            });
                            builder.show();
                        } else {
                            update_data();
                        }
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
            });

            usr_bloginlogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    count++;
                    if (count == 1) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                count = 0;
                            }
                        }, 2000);
                        if (usernamelogin != null) {
                            Log.v("Data : ", usernamelogin + "," + passwordlogin + "," + emaillogin);
                            Map<String, String> param = new HashMap<String, String>();
                            param.put("username", usernamelogin);
                            param.put("password", passwordlogin);
                            param.put("email", emaillogin);
                            logout(param);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Fuser.this);
                            builder.setTitle("Session ended");
                            builder.setCancelable(false);
                            builder.setMessage("Sesi anda berakhir, mohon login kembali...");
                            builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.execSQL("delete from tbl_user");
                                    SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                    prefs.edit().clear().commit();
                                    Intent formku = new Intent(Fuser.this, Flogin.class);
                                    startActivity(formku);
                                    finish();
                                }
                            });
                            builder.show();
                        }
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
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(Fuser.this);
            builder.setTitle("Application Error");
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

        hm_navbar = (BottomNavigationView) findViewById(R.id.hm_navbar);
        hm_navbar.setSelectedItemId(R.id.hm_user);
        hm_navbar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.hm_menu:
                        startActivity(new Intent(getApplicationContext(),FMenuList.class));
                        finish();
                        overridePendingTransition(1000,1000);
                        return true;
                    case R.id.hm_user:
                        return true;
                }
                return false;
            }
        });


    }

    public void update_data () {
        builderupdate = new AlertDialog.Builder(Fuser.this);
        LayoutInflater inflater = Fuser.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.cust_popup_updateuser, null);
        AutoCompleteTextView updt_username = (AutoCompleteTextView) view.findViewById(R.id.updt_username),
                updt_email=(AutoCompleteTextView) view.findViewById(R.id.updt_email),
                updt_alias=(AutoCompleteTextView) view.findViewById(R.id.updt_alias),
                updt_oldpassword=(AutoCompleteTextView) view.findViewById(R.id.updt_oldpassword),
                updt_newpassword=(AutoCompleteTextView) view.findViewById(R.id.updt_newpassword),
                updt_confpassword=(AutoCompleteTextView) view.findViewById(R.id.updt_confpassword);

        TextInputLayout updt_username_head = (TextInputLayout) view.findViewById(R.id.updt_username_head),
                updt_email_head=(TextInputLayout) view.findViewById(R.id.updt_email_head),
                updt_alias_head=(TextInputLayout) view.findViewById(R.id.updt_alias_head),
                updt_oldpassword_head=(TextInputLayout) view.findViewById(R.id.updt_oldpassword_head),
                updt_newpassword_head=(TextInputLayout) view.findViewById(R.id.updt_newpassword_head),
                updt_confpassword_head=(TextInputLayout) view.findViewById(R.id.updt_confpassword_head);
        updt_oldpassword_head.setHelperText("*Required");
        updt_newpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatepass(updt_newpassword_head,updt_newpassword.getText().toString(),updt_confpassword_head,updt_confpassword.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        updt_confpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatepass(updt_newpassword_head,updt_newpassword.getText().toString(),updt_confpassword_head,updt_confpassword.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        updt_breset = (CircularProgressButton) view.findViewById(R.id.updt_breset);
        updt_breset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (updt_oldpassword_head.getHelperText()==null) {
                    if (updt_email_head.getHelperText()==null) {
                        if (validateall(updt_username.getText().toString(),updt_email.getText().toString(),updt_alias.getText().toString(),updt_newpassword.getText().toString())==true) {
                            if (validateemail(updt_email_head, updt_email.getText().toString()) == true) {
                                if (validatepass(updt_newpassword_head, updt_newpassword.getText().toString(), updt_confpassword_head, updt_confpassword.getText().toString()) == true) {
                                    Button bton = ((AlertDialog) show)
                                            .getButton(AlertDialog.BUTTON_NEGATIVE);
                                    bton.setEnabled(false);
                                    Map<String, String> param = new HashMap<String, String>();
                                    if (updt_username.getText().toString().trim().length()>0) {
                                        param.put("username", updt_username.getText().toString().toLowerCase());
                                    }
                                    if (updt_email.getText().toString().trim().length()>0) {
                                        param.put("email", updt_email.getText().toString());
                                    }
                                    if (updt_alias.getText().toString().trim().length()>0) {
                                        param.put("alias", updt_alias.getText().toString().toUpperCase());
                                    }
                                    if (updt_newpassword.getText().toString().trim().length()>0) {
                                        param.put("password", updt_newpassword.getText().toString());
                                    }
                                    param.put("old_password", updt_oldpassword.getText().toString());
                                    changeuser(param);
                                }
                            }
                        } else {
                            Toast.makeText(Fuser.this, "Cant put emty data", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
        updt_oldpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()>0) {
                    updt_oldpassword_head.setHelperText(null);
                } else {
                    updt_oldpassword_head.setHelperText("*Required");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        updt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String checkemail = updt_email.getText().toString();
                if(checkemail.isEmpty()==false){
                    if(!EMAIL_ADDRESS_PATTERN.matcher(checkemail).matches()){
                        updt_email_head.setHelperText("Mohon masukkan email valid");
                    } else {
                        updt_email_head.setHelperText(null);
                    }
                } else {
                    updt_email_head.setHelperText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        builderupdate.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builderupdate.setCancelable(false);
        builderupdate.setView(view);
        show=builderupdate.show();
    }

    private boolean validateall(String username, String email, String alias, String pass) {
        boolean temp=false;
        if(username.isEmpty() && email.isEmpty() && alias.isEmpty() && pass.isEmpty()){
            temp=false;
        } else {
            temp=true;
        }
        return temp;
    }

    private boolean validateemail(TextInputLayout layoutemail, String text) {
        boolean temp=false;
        String checkemail = text;
        if(checkemail.isEmpty()==false){
            if(!EMAIL_ADDRESS_PATTERN.matcher(checkemail).matches()){
                layoutemail.setHelperText("Mohon masukkan email valid");
                temp=false;
            } else {
                layoutemail.setHelperText(null);
                temp=true;
            }
        } else {
            temp=true;
        }
        return temp;
    }

    private boolean validatepass(TextInputLayout inputLayoutPass ,String password,TextInputLayout inputLayoutConfirm,String confirm) {
        boolean temp=false;
        String pass=password;
        String cpass=confirm;
        if (pass.isEmpty() && cpass.isEmpty()==false){
            if (!pass.equals(cpass)) {
                inputLayoutConfirm.setHelperText("Password tidak sama");
                temp = false;
            } else {
                inputLayoutPass.setHelperText(null);
                inputLayoutConfirm.setHelperText(null);
                temp = true;
            }
        } else if (cpass.isEmpty() && pass.isEmpty()==false){
            if (!pass.equals(cpass)) {
                inputLayoutConfirm.setHelperText("Password tidak sama");
                temp = false;
            } else {
                inputLayoutPass.setHelperText(null);
                inputLayoutConfirm.setHelperText(null);
                temp = true;
            }
        } else if (cpass.isEmpty()==false && pass.isEmpty()==false) {
            if (!pass.equals(cpass)) {
                inputLayoutConfirm.setHelperText("Password tidak sama");
                temp = false;
            } else {
                inputLayoutPass.setHelperText(null);
                inputLayoutConfirm.setHelperText(null);
                temp = true;
            }
        } else {
            inputLayoutPass.setHelperText(null);
            inputLayoutConfirm.setHelperText(null);
            temp = true;
        }
        return temp;
    }

    private final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9+._%-+]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" +
                    "(" +
                    "." +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" +
                    ")+"
    );

    private void changeuser (Map<String,String> param) {
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='edit_user'",null);
        cr.moveToFirst();
        String URL = API+cr.getString(1)+id_user;

        updt_breset.startAnimation();
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status==200) {
                                db.execSQL("delete from tbl_user");
                                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                prefs.edit().clear().commit();
                                JSONObject obj = jsonObject.getJSONObject("data");
                                String idlogin=obj.getString("id");
                                String userlogin=obj.getString("username");
                                String passlogin=obj.getString("password");
                                String aliaslogin=obj.getString("alias");
                                String deviceidlogin=obj.getString("device_id");
                                String devicelogin=obj.getString("device");
                                String emaillogins=obj.getString("email");
                                String kodecabang=obj.getString("kode_cabang");
                                db.execSQL("insert into tbl_user (`id`,`username`,`password`,`email`,`alias`,`kode_cabang`) values " +
                                        "('"+idlogin+"','"+userlogin+"','"+passlogin+"','"+emaillogins+"','"+aliaslogin+"','"+kodecabang+"')");
                                SharedPreferences prefs2 = getSharedPreferences("UserData", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs2.edit();
                                editor.putString("id", idlogin);
                                editor.putString("username", userlogin);
                                editor.putString("password", passlogin);
                                editor.putString("alias", aliaslogin);
                                editor.putString("device_id", deviceidlogin);
                                editor.putString("device", devicelogin);
                                editor.putString("email", emaillogins);
                                editor.putString("kode_cabang", kodecabang);
                                editor.commit();
                                emaillogin=emaillogins;
                                id_user=idlogin;
                                usr_alias.setText(aliaslogin);
                                usernamelogin=userlogin;
                                passwordlogin=passlogin;
                                show.dismiss();
                                Toast.makeText(Fuser.this,"Update done", Toast.LENGTH_LONG).show();
                            } else {
                                String errorname = jsonObject.getString("message");
                                Toast.makeText(Fuser.this,errorname, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v("JsonObjectError",e.getMessage());
                            Toast.makeText(Fuser.this,e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        Button bton = ((AlertDialog) show)
                                .getButton(AlertDialog.BUTTON_NEGATIVE);
                        bton.setEnabled(true);
                        updt_breset.revertAnimation();
                        updt_breset.setBackgroundResource(R.drawable.round_ungu);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "Something wrong, please contact developer...";
                        if (error instanceof NetworkError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again after some time!!";
                        } else if (error instanceof AuthFailureError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again after some time!!";
                        } else if (error instanceof NoConnectionError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (error instanceof TimeoutError) {
                            message = "Connection TimeOut! Please check your internet connection.";
                        }
                        Toast.makeText(Fuser.this,message, Toast.LENGTH_LONG).show();
                        Button bton = ((AlertDialog) show)
                                .getButton(AlertDialog.BUTTON_NEGATIVE);
                        bton.setEnabled(true);
                        updt_breset.revertAnimation();
                        updt_breset.setBackgroundResource(R.drawable.round_ungu);
                    }
                })
        {
            @NonNull
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = param;
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
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Fuser.this);
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void logout (Map<String,String> paramlogin) {
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='logout'",null);
        cr.moveToFirst();
        String URL = API+cr.getString(1);
        Log.v("API",URL);
        hm_navbar.setVisibility(View.INVISIBLE);
        usr_beditaccount.setEnabled(false);
        usr_bloginlogout.startAnimation();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            Log.v("Status Logout","int : "+status);
                            if (status==200) {
                                db.execSQL("delete from tbl_user");
                                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                prefs.edit().clear().commit();
                                Intent formku = new Intent(Fuser.this, Flogin.class);
                                formku.putExtra("start",0);
                                startActivity(formku);
                                finish();
                            } else {
                                String errorname = jsonObject.getString("message");
                                Toast.makeText(Fuser.this,errorname, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v("JsonObjectError",e.getMessage());
                            Toast.makeText(Fuser.this,e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        hm_navbar.setVisibility(View.VISIBLE);
                        usr_bloginlogout.revertAnimation();
                        usr_bloginlogout.setBackgroundResource(R.drawable.round_kuning);
                        usr_beditaccount.setEnabled(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "Something wrong, please contact developer...";
                        if (error instanceof NetworkError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again after some time!!";
                        } else if (error instanceof AuthFailureError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again after some time!!";
                        } else if (error instanceof NoConnectionError) {
                            message = "Cannot connect to Internet...Please check your connection!";
                        } else if (error instanceof TimeoutError) {
                            message = "Connection TimeOut! Please check your internet connection.";
                        }
                        Toast.makeText(Fuser.this,message, Toast.LENGTH_LONG).show();
                        hm_navbar.setVisibility(View.VISIBLE);
                        usr_beditaccount.setEnabled(true);
                        usr_bloginlogout.revertAnimation();
                        usr_bloginlogout.setBackgroundResource(R.drawable.round_kuning);
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
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Fuser.this);
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    //TombolBack
    public void onBackPressed() {
        if (hm_navbar.getVisibility()==View.VISIBLE) {
            hm_navbar.setSelectedItemId(R.id.hm_menu);
        }
        return;
    }
}