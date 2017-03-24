package com.example.dell.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

import com.example.dell.listener.HttpCallBackListener;
import com.example.dell.service.DownloadService;
import com.example.dell.utils.HttpUtils;
import com.example.dell.utils.PrivateUtils;
import com.example.dell.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 第一页面，负责分发
 */
public class SplashActivity extends AppCompatActivity {
    private static final int SUCCESS = 0;
    private static final int ERROR = 1;
    private static final int SERVICE_STOP = 2;
    private CoordinatorLayout mCoordinatorLayout;
    //动画开始时间
    private long startTime;
    private long endTime;
    private MyBroadcast mMyBroadcast;
    //下载链接，供广播使用
    private String mStrUrl;

    private DownloadService.MyBinder mMyBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (DownloadService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    break;
                case ERROR:
                    //无数据
                    Toast.makeText(SplashActivity.this, "请检查网络", Toast.LENGTH_LONG).show();
                    break;
                case SERVICE_STOP:
                    Toast.makeText(SplashActivity.this, "软件不在进行支持", Toast.LENGTH_LONG).show();
                    finish();
                    break;
            }
        }
    };


    /**
     * 判断版本是否更新
     *
     * @param version
     * @param url
     */
    private void isNewVersion(int version, String url, String content) {
        mStrUrl = url;
        //设置时间
        endTime = System.currentTimeMillis();
        long time = endTime - startTime;
        if (time < 3000) {
            //暂停
            try {
                Thread.sleep(3000 - time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //获取当前版本
        int localVersion = getLocalVersion();
        if (localVersion == -1) {
            //检查网络*********************************
            mHandler.sendEmptyMessage(ERROR);

        } else if (localVersion == version) {
            //版本相同，进入首页
            jumpPage();

        } else if (localVersion < version) {
            //更新版本
            downloadNewVersion(url, content);
        } else if (localVersion > version) {
            //最新版本，进入首页
            jumpPage();
        }
    }

    /**
     * 下载新版本
     *
     * @param content
     * @param strUrl
     */
    private void downloadNewVersion(final String strUrl, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setMessage(content);
                builder.setTitle("是否下载新版本？").setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //下载新版本
                        //启动下载
                        mMyBinder.startDownload(strUrl);

                    }

                }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //进入首页
                        jumpPage();
                    }
                }).setCancelable(false).show();
            }
        });
    }

    //注册广播
    private void regBroadcast() {
        mMyBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter("com.jump.apk");
//        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mMyBroadcast, intentFilter);
    }

    /**
     * 申请权限
     */
    private void sqqx() {
        //申请权限
        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //无权限
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }
        //已经同意
        //注册广播
        regBroadcast();
        initAnimation();
        getData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        bindID();
        //启动服务
        startServer();
        //申请权限
        sqqx();
    }

    //启动服务
    private void startServer() {
        //开启服务
        Intent intent = new Intent(SplashActivity.this, DownloadService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void bindID() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_splash);
    }

    //初始化动画
    private void initAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(3000);
        mCoordinatorLayout.startAnimation(alphaAnimation);
        //设置时间
        startTime = System.currentTimeMillis();
    }

    /**
     * 获取数据，子线程
     */
    private void getData() {
        HttpUtils.getData(PrivateUtils.SPLASH_URL, new HttpCallBackListener() {
            @Override
            public void onSuccess(String data) {
                //获取成功
                //有数据
                parsingJson(data);
            }

            @Override
            public void onError(int code, String error) {
                //获取失败
                Message msg = mHandler.obtainMessage(ERROR);
                msg.obj = error;
                mHandler.sendMessage(msg);
            }
        });
    }

    /**
     * 解析Json数据
     */
    private void parsingJson(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            int server = jsonObject.getInt("server");
            int version = jsonObject.getInt("version");
            String url = jsonObject.getString("url");
            String content = jsonObject.getString("content");
            if (server == 0) {
                //服务器禁止使用，销毁界面
                mHandler.sendEmptyMessage(SERVICE_STOP);
                return;
            }
            //比较版本
            isNewVersion(version, url, content);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前的版本
     */
    private int getLocalVersion() {
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            //版本号
            int versionCode = info.versionCode;
            //版本名
            String versionName = info.versionName;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 跳转页面
     */
    private void jumpPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean firstRun = Utils.isFirstRun(SplashActivity.this);
                if (firstRun) {
                    //Guide
                    Intent intent = new Intent(SplashActivity.this, GuideActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                //首页
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑更新服务
        if (mMyBinder != null) {
            mMyBinder.cancelDownload();
            //为空
            mMyBinder = null;
        }

        //取消广播
        if (mMyBroadcast != null) {
            unregisterReceiver(mMyBroadcast);
        }

        unbindService(mServiceConnection);
    }

    //权限返回
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                //判断
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //同意
                    //注册广播
                    regBroadcast();
                    initAnimation();
                    getData();

                } else {
                    //不同意
                    Toast.makeText(SplashActivity.this, "无法下载更新与保存二维码\n请允许软件使用存储的权限", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    //广播
    class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.jump.apk")) {
                //执行安装apk
                Intent newApk = new Intent();
                newApk.setAction(Intent.ACTION_VIEW);
                newApk.addCategory(Intent.CATEGORY_DEFAULT);
                //下载链接
                //文件名
                String fileName = mStrUrl.substring(mStrUrl.lastIndexOf("/"));
                //路径
                String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                File file = new File(fileDir, fileName);
                //有错误24以上
                if (Build.VERSION.SDK_INT >= 24) {
                    //使用FIleProvider
                    Uri uri = FileProvider.getUriForFile(SplashActivity.this, "com.example.dell.jump.newapk", file);
                    //对目标应用临时授权该Uri所代表的文件
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent1.setDataAndType(uri, "application/vnd.android.package-archive");
                } else {
                    Uri uri = Uri.fromFile(file);
                    intent1.setDataAndType(uri, "application/vnd.android.package-archive");
                }
                startActivity(intent1);
            } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                //监听网络
                //得到网络连接管理器
                ConnectivityManager connectionManager = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                //通过管理器得到网络实例
                NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
                //判断是否连接
                if (networkInfo != null && networkInfo.isAvailable()) {

                    Toast.makeText(SplashActivity.this, "网络正常",
                            Toast.LENGTH_SHORT).show();
                    getData();
                } else {
                    Toast.makeText(SplashActivity.this, "网络异常",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
