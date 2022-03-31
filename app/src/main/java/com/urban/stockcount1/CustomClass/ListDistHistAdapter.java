package com.urban.stockcount1.CustomClass;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.urban.stockcount1.FHistory_Distin;
import com.urban.stockcount1.R;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

public class ListDistHistAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private AlertDialog show;
    private Context mContext;
    private ArrayList<String> noukti;
    private ArrayList<String> pengirim;
    private ArrayList<String> penerima;
    private ArrayList<String> qtydikirim;
    private ArrayList<String> qtyditerima;
    private ArrayList<String> tgldibuat;
    private ArrayList<String> tglmasuk;
    private List<Boolean> checked;
    private List<Boolean> expanded;
    private List<Boolean> visibiliy;
    private SQLiteDatabase db;

    public ListDistHistAdapter(Context context,
                               ArrayList<String> noukti2,
                               ArrayList<String> pengirim2,
                               ArrayList<String> penerima2,
                               ArrayList<String> qtydikirim2,
                               ArrayList<String> qtyditerima2,
                               ArrayList<String> tgldibuat2,
                               ArrayList<String> tglmasuk2) {
        super();
        mContext = context;
        noukti = noukti2;
        pengirim = pengirim2;
        penerima=penerima2;
        qtydikirim=qtydikirim2;
        qtyditerima=qtyditerima2;
        tgldibuat=tgldibuat2;
        tglmasuk=tglmasuk2;
        checked = new ArrayList<>();
        expanded = new ArrayList<>();
        visibiliy = new ArrayList<>();
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(String name : noukti) checked.add(false);
        for(String name : noukti) expanded.add(true);
        for(String name : noukti) visibiliy.add(false);
    }

    public int getCount() {
        return noukti.size();
    }

    // getView method is called for each item of ListView
    public View getView(final int position, View view, ViewGroup parent) {
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        view = inflater.inflate(R.layout.cust_adapter_historydistin, parent, false);
        //
        TextView din_hist_noukti = (TextView) view.findViewById(R.id.din_hist_noukti);
        TextView din_hist_pengirim = (TextView) view.findViewById(R.id.din_hist_pengirim);
        TextView din_hist_penerima = (TextView) view.findViewById(R.id.din_hist_penerima);
        TextView din_hist_dikirim = (TextView) view.findViewById(R.id.din_hist_dikirim);
        TextView din_hist_diterima = (TextView) view.findViewById(R.id.din_hist_diterima);
        TextView din_hist_dibuat = (TextView) view.findViewById(R.id.din_hist_dibuat);
        TextView din_hist_masuk = (TextView) view.findViewById(R.id.din_hist_masuk);
        ImageButton din_hist_bremarks = (ImageButton) view.findViewById(R.id.din_hist_bremarks);
        ImageButton din_hist_bexpand = (ImageButton) view.findViewById(R.id.din_hist_bexpand);
        CheckBox din_hist_check = (CheckBox) view.findViewById(R.id.din_hist_check);
        //Expanded
        LinearLayout din_hist_expand = (LinearLayout) view.findViewById(R.id.din_hist_expand);
        CardView din_hist_cardlist = (CardView) view.findViewById(R.id.din_hist_cardlist);
        //
        din_hist_noukti.setTag(position);
        din_hist_pengirim.setTag(position);
        din_hist_penerima.setTag(position);
        din_hist_dikirim.setTag(position);
        din_hist_diterima.setTag(position);
        din_hist_dibuat.setTag(position);
        din_hist_masuk.setTag(position);
        din_hist_bremarks.setTag(position);
        din_hist_bexpand.setTag(position);
        din_hist_check.setTag(position);
        din_hist_expand.setTag(position);
        din_hist_cardlist.setTag(position);
        //
        din_hist_noukti.setText(noukti.get(position));
        din_hist_pengirim.setText(pengirim.get(position));
        din_hist_penerima.setText(penerima.get(position));
        din_hist_dikirim.setText(qtydikirim.get(position));
        din_hist_diterima.setText(qtyditerima.get(position));
        din_hist_dibuat.setText(tgldibuat.get(position));
        din_hist_masuk.setText(tglmasuk.get(position));
        din_hist_check.setChecked(checked.get(position));
        //Listener
        if (visibiliy.get(position)==true) {
            din_hist_check.setVisibility(View.VISIBLE);
        } else {
            din_hist_check.setVisibility(View.GONE);
        }
        if (expanded.get(position)==true) {
            din_hist_expand.setVisibility(View.VISIBLE);
            din_hist_bexpand.setBackgroundResource(R.drawable.ico_up);
        } else {
            din_hist_expand.setVisibility(View.GONE);
            din_hist_bexpand.setBackgroundResource(R.drawable.ico_down);
        }
        //
        din_hist_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked.set(position, din_hist_check.isChecked());
            }
        });
        din_hist_bexpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(din_hist_expand.getVisibility() == View.VISIBLE){
                    ValueAnimator anim = ValueAnimator.ofInt(460, 145);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = din_hist_cardlist.getLayoutParams();
                            din_hist_cardlist.setLayoutParams(layoutParams);
                            din_hist_bexpand.setBackgroundResource(R.drawable.ico_down);
                            din_hist_expand.setVisibility(View.GONE);
                            layoutParams.height = val;
                        }
                    });
                    anim.setDuration(400);
                    anim.start();
                    expanded.set(position, false);
                }
                else {
                    ValueAnimator anim = ValueAnimator.ofInt(145, 460);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = din_hist_cardlist.getLayoutParams();
                            din_hist_cardlist.setLayoutParams(layoutParams);
                            din_hist_bexpand.setBackgroundResource(R.drawable.ico_up);
                            din_hist_expand.setVisibility(View.VISIBLE);
                            layoutParams.height = val;
                        }
                    });
                    anim.setDuration(400);
                    anim.start();
                    expanded.set(position, true);
                }
            }
        });
        din_hist_bremarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cr = db.rawQuery("SELECT `keterangan` FROM tbl_distin where `No_bukti`='"+noukti.get(position)+"'",null);
                cr.moveToFirst();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                LayoutInflater inflat = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view2 = inflat.inflate(R.layout.cust_popup_remarks, null);
                AutoCompleteTextView din_pop_keterangan = (AutoCompleteTextView) view2.findViewById(R.id.din_pop_keterangan);
                din_pop_keterangan.setText(cr.getString(0));
                builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.execSQL("UPDATE tbl_distin SET `keterangan`='"+din_pop_keterangan.getText().toString()+"' where `No_bukti`='"+noukti.get(position)+"'");
                        show.dismiss();
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
            }
        });
        return view;
    }

    public void setVisible (Boolean isVisible){
        for(int i = noukti.size() - 1; i >= 0; i--){
            visibiliy.set(i,isVisible);
        }
    }

    public int getCountSelectedCheckBoxes(){
        int toReturn = 0;
        for(boolean b : checked) if(b) toReturn++;
        return toReturn;
    }

    public void setChecked(int i, Boolean IsCheck){
        checked.set(i,IsCheck);
        notifyDataSetChanged();
    }

    public void delete (int i) {
        noukti.remove(i);
        pengirim.remove(i);
        penerima.remove(i);
        qtydikirim.remove(i);
        qtyditerima.remove(i);
        tgldibuat.remove(i);
        tglmasuk.remove(i);
        checked.remove(i);
        expanded.remove(i);
        visibiliy.remove(i);
    }

    public Boolean isChecked(int position) {
        Boolean chec=false;
        chec=checked.get(position);
        return chec;
    }

    public String getName (int pos) {
        String na = null;
        na = noukti.get(pos);
        return  na;
    }

    public boolean isEmpty(){
        return noukti.isEmpty();
    }

    public List<Boolean> getCheckList(){
        return checked;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
}