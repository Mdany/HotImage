package com.chenyu.monster.hotimage;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.InputStream;

/**
 * Created by chenyu on 16/7/5.
 */
public class BodyPartActivity extends AppCompatActivity implements View.OnClickListener, BodyClickView.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * 男性
     */
    private static final String MALE_FLAG = "male";
    /**
     * 女性
     */
    private static final String FEMALE_FLAG = "female";
    /**
     * 儿童
     */
    private static final String CHILD_FLAG = "child";
    /**
     * 正面
     */
    private static final String FRONT_BODY = "_front_body.png";
    private static final String FRONT_XML = "_front.xml";
    /**
     * 背面
     */
    private static final String BACK_BODY = "_back_body.png";
    private static final String BACK_XML = "_back.xml";
    /**
     * 头部
     */
    private static final String HEAD_BODY = "_head_body.png";
    private static final String HEAD_XML = "_head.xml";
    /**
     * 特殊跳转部位 头部
     */
    private static final String SPECIAL_BODY_PART = "head";
    /**
     * 其他身体部位
     */
    private TextView otherBody;
    /**
     * 男性选择
     */
    private TextView maleTv;
    /**
     * 女性选择
     */
    private TextView femaleTv;
    /**
     * 儿童选择
     */
    private TextView childTv;
    /**
     * 人体正反面切换
     */
    private ToggleButton switchBody;
    /**
     * 显示人体图
     */
    private BodyClickView humanBody;
    /**
     * 记录当前性别
     */
    private StringBuffer sexFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_body_part_select);
        initView();
        initData();
    }

    /**
     * 设置view
     */
    private void initView() {
        otherBody = (TextView) findViewById(R.id.tv_other_body);
        maleTv = (TextView) findViewById(R.id.tv_male);
        femaleTv = (TextView) findViewById(R.id.tv_female);
        childTv = (TextView) findViewById(R.id.tv_child);
        switchBody = (ToggleButton) findViewById(R.id.st_check);
        humanBody = (BodyClickView) findViewById(R.id.bv_human_body);

        maleTv.setOnClickListener(this);
        femaleTv.setOnClickListener(this);
        childTv.setOnClickListener(this);
        otherBody.setOnClickListener(this);
        humanBody.setOnBodyClickListener(this);
        switchBody.setOnCheckedChangeListener(this);
    }

    /**
     * 设置数据
     */
    private void initData() {
        sexFlag = new StringBuffer();
        selectMale();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_male:
                selectMale();
                break;
            case R.id.tv_female:
                selectFemale();
                break;
            case R.id.tv_child:
                selectChild();
                break;
        }
    }

    @Override
    public void OnClick(View view, BodyArea hotArea) {
        if (hotArea == null) return;
        if (!isHead() && SPECIAL_BODY_PART.equals(hotArea.areaId)) {
            switchBodyHead(true);
        } else {
            humanBody.clearArea();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switchBodySide(isChecked);
    }

    /**
     * 选择读取哪种性别和哪个方面的人体数据
     *
     * @param sexFlag
     */
    private void openFile(String sexFlag, String partFlag, String partXml) {
        AssetManager assetManager = getResources().getAssets();
        InputStream imgInputStream = null;
        InputStream fileInputStream = null;
        try {
            imgInputStream = assetManager.open(sexFlag + partFlag);
            fileInputStream = assetManager.open(sexFlag + partXml);
            humanBody.setImageBitmap(fileInputStream, imgInputStream, BodyClickView.FIT_XY);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeInputStream(imgInputStream);
            FileUtils.closeInputStream(fileInputStream);
        }
    }

    /**
     * 选中男性
     */
    private void selectMale() {
        if (MALE_FLAG.equals(sexFlag.toString())) return;
        maleTv.setActivated(true);
        femaleTv.setActivated(false);
        childTv.setActivated(false);
        sexFlag.replace(0, sexFlag.length(), MALE_FLAG);
        switchBodySide(switchBody.isChecked());
    }

    /**
     * 选中女性
     */
    private void selectFemale() {
        if (FEMALE_FLAG.equals(sexFlag.toString())) return;
        maleTv.setActivated(false);
        femaleTv.setActivated(true);
        childTv.setActivated(false);
        sexFlag.replace(0, sexFlag.length(), FEMALE_FLAG);
        switchBodySide(switchBody.isChecked());
    }

    /**
     * 选中儿童
     */
    private void selectChild() {
        if (CHILD_FLAG.equals(sexFlag.toString())) return;
        maleTv.setActivated(false);
        femaleTv.setActivated(false);
        childTv.setActivated(true);
        sexFlag.replace(0, sexFlag.length(), CHILD_FLAG);
        switchBodySide(switchBody.isChecked());
    }

    /**
     * 切换人体正反面
     */
    private void switchBodySide(boolean isFront) {
        if (isFront) {
            openFile(sexFlag.toString(), FRONT_BODY, FRONT_XML);
        } else {
            openFile(sexFlag.toString(), BACK_BODY, BACK_XML);
        }
    }

    /**
     * 切换是否显示的是头部
     */
    private void switchBodyHead(boolean isHead) {
        if (isHead) {
            maleTv.setVisibility(View.GONE);
            femaleTv.setVisibility(View.GONE);
            childTv.setVisibility(View.GONE);
            switchBody.setVisibility(View.GONE);
            otherBody.setVisibility(View.GONE);
            openFile(sexFlag.toString(), HEAD_BODY, HEAD_XML);
        } else {
            maleTv.setVisibility(View.VISIBLE);
            femaleTv.setVisibility(View.VISIBLE);
            childTv.setVisibility(View.VISIBLE);
            switchBody.setVisibility(View.VISIBLE);
            otherBody.setVisibility(View.VISIBLE);
            switchBodySide(switchBody.isChecked());
        }
    }

    /**
     * 判断当前是否为头部
     *
     * @return
     */
    private boolean isHead() {
        return maleTv.getVisibility() != View.VISIBLE &&
                femaleTv.getVisibility() != View.VISIBLE &&
                childTv.getVisibility() != View.VISIBLE &&
                switchBody.getVisibility() != View.VISIBLE &&
                otherBody.getVisibility() != View.VISIBLE;
    }

    @Override
    public void onBackPressed() {
        if (isHead()) {
            switchBodyHead(false);
        } else {
            super.onBackPressed();
        }
    }
}
