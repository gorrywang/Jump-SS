package com.example.dell.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.example.dell.activity.R;
import com.example.dell.activity.SplashActivity;
import com.example.dell.listener.DownloadListener;
import com.example.dell.thread.DownloadTask;

/**
 * 下载服务
 */
public class DownloadService extends Service {
    //下载
    private DownloadTask mDownloadTask;
    //链接
    private String mUrl;
    //监听事件
    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            mDownloadTask = null;
            //去除标题
            stopForeground(true);
            //更新标题
            getNotificationManager().notify(1, getNotification("Download Success", -1));
            Toast.makeText(DownloadService.this, "下载成功", Toast.LENGTH_SHORT).show();
            //发送安装广播
            Intent intent = new Intent("com.jump.apk");
            sendBroadcast(intent);
        }

        @Override
        public void onFailed() {
            mDownloadTask = null;
            //去除标题
            stopForeground(true);
            //更新标题
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(DownloadService.this, "下载失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            mDownloadTask = null;
            //去除标题
            stopForeground(true);
            //更新标题
            getNotificationManager().notify(1, getNotification("Download Cancel", -1));
            Toast.makeText(DownloadService.this, "取消下载", Toast.LENGTH_SHORT).show();
        }
    };

    //myBind
    public class MyBinder extends Binder {
        //开始下载
        public void startDownload(String strUrl) {
            if (mDownloadTask == null) {
                //url
                mUrl = strUrl;
                //初始化task
                mDownloadTask = new DownloadTask(listener, DownloadService.this);
                //下载
                mDownloadTask.execute(mUrl);
                //开始下载提示
                startForeground(1, getNotification("Downloading...", 0));
                Toast.makeText(DownloadService.this, "开始下载", Toast.LENGTH_SHORT).show();
            }
        }

        //取消下载
        public void cancelDownload() {
            if (mDownloadTask != null) {
                //取消下载
                mDownloadTask.cancelDownload();
            }
        }
    }


    public MyBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    //获取通知管理
    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    //建立通知
    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.noti)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.rocket))
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setAutoCancel(true);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();

    }
}
