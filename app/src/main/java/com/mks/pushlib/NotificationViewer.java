package com.mks.pushlib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mks.pushlib.ExternalLibServicer.ExternalLibServicer;
import com.mks.pushlib.Logger.Logger;
import com.mks.pushlib.NotificationParams.NotificationParams;

public class NotificationViewer {

    public String getVersion(Context cnt) {
        Logger.log("NotificationViewer_Default.getVersion()");
        return C.CODE_VERSION;
    }

    public boolean showNotification(Context cnt, String message) {
        return FCMMessagingService.showNotification(cnt, message);
    }


    public void subscribeToTopic(Context cnt, String topic) {

        String AppName = getAppName(cnt);
        Logger.log("subscribeToTopic: mks_group_" + AppName + "_" + topic );
        FirebaseMessaging.getInstance().subscribeToTopic("mks_group_" + AppName + "_" + topic);
    }


    public void unsubscribeFromTopic(Context cnt, String topic) {
        String AppName = getAppName(cnt);
        Logger.log("unsubscribeFromTopic: mks_group_" + AppName + "_" + topic );
        FirebaseMessaging.getInstance().unsubscribeFromTopic("mks_group_" + AppName + "_" + topic);
    }

    public void init(Context cnt) {
        Logger.log("NotificationViewer_Default.init()");
        String AppName = getAppName(cnt); //cnt.getPackageName().toString().split("\\.")[cnt.getPackageName().toString().split("\\.").length - 1];
        Logger.log("subscribeToTopic: mks_group_" + AppName );
        System.out.println ("!!! Token = " + FirebaseInstanceId.getInstance().getToken());
        FirebaseMessaging.getInstance().subscribeToTopic("mks_group_" + AppName);
        FirebaseMessaging.getInstance().subscribeToTopic("mks_group_main");
    }

    public void send(final Context cnt, final NotificationParams param) {
        Logger.log("NotificationViewer_Default.send()");
        String appVersion = "";
        try {
            PackageInfo pInfo = cnt.getPackageManager().getPackageInfo(cnt.getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Показываем нотификацию только если она предназначалась для приложения заданйо версии
        if (( param.getTargetAppVersion() != null) &&
                (!param.getTargetAppVersion().equalsIgnoreCase("")) &&
                ( appVersion != null) &&
                (!param.getTargetAppVersion().equalsIgnoreCase(appVersion)))
        {
            return;
        }

        int defaultIcon = cnt.getApplicationInfo().icon;

        final int NOTIFICATION_ID = 1;

        Intent intent = null;
        PendingIntent pendingIntent = null;
        if (param.getIsOpenInBrowser().equalsIgnoreCase("True")) {
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(param.getLink()));
            pendingIntent = PendingIntent.getActivity(cnt, 0, notificationIntent, 0);
        }
        else {
            if ((param.getAction() != null) && (param.getAppForStart() != null)) {
                intent = new Intent(Intent.ACTION_MAIN);

                intent.setClassName(param.getAppForStart(), param.getAction());

                intent.setAction(param.getIntentAction());
                intent.putExtra(param.getNameExtra1(), param.getExtra1());
                intent.putExtra(param.getNameExtra2(), param.getExtra2());
                intent.putExtra(param.getNameExtra3(), param.getExtra3());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                pendingIntent = PendingIntent.getActivity(cnt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            }
        }

        NotificationCompat.Style style = null;
        switch (param.getStyle())
        {
            case "BigTextStyle":
                style = new NotificationCompat.BigTextStyle().bigText(param.getMsgText());
                break;
            case "BigPictureStyle":
                style = new NotificationCompat.BigPictureStyle().bigPicture(param.getLargeIcon());
                break;
            default:
                style = null;
                break;
        }

        //NotificationCompat.Style style = new NotificationCompat.BigTextStyle().bigText(param.getMsgText());

        NotificationCompat.Builder b = new NotificationCompat.Builder(cnt, "M_CH_ID");
        b.setContentTitle(param.getTitel()   == null ? ""              : param.getTitel());
        b.setContentText( param.getMsgText() == null ? ""              : param.getMsgText());
        b.setSmallIcon(   param.getIcon()    != 0    ? param.getIcon() : defaultIcon);

        b.setPriority(param.getPriority());
        b.setDefaults(param.getDefaults());
        if (param.getСategory()  != null) {b.setCategory(param.getСategory());}
        if (param.getLargeIcon() != null) {b.setLargeIcon(param.getLargeIcon());}
        if (pendingIntent != null)        {b.setContentIntent(pendingIntent);}
        if (style != null)                {b.setStyle(style);}
        b.setAutoCancel(param.getAutoCancel());


        if((param.getVibrate() != null) && (param.getVibrate().length >=5)) b.setVibrate(param.getVibrate());


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) cnt.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            b.setChannelId(CHANNEL_ID);
        }

        Notification notification = b.build();
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(cnt);
//        notificationManager.notify(NOTIFICATION_ID, notification);

        NotificationManager mNotificationManager = (NotificationManager) cnt.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Надо отправить сообщение о пуше
             //   new RestServicer().send(cnt, false, null, "IntLib_push", "" + param.getTag());
                try {
                    ExternalLibServicer libServicer = new ExternalLibServicer();
                    String extPackageName = "com.mks.uplib";
                    Class clazzShell = libServicer.getClass( extPackageName + ".Shell");
                    Class clazzSendStatLib = libServicer.getClass(extPackageName + ".Libs.SendStatLib.SendStatLib");
                    Object instance = libServicer.callStaticMethod(clazzShell, "SendStatLib", new Object[]{cnt}, new Class[]{Context.class});
                    libServicer.callMethod(clazzSendStatLib, instance, "sendStat", new Object[]{cnt, "IntLib_push", "" + param.getTag()}, new Class[]{Context.class, String.class, String.class});
                }
                catch(Error e)
                {
                    System.out.println ("" + e.getMessage());
                }
            }
        });

    }

    private String getAppName(Context cnt)
    {
        return cnt.getPackageName().toString().split("\\.")[cnt.getPackageName().toString().split("\\.").length - 1];
    }

}
