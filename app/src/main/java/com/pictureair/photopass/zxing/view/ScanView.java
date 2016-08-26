
package com.pictureair.photopass.zxing.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.pictureair.photopass.R;
import com.pictureair.photopass.zxing.camera.CameraManager;

/**
 * @author talon
 * OCR 扫描线。自定义View
 * 遇到的问题：drawBitmap (Bitmap bitmap, Rect src, Rect dst, Paint paint)
 * Rect src: 是对图片进行裁截，若是空null则显示整个图片 RectF dst：是图片在Canvas画布中显示的区域。  所以此处的矩形区域的坐标是相对于 Canvas 的。在布局文件和CameraManager中控制。
 */
public final class ScanView extends View {
    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 10L;
    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;
    /**
     * 画笔对象的引用
     */
    private Paint paint;

    /**
     * 中间滑动线的最顶端位置／最右边的位置
     */
    private int slideRight;

    private boolean isFirst;

    private BitmapDrawable bitmapDrawable;

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        bitmapDrawable = (BitmapDrawable) (ContextCompat.getDrawable(getContext(), R.drawable.qrcode_scan_line_ocr));
    }

    @Override
    public void onDraw(Canvas canvas) {
        //中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getOCRFrameRect();
        if (frame == null) {
            return;
        }
        //初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideRight = frame.right;
        }

        slideRight -= SPEEN_DISTANCE;
        if (slideRight <= frame.left) {
            slideRight = frame.right;
        }

        Rect lineRect = new Rect();
        lineRect.left = slideRight;
        lineRect.right = slideRight + 18;// 18 是线的宽度。
        lineRect.top = frame.top;
        lineRect.bottom = frame.bottom;
        canvas.drawBitmap(bitmapDrawable.getBitmap(), null, lineRect, paint);
        //只刷新扫描框的内容，其他地方不刷新
        postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                frame.right, frame.bottom);
    }

}
