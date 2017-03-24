package com.example.dell.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.dell.adapter.MyPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapter;
    private List<View> mList = new ArrayList<View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        bindID();
        getData();
        mAdapter = new MyPagerAdapter(mList);
        mViewPager.setAdapter(mAdapter);
    }

    //获取数据
    private void getData() {
        View inflate1 = View.inflate(GuideActivity.this, R.layout.guide_one, null);
        View inflate2 = View.inflate(GuideActivity.this, R.layout.guide_two, null);
        View inflate3 = View.inflate(GuideActivity.this, R.layout.guide_three, null);
        mList.add(inflate1);
        mList.add(inflate2);
        mList.add(inflate3);
        //监听
        Button button = (Button) inflate3.findViewById(R.id.guide_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void bindID() {
        mViewPager = (ViewPager) findViewById(R.id.guide_viewpager_watch);
    }


}
