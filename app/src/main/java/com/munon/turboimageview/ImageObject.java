package com.munon.turboimageview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.itikarus.miska.R;
import com.itikarus.miska.library.utils.ImageUtils;

public class ImageObject extends MultiTouchObject {
    private static final double INITIAL_SCALE_FACTOR = 0.15;

    private transient Drawable drawable;

    public ImageObject(int resourceId, Resources res) {
        super(res);
        drawable = res.getDrawable(resourceId);
        initPaint();
    }

    public ImageObject(Drawable drawable, Resources res) {
        super(res);
        this.drawable = drawable;
        initPaint();
    }

    public ImageObject(Bitmap bitmap, Resources res) {
        super(res);
        this.drawable = new BitmapDrawable(res, bitmap);
        initPaint();
    }

    public ImageObject(Bitmap bitmap,String URL, Resources res) {
        super(res);
        this.drawable = new BitmapDrawable(res, bitmap);
        imageUrl = URL;
        initPaint();
    }

    private void initPaint() {
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(borderColor);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(5.0f);

        borderNodePaint.setStyle(Paint.Style.FILL);
        borderNodePaint.setColor(borderColor);
        borderNodePaint.setAntiAlias(true);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(res.getDimension(R.dimen.sp_10));
    }

    public void draw(Canvas canvas) {
        canvas.save();

        float dx = (maxX + minX) / 2;
        float dy = (maxY + minY) / 2;

        drawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
        if (flippedHorizontally) {
            canvas.scale(-1f, 1f, dx, dy);
        }
        canvas.translate(dx, dy);
        if (flippedHorizontally) {
            canvas.rotate(-angle * 180.0f / (float) Math.PI);
        } else {
            canvas.rotate(angle * 180.0f / (float) Math.PI);
        }
        canvas.translate(-dx, -dy);

        drawable.draw(canvas);

        if (isLatestSelected) {

            if (flippedHorizontally) {
                canvas.scale(-1f, 1f, dx, dy);
            }

            canvas.drawRect((int) minX, (int) minY, (int) maxX, (int) maxY, borderPaint);

            int startX, startY, endX, endY;

            //Let top Rectangle point
            startX = (int) minX -10;
            startY = (int) minY - 10;
            endX = (int) minX + 10;
            endY = (int) minY + 10;
            canvas.drawRect(startX, startY, endX, endY, borderNodePaint);

            // Flip button
            Drawable img_flip = res.getDrawable(R.drawable.ic_flip);
            startX = (int) (minX + (maxX - minX) / 2 ) - (int) res.getDimension(R.dimen.dip_12);
            startY = (int) minY - (int) res.getDimension(R.dimen.dip_40);
            endX = (int) (minX + (maxX - minX) / 2 ) + (int) res.getDimension(R.dimen.dip_12);
            endY = (int) minY;
            img_flip.setBounds(startX, startY, endX, endY);
            img_flip.draw(canvas);

            // Goto Ahead Button
            Drawable img_ahead = res.getDrawable(R.drawable.ic_triangle_up);
            startX = (int) (minX + (maxX - minX) / 4 - res.getDimension(R.dimen.dip_10)) - (int) res.getDimension(R.dimen.dip_5);
            startY = (int) minY - (int) res.getDimension(R.dimen.dip_30);
            endX = (int) (minX + (maxX - minX) / 4 - res.getDimension(R.dimen.dip_10)) + (int) res.getDimension(R.dimen.dip_5);
            endY = (int) minY - (int) res.getDimension(R.dimen.dip_25);
            img_ahead.setBounds(startX, startY, endX, endY);
            img_ahead.draw(canvas);

            Rect textBound= new Rect();
            String aheadString = res.getString(R.string.ahead);
            textPaint.getTextBounds(aheadString, 0, aheadString.length(), textBound);

            int aheadStringWidth = textBound.width();
            int aheadStringHeight = textBound.height();

            startX = (int) (minX + (maxX - minX) / 4 - res.getDimension(R.dimen.dip_10)) - aheadStringWidth / 2;
            startY = (int) minY - (int) res.getDimension(R.dimen.dip_23) + aheadStringHeight;
            canvas.drawText(aheadString, startX, startY, textPaint);

            // Goto Behind Button
            Drawable img_behind = res.getDrawable(R.drawable.ic_triangle_down);
            startX = (int) (minX + (maxX - minX) * 3 / 4 + res.getDimension(R.dimen.dip_10)) - (int) res.getDimension(R.dimen.dip_5);
            startY = (int) minY - (int) res.getDimension(R.dimen.dip_30);
            endX = (int) (minX + (maxX - minX) * 3 / 4 + res.getDimension(R.dimen.dip_10)) + (int) res.getDimension(R.dimen.dip_5);
            endY = (int) minY - (int) res.getDimension(R.dimen.dip_25);
            img_behind.setBounds(startX, startY, endX, endY);
            img_behind.draw(canvas);

            textBound= new Rect();
            aheadString = res.getString(R.string.behind);
            textPaint.getTextBounds(aheadString, 0, aheadString.length(), textBound);

            aheadStringWidth = textBound.width();
            aheadStringHeight = textBound.height();

            startX = (int) (minX + (maxX - minX) * 3 / 4 + res.getDimension(R.dimen.dip_10)) - aheadStringWidth / 2;
            startY = (int) minY - (int) res.getDimension(R.dimen.dip_23) + aheadStringHeight;
            canvas.drawText(aheadString, startX, startY, textPaint);

            //Right Top Rectangle Point
            startX = (int) maxX -10;
            startY = (int) minY - 10;
            endX = (int) maxX + 10;
            endY = (int) minY + 10;
            canvas.drawRect(startX, startY, endX, endY, borderNodePaint);

            // Close Image Button
            Drawable img_Close = res.getDrawable(R.drawable.ic_cancel_black_24px);
            startX = (int) minX -20;
            startY = (int) maxY - 20;
            endX = (int) minX + 20;
            endY = (int) maxY + 20;
            img_Close.setBounds(startX, startY, endX, endY);
            img_Close.draw(canvas);
            // Bottom Right Rectangle Point
            startX = (int) maxX -10;
            startY = (int) maxY - 10;
            endX = (int) maxX + 10;
            endY = (int) maxY + 10;
            canvas.drawRect(startX, startY, endX, endY, borderNodePaint);

        }

        canvas.restore();

    }

    /**
     * Called by activity's onPause() method to free memory used for loading the images
     */
    @Override
    public void unload() {
        this.drawable = null;
    }

    /** Called by activity's onResume() method to init the images */
    @SuppressWarnings("deprecation")
    @Override
    public void init(Context context, float startMidX, float startMidY) {
        Resources res = context.getResources();
        init(res);

        this.startMidX = startMidX;
        this.startMidY = startMidY;

        width = drawable.getIntrinsicWidth();
        height = drawable.getIntrinsicHeight();

        float centerX;
        float centerY;
        float scaleX;
        float scaleY;
        float angle;
        if (firstLoad) {
            centerX = startMidX;
            centerY = startMidY;

            float scaleFactor = (float) (Math.max(displayWidth, displayHeight) / (float) Math.max(width, height) * INITIAL_SCALE_FACTOR);
            scaleX = scaleY = scaleFactor;
            angle = 0.0f;

            firstLoad = false;
        } else {
            centerX = this.centerX;
            centerY = this.centerY;
            scaleX = this.scaleX;
            scaleY = this.scaleY;
            angle = this.angle;
        }
        setPos(centerX, centerY, scaleX, scaleY, angle);
    }
}
