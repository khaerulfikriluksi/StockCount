package com.urban.stockcount1.CustomClass;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.urban.stockcount1.R;
import com.urban.stockcount1.FHistory_Counting;

import java.util.ArrayList;
import java.util.List;

public class ListHistoryAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> cabanglist;
    private ArrayList<String> idscanlist;
    private ArrayList<String> listtgl;
    private List<Boolean> checked;
    private ArrayList<String>petugaslist;
    private ArrayList<String>raklist;
    private SQLiteDatabase database;

    public ListHistoryAdapter(Context context, ArrayList<String> arraycabang,ArrayList<String> scanlist, ArrayList<String> tglnya,ArrayList<String> petugas,ArrayList<String> rak) {
        super();
        mContext = context;
        cabanglist = arraycabang;
        idscanlist = scanlist;
        petugaslist=petugas;
        raklist=rak;
        listtgl=tglnya;
        checked = new ArrayList<>();
        for(String name : idscanlist) checked.add(false);
    }

    public int getCount() {
        return idscanlist.size();
    }

    // getView method is called for each item of ListView
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.cust_list_history, parent, false);
        //
        TextView lpetugas=(TextView)view.findViewById(R.id.lpetugas);
        lpetugas.setTag(position);
        TextView lpalet=(TextView)view.findViewById(R.id.lpalet);
        lpalet.setTag(position);
        TextView lnmcabanglist= (TextView)view.findViewById(R.id.lnmcabanglist);
        lnmcabanglist.setTag(position);
        TextView lidscanlist= (TextView)view.findViewById(R.id.lidscanlist);
        lidscanlist.setTag(position);
        TextView ltglscanlist= (TextView)view.findViewById(R.id.ltglscanlist);
        ltglscanlist.setTag(position);
        final CheckBox checklisthistory = (CheckBox) view.findViewById(R.id.checklisthistory);
        checklisthistory.setTag(position);
        //
        lnmcabanglist.setText(cabanglist.get(position));
        lidscanlist.setText(idscanlist.get(position));
        ltglscanlist.setText(listtgl.get(position));
        checklisthistory.setChecked(checked.get(position));
        lpetugas.setText(petugaslist.get(position));
        lpalet.setText(raklist.get(position));

        if (FHistory_Counting.isActionMode)
        {
            checklisthistory.setVisibility(View.VISIBLE);
        } else
        {
            checklisthistory.setVisibility(View.GONE);
        }
        // Click listener of button
        checklisthistory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked.set(position, checklisthistory.isChecked());
                int pos = (int)buttonView.getTag();
                FHistory_Counting.actionMode.setTitle(getCountSelectedCheckBoxes()+" Item selected");
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
        return idscanlist.isEmpty();
    }

    public void delete(int i){
        cabanglist.remove(i);
        listtgl.remove(i);
        idscanlist.remove(i);
        checked.remove(i);
        petugaslist.remove(i);
        raklist.remove(i);
        notifyDataSetChanged();
    }

    public void setChecked(int i, Boolean IsCheck){
        checked.set(i,IsCheck);
        notifyDataSetChanged();
    }

    public String getName(int position){
        return idscanlist.get(position);
    }

    public String getTanggal(int position){
        return listtgl.get(position);
    }

    public String getCabang(int position){
        return cabanglist.get(position);
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
