package com.example.dell.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ExplainActivity extends Activity {

    private TextView mTextTitle, mTextContent;
    private Button mBtn;
    public static final String EXPLAIN_TITLE = "0";
    public static final String EXPLAIN_CONTENT = "1";

    private String mTitle, mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explain);
        bindID();
        getData();
        mTextTitle.setText(mTitle);
        mTextContent.setText(mContent);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getData() {
        Intent intent = getIntent();
        mTitle = intent.getStringExtra(EXPLAIN_TITLE);
        mContent = intent.getStringExtra(EXPLAIN_CONTENT);
    }

    private void bindID() {
        mTextTitle = (TextView) findViewById(R.id.explain_text_title);
        mTextContent = (TextView) findViewById(R.id.explain_text_content);
        mBtn = (Button) findViewById(R.id.explain_button_ok);
    }


}
