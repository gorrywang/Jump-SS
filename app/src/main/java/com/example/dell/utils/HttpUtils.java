package com.example.dell.utils;

import com.example.dell.listener.HttpCallBackListener;
import com.example.dell.listener.LoadPicListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Dell on 2017/2/9.
 * 联网基类
 */

public class HttpUtils {

    public static final int ERROR_INTERNET = 0;
    public static final int ERROR_IO = 1;
    public static final int ERROR_URL = 2;

    /**
     * 联网获取数据
     *
     * @param strUrl
     * @param callBackListener
     */
    public static void getData(final String strUrl, final HttpCallBackListener callBackListener) {
        new Thread() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(strUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(10000);
                    connection.setRequestMethod("GET");
//                    int responseCode = connection.getResponseCode();
//                    if (responseCode == 200) {
                    //联网成功
                    inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String temp = null;
                    while ((temp = reader.readLine()) != null) {
                        builder.append(temp);
                    }
                    if (callBackListener != null) {
                        callBackListener.onSuccess(builder.toString());
                    }
//                    } else {
//                        //联网失败
//                        if (callBackListener != null) {
//                            callBackListener.onError(ERROR_INTERNET, "联网失败:" + responseCode);
//                        }
//                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    if (callBackListener != null) {
                        callBackListener.onError(ERROR_URL, "URL错误");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callBackListener != null) {
                        callBackListener.onError(ERROR_IO, "IO流错误");
                    }
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }.start();
    }

    /**
     * 联网加载图片，本项目暂时用不到
     *
     * @param strUrl
     * @param listener
     */
    public static void loadPic(final String strUrl, final LoadPicListener listener) {
        new Thread() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    URL url = new URL(strUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(10000);
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        //联网成功
                        inputStream = connection.getInputStream();
                        if (listener != null) {
                            listener.onSuccess(inputStream);
                        }
                    } else {
                        //联网失败
                        if (listener != null) {
                            listener.onError(ERROR_INTERNET, "联网失败:" + responseCode);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onError(ERROR_URL, "URL错误");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onError(ERROR_INTERNET, "IO流错误");
                    }

                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }.start();
    }

}
