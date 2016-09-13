package com.chenyu.monster.hotimage;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;

/**
 * Created by chenyu on 16/6/22.
 */
public class BodyClickActivity extends AppCompatActivity implements BodyClickView.OnClickListener {
    private BodyClickView mHotView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_body_click);
        initParam();
        initData();
    }

    private void initParam() {
        mHotView = (BodyClickView) findViewById(R.id.a_main_hotview);
    }

    protected void initData() {
        AssetManager assetManager = getResources().getAssets();
        InputStream imgInputStream = null;
        InputStream fileInputStream = null;
        try {
            imgInputStream = assetManager.open("female_head_body.png");
            fileInputStream = assetManager.open("female_head.xml");
            mHotView.setImageBitmap(fileInputStream, imgInputStream, BodyClickView.FIT_XY);
            mHotView.setOnBodyClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeInputStream(imgInputStream);
            FileUtils.closeInputStream(fileInputStream);
        }
    }

    @Override
    public void OnClick(View view, BodyArea hotArea) {
        Toast.makeText(this, "你点击了" + hotArea.desc, Toast.LENGTH_SHORT).show();
    }
}
