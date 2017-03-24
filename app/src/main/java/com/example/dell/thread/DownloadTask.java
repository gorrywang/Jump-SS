package com.example.dell.thread;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.dell.listener.DownloadListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Dell on 2017/2/9.
 * 下载线程
 */

public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    private DownloadListener mDownloadListener;
    private Context mContext;
    private boolean mCancel = false;

    public DownloadTask(DownloadListener mDownloadListener, Context mContext) {
        this.mDownloadListener = mDownloadListener;
        this.mContext = mContext;
    }

    private static final int TYPE_SUCCESS = 1;
    private static final int TYPE_FAILED = 2;
    private static final int TYPE_CANCEL = 3;

    /**
     * 子线程
     *
     * @param params
     * @return
     */
    @Override
    protected Integer doInBackground(String... params) {
        long downloadFileLength = 0;
        //下载链接
        String downloadUrl = params[0];
        //文件名
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        //路径
//        String fileDir = mContext.getCacheDir().getPath();
        String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        //创建File对象
        File file = new File(fileDir, fileName);
        //判断文件是否存在
        if (file.exists()) {
            //存在，获取长度
            downloadFileLength = file.length();
        }
        //获取全部文件的长度
        long contentLength = getContentLength(downloadUrl);
        //判断长度
        if (contentLength == 0) {
            //获取失败
            return TYPE_FAILED;
        } else if (contentLength == downloadFileLength) {
            //相等，下载完毕
            return TYPE_SUCCESS;
        }
        //没有下完，继续下载
        OkHttpClient client = new OkHttpClient();
        //请求
        Request request = new Request.Builder()
                .addHeader("RANGE", "bytes=" + downloadFileLength + "-")
                .url(downloadUrl)
                .build();
        InputStream inputStream = null;
        RandomAccessFile saveFile = null;

        //响应
        try {
            Response response = client.newCall(request).execute();
            inputStream = response.body().byteStream();
            saveFile = new RandomAccessFile(file, "rw");
            int len = 0;
            int total = 0;
            byte[] b = new byte[1024];
            while ((len = inputStream.read(b)) != -1) {
                //有数据
                if (mCancel) {
                    return TYPE_CANCEL;
                } else {
                    //继续下载
                    saveFile.write(b, 0, len);
                    //进度
                    total += len;
                    //判断进度
                    int progress = (int) ((total + downloadFileLength) * 100 / contentLength);
                    //更新进度
                    publishProgress(progress);
                }
            }
            //下载成功
            response.close();
            return TYPE_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (saveFile != null) {
                    saveFile.close();
                }
            } catch (Exception e) {

            }
        }
        return TYPE_FAILED;
    }

    private int lastProgress = 0;

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            //进度
            mDownloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    //完成
    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer) {
            case TYPE_CANCEL:
                //取消下载
                mDownloadListener.onCancel();
                break;
            case TYPE_FAILED:
                //下载失败
                mDownloadListener.onFailed();
                break;
            case TYPE_SUCCESS:
                //下载成功
                mDownloadListener.onSuccess();
                break;
        }
    }

    /**
     * 下载总长度
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        //客户端
        OkHttpClient client = new OkHttpClient();
        //请求
        Request request = new Request.Builder().url(downloadUrl).build();
        //响应
        try {
            Response response = client.newCall(request).execute();
            long contentLength = response.body().contentLength();
            return contentLength;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //取消下载
    public void cancelDownload() {
        mCancel = true;
    }
}
