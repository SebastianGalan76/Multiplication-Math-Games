package com.coresaken.multiplication.controller;

import static android.content.Context.ALARM_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class DailyReminderController {
    private static final int REQUEST_CODE = 0;

    public static boolean checkPermission(Context context){
        return ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED;
    }

    public static void startNotification(Context context){
        PlayerSettings ps = PlayerSettings.getInstance();

        if(ps.reminder){
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.HOUR_OF_DAY, ps.reminderTime[0]);
            calendar.set(Calendar.MINUTE, ps.reminderTime[1]);

            calendar.set(Calendar.SECOND, 0);

            if(Calendar.getInstance().after(calendar)){
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            Intent intent = new Intent(context, ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_MUTABLE);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
        else{
            stopNotification(context);
        }
    }

    public static void stopNotification(Context context){
        Intent intent = new Intent(context, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if(alarmManager!=null){
            alarmManager.cancel(pendingIntent);
        }
    }
}
