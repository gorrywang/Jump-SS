package com.example.dell.listener;

/**
 * Created by Dell on 2017/2/9.
 * 回调接口
 */

public interface DownloadListener {
    //进度更新
    void onProgress(int progress);

    //下载成功
    void onSuccess();

    //下载失败
    void onFailed();

    //取消下载
    void onCancel();
}
