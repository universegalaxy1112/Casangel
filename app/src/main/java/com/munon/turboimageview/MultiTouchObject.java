package com.munon.turboimageview;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;

import com.itikarus.miska.R;
import com.itikarus.miska.models.product_model.ProductDetails;
import com.itikarus.miska.workspace.ProjectModel;

import java.io.Serializable;

public abstract class MultiTouchObject implements Serializable {

    protected boolean firstLoad = true;

    public ProductDetails getProductModel() {
        return productModel;
    }

    public void setProductModel(ProductDetails productModel) {
        this.productModel = productModel;
    }

    protected ProductDetails productModel;

    protected transient Paint textPaint = new Paint();
    protected final transient Paint borderPaint = new Paint();
    protected final transient Paint borderNodePaint = new Paint();

    protected static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    protected int borderColor = DEFAULT_BORDER_COLOR;

    protected String imageUrl = "";

    protected int width;
    protected int height;

    // width/height of screen
    protected int displayWidth;
    protected int displayHeight;

    protected float centerX;
    protected float centerY;
    protected float scaleX;
    protected float scaleY;
    protected float angle;

    protected float minX;
    protected float maxX;
    protected float minY;
    protected float maxY;
    protected float cancelPointX;
    protected float cancelPointY;
    protected float donePointX;
    protected float donePointY;

    protected float flipBtnCenterX;
    protected float flipBtnCenterY;

    protected float aheadBtnCenterX;
    protected float aheadBtnCenterY;

    protected float behindBtnCenterX;
    protected float behindBtnCenterY;


    protected final static int GRAB_AREA_SIZE = 40;
    protected boolean isGrabAreaSelected = false;
    protected boolean isLatestSelected = false;

    protected float grabAreaX1;
    protected float grabAreaY1;
    protected float grabAreaX2;
    protected float grabAreaY2;

    protected float startMidX;
    protected float startMidY;

    protected boolean flippedHorizontally;

    private static final int UI_MODE_ROTATE = 1;
    private static final int UI_MODE_ANISOTROPIC_SCALE = 2;
    protected final int mUIMode = UI_MODE_ROTATE;
    protected Resources res;

    public MultiTouchObject(Resources res) {
        this.res = res;
        init(res);
    }

    protected void init(Resources res) {

        flippedHorizontally = false;

        DisplayMetrics metrics = res.getDisplayMetrics();

        displayWidth =
            (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                ? Math.max(metrics.widthPixels, metrics.heightPixels)
                : Math.min(metrics.widthPixels, metrics.heightPixels);

        displayHeight =
            (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                ? Math.min(metrics.widthPixels, metrics.heightPixels)
                : Math.max(metrics.widthPixels, metrics.heightPixels);

    }

    /**
     * Set the position and scale of an image in screen coordinates
     */
    public boolean setPos(PositionAndScale newImgPosAndScale) {
        float newScaleX;
        float newScaleY;

        if ((mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0) {
            newScaleX = newImgPosAndScale.getScaleX();
        } else {
            newScaleX = newImgPosAndScale.getScale();
        }

        if ((mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0) {
            newScaleY = newImgPosAndScale.getScaleY();
        } else {
            newScaleY = newImgPosAndScale.getScale();
        }

        return setPos(newImgPosAndScale.getXOff(),
            newImgPosAndScale.getYOff(),
            newScaleX,
            newScaleY,
            newImgPosAndScale.getAngle());
    }

    /**
     * Set the position and scale of an image in screen coordinates
     */
    protected boolean setPos(float centerX, float centerY,
                             float scaleX, float scaleY, float angle) {
        float ws = (width / 2) * scaleX;
        float hs = (height / 2) * scaleY;

        minX = centerX - ws;
        minY = centerY - hs;
        maxX = centerX + ws;
        maxY = centerY + hs;

        // (x, y) = (minX, maxY)
        float tmpX = minX - centerX;
        float tmpY = maxY - centerY;

        float tmpPointX = tmpX * ((float)Math.cos((double) angle))- tmpY * ((float) Math.sin((double) angle));
        float tmpPointY = tmpY * ((float)Math.cos((double) angle))+ tmpX * ((float) Math.sin((double) angle));

        cancelPointX = tmpPointX + centerX;
        cancelPointY = tmpPointY + centerY;

        // (x, y) = (maxX, maxY)
        tmpX = maxX - centerX;
        tmpY = maxY - centerY;

        tmpPointX = tmpX * ((float)Math.cos((double) angle))- tmpY * ((float) Math.sin((double) angle));
        tmpPointY = tmpY * ((float)Math.cos((double) angle))+ tmpX * ((float) Math.sin((double) angle));

        donePointX = tmpPointX + centerX;
        donePointY = tmpPointY + centerY;


        // for flip button
        tmpX = (minX + (maxX - minX) / 2) - centerX;
        tmpY = (minY - res.getDimension(R.dimen.dip_50) /2) - centerY;

        tmpPointX = tmpX * ((float)Math.cos((double) angle))- tmpY * ((float) Math.sin((double) angle));
        tmpPointY = tmpY * ((float)Math.cos((double) angle))+ tmpX * ((float) Math.sin((double) angle));

        flipBtnCenterX = tmpPointX + centerX;
        flipBtnCenterY = tmpPointY + centerY;

        // for ahead button
        tmpX = (minX + (maxX - minX) / 4 -res.getDimension(R.dimen.dip_10)) - centerX;
        tmpY = (minY - res.getDimension(R.dimen.dip_30) + res.getDimension(R.dimen.dip_10)) - centerY;

        tmpPointX = tmpX * ((float)Math.cos((double) angle))- tmpY * ((float) Math.sin((double) angle));
        tmpPointY = tmpY * ((float)Math.cos((double) angle))+ tmpX * ((float) Math.sin((double) angle));

        aheadBtnCenterX = tmpPointX + centerX;
        aheadBtnCenterY = tmpPointY + centerY;

        // for behind button
        tmpX = (minX + (maxX - minX) * 3 / 4 -res.getDimension(R.dimen.dip_10)) - centerX;
        tmpY = (minY - res.getDimension(R.dimen.dip_30) + res.getDimension(R.dimen.dip_10)) - centerY;

        tmpPointX = tmpX * ((float)Math.cos((double) angle))- tmpY * ((float) Math.sin((double) angle));
        tmpPointY = tmpY * ((float)Math.cos((double) angle))+ tmpX * ((float) Math.sin((double) angle));

        behindBtnCenterX = tmpPointX + centerX;
        behindBtnCenterY = tmpPointY + centerY;

        grabAreaX1 = maxX - GRAB_AREA_SIZE;
        grabAreaY1 = maxY - GRAB_AREA_SIZE;
        grabAreaX2 = maxX;
        grabAreaY2 = maxY;

        this.centerX = centerX;
        this.centerY = centerY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.angle = angle;

        return true;
    }

    public ProjectModel getCloneProjectModel(){
        ProjectModel model = new ProjectModel();
        model.setFirstLoad(firstLoad);
        model.setWidth(width);
        model.setHeight(height);
        model.setDisplayWidth(displayWidth);
        model.setDisplayHeight(displayHeight);
        model.setAngle(angle);
        model.setCancelPointX(cancelPointX);
        model.setCancelPointY(cancelPointY);
        model.setCenterX(centerX);
        model.setCenterY(centerY);
        model.setDonePointX(donePointX);
        model.setDonePointY(donePointY);
        model.setFlipBtnX(flipBtnCenterX);
        model.setFlipBtnY(flipBtnCenterY);
        model.setAheadBtnX(aheadBtnCenterX);
        model.setAheadBtnY(aheadBtnCenterY);
        model.setBehindBtnX(behindBtnCenterX);
        model.setBehindBtnY(behindBtnCenterY);
        model.setMaxX(maxX);
        model.setMaxY(maxY);
        model.setMinX(minX);
        model.setMinY(minY);
        model.setScaleX(scaleX);
        model.setScaleY(scaleY);
        model.setGrabAreaSelected(isGrabAreaSelected);
        model.setLatestSelected(isLatestSelected);
        model.setGrabAreaX1(grabAreaX1);
        model.setGrabAreaY1(grabAreaY1);
        model.setGrabAreaX2(grabAreaX2);
        model.setGrabAreaY2(grabAreaY2);
        model.setStartMidX(startMidX);
        model.setStartMidY(startMidY);
        model.setProductModel(productModel);
        model.setImageUrl(imageUrl);
        return model;
    }

    public void restoreInfoFromModel(ProjectModel model){
        firstLoad = model.getFirstLoad();
        angle = model.getAngle();
        width = model.getWidth();
        height = model.getHeight();
        displayWidth = model.getDisplayWidth();
        displayHeight = model.getDisplayHeight();
        cancelPointX = model.getCancelPointX();
        cancelPointY = model.getCancelPointY();
        centerX = model.getCenterX();
        centerY = model.getCenterY();
        donePointX = model.getDonePointX();
        donePointY = model.getDonePointY();

        flipBtnCenterX = model.getFlipBtnX();
        flipBtnCenterY = model.getFlipBtnY();

        aheadBtnCenterX = model.getAheadBtnX();
        aheadBtnCenterY = model.getAheadBtnY();

        behindBtnCenterX = model.getBehindBtnX();
        behindBtnCenterY = model.getBehindBtnY();

        maxX = model.getMaxX();
        maxY = model.getMaxY();
        minX = model.getMinX();
        minY = model.getMinY();
        scaleX = model.getScaleX();
        scaleY = model.getScaleY();
        isGrabAreaSelected = model.isGrabAreaSelected();
        isLatestSelected = model.isLatestSelected();
        grabAreaX1 = model.getGrabAreaX1();
        grabAreaY1 = model.getGrabAreaY1();
        grabAreaX2 = model.getGrabAreaX2();
        grabAreaY2 = model.getGrabAreaY2();
        startMidX = model.getStartMidX();
        startMidY = model.getStartMidY();
        productModel = model.getProductModel();
    }

    /**
     * Return whether or not the given screen coords are inside this image
     */
    public boolean containsPoint(float touchX, float touchY) {

        // (x, y) = (minX, maxY)
        float tmpX = touchX - centerX;
        float tmpY = touchY - centerY;

        float tmpCancelPointX = tmpX * ((float)Math.cos((double) -angle))- tmpY * ((float) Math.sin((double) -angle));
        float tmpCancelPointY = tmpY * ((float)Math.cos((double) -angle))+ tmpX * ((float) Math.sin((double) -angle));

        float restoreRotateTouchX = tmpCancelPointX + centerX;
        float restoreRotateTouchY = tmpCancelPointY + centerY;

        boolean isContainedInMainArea = (restoreRotateTouchX >= minX - 20 && restoreRotateTouchX <= maxX + 20 && restoreRotateTouchY >= minY - 20 && restoreRotateTouchY <= maxY + 20);

        boolean isContainedInFlipBoard =    restoreRotateTouchX > (minX + ((maxX - minX) /2)) - res.getDimension(R.dimen.dip_10) &&
                                            restoreRotateTouchX < (minX + ((maxX - minX) /2)) + res.getDimension(R.dimen.dip_10) &&
                                            restoreRotateTouchY > minY - res.getDimension(R.dimen.dip_50) &&
                                            restoreRotateTouchY < minY;

        boolean isContainedInAheadArea =    restoreRotateTouchX > (minX + ((maxX - minX) /4)) - res.getDimension(R.dimen.dip_10) - res.getDimension(R.dimen.dip_20) &&
                                            restoreRotateTouchX < (minX + ((maxX - minX) /4)) - res.getDimension(R.dimen.dip_10) + res.getDimension(R.dimen.dip_20) &&
                                            restoreRotateTouchY > minY - res.getDimension(R.dimen.dip_30) &&
                                            restoreRotateTouchY < minY - res.getDimension(R.dimen.dip_5) ;

        boolean isContainedInBehindArea =   restoreRotateTouchX > (minX + ((maxX - minX) * 3 /4)) - res.getDimension(R.dimen.dip_10) - res.getDimension(R.dimen.dip_20) &&
                                            restoreRotateTouchX < (minX + ((maxX - minX) * 3/4)) + res.getDimension(R.dimen.dip_10) + res.getDimension(R.dimen.dip_20) &&
                                            restoreRotateTouchY > minY - res.getDimension(R.dimen.dip_30) &&
                                            restoreRotateTouchY < minY - res.getDimension(R.dimen.dip_5) ;


        //return (touchX >= minX - 20 && touchX <= maxX + 20 && touchY >= minY - 20 && touchY <= maxY + 20);
        return isContainedInMainArea || (isSelected() && isContainedInFlipBoard) || isContainedInAheadArea || isContainedInBehindArea;
    }

    public boolean grabAreaContainsPoint(float touchX, float touchY) {
        return (touchX >= grabAreaX1 && touchX <= grabAreaX2 &&
            touchY >= grabAreaY1 && touchY <= grabAreaY2);
    }

    public void reload(Context context) {
        firstLoad = false; // Let the init know properties have changed so reload those,
        // don't go back and start with defaults
        init(context, centerX, centerY);
    }

    public abstract void draw(Canvas canvas);

    public abstract void init(Context context, float startMidX, float startMidY);

    public abstract void unload();

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getAngle() {
        return angle;
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setIsGrabAreaSelected(boolean selected) {
        isGrabAreaSelected = selected;
    }

    public boolean isGrabAreaSelected() {
        return isGrabAreaSelected;
    }

    public boolean isSelected() {
        return isLatestSelected;
    }

    public void setSelected(boolean selected) {
        this.isLatestSelected = selected;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        borderPaint.setColor(borderColor);
    }

    public boolean isFlippedHorizontally() {
        return flippedHorizontally;
    }

    public void setFlippedHorizontally(boolean flipped) {
        this.flippedHorizontally = flipped;
    }
}
