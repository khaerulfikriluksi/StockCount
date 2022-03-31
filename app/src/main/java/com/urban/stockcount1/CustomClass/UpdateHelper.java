package com.urban.stockcount1.CustomClass;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;

public class UpdateHelper {

    private FirebaseDatabase database;
    private DatabaseReference myRef;


    public interface onUpdateCheckListener {
        void onUpdateCheckListener (String url);
    }

    public static Builder with(Context context){
        return new Builder(context);
    }

    private onUpdateCheckListener onUpdateCheckListener;
    private Context context;


    public UpdateHelper(Context context, UpdateHelper.onUpdateCheckListener onUpdateCheckListener) {
        this.onUpdateCheckListener = onUpdateCheckListener;
        this.context = context;
    }

    public void check(){
        database = FirebaseDatabase.getInstance("https://stockapp-4abf1-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference("settings");
        myRef.child("app_stock_version").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    int updatever = Integer.parseInt(String.valueOf(task.getResult().getValue()));
                    if (currentver() < updatever) {
                        if (Cache.getInstance().isUpdate()) {
                            Cache.getInstance().replace("UPDATE", "true");
                        } else {
                            Cache.getInstance().setDataList("UPDATE", "true");
                        }
                        String ur = "true";
                        onUpdateCheckListener.onUpdateCheckListener(ur);
                    } else {
                        onUpdateCheckListener.onUpdateCheckListener("false");
                    }
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onUpdateCheckListener.onUpdateCheckListener("false");
            }
        });
    }

    private int currentver(){
        int ver = 0 ;
        try{
            String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            int versioncode= context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),0).versionCode;
            String st = versionName+String.valueOf(versioncode);
            ver = Integer.parseInt(st.replace(".",""));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ver;
    }

    public static class Builder {
        private Context context;
        private onUpdateCheckListener onUpdateCheckListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateCheck(onUpdateCheckListener onUpdateCheckListener){
            this.onUpdateCheckListener = onUpdateCheckListener;
            return this;
        }

        public UpdateHelper build(){
            return new UpdateHelper(context, onUpdateCheckListener);
        }

        public UpdateHelper check(){
            UpdateHelper updateHelper = build();
            updateHelper.check();
            return updateHelper;
        }
    }
}