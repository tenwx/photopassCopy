package com.pictureair.photopass.editPhoto.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.pictureair.photopass.util.PictureAirLog;

import java.util.LinkedHashMap;

/**
 * 贴图操作控件
 *
 * @author panyi
 */
public class FontView extends ImageView {
    private static int STATUS_IDLE = 0;
    private static int STATUS_MOVE = 1;// 移动状态
    private static int STATUS_DELETE = 2;// 删除状态
    private static int STATUS_ROTATE = 3;// 图片旋转状态

    private int imageCount;// 已加入照片的数量
    private Context mContext;
    private int currentStatus;// 当前状态
    public FontItem currentItem;// 当前操作的贴图数据
    private float oldx, oldy;
    private Paint rectPaint = new Paint();
//    private Paint boxPaint = new Paint();
    private float leftTopX, leftTopY, rightBottomX, rightBottomY;

    private LinkedHashMap<Integer, FontItem> bank = new LinkedHashMap<>();// 存贮每层贴图数据

    public FontView(Context context) {
        super(context);
        init(context);
    }

    public FontView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FontView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        currentStatus = STATUS_IDLE;

        rectPaint.setColor(Color.RED);
        rectPaint.setAlpha(100);

    }

    public void addBitImage(final Bitmap addBit) {
        FontItem item = new FontItem(this.getContext());
        item.init(addBit, this);
//		if (currentItem != null) {
//			currentItem.isDrawHelpTool = false;
//		}else {
        currentItem = item;
//		}
        //控制可以显示几个饰品，如果是一个，就把 ＋＋去掉
//		bank.put(++imageCount, item);
        bank.put(imageCount, item);
        this.invalidate();// 重绘视图
    }

    public void setBitmap(Bitmap bitmap) {
//		FontItem item = new FontItem(this.getContext());
//		item.init(bitmap, this);
//		bank.clear();
//		bank.put(++imageCount, item);
//		this.invalidate();// 重绘视图
        if (currentItem == null) {
            PictureAirLog.d("＝＝＝＝＝＝＝", "currentItem is null");
            PictureAirLog.out("currentItem is null");
        } else {
            PictureAirLog.d("＝＝＝＝＝＝＝", "currentItem is not null");
            PictureAirLog.out("currentItem is not null");
        }
        if (null != bitmap) {
            currentItem.setBitmap(bitmap, this); //在当前的bitmap进行操作。
            this.invalidate();// 重绘视图
        }
    }

    public void init(Bitmap bitmap) {
//		currentItem = null;
        currentItem.init(bitmap, this);
    }


    /**
     * 绘制客户页面
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // System.out.println("on draw!!~");
        for (Integer id : bank.keySet()) {
            FontItem item = bank.get(id);
            item.draw(canvas);
        }// end for each
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // System.out.println(w + "   " + h + "    " + oldw + "   " + oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);// 是否向下传递事件标志 true为消耗

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                int deleteId = -1;
                for (Integer id : bank.keySet()) {
                    FontItem item = bank.get(id);
                    if (item.detectDeleteRect.contains(x, y)) {// 删除模式
                        // ret = true;
                        deleteId = id;
                        currentStatus = STATUS_DELETE;
                    } else if (item.detectRotateRect.contains(x, y)) {// 点击了旋转按钮
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isDrawHelpTool = false;
                        }
                        currentItem = item;
                        currentItem.isDrawHelpTool = true;
                        currentStatus = STATUS_ROTATE;
                        oldx = x;
                        oldy = y;
//				} else if (item.dstRect.contains(x, y)) {// 移动模式
                    } else {// 移动模式   //此处不做判断，代表触摸屏幕任何一个地方都可 拖动组件。
                        // 被选中一张贴图
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isDrawHelpTool = false;
                        }
                        currentItem = item;
                        currentItem.isDrawHelpTool = true;
                        currentStatus = STATUS_MOVE;
                        oldx = x;
                        oldy = y;
                    }// end if
                }// end for each

                if (!ret && currentItem != null && currentStatus == STATUS_IDLE) {// 没有贴图被选择
                    currentItem.isDrawHelpTool = false;
//				currentItem = null;
                    invalidate();
                }

                //取消 文字的删除 按钮。
//			if (deleteId > 0 && currentStatus == STATUS_DELETE) {// 删除选定贴图
//				bank.remove(deleteId);
//				currentStatus = STATUS_IDLE;// 返回空闲状态
//				invalidate();
//			}// end if

                break;
            case MotionEvent.ACTION_MOVE:
                ret = true;
                if (currentStatus == STATUS_MOVE) {// 移动贴图
                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updatePos(dx, dy, leftTopX, leftTopY, rightBottomX, rightBottomY);
                        invalidate();
                    }// end if
                    oldx = x;
                    oldy = y;
                } else if (currentStatus == STATUS_ROTATE) {// 旋转 缩放图片操作
                    // System.out.println("旋转");
                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updateRotateAndScale(oldx, oldy, dx, dy);// 旋转
                        invalidate();
                    }// end if
                    oldx = x;
                    oldy = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                ret = false;
                currentStatus = STATUS_IDLE;
                break;
        }// end switch
        return ret;
    }

    public LinkedHashMap<Integer, FontItem> getBank() {
        return bank;
    }

    public void clear() {
        bank.clear();
        this.invalidate();
    }

    /**
     * 更新坐标
     * @param leftTopX
     * @param leftTopY
     * @param rightBottomX
     * @param rightBottomY
     */
    public void updateCoordinate(float leftTopX, float leftTopY, float rightBottomX, float rightBottomY) {
        this.leftTopX = leftTopX;
        this.leftTopY = leftTopY;
        this.rightBottomX = rightBottomX;
        this.rightBottomY = rightBottomY;
    }
}// end class
