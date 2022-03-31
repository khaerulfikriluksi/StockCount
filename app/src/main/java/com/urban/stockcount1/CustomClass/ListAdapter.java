package com.urban.stockcount1.CustomClass;

import android.content.Context;
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
import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> listkode;
    private ArrayList<String> listnama;
    private ArrayList<String> val;
    private String lid;
    private List<Boolean> checked;
    private SQLiteDatabase database;

    public ListAdapter(Context context, ArrayList<String> arraykode,ArrayList<String> arraynama, ArrayList<String> qtynya, String idscan) {
        super();
        mContext = context;
        listkode = arraykode;
        listnama = arraynama;
        val=qtynya;
        lid=idscan;
        checked = new ArrayList<>();
        for(String name : val) checked.add(false);
    }

    public int getCount() {
        return listkode.size();
    }

    // getView method is called for each item of ListView
    public View getView(final int position, View view, ViewGroup parent) {
        database = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.cus_list_item_scan, parent, false);
        //
        EditText eqty = (EditText) view.findViewById(R.id.eqtyscan);
        eqty.setTag(position);
        TextView ekodescn= (TextView)view.findViewById(R.id.kdbrgscan);
        ekodescn.setTag(position);
        TextView enamascn= (TextView)view.findViewById(R.id.nmbrgscan);
        enamascn.setTag(position);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkitemscan);
        checkBox.setTag(position);
        checkBox.setChecked(checked.get(position));
        // get the reference of textView and button
        Button bmin = (Button) view.findViewById(R.id.bminqty);
        bmin.setTag(position);
        Button bpls = (Button) view.findViewById(R.id.bplsqty);
        bpls.setTag(position);
        //
        ekodescn.setText(listkode.get(position));
        enamascn.setText(listnama.get(position));
        eqty.setText(val.get(position));
        // Click listener of button
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked.set(position, checkBox.isChecked());
            }
        });
        eqty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               //
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value;
                if (s == null || s.toString().isEmpty()) {
                    value = "0";
                } else {
                    value = s.toString();
                }
                val.set(position,value.toString());
                database.execSQL("UPDATE tbl_counting_detail SET `qty`='" + val.get(position) + "' WHERE `kode_barang`='" + listkode.get(position) + "' AND `no_doc`='" + lid + "'");
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        bmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cek=eqty.getText().toString();
                String rekt;
                if (cek.matches("")) {
                    rekt="0";
                } else{
                    rekt=eqty.getText().toString();
                }
                int rentPrice = Integer.parseInt(rekt);
                int out = rentPrice-1;
                int hasil;
                if (out < 0) {
                    hasil=0;
                } else {
                    hasil=out;
                }
                eqty.setText(String.valueOf(hasil));
            }
        });
        bpls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cek=eqty.getText().toString();
                String rekt;
                if (cek.matches("")) {
                    rekt="0";
                } else{
                    rekt=eqty.getText().toString();
                }
                int rentPrice = Integer.parseInt(rekt);
                int out = rentPrice+1;
                int hasil;
                if (out < 0) {
                    hasil=0;
                } else {
                    hasil=out;
                }
                eqty.setText(String.valueOf(hasil));
            }
        });
        return view;
    }

    public int getCountSelectedCheckBoxes(){
        int toReturn = 0;
        for(boolean b : checked) if(b) toReturn++;
        return toReturn;
    }

    public boolean isEmpty(){
        return listkode.isEmpty();
    }

    public void delete(int i){
        val.remove(i);
        listkode.remove(i);
        listnama.remove(i);
        checked.remove(i);
        notifyDataSetChanged();
    }

    public void setChecked(int i, Boolean IsCheck){
        checked.set(i,IsCheck);
        notifyDataSetChanged();
    }

    public String getName(int position){
        return listkode.get(position);
    }

    public List<Boolean> getCheckList(){
        return checked;
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
//        return list.get(position);
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}