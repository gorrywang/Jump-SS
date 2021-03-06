package com.example.dell.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dell.adapter.MyRecyclerAdapter;
import com.example.dell.listener.HttpCallBackListener;
import com.example.dell.utils.HttpUtils;
import com.example.dell.utils.PrivateUtils;
import com.example.dell.vo.ShowVO;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter mAdapter;
    private List<ShowVO> mList = new ArrayList<ShowVO>();
    private SwipeRefreshLayout mRefresh;
    //时间值
    private String mTime = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindID();
        //设置toolbar
        setSupportActionBar(mToolbar);
        getData();
        getContent();
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 2);
        mAdapter = new MyRecyclerAdapter(mList);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        //一开始刷新数据
        if (mRefresh != null) {
            mRefresh.setRefreshing(true);
        }
        //下拉刷新
        refreshData();
    }


    /**
     * 获取数据
     */
    private String mHelp;
    private String mAbout;

    private void getContent() {
        HttpUtils.getData(PrivateUtils.HELP_URL, new HttpCallBackListener() {
            @Override
            public void onSuccess(String data) {
                //解析数据
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    mHelp = jsonObject.getString("help");
                    mAbout = jsonObject.getString("about");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int code, String error) {

            }
        });
    }

    private void refreshData() {
        mRefresh.setColorSchemeResources(R.color.colorPrimary);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getData();
                    }
                }).start();
            }
        });
    }

    /**
     * 获取数据
     */
    private void getData() {
        HttpUtils.getData(PrivateUtils.MAIN_URL, new HttpCallBackListener() {
            @Override
            public void onSuccess(String data) {
                //联网成功
                Log.e("DATA", data);
                //解析数据
                jx(data);
                //刷新
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        if (mRefresh != null) {
                            mRefresh.setRefreshing(false);
                        }
                    }
                });
            }

            @Override
            public void onError(int code, String error) {
                //错误
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        if (mRefresh != null) {
                            mRefresh.setRefreshing(false);
                        }
                        Toast.makeText(MainActivity.this, "链接超时请检查网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //只能刷一次
    private boolean mIsBool = false;
    //修复有的链接没有ss
    private boolean mBool = true;

    //解析数据
    private void jx(String data) {
        if (mIsBool) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "暂无更新", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        mIsBool = true;
        Document document = Jsoup.parse(data);
        Elements table = document.select("table");
        //解析table
        for (Element element : table) {
            if (!(element == table.get(0)))
                return;
            //获取每行
            Elements tr = element.select("tr");
            int s = 0;
            for (Element element1 : tr) {
                if (s == 0) {
                    s++;
                } else {

                    //有多少单元格
                    Elements td = element1.select("td");
                    //遍历所有单元格
                    int aa = 1;
                    String mAddress = null;
                    String mIp = null;
                    String mPost = null;
//                    String mPassword = null;
                    String mEncryption = null;
//                    String mName = null;
                    String mImg = null;
                    int mmm = R.mipmap.ic_launcher;
                    for (Element element2 : td) {
                        if (aa == 7) {
                            //链接
                            String url = element2.toString();
                            Document parse = Jsoup.parse(url);
                            Elements a = parse.select("a");
                            int aaa = 1;
                            for (Element element3 : a) {
                                if (aaa == a.size()) {
                                    Log.e("a", element3.attr("href").toString());
                                    String href = element3.attr("href").toString();
                                    //是否存在ss字样
                                    int indexOf = href.indexOf("ss");
                                    if (indexOf == -1) {
                                        mBool = false;
                                        continue;
                                    }
                                    String ss = href.substring(indexOf);
                                    Log.e("a", ss);
                                    mImg = ss;
                                }
                                aaa++;
                            }
                        } else {
                            Log.e("data", element2.text());
                            switch (aa) {
                                case 1:
                                    //服务器地址
                                    mAddress = element2.text();
                                    //判断 国家国旗,取消
                                    if (mAddress.indexOf("加拿大") != -1) {
                                        mmm = R.drawable.flag_canada;
                                    } else if (mAddress.indexOf("中国") != -1 || mAddress.indexOf("台湾") != -1 || mAddress.indexOf("香港") != -1 || mAddress.indexOf("澳门") != -1 || mAddress.indexOf("北京") != -1 || mAddress.indexOf("上海") != -1) {
                                        mmm = R.drawable.flag_china;
                                    } else if (mAddress.indexOf("日本") != -1) {
                                        mmm = R.drawable.flag_japan;
                                    } else if (mAddress.indexOf("俄罗斯") != -1) {
                                        mmm = R.drawable.flag_russia;
                                    } else if (mAddress.indexOf("新加坡") != -1) {
                                        mmm = R.drawable.flag_singapore;
                                    } else if (mAddress.indexOf("美国") != -1) {
                                        mmm = R.drawable.flag_usa;
                                    }
                                    break;
                                case 2:
                                    //服务器ip
                                    mIp = element2.text();
                                    break;
                                case 3:
                                    mPost = element2.text();
                                    break;
                                case 4:
//                                    mPassword = element2.text();
                                    break;
                                case 5:
                                    mEncryption = element2.text();
                                    break;
                                case 6:
//                                    mName = element2.text();
                                    break;
                            }
                            aa++;
                        }
                    }
                    //修复有的链接没有ss
                    if (mBool) {
                        //添加
//                        ShowVO showVO = new ShowVO(mAddress, mIp, mPost, mPassword, mEncryption, mName, mImg, mmm);
                        ShowVO showVO = new ShowVO(mAddress, mIp, mPost, mEncryption, mImg, mmm);

                        mList.add(showVO);
                        showVO = null;
                    } else {
                        mBool = true;
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * 菜单监听
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_1:
                Intent intent = new Intent(MainActivity.this, ExplainActivity.class);
                intent.putExtra(ExplainActivity.EXPLAIN_CONTENT, mHelp);
                intent.putExtra(ExplainActivity.EXPLAIN_TITLE, "使用说明");
                startActivity(intent);
                break;
            case R.id.menu_2:
                Intent intent2 = new Intent(MainActivity.this, ExplainActivity.class);
                intent2.putExtra(ExplainActivity.EXPLAIN_CONTENT, mAbout);
                intent2.putExtra(ExplainActivity.EXPLAIN_TITLE, "关于我们");
                startActivity(intent2);
                break;
        }
        return true;
    }


    private void bindID() {
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar_title);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_show);
        mRefresh = (SwipeRefreshLayout) findViewById(R.id.main_refresh_load);
    }

}
