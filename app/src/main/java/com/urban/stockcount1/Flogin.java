package com.urban.stockcount1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Flogin extends AppCompatActivity {

    SQLiteDatabase db;
    private Map<String,String> param = new HashMap<String,String>();
    private int newstart;
    private AlertDialog show;
    private AlertDialog.Builder builderreset;
    private CircularProgressButton rst_breset;
    private String Api=null,TokenIDFCM=null;
    private String stau,device_id,device_name
            ,idlogin,userlogin,passlogin,aliaslogin
            ,deviceidlogin,devicelogin,emaillogin,kodecabang;
    private CircularProgressButton log_blogin;
    private TextView log_register, log_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flogin);
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        try{
            String versionName = Flogin.this.getPackageManager()
                    .getPackageInfo(Flogin.this.getPackageName(), 0).versionName;
            int versioncode= Flogin.this.getPackageManager()
                    .getPackageInfo(Flogin.this.getPackageName(),0).versionCode;
            TextView log_version = (TextView) findViewById(R.id.log_version);
            log_version.setText("--Versi "+versionName+"."+String.valueOf(versioncode)+"--");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
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
            device_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            device_name=android.os.Build.MODEL;
            AutoCompleteTextView log_username=(AutoCompleteTextView) findViewById(R.id.log_username),
                    log_password=(AutoCompleteTextView) findViewById(R.id.log_password);
            log_blogin=(CircularProgressButton) findViewById(R.id.log_blogin);
            log_register = (TextView) findViewById(R.id.log_register);
            log_reset = (TextView) findViewById(R.id.log_reset);
            newstart=getIntent().getExtras().getInt("start");
            Api=cursor.getString(1);
            log_blogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (log_username.getText().toString().trim().length()>0){
                        if (log_password.getText().toString().trim().length()>0){
                            param.clear();
                            param.put("username", log_username.getText().toString());
                            param.put("password", log_password.getText().toString());
                            param.put("device", device_name);
                            param.put("device_id", device_id);
                            Gettoken();
                        } else {
                            log_password.requestFocus();
                        }
                    } else {
                        log_username.requestFocus();
                    }
                }
            });
        } else{
            AlertDialog.Builder builder = new AlertDialog.Builder(Flogin.this);
            builder.setTitle("Application Error");
            builder.setMessage("API tidak ada, silahkan kontak developer...");
            builder.setPositiveButton("Tutup aplikasi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    System.exit(0);
                }
            });
            builder.setCancelable(false);
            builder.show();
        }

    }

    private void Gettoken() {
        log_blogin.startAnimation();
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w("GET-TOKEN", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    TokenIDFCM=token;
                    Log.w("TOKEN", TokenIDFCM);
                    param.put("token_id", TokenIDFCM);
                    login(param);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    log_blogin.revertAnimation();
                    log_blogin.setBackgroundResource(R.drawable.round_ungu);
                    Toast.makeText(Flogin.this,"Google server not responding", Toast.LENGTH_LONG).show();
                }
            });
    }

    public void reset_log (View v) {
        builderreset = new AlertDialog.Builder(Flogin.this);
        LayoutInflater inflater = Flogin.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.cust_popup_reset_login, null);
        AutoCompleteTextView rst_email = (AutoCompleteTextView) view.findViewById(R.id.rst_email),
                rst_password=(AutoCompleteTextView) view.findViewById(R.id.rst_password);
        TextInputLayout rst_email_head = (TextInputLayout) view.findViewById(R.id.rst_email_head),
                rst_password_head=(TextInputLayout) view.findViewById(R.id.rst_password_head);
        rst_password_head.setHelperText("*Required");
        rst_email_head.setHelperText("*Required");
        rst_breset = (CircularProgressButton) view.findViewById(R.id.rst_breset);
        rst_breset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rst_email_head.getHelperText()==null) {
                    if (rst_password_head.getHelperText()==null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                        Button positiveButton = ((AlertDialog) show)
//                                .getButton(AlertDialog.BUTTON_NEGATIVE);
//                        positiveButton.setEnabled(false);
                        Map<String, String> param = new HashMap<String, String>();
                        param.put("email", rst_email.getText().toString());
                        param.put("password", rst_password.getText().toString());
                        resetlogin(param);
                    }
                }
            }
        });
        rst_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()>0) {
                    rst_password_head.setHelperText(null);
                } else {
                    rst_password_head.setHelperText("*Required");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        rst_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String checkemail = rst_email.getText().toString();
                if(checkemail.isEmpty()){
                    rst_email_head.setHelperText("*Required");
                }
                else if(!EMAIL_ADDRESS_PATTERN.matcher(checkemail).matches()){
                    rst_email_head.setHelperText("Mohon masukkan email valid");
                } else {
                    rst_email_head.setHelperText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        builderreset.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                show.dismiss();
            }
        });
        builderreset.setCancelable(false);
        builderreset.setView(view);
        show=builderreset.show();
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

    private void resetlogin (Map<String,String> paramreset) {
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='forgot'",null);
        cr.moveToFirst();
        String URL = Api+cr.getString(1);
        Button bton = ((AlertDialog) show)
                .getButton(AlertDialog.BUTTON_NEGATIVE);
        bton.setEnabled(false);
        Log.v("ResetLogin",URL);
        rst_breset.startAnimation();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("ResetLoginResult",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            stau=String.valueOf(status);
                            if (status==200) {
                                String message = jsonObject.getString("message");
                                Toast.makeText(Flogin.this,message, Toast.LENGTH_LONG).show();
                                show.dismiss();
                            } else {
                                String errorname = jsonObject.getString("message");
                                Toast.makeText(Flogin.this,errorname, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v("JsonObjectError",e.getMessage());
                            Toast.makeText(Flogin.this,e.getMessage()+"  "+stau, Toast.LENGTH_LONG).show();
                        }
                        bton.setEnabled(true);
                        rst_breset.revertAnimation();
                        rst_breset.setBackgroundResource(R.drawable.round_ungu);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        bton.setEnabled(true);
                        String message = "Something wrong, please contact developer";
                        if (error instanceof NetworkError) {
                            message = "Cannot connect to Server...Please check your connection!";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again after some time!!";
                        } else if (error instanceof AuthFailureError) {
                            message = "Cannot connect to Server...Please check your connection!";
                        } else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again after some time!!";
                        } else if (error instanceof NoConnectionError) {
                            message = "Cannot connect to Server...Please check your connection!";
                        } else if (error instanceof TimeoutError) {
                            message = "Connection TimeOut! Please check your internet connection.";
                        }
                        Toast.makeText(Flogin.this,message, Toast.LENGTH_LONG).show();
                        rst_breset.revertAnimation();
                        rst_breset.setBackgroundResource(R.drawable.round_ungu);
                    }
                })
        {
            @NonNull
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = paramreset;
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
                    Toast.makeText(Flogin.this,"Lost connection from server", Toast.LENGTH_LONG).show();
                }
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Flogin.this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void login (Map<String,String> paramlogin) {
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='login'",null);
        cr.moveToFirst();
        log_register.setVisibility(View.INVISIBLE);
        log_reset.setVisibility(View.INVISIBLE);
        String URL = Api+cr.getString(1);
        Log.v("Login",URL);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            stau=String.valueOf(status);
                            if (status==200) {
                                db.execSQL("delete from tbl_user");
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
                                Intent formku = new Intent(Flogin.this, FMenuUtama.class);
                                startActivity(formku);
                                finish();
                            } else {
                                String errorname = jsonObject.getString("message");
                                Log.v("JsonObjectError",errorname);
                                Toast.makeText(Flogin.this,errorname, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.v("JsonObjectError",e.getMessage());
                            Toast.makeText(Flogin.this,e.getMessage()+"  "+stau, Toast.LENGTH_LONG).show();
                        }
                        log_register.setVisibility(View.VISIBLE);
                        log_reset.setVisibility(View.VISIBLE);
                        log_blogin.revertAnimation();
                        log_blogin.setBackgroundResource(R.drawable.round_ungu);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.v("VolleyPostError",error.getMessage());
                        String message = "Something wrong, please contact developer...";
                        if (error instanceof NetworkError) {
                            message = "Cannot connect to Server...Please check your connection!";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again after some time!!";
                        } else if (error instanceof AuthFailureError) {
                            message = "Cannot connect to Server...Please check your connection!";
                        } else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again after some time!!";
                        } else if (error instanceof NoConnectionError) {
                            message = "Cannot connect to Server...Please check your connection!";
                        } else if (error instanceof TimeoutError) {
                            message = "Connection TimeOut! Please check your internet connection.";
                        }
                        Toast.makeText(Flogin.this,message, Toast.LENGTH_LONG).show();
                        log_register.setVisibility(View.VISIBLE);
                        log_reset.setVisibility(View.VISIBLE);
                        log_blogin.revertAnimation();
                        log_blogin.setBackgroundResource(R.drawable.round_ungu);
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
                if (response.statusCode!=200) {
                    Toast.makeText(Flogin.this,"Lost connection from server", Toast.LENGTH_LONG).show();
                }
                return super.parseNetworkResponse(response);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Flogin.this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    public void register (View v) {
        Intent formku = new Intent(Flogin.this, FRegister.class);
        startActivity(formku);
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