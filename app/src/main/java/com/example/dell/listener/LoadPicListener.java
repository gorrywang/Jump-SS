package com.example.dell.listener;

import java.io.InputStream;

/**
 * Created by Dell on 2017/2/10.
 */

public interface LoadPicListener {
    void onSuccess(InputStream inputStream);

    void onError(int code ,String error);
}
