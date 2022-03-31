package com.urban.stockcount1.CustomClass;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import com.urban.stockcount1.R;
import com.urban.stockcount1.FHistory_Counting;

import java.util.ArrayList;
import java.util.List;

public class ListItemDistinAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> namabrg;
    private ArrayList<String> kodebrg;
    private ArrayList<String> qtysj;
    private ArrayList<String>qtyditerima;
    private List<Boolean> checked;
    private String idnya;
    private SQLiteDatabase db;

    public ListItemDistinAdapter(Context context,
                                 ArrayList<String> namabrg2,
                                 ArrayList<String> kodebrg2,
                                 ArrayList<String> qtysj2,
                                 ArrayList<String> qtyditerima2,
                                 String idnya2) {
        super();
        mContext = context;
        namabrg = namabrg2;
        kodebrg = kodebrg2;
        qtysj=qtysj2;
        qtyditerima=qtyditerima2;
        checked = new ArrayList<>();
        idnya=idnya2;
        for(String name : namabrg) checked.add(false);
    }

    public int getCount() {
        return namabrg.size();
    }

    public View getView(final int position, View view, ViewGroup parent) {
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.cus_list_item_distin, parent, false);
        //
        TextView din_namabrg = (TextView) view.findViewById(R.id.din_namabrg),
                din_kodebrg = (TextView) view.findViewById(R.id.din_kodebrg),
                din_qtyterkirim = (TextView) view.findViewById(R.id.din_qtyterkirim);
        EditText din_qtyditerima = (EditText) view.findViewById(R.id.din_qtyditerima);
        Button din_min_qty = (Button) view.findViewById(R.id.din_min_qty),
                din_plus_qty = (Button) view.findViewById(R.id.din_plus_qty);
        CheckBox din_check = (CheckBox) view.findViewById(R.id.din_check);
        //
        din_namabrg.setTag(position);
        din_kodebrg.setTag(position);
        din_qtyterkirim.setTag(position);
        din_qtyditerima.setTag(position);
        din_min_qty.setTag(position);
        din_plus_qty.setTag(position);
        din_check.setTag(position);
        //
        din_namabrg.setText(namabrg.get(position));
        din_kodebrg.setText(kodebrg.get(position));
        din_qtyterkirim.setText(qtysj.get(position));
        din_qtyditerima.setText(qtyditerima.get(position));
        din_check.setChecked(checked.get(position));
        //
        din_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked.set(position, din_check.isChecked());
            }
        });
        din_qtyditerima.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value;
                if (s == null || s.toString().isEmpty()) {
                    value = "0";
                } else {
                    value = s.toString();
                }
                qtyditerima.set(position,value.toString());
                db.execSQL("UPDATE tbl_distin_detail SET `Qty_receive`='" + qtyditerima.get(position) + "' WHERE `Kode_barang`='" + kodebrg.get(position) + "' AND `No_bukti`='" + idnya + "'");
                int nilai = 0;
                Cursor cr = db.rawQuery("select SUM(`Qty_receive`) as `receive` from `tbl_distin_detail` WHERE `No_bukti`='"+idnya+"' GROUP by `No_bukti`",null);
                cr.moveToFirst();
                if (cr.getCount()>0) {
                    nilai = cr.getInt(0);
                } else {
                    nilai=0;
                }
                db.execSQL("UPDATE tbl_distin SET `Total_qty_receive`='"+nilai+"' WHERE `No_bukti`='"+idnya+"'");
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        din_min_qty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cek=din_qtyditerima.getText().toString();
                String rekt;
                if (cek.matches("")) {
                    rekt="0";
                } else{
                    rekt=din_qtyditerima.getText().toString();
                }
                int rentPrice = Integer.parseInt(rekt);
                int out = rentPrice-1;
                int hasil;
                if (out < 0) {
                    hasil=0;
                } else {
                    hasil=out;
                }
                din_qtyditerima.setText(String.valueOf(hasil));
            }
        });
        din_plus_qty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cek=din_qtyditerima.getText().toString();
                String rekt;
                if (cek.matches("")) {
                    rekt="0";
                } else{
                    rekt=din_qtyditerima.getText().toString();
                }
                int rentPrice = Integer.parseInt(rekt);
                int out = rentPrice+1;
                int hasil;
                if (out < 0) {
                    hasil=0;
                } else {
                    hasil=out;
                }
                din_qtyditerima.setText(String.valueOf(hasil));
            }
        });
        return view;
    }

    public int getCountSelectedCheckBoxes(){
        int toReturn = 0;
        for(boolean b : checked) if(b) toReturn++;
        return toReturn;
    }

    public String getKodeBarang (int position) {
        String kode = null;
        kode = kodebrg.get(position);
        return kode;
    }

    public boolean isEmpty(){
        return namabrg.isEmpty();
    }

    public void delete(int i){
        namabrg.remove(i);
        kodebrg.remove(i);
        qtysj.remove(i);
        checked.remove(i);
        qtyditerima.remove(i);
        notifyDataSetChanged();
    }

    public void setChecked(int i, Boolean IsCheck){
        checked.set(i,IsCheck);
        notifyDataSetChanged();
    }

    public String getName (int i) {
        String x = kodebrg.get(i);
        return x;
    }


    public List<Boolean> getCheckList(){
        return checked;
    }

    public Object getItem(int position) {
        return position;
//        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
}

