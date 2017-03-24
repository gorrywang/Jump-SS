package com.example.dell.listener;

/**
 * Created by Dell on 2017/2/9.
 */

public interface HttpCallBackListener {
    //成功
    void onSuccess(String data);

    //失败
    void onError(int code ,String error);
}
