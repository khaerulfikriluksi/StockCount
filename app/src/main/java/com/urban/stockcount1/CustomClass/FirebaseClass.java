package com.urban.stockcount1.CustomClass;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.installations.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.urban.stockcount1.FSplash;
import com.urban.stockcount1.R;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class FirebaseClass extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private SQLiteDatabase db;


    private void Receive(RemoteMessage remoteMessage) {
        String setting = "0";
        String date = "0";
        String hal_id = "0";
        String M_view = "0";
        String cal_id = "0";

        Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        if (remoteMessage.getData().size() > 0) {
            Log.d("cektag", remoteMessage.getData().get("setting"));
            if (Integer.parseInt(remoteMessage.getData().get("setting"))!=1) {
                Log.d(TAG, "Setting=0");
                Intent intent = new Intent(this, FSplash.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT);
                Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.notification_sound);
                AudioAttributes att = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                String channelId = "StockAppID01";
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = manager.getNotificationChannel(channelId);
                    if (channel == null) {
                        channel = new NotificationChannel(channelId,"Title",NotificationManager.IMPORTANCE_HIGH);
                        channel.setDescription("Description");
                        channel.enableVibration(true);
                        channel.enableLights(true);
                        channel.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000 });
                        channel.setSound(soundUri, att);
                        channel.setShowBadge(true);
                        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                        manager.createNotificationChannel(channel);
                    }
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channelId)
                        .setSmallIcon(R.mipmap.icon_launcher)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        //.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ico_chrome))
                        //.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(),R.drawable.ico_chrome)).bigLargeIcon(null))
                        .setLargeIcon(null)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setSound(soundUri)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker("Ada permintaan");
                NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());
                m.notify(1,builder.build());
            }
        }
        String click_action = remoteMessage.getNotification().getClickAction();
    }

    @Override
    public void handleIntent(Intent intent) {
        db = SQLiteDatabase.openDatabase("data/data/com.urban.stockapp/databases/stockapp.db", null,
                SQLiteDatabase.OPEN_READWRITE);
        try
        {
            if (intent.getExtras() != null)
            {
                Log.v("handleIntent", "Data Diterima");
                RemoteMessage.Builder builder = new RemoteMessage.Builder("MyFirebaseMsgService");
                for (String key : intent.getExtras().keySet())
                {
                    builder.addData(key, intent.getExtras().get(key).toString());
                }
                this.Receive(builder.build());
                if (Integer.parseInt((String) intent.getExtras().get("setting"))==1) {
                    String online=null;
                    String local=null;
                    if (intent.getExtras().get("ip_online").toString().length()>0) {
                        online = (String) intent.getExtras().get("ip_online");
                        if (intent.getExtras().get("ip_local").toString().length()>0) {
                            local = (String) intent.getExtras().get("ip_local");
                            db.execSQL("update tbl_api set `api_online`='"+online+"',`api_offline`='"+local+"'");
                        } else {
                            db.execSQL("update tbl_api set `api_online`='"+online+"'");
                        }
                    } else {
                        if (intent.getExtras().get("ip_local").toString().length()>0) {
                            local = (String) intent.getExtras().get("ip_local");
                            db.execSQL("update tbl_api set `api_offline`='"+local+"'");
                        } else {
                            db.execSQL("update tbl_api set `api_online`=null,`api_offline`=null");
                        }
                    }
                }
            }
            else
            {
                super.handleIntent(intent);
            }
        }
        catch (Exception e)
        {
            super.handleIntent(intent);
            Log.v("handleIntent", e.getMessage());
        }
    }

    @Override
    public void onNewToken(String token){
        Log.d(TAG, "onNewToken : " + token);
    }
}