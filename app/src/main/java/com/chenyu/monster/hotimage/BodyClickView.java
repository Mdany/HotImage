package com.chenyu.monster.hotimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenyu on 16/6/22.
 */
public class BodyClickView extends View {
    private Context mContext;
    /**
     * 热点区域
     */
    private Map<String, BodyArea> mBodyAreas;
    /**
     * 检测区域
     */
    private Map<String, BodyArea.CheckArea> mCheckAreas;
    /**
     * 热点的key值
     */
    private Set<String> mBodyKeys;
    /**
     * 原图片
     */
    private Bitmap mSourceBitmap = null;
    /**
     * 用于保存Matrix值
     */
    private float[] mValues;
    /**
     * 记录点的位置
     */
    private PointF mPointF, mMidPointF;
    /**
     * Path中转RectF时的中间变量,可重复利用
     */
    private final RectF mEmptyRectF = new RectF();
    /**
     * 视图宽度
     */
    private static int VIEW_WIDTH = 0;
    /**
     * 视图高度
     */
    private static int VIEW_HEIGHT = 0;
    /**
     * 原图宽度
     */
    private static int BIT_WIDTH = 0;
    /**
     * 原图高度
     */
    private static int BIT_HEIGHT = 0;
    /**
     * 按下时间,用于检测点击事件
     * 间隔时间小于定值则位点击时间，这里没用，w因为我想要手指离开屏幕才会触发热区点击事件
     */
    private long mDownTime;

    private MotionEvent lastClick = null;

    // 控件 内边距
    private float mPadding = 0;

    private BodyArea mBodyArea;

    //
    private Matrix mSaveMatrix, mMatrix;
    //最大与最小缩放值
    private float minScale, maxScale;
    private short mFitXY = 0;
    //不进行适配
    public final static short FIT_NONE = 0;
    //X方向适配
    public final static short FIT_X = 1;
    //Y方向适配
    public final static short FIT_Y = 2;
    //XY方向适配，以最小作为标准
    public final static short FIT_XY = 3;


    /**
     * 监听点击事件
     */
    private OnClickListener mClickListener;

    public BodyClickView(Context context) {
        super(context);
        init();
    }

    public BodyClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BodyClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BodyClickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mContext = getContext();
        mCheckAreas = new HashMap<>();
        mBodyAreas = new HashMap<>();
        mBodyKeys = new LinkedHashSet<String>();
        mPointF = new PointF();
        mSaveMatrix = new Matrix();
        mMatrix = new Matrix();
        mMidPointF = new PointF();
        mValues = new float[9];
        maxScale = 4.0f;
        minScale = 1.0f;
        mPadding = getResources().getDimension(R.dimen.margin_large);
        //获取View的高与宽,并将图片放于中间位置
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                VIEW_WIDTH = getWidth();
                VIEW_HEIGHT = getHeight();
                moveToCenter(mSourceBitmap);
                scaleToFit(mSourceBitmap);
            }
        });
    }

    public void reset() {
        mFitXY = 0;
        mBodyAreas.clear();
        mCheckAreas.clear();
        mBodyKeys.clear();
        mPointF.x = 0;
        mPointF.y = 0;
        mMidPointF.x = 0;
        mMidPointF.y = 0;
        mSaveMatrix.reset();
        minScale = 1.0f;
        maxScale = 4.0f;
        mMatrix.reset();
        for (int i = 0; i < 9; ++i) {
            mValues[i] = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSourceBitmap != null) {
            canvas.drawBitmap(mSourceBitmap, mMatrix, null);
            drawPath(canvas);
        } else {
        }
    }

    /**
     * 画
     *
     * @param canvas
     */
    private void drawPath(Canvas canvas) {
        for (String key : mBodyKeys) {
            float scale = getCurrentScale();
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//			paint.setColor(Color.BLUE);
            paint.setARGB(80, 68, 173, 161);
            paint.setStyle(Paint.Style.FILL);
            canvas.scale(scale, scale);
            canvas.translate((VIEW_WIDTH / scale - BIT_WIDTH) / 2, (VIEW_HEIGHT / scale - BIT_HEIGHT)
                    / 2);
            canvas.drawPath(mCheckAreas.get(key).getPath(), paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean state = super.onTouchEvent(event);
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        if (mSourceBitmap != null) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownTime = System.currentTimeMillis();
                    mPointF.set(x, y);
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    upToCheckIsClick(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveEvent(event);
                    break;
            }
            state = true;
        }
        return state;
    }

    /**
     * 当前默认只取最前面一个
     *
     * @param
     */
    protected void upToCheckIsClick(MotionEvent event) {
//        long curTime = System.currentTimeMillis() - mDownTime;
        checkAreas(event);
        checkAreaIsClick(event);
        invalidate();
    }

    /**
     * 移动事件触发，是否是热区，是热区则drawPath()
     * 如果想响应热区点击事件，调用checkAreaIsClick()
     *
     * @param event
     */
    protected void moveEvent(MotionEvent event) {
        checkAreas(event);
        invalidate();
    }

    /**
     * 响应热区点击事件，与checkAreas(MotionEvent event)一起使用
     *
     * @param event
     */
    private void checkAreaIsClick(MotionEvent event) {
        if (!mBodyKeys.isEmpty()) {
            BodyArea area;
            for (String key : mBodyKeys) {
                area = mBodyAreas.get(key);
                mClickListener.OnClick(this, area);
                break;
            }
        }
    }

    /**
     * 是否点击在热点区检测
     *
     * @param event
     */
    protected void checkAreas(MotionEvent event) {
        mBodyKeys.clear();
        float[] curMove = getCurrentMoveXY();
        float scale = getCurrentScale();
        for (String key : mCheckAreas.keySet()) {
            if (mCheckAreas.get(key).isInArea(mEmptyRectF, (event.getX() - curMove[0]) / scale, (event.getY() - curMove[1]) / scale)) {
                mBodyKeys.add(key);
                lastClick = event;
                break;
            }
        }
    }

    /**
     * @param bitmap
     */
    public void setImageBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap, FIT_NONE);
    }

    /**
     * 设置图片
     *
     * @param bitmap
     */
    public void setImageBitmap(Bitmap bitmap, short fitXY) {
        reset();
        mFitXY = fitXY;
        mSourceBitmap = bitmap;
        if (mSourceBitmap != null) {
            BIT_WIDTH = mSourceBitmap.getWidth();
            BIT_HEIGHT = mSourceBitmap.getHeight();
        }
        invalidate();
    }

    public void setImageBitmap(String hotFilePath, String hotImgPath) {
        setImageBitmap(hotFilePath, hotImgPath, FIT_NONE);
    }

    /**
     * 从资源文件中加载xml与picture
     *
     * @param hotFilePath:热点区域的位置定义文件(.xml)
     * @param hotImgPath:图片位置
     * @param fitXY                         [0, 1, 2, 3]
     *                                      0: no fit
     *                                      1: fitx
     *                                      2: fity
     *                                      3: fitxy get min scale X,Y
     */
    public void setImageBitmap(String hotFilePath, String hotImgPath, short fitXY) {
        reset();
        mFitXY = fitXY;
        mBodyArea = XMLUtils.getInstance(mContext).readDoc(hotFilePath);
        mSourceBitmap = BitmapFactory.decodeFile(hotImgPath);
        resetFiles();
    }

    /**
     * 设置图片，从流中加载
     *
     * @param hotFileStream
     * @param hotImgStream  0: no fit
     *                      1: fitx
     *                      2: fity
     *                      3: fitxy get min scale X,Y
     */
    public void setImageBitmap(InputStream hotFileStream, InputStream hotImgStream) {
        setImageBitmap(hotFileStream, hotImgStream, FIT_NONE);
    }

    /**
     * 设置图片，从流中加载
     *
     * @param hotFileStream
     * @param hotImgStream
     * @param fitXY         [0, 1, 2, 3]
     *                      0: no fit
     *                      1: fitx
     *                      2: fity
     *                      3: fitxy get min scale X,Y
     */
    public void setImageBitmap(InputStream hotFileStream, InputStream hotImgStream, short fitXY) {
        reset();
        mFitXY = fitXY;
        mBodyArea = XMLUtils.getInstance(mContext).readDoc(hotFileStream);
        mSourceBitmap = BitmapFactory.decodeStream(hotImgStream);
        resetFiles();
    }

    /**
     * 图片信息重置
     */
    protected void resetFiles() {
        try {
            if (mSourceBitmap != null) {
                BIT_HEIGHT = mSourceBitmap.getHeight();
                BIT_WIDTH = mSourceBitmap.getWidth();
            }
            moveToCenter(mSourceBitmap);
            scaleToFit(mSourceBitmap);
            addToKeys(mBodyArea);
            invalidate();
        } catch (Exception e) {
        }
    }

    /**
     * 添加到热点集合中
     *
     * @param area
     */
    protected void addToKeys(BodyArea area) {
        if (area != null) {
            String areaCode = area.areaId;
            BodyArea.CheckArea checkArea = area.getCheckArea();
            mBodyAreas.put(areaCode, area);
            if (checkArea != null) {
                mCheckAreas.put(areaCode, checkArea);
            }
            for (BodyArea hot : area.areas) {
                addToKeys(hot);
            }
        }
    }

    /**
     * 返回当前的移动位置
     *
     * @return
     */
    public float[] getCurrentMoveXY() {
        mMatrix.getValues(mValues);
        return new float[]{mValues[2], mValues[5]};
    }

    /**
     * 返回当前的ScaleX
     */
    public float getCurrentScale() {
        mMatrix.getValues(mValues);
        return Math.abs(mValues[0] == 0 ? mValues[1] : mValues[0]);
    }

    /**
     * 移动到中间位置
     *
     * @param bitmap
     */
    protected void moveToCenter(Bitmap bitmap) {
        if (bitmap != null && VIEW_HEIGHT != 0 && VIEW_WIDTH != 0) {
            mMatrix.setTranslate((VIEW_WIDTH - BIT_WIDTH) / 2, (VIEW_HEIGHT - BIT_HEIGHT) / 2);
        }
    }

    /**
     * 缩放到合适大小
     *
     * @param bitmap
     */
    protected void scaleToFit(Bitmap bitmap) {
        if (bitmap != null && VIEW_HEIGHT != 0 && VIEW_WIDTH != 0) {
            float newScaleX = minScale;
            float newScaleY = minScale;
            if (mFitXY == 1 || mFitXY == 3) {
//				if(BIT_WIDTH > VIEW_WIDTH) {
                newScaleX = (VIEW_WIDTH * 1.0f) / BIT_WIDTH;
//				}
            }
            if (mFitXY == 2 || mFitXY == 3) {
//				if(BIT_HEIGHT > VIEW_HEIGHT) {
                newScaleY = ((VIEW_HEIGHT - mPadding * 2) * 1.0f) / BIT_HEIGHT;
//				}
            }
            minScale = Math.min(newScaleX, newScaleY);
            mMatrix.postScale(minScale, minScale, VIEW_WIDTH / 2, VIEW_HEIGHT / 2);
        }
    }

    /**
     * 设置点击事件
     *
     * @param clickListener
     */
    public void setOnBodyClickListener(OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    /**
     * 点击 热图 区域 选中效果
     */
    public interface OnClickListener {
        void OnClick(View view, BodyArea hotArea);
    }

    /**
     * 清理识别区域
     */
    public void clearArea() {
        mBodyKeys.clear();
        invalidate();
    }
}
