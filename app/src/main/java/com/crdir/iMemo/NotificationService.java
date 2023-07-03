package com.crdir.iMemo;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.List;

public class NotificationService extends Service {

    private static final String TAG = "TAG";
    private static final int CHECK_TICK = 1 * 60 * 1000;
    private static final int GET_STAMINA_ID = 1;
    private static final int PUNCH_CARD_ID = 2;

    private NotificationService m_service = null;
    private NotificationManager m_notificationMgr = null;
    private NotifyThread m_notifyThread = null;

    private SharedPreferences sharedPreferences;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        m_service = this;
        m_notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (m_notificationMgr == null) {
            Log.i(TAG, "NotificationService notificationMgr null");
        }
        m_notifyThread = new NotifyThread();
        m_notifyThread.start();

        Log.i(TAG, "NotificationService onCreate...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "NotificationService onStartCommand...");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "NotificationService onDestroy...");

        if (m_notifyThread != null) {
            m_notifyThread.stopThread();
        }

        super.onDestroy();
    }

    public void notify(int notifyId, String strTitle, String strMsg) {
        if (m_notificationMgr != null) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher); //设置图标
            builder.setTicker("A iMemo");
            builder.setContentTitle(strTitle); //设置标题
            builder.setContentText(strMsg); //消息内容
            builder.setWhen(System.currentTimeMillis()); //发送时间
            builder.setDefaults(Notification.DEFAULT_ALL); //设置默认的提示音，振动方式，灯光
            builder.setAutoCancel(true);//打开程序后图标消失

            ComponentName componentName = new ComponentName(this, MainActivity.class);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(componentName);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentIntent(pendingIntent);
            Notification notification = builder.build();

            m_notificationMgr.notify(notifyId, notification);
        }
    }

    public static boolean isRunning(Context context) {
        ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityMgr != null) {
            List<ActivityManager.RunningServiceInfo> serviceList = activityMgr.getRunningServices(50);

            if (serviceList.isEmpty()) {
                return false;
            }

            for (int i = 0, n = serviceList.size(); i < n; ++i) {
                if (serviceList.get(i).service.getClassName().toString().equals("com.jthd.marsX.NotificationService")) {
                    return true;
                }
            }
        }

        return false;
    }

    private class NotifyThread extends Thread {
        private boolean m_bStop = false;

        public synchronized void stopThread() {
            m_bStop = true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            Log.i(TAG, "NotifyThread run...");

            while (!m_bStop) {
                checkNotify();

                try {
                    sleep(CHECK_TICK);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.i(TAG, "NotifyThread stop...");
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void checkNotify() {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            sharedPreferences = getSharedPreferences("usr_info", MODE_PRIVATE);
            int targetYear = sharedPreferences.getInt("targetYear", -1);
            int targetMonth = sharedPreferences.getInt("targetMonth", -1);
            int targetDay = sharedPreferences.getInt("targetDay", -1);
            int targetHour = sharedPreferences.getInt("targetHour", -1);
            int targetMinute = sharedPreferences.getInt("targetMinute", -1);
            String strMsg = sharedPreferences.getString("TransactionContent", "");

            Log.i(TAG, "tarTime: " + targetYear + " " + targetMonth + " " + targetDay + " " + targetHour + " " + targetMinute);
            Log.i(TAG, "currentTime: " + year + " " + month + " " + day + " " + hour + " " + minute);

            if (year == targetYear &&
                    month == targetMonth &&
                    day == targetDay &&
                    hour == targetHour &&
                    (minute >= targetMinute && minute <= targetMinute + 2)) {
                m_service.notify(GET_STAMINA_ID, "一个来自备忘录的提醒", strMsg);
            }
        }
    }
}
