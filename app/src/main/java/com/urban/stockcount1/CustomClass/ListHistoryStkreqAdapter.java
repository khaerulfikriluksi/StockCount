package com.urban.stockcount1.CustomClass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.clzola.glottie.GlottieView;
import com.clzola.glottie.GlottieViewTarget;
import com.makeramen.roundedimageview.RoundedImageView;
import com.urban.stockcount1.FHistory_StockRequest;
import com.urban.stockcount1.R;

import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class ListHistoryStkreqAdapter extends RecyclerView.Adapter<ListHistoryStkreqAdapter.ViewHolder> {

    private static final String TAG = "Home_recycle_adapter";
    private Context mContext;
    private ArrayList<String> noreq=new ArrayList<>();
    private ArrayList<String> tgl=new ArrayList<>();
    private ArrayList<String> namareq=new ArrayList<>();
    private ArrayList<String> status=new ArrayList<>();
    private ArrayList<String> pembuat=new ArrayList<>();
    private ArrayList<String> diprosesoleh=new ArrayList<>();

    public ListHistoryStkreqAdapter(Context mContext, ArrayList<String> noreq, ArrayList<String> tgl, ArrayList<String> namareq, ArrayList<String> status, ArrayList<String> pembuat, ArrayList<String> diprosesoleh) {
        this.mContext = mContext;
        this.noreq = noreq;
        this.tgl = tgl;
        this.namareq = namareq;
        this.status = status;
        this.pembuat = pembuat;
        this.diprosesoleh = diprosesoleh;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.v(TAG,"onCreateViewHolder Terpanggil");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cus_list_histstkreq, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Log.v(TAG,"onBindViewHolder Terpanggil");
        holder.str_hist_noreq.setText(noreq.get(position));
        holder.str_hist_tgl.setText(tgl.get(position));
        holder.str_hist_namareq.setText(namareq.get(position));
        holder.str_hist_status.setText(status.get(position));
        holder.str_hist_pembuat.setText(pembuat.get(position));
        holder.str_hist_diprosesoleh.setText(diprosesoleh.get(position));
        if (status.get(position).contains("draft")) {
            holder.str_hist_status.setBackgroundResource(R.drawable.round_ungu);
        } else if (status.get(position).contains("proses")) {
            holder.str_hist_status.setBackgroundResource(R.drawable.round_kuning);
        } else if (status.get(position).contains("selesai")) {
            holder.str_hist_status.setBackgroundResource(R.drawable.round_hijau);
        } else if (status.get(position).contains("cancel")) {
            holder.str_hist_status.setBackgroundResource(R.drawable.round_merah);
        }
        holder.str_hist_cardlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,noreq.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return noreq.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView str_hist_noreq,str_hist_tgl,
                str_hist_namareq,str_hist_status,
                str_hist_pembuat,str_hist_diprosesoleh;
        CardView str_hist_cardlist;

        public ViewHolder (View view) {
            super(view);
            str_hist_noreq=(TextView) view.findViewById(R.id.str_hist_noreq);
            str_hist_tgl=(TextView) view.findViewById(R.id.str_hist_tgl);
            str_hist_namareq=(TextView) view.findViewById(R.id.str_hist_namareq);
            str_hist_status=(TextView) view.findViewById(R.id.str_hist_status);
            str_hist_pembuat=(TextView) view.findViewById(R.id.str_hist_pembuat);
            str_hist_diprosesoleh=(TextView) view.findViewById(R.id.str_hist_diprosesoleh);
            str_hist_cardlist = (CardView) view.findViewById(R.id.str_hist_cardlist);
        }
    }
}
