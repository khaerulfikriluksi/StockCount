package com.urban.stockcount1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.urban.stockcount1.CustomClass.ListHistoryStkreqAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FHistory_StockRequest extends AppCompatActivity {

//    private ArrayList<String> data;
//    ArrayAdapter<String> sd;
//    public int TOTAL_LIST_ITEMS = 15;
//    public int NUM_ITEMS_PAGE   = 100;
//    private int noOfBtns;
//    private Button[] btns;
    private String API, tanggal, status;
    private LottieAnimationView str_hist_animsearch;
    private LinearLayout str_hist_nointernet;
    private TextView str_hist_numpage;
    private RecyclerView str_hist_listview;
    private SQLiteDatabase db;
    private ListHistoryStkreqAdapter adapter;
    private AlertDialog show;
    private List<String> stat = new ArrayList<>();
    private Button str_hist_last,str_hist_next,str_hist_prev,str_hist_first;
    private ImageButton str_hist_bfilter;
    private EditText str_hist_curpage,str_hist_search;
    private SwipeRefreshLayout str_hist_pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fhistory_stock_request);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
            window.setNavigationBarColor(this.getResources().getColor(R.color.putih_pucat));
        }
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor2 = db.rawQuery("SELECT * FROM tbl_api", null);
        cursor2.moveToFirst();
        if (cursor2.getCount()>0) {
            API = cursor2.getString(2);
//            API = cursor2.getString(1);
            Cursor cr = db.rawQuery("SELECT * FROM tbl_user", null);
            cr.moveToFirst();
            if (cr.getCount()>0) {
                final Calendar calendar = Calendar.getInstance();
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                tanggal = simpleDateFormat.format(calendar.getTime());
                status="ALL";
                //
                stat.add("ALL");
                stat.add("PROSES");
                stat.add("SELESAI");
                stat.add("CANCEL");
                stat.add("DRAFT");
                //
                str_hist_last = (Button) findViewById(R.id.str_hist_last);
                str_hist_next = (Button) findViewById(R.id.str_hist_next);
                str_hist_prev = (Button) findViewById(R.id.str_hist_prev);
                str_hist_first = (Button) findViewById(R.id.str_hist_first);
                str_hist_animsearch = (LottieAnimationView) findViewById(R.id.str_hist_animsearch);
                str_hist_nointernet = (LinearLayout) findViewById(R.id.str_hist_nointernet);
                str_hist_numpage = (TextView) findViewById(R.id.str_hist_numpage);
                str_hist_curpage = (EditText) findViewById(R.id.str_hist_curpage);
                str_hist_pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.str_hist_pullToRefresh);
                str_hist_search = (EditText) findViewById(R.id.str_hist_search);
                str_hist_bfilter = (ImageButton) findViewById(R.id.str_hist_bfilter);
                //
                LinearLayoutManager layoutManager = new LinearLayoutManager(FHistory_StockRequest.this,LinearLayoutManager.VERTICAL,false);
                str_hist_listview=(RecyclerView) findViewById(R.id.str_hist_listview);
                str_hist_listview.setLayoutManager(layoutManager);
                GetHistlistSTR();
                str_hist_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        str_hist_curpage.setText("1");
                        GetHistlistSTR();
                        return false;
                    }
                });
                str_hist_pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        GetHistlistSTR();
                    }
                });
                str_hist_curpage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (str_hist_curpage.getText().toString().trim().length()<1) {
                            str_hist_curpage.setText("1");
                        }
                        GetHistlistSTR();
                        return false;
                    }
                });
                str_hist_last.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        str_hist_curpage.setText(str_hist_numpage.getText().toString().trim());
                        GetHistlistSTR();
                    }
                });
                str_hist_first.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        str_hist_curpage.setText("1");
                        GetHistlistSTR();
                    }
                });
                str_hist_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String cek=str_hist_curpage.getText().toString().trim();
                        String rekt;
                        if (cek.matches("")) {
                            rekt="1";
                        } else{
                            rekt=str_hist_curpage.getText().toString();
                        }
                        int rentPrice = Integer.parseInt(rekt);
                        int out = rentPrice+1;
                        int hasil;
                        if (out < 0) {
                            hasil=0;
                        } else {
                            hasil=out;
                        }
                        str_hist_curpage.setText(String.valueOf(hasil));
                        GetHistlistSTR();
                    }
                });
                str_hist_prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String cek=str_hist_curpage.getText().toString().trim();
                        String rekt;
                        if (cek.matches("")) {
                            rekt="1";
                        } else{
                            rekt=str_hist_curpage.getText().toString();
                        }
                        int rentPrice = Integer.parseInt(rekt);
                        int out = rentPrice-1;
                        int hasil;
                        if (out < 0) {
                            hasil=0;
                        } else {
                            hasil=out;
                        }
                        str_hist_curpage.setText(String.valueOf(hasil));
                        GetHistlistSTR();
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_StockRequest.this);
                builder.setTitle("Session ended");
                builder.setCancelable(false);
                builder.setMessage("Sesi anda berakhir, mohon login kembali...");
                builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.execSQL("delete from tbl_user");
                        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                        prefs.edit().clear().commit();
                        Intent formku = new Intent(FHistory_StockRequest.this, Flogin.class);
                        startActivity(formku);
                        finish();
                    }
                });
                builder.show();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_StockRequest.this);
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
//        Btnfooter();
//        data = new ArrayList<String>();
//        for(int i=0;i<TOTAL_LIST_ITEMS;i++)
//        {
//            data.add("This is Item "+(i+1));
//        }
//        loadList(0);
//        CheckBtnBackGroud(0);

    }

    public void OpenFilter(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_StockRequest.this);
        LayoutInflater inflat = (LayoutInflater) FHistory_StockRequest.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view2 = inflat.inflate(R.layout.cust_popup_filterhist_stk, null);
        TextInputLayout str_hist_filterstat_head = (TextInputLayout) view2.findViewById(R.id.str_hist_filterstat_head),
                str_hist_filtertgl_head = (TextInputLayout) view2.findViewById(R.id.str_hist_filtertgl_head);
        AutoCompleteTextView str_hist_filterstat = (AutoCompleteTextView) view2.findViewById(R.id.str_hist_filterstat),
                str_hist_filtertgl = (AutoCompleteTextView) view2.findViewById(R.id.str_hist_filtertgl);
        //
        str_hist_filterstat.setText(status);
        str_hist_filtertgl.setText(tanggal);
        //
        ArrayAdapter<String> adapterstat = new ArrayAdapter<String>(FHistory_StockRequest.this,
                R.layout.cust_dropdown_spinner, stat);
        str_hist_filterstat.setAdapter(adapterstat);
        str_hist_filtertgl_head.setEndIconDrawable(R.drawable.ico_calendar);
        str_hist_filtertgl_head.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_hist_filtertgl.callOnClick();
            }
        });
        str_hist_filtertgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String insertDate = str_hist_filtertgl.getText().toString();
                String[] items1 = insertDate.split("-");
                String d1=items1[2];
                String m1=items1[1];
                String y1=items1[0];
                int datenow = Integer.parseInt(d1);
                int monthnow = Integer.parseInt(m1)-1;
                int yearnow = Integer.parseInt(y1);
                final Calendar calendar = Calendar.getInstance();
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                DatePickerDialog datePickerDialog= new DatePickerDialog(FHistory_StockRequest.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year,month,dayOfMonth);
                        str_hist_filtertgl.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                },yearnow,monthnow,datenow);
                datePickerDialog.setTitle("To Date");
                datePickerDialog.show();
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                status = str_hist_filterstat.getText().toString();
                tanggal = str_hist_filtertgl.getText().toString();
                show.dismiss();
                str_hist_curpage.setText("1");
                GetHistlistSTR();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                show.dismiss();
            }
        });
        builder.setView(view2);
        show=builder.show();
    }

    public void kembali_home(View v) {
        onBackPressed();
    }

    public void onBackPressed() {
        Intent Formku = new Intent(FHistory_StockRequest.this, FMenuList.class);
        startActivity(Formku);
        finish();
    }

    public void refreshgetSTR(View v) {
        GetHistlistSTR();
    }

    private void GetHistlistSTR() {
        Cursor cursor2 = db.rawQuery("SELECT * FROM tbl_api", null);
        cursor2.moveToFirst();
        if (cursor2.getCount()>0) {
            API = cursor2.getString(2);
//            API = cursor2.getString(1);
        }
        str_hist_search.setEnabled(false);
        str_hist_bfilter.setClickable(false);
        str_hist_animsearch.setVisibility(View.VISIBLE);
        str_hist_nointernet.setVisibility(View.GONE);
        str_hist_curpage.setEnabled(false);
        str_hist_first.setEnabled(false);
        str_hist_prev.setEnabled(false);
        str_hist_next.setEnabled(false);
        str_hist_last.setEnabled(false);
        Cursor cr = db.rawQuery("SELECT * FROM tbl_api_path WHERE `name`='history_request'",null);
        cr.moveToFirst();
        String page = str_hist_curpage.getText().toString().trim();
        String st="";
        if (status.contains("ALL")) {
            st="";
        } else {
            st=status;
        }
        String statu = st;
        String tgl_buat = tanggal;
        String search = str_hist_search.getText().toString().trim();
        String jsonurl = API+cr.getString(1)+"?page="+page+"&status="+statu+"&tgl_buat="+tgl_buat+"&search="+search;
        Log.v("OpenDistin",jsonurl);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, jsonurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        str_hist_search.setEnabled(true);
                        str_hist_bfilter.setClickable(true);
                        str_hist_curpage.setEnabled(true);
                        str_hist_listview.setAdapter(null);
                        str_hist_animsearch.setVisibility(View.GONE);
                        str_hist_nointernet.setVisibility(View.GONE);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status==200) {
                                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                str_hist_numpage.setText(String.valueOf(jsonObject1.getInt("total_pages")));
                                int total_page = jsonObject1.getInt("total_pages");
                                int curpage = jsonObject1.getInt("page");
                                int row = jsonObject1.getInt("total");
                                str_hist_curpage.setFilters(new InputFilter[]{ new InputFilterMinMax("1", String.valueOf(total_page))});
                                if (row>0) {
                                    if (total_page!=curpage) {
                                        if (curpage==1) {
                                            if (curpage==total_page) {
                                                str_hist_first.setEnabled(false);
                                                str_hist_prev.setEnabled(false);
                                                str_hist_next.setEnabled(false);
                                                str_hist_last.setEnabled(false);
                                            } else {
                                                str_hist_first.setEnabled(false);
                                                str_hist_prev.setEnabled(false);
                                                str_hist_next.setEnabled(true);
                                                str_hist_last.setEnabled(true);
                                            }
                                        } else if (curpage==total_page) {
                                            str_hist_first.setEnabled(true);
                                            str_hist_prev.setEnabled(true);
                                            str_hist_next.setEnabled(false);
                                            str_hist_last.setEnabled(false);
                                        } else{
                                            str_hist_first.setEnabled(true);
                                            str_hist_prev.setEnabled(true);
                                            str_hist_next.setEnabled(true);
                                            str_hist_last.setEnabled(true);
                                        }
                                    } else {
                                        if (curpage==1) {
                                            if (curpage==total_page) {
                                                str_hist_first.setEnabled(false);
                                                str_hist_prev.setEnabled(false);
                                                str_hist_next.setEnabled(false);
                                                str_hist_last.setEnabled(false);
                                            } else {
                                                str_hist_first.setEnabled(false);
                                                str_hist_prev.setEnabled(false);
                                                str_hist_next.setEnabled(true);
                                                str_hist_last.setEnabled(true);
                                            }
                                        } else if (curpage==total_page) {
                                            str_hist_first.setEnabled(true);
                                            str_hist_prev.setEnabled(true);
                                            str_hist_next.setEnabled(false);
                                            str_hist_last.setEnabled(false);
                                        } else{
                                            str_hist_first.setEnabled(true);
                                            str_hist_prev.setEnabled(true);
                                            str_hist_next.setEnabled(true);
                                            str_hist_last.setEnabled(true);
                                        }
                                    }
                                } else {
                                    str_hist_first.setEnabled(false);
                                    str_hist_prev.setEnabled(false);
                                    str_hist_next.setEnabled(false);
                                    str_hist_last.setEnabled(false);
                                }
                                ArrayList<String> noreq = new ArrayList<>();
                                ArrayList<String> tgl = new ArrayList<>();
                                ArrayList<String> namareq = new ArrayList<>();
                                ArrayList<String> status2 = new ArrayList<>();
                                ArrayList<String> pembuat = new ArrayList<>();
                                ArrayList<String> diprosesoleh = new ArrayList<>();
                                JSONArray jsonArray = jsonObject1.getJSONArray("data");
                                for (int position = 0; position < jsonArray.length(); position++) {
                                    JSONObject data = jsonArray.getJSONObject(position);
                                    noreq.add(data.getString("no_request"));
                                    tgl.add(data.getString("tgl_buat"));
                                    namareq.add(data.getString("nama_request"));
                                    status2.add(data.getString("status"));
                                    pembuat.add(data.getString("user_pembuat"));
                                    diprosesoleh.add(data.getString("user_pemeroses"));
                                }
                                adapter = new ListHistoryStkreqAdapter(FHistory_StockRequest.this,
                                        noreq,tgl,namareq,status2,pembuat,diprosesoleh);
                                if (noreq.size()>0) {
                                    str_hist_listview.setAdapter(adapter);
                                } else {
                                    str_hist_curpage.setEnabled(false);
                                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "No Data", Snackbar.LENGTH_SHORT);
                                    mySnackbar.show();
                                }
                            } else {
                                String errorname = jsonObject.getString("message");
                                Log.v("Return-400",errorname);
                                Toast.makeText(FHistory_StockRequest.this,errorname, Toast.LENGTH_LONG).show();
                                str_hist_nointernet.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            str_hist_bfilter.setClickable(true);
                            Log.v("ParseException",e.getMessage());
                            AlertDialog.Builder builder = new AlertDialog.Builder(FHistory_StockRequest.this);
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
                        str_hist_animsearch.setVisibility(View.GONE);
                        str_hist_nointernet.setVisibility(View.GONE);
                        str_hist_pullToRefresh.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Connection lost", Snackbar.LENGTH_SHORT);
                        mySnackbar.show();
                        str_hist_search.setEnabled(true);
                        str_hist_bfilter.setClickable(true);
                        str_hist_pullToRefresh.setRefreshing(false);
                        str_hist_animsearch.setVisibility(View.GONE);
                        str_hist_nointernet.setVisibility(View.VISIBLE);
                        String message = null; // error message, show it in toast or dialog, whatever you want
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                            message = "Cannot connect to Internet";
                        } else if (error instanceof ServerError) {
                            message = "The server could not be found. Please try again later";
                        }  else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again later";
                        }
                        Log.v("Err",message);
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
        RequestQueue requestQueue = Volley.newRequestQueue(FHistory_StockRequest.this);
        requestQueue.add(stringRequest);
    }

    public class InputFilterMinMax implements InputFilter {
        private int min, max;
        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }
        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }
        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }



//    private void Btnfooter()
//    {
//        int val = TOTAL_LIST_ITEMS%NUM_ITEMS_PAGE;
//        val = val==0?0:1;
//        noOfBtns=TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE+val;
//
//        LinearLayout ll = (LinearLayout)findViewById(R.id.btnLay);
//
//        btns    =new Button[noOfBtns];
//
//        for(int i=0;i<noOfBtns;i++)
//        {
//            btns[i] =   new Button(this);
//            btns[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
//            btns[i].setText(""+(i+1));
//
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            ll.addView(btns[i], lp);
//
//            final int j = i;
//            btns[j].setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v)
//                {
//                    loadList(j);
//                    CheckBtnBackGroud(j);
//                }
//            });
//        }
//
//    }
//
//    private void CheckBtnBackGroud(int index)
//    {
////        title.setText("Page "+(index+1)+" of "+noOfBtns);
//        for(int i=0;i<noOfBtns;i++)
//        {
//            if(i==index)
//            {
//                btns[index].setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
//                btns[i].setTextColor(getResources().getColor(android.R.color.white));
//            }
//            else
//            {
//                btns[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
//                btns[i].setTextColor(getResources().getColor(android.R.color.black));
//            }
//        }
//
//    }
//
//    private void loadList(int number)
//    {
//        ArrayList<String> sort = new ArrayList<String>();
//
//        int start = number * NUM_ITEMS_PAGE;
//        for(int i=start;i<(start)+NUM_ITEMS_PAGE;i++)
//        {
//            if(i<data.size())
//            {
//                sort.add(data.get(i));
//            }
//            else
//            {
//                break;
//            }
//        }
//        sd = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                sort);
//    }


}