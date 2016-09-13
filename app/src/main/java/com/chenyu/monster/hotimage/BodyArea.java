package com.chenyu.monster.hotimage;

import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenyu on 16/6/22.
 */
public class BodyArea implements Parcelable {
    /**
     * 区域id
     */
    public String areaId;
    /**
     * bodyPart
     */
    public String bodyPart;
    /**
     * 区域名称
     */
    public String areaTitle;
    /**
     * 区域介绍
     */
    public String desc;
    /***
     * 两组区域点坐标,比如胳膊就是两组区域,胸部就是一组区域
     */
    public int[] pts1;
    public int[] pts2;
    public List<BodyArea> areas = new ArrayList<BodyArea>();

    public Path path;
    public boolean isRectF;

    public BodyArea() {
    }

    protected BodyArea(Parcel in) {
        areaId = in.readString();
        areaTitle = in.readString();
        desc = in.readString();
        pts1 = in.createIntArray();
        pts2 = in.createIntArray();
        areas = in.createTypedArrayList(BodyArea.CREATOR);
    }

    public static final Creator<BodyArea> CREATOR = new Creator<BodyArea>() {
        @Override
        public BodyArea createFromParcel(Parcel in) {
            return new BodyArea(in);
        }

        @Override
        public BodyArea[] newArray(int size) {
            return new BodyArea[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(areaId);
        dest.writeString(areaTitle);
        dest.writeString(desc);
        dest.writeIntArray(pts1);
        dest.writeIntArray(pts2);
        dest.writeTypedList(areas);
    }

    public void setPts1(String[] pts) {
        setStrArrayToIntArray1(pts);
    }

    private void setStrArrayToIntArray1(String[] pts) {
        if (null != pts && 0 != pts.length) {
            int len = pts.length;
            this.pts1 = new int[len];
            for (int i = 0; i < len; ++i) {
                try {
                    this.pts1[i] = Integer.parseInt(pts[i]);
                } catch (Exception e) {
                    this.pts1[i] = 0;
                    Log.e("SmartPit", e.getMessage());
                }
            }
        }
    }

    public void setPts1(String pts, String split) {
        if (!TextUtils.isEmpty(pts) && !TextUtils.isEmpty(split)) {
            String[] points = pts.split(split);
            setPts1(points);
        }
    }

    public void setPts2(String[] pts) {
        setStrArrayToIntArray2(pts);
    }

    private void setStrArrayToIntArray2(String[] pts) {
        if (null != pts && 0 != pts.length) {
            int len = pts.length;
            this.pts2 = new int[len];
            for (int i = 0; i < len; ++i) {
                try {
                    this.pts2[i] = Integer.parseInt(pts[i]);
                } catch (Exception e) {
                    this.pts2[i] = 0;
                    Log.e("SmartPit", e.getMessage());
                }
            }
        }
    }

    public void setPts2(String pts, String split) {
        if (!TextUtils.isEmpty(pts) && !TextUtils.isEmpty(split)) {
            String[] points = pts.split(split);
            setPts2(points);
        }
    }

    public CheckArea getCheckArea() {
        CheckArea checkArea = null;
        if (pts1 != null) {
            if (pts2 != null) {
                checkArea = new CheckArea(pts1, pts2);
            } else {
                checkArea = new CheckArea(pts1);
            }
        }
        return checkArea;
    }

    /**
     * 写内部类的原因在于在Activity之间传递时，
     * 不能传未实现Serializable接口的类
     */
    public class CheckArea {
        private final Path path;
        //当前处理是从点的个数来判断是矩形 还是多边形,这两种的方式对点的位置判断不太一样
        private final boolean isRectF;

        private CheckArea(int[] pts) {
            this.path = new Path();
            int len = pts.length;
            isRectF = len == 4;
            drawPath(pts);
        }

        private CheckArea(int[] pts1, int[] pts2) {
            this.path = new Path();
            isRectF = false;
            drawPath(pts1);
            drawPath(pts2);
        }

        private void drawPath(int[] pts) {
            if (this.path == null) return;
            int len = pts.length;
            for (int i = 0; i < len; ) {
                if (i == 0) {
                    this.path.moveTo(pts[i++], pts[i++]);
                } else {
                    this.path.lineTo(pts[i++], pts[i++]);
                }
            }
            this.path.close();
        }

        public Path getPath() {
            return this.path;
        }

        /**
         * 检测是否在区域范围内
         *
         * @param rectf 从外部传可以重用
         * @param x
         * @param y
         * @return
         */
        public boolean isInArea(RectF rectf, float x, float y) {
            boolean resStatus = false;
            if (this.path != null) {
                rectf.setEmpty();
                path.computeBounds(rectf, true);
                if (isRectF) {
                    //当是矩形时
                    resStatus = rectf.contains(x, y);
                } else {
                    //如果是多边形时
                    Region region = new Region();
                    region.setPath(path, region);
                    region.setPath(path, new Region((int) rectf.left, (int) rectf.top, (int) rectf.right, (int) rectf.bottom));
                    resStatus = region.contains((int) x, (int) y);
                }
            }
            return resStatus;
        }
    }
}
