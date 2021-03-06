/*
 * Copyright 2013 David Schreiber
 *           2013 John Paul Nalog
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.pinwheel.agility.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.SpinnerAdapter;

@Deprecated
public class SweetCoverFlow extends Gallery {

    // =============================================================================
    // Constants
    // =============================================================================

    public static final int ACTION_DISTANCE_AUTO = Integer.MAX_VALUE;

    public static final float SCALEDOWN_GRAVITY_TOP = 0.0f;

    public static final float SCALEDOWN_GRAVITY_CENTER = 0.5f;

    public static final float SCALEDOWN_GRAVITY_BOTTOM = 1.0f;

    // =============================================================================
    // Private members
    // =============================================================================

    private float reflectionRatio = 0.4f;

    private int reflectionGap = 20;

    private boolean reflectionEnabled = false;

    private float unselectedAlpha;

    /**
     * Camera used for view transformation.
     */
    private Camera transformationCamera;

    private int maxRotation = 75;

    /**
     * Factor (0-1) that defines how much the unselected children should be scaled down. 1 means no scaledown.
     */
    private float unselectedScale;

    private float scaleDownGravity = SCALEDOWN_GRAVITY_CENTER;

    /**
     * Distance in pixels between the transformation effects (alpha, rotation, zoom) are applied.
     */
    private int actionDistance;

    /**
     * Saturation factor (0-1) of items that reach the outer effects distance.
     */
    private float unselectedSaturation;

    // =============================================================================
    // Constructors
    // =============================================================================

    public SweetCoverFlow(Context context) {
        super(context);
        this.initialize();
        this.setDefaultValue();
    }

    public SweetCoverFlow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
        this.setDefaultValue();
//        this.applyXmlAttributes(attrs);
    }

    public SweetCoverFlow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initialize();
        this.setDefaultValue();
//        this.applyXmlAttributes(attrs);
    }

    private void initialize() {
        this.transformationCamera = new Camera();
        this.setSpacing(0);
    }

    private void setDefaultValue() {
        this.actionDistance = ACTION_DISTANCE_AUTO;
        this.scaleDownGravity = 1.0f;
        this.maxRotation = 45;
        this.unselectedAlpha = 0.3f;
        this.unselectedSaturation = 0.0f;
        this.unselectedScale = 0.75f;
    }

//    private void applyXmlAttributes(AttributeSet attrs) {
//        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FancyCoverFlow);
//
//        this.actionDistance = a.getInteger(R.styleable.FancyCoverFlow_actionDistance, ACTION_DISTANCE_AUTO);
//        this.scaleDownGravity = a.getFloat(R.styleable.FancyCoverFlow_scaleDownGravity, 1.0f);
//        this.maxRotation = a.getInteger(R.styleable.FancyCoverFlow_maxRotation, 45);
//        this.unselectedAlpha = a.getFloat(R.styleable.FancyCoverFlow_unselectedAlpha, 0.3f);
//        this.unselectedSaturation = a.getFloat(R.styleable.FancyCoverFlow_unselectedSaturation, 0.0f);
//        this.unselectedScale = a.getFloat(R.styleable.FancyCoverFlow_unselectedScale, 0.75f);
//    }

    // =============================================================================
    // Getter / Setter
    // =============================================================================

    public float getReflectionRatio() {
        return reflectionRatio;
    }

    public void setReflectionRatio(float reflectionRatio) {
        if (reflectionRatio <= 0 || reflectionRatio > 0.5f) {
            throw new IllegalArgumentException("reflectionRatio may only be in the interval (0, 0.5]");
        }

        this.reflectionRatio = reflectionRatio;

        if (this.getAdapter() != null) {
            ((SweetCoverFlowAdapter) this.getAdapter()).notifyDataSetChanged();
        }
    }

    public int getReflectionGap() {
        return reflectionGap;
    }

    public void setReflectionGap(int reflectionGap) {
        this.reflectionGap = reflectionGap;

        if (this.getAdapter() != null) {
            ((SweetCoverFlowAdapter) this.getAdapter()).notifyDataSetChanged();
        }
    }

    public boolean isReflectionEnabled() {
        return reflectionEnabled;
    }

    public void setReflectionEnabled(boolean reflectionEnabled) {
        this.reflectionEnabled = reflectionEnabled;

        if (this.getAdapter() != null) {
            ((SweetCoverFlowAdapter) this.getAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * Use this to provide a {@link SweetCoverFlowAdapter} to the coverflow. This
     * method will throw an {@link ClassCastException} if the passed adapter does not
     * subclass {@link SweetCoverFlowAdapter}.
     */
    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if (!(adapter instanceof SweetCoverFlowAdapter)) {
            throw new ClassCastException(SweetCoverFlow.class.getSimpleName() + " only works in conjunction with a " + SweetCoverFlowAdapter.class.getSimpleName());
        }

        super.setAdapter(adapter);
    }

    /**
     * Returns the maximum rotation that is applied to items left and right of the center of the coverflow.
     */
    public int getMaxRotation() {
        return maxRotation;
    }

    /**
     * Sets the maximum rotation that is applied to items left and right of the center of the coverflow.
     */
    public void setMaxRotation(int maxRotation) {
        this.maxRotation = maxRotation;
    }

    public float getUnselectedAlpha() {
        return this.unselectedAlpha;
    }

    public float getUnselectedScale() {
        return unselectedScale;
    }

    public void setUnselectedScale(float unselectedScale) {
        this.unselectedScale = unselectedScale;
    }

    public float getScaleDownGravity() {
        return scaleDownGravity;
    }

    public void setScaleDownGravity(float scaleDownGravity) {
        this.scaleDownGravity = scaleDownGravity;
    }

    public int getActionDistance() {
        return actionDistance;
    }

    public void setActionDistance(int actionDistance) {
        this.actionDistance = actionDistance;
    }

    @Override
    public void setUnselectedAlpha(float unselectedAlpha) {
        super.setUnselectedAlpha(unselectedAlpha);
        this.unselectedAlpha = unselectedAlpha;
    }

    public float getUnselectedSaturation() {
        return unselectedSaturation;
    }

    public void setUnselectedSaturation(float unselectedSaturation) {
        this.unselectedSaturation = unselectedSaturation;
    }

    // =============================================================================
    // Supertype overrides
    // =============================================================================

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        // We can cast here because FancyCoverFlowAdapter only creates wrappers.
        SweetCoverFlowItemWrapper item = (SweetCoverFlowItemWrapper) child;

        // Since Jelly Bean childs won't get invalidated automatically, needs to be added for the smooth coverflow animation
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            item.invalidate();
        }

        final int coverFlowWidth = this.getWidth();
        final int coverFlowCenter = coverFlowWidth / 2;
        final int childWidth = item.getWidth();
        final int childHeight = item.getHeight();
        final int childCenter = item.getLeft() + childWidth / 2;

        // Use coverflow width when its defined as automatic.
        final int actionDistance = (this.actionDistance == ACTION_DISTANCE_AUTO) ? (int) ((coverFlowWidth + childWidth) / 2.0f) : this.actionDistance;

        // Calculate the abstract amount for all effects.
        final float effectsAmount = Math.min(1.0f, Math.max(-1.0f, (1.0f / actionDistance) * (childCenter - coverFlowCenter)));

        // Clear previous transformations and set transformation type (matrix + alpha).
        t.clear();
        t.setTransformationType(Transformation.TYPE_BOTH);

        // Alpha
        if (this.unselectedAlpha != 1) {
            final float alphaAmount = (this.unselectedAlpha - 1) * Math.abs(effectsAmount) + 1;
            t.setAlpha(alphaAmount);
        }

        // Saturation
        if (this.unselectedSaturation != 1) {
            // Pass over saturation to the wrapper.
            final float saturationAmount = (this.unselectedSaturation - 1) * Math.abs(effectsAmount) + 1;
            item.setSaturation(saturationAmount);
        }

        final Matrix imageMatrix = t.getMatrix();

        // Apply rotation.
        if (this.maxRotation != 0) {
            final int rotationAngle = (int) (-effectsAmount * this.maxRotation);
            this.transformationCamera.save();
            this.transformationCamera.rotateY(rotationAngle);
            this.transformationCamera.getMatrix(imageMatrix);
            this.transformationCamera.restore();
        }

        // Zoom.
        if (this.unselectedScale != 1) {
            final float zoomAmount = (this.unselectedScale - 1) * Math.abs(effectsAmount) + 1;
            // Calculate the scale anchor (y anchor can be altered)
            final float translateX = childWidth / 2.0f;
            final float translateY = childHeight * this.scaleDownGravity;
            imageMatrix.preTranslate(-translateX, -translateY);
            imageMatrix.postScale(zoomAmount, zoomAmount);
            imageMatrix.postTranslate(translateX, translateY);
        }

        return true;
    }

    // =============================================================================
    // Public classes
    // =============================================================================

    public static class LayoutParams extends Gallery.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    // =============================================================================
    // Public Adapter classes
    // =============================================================================

    public abstract static class SweetCoverFlowAdapter extends BaseAdapter {

        // =============================================================================
        // Supertype overrides
        // =============================================================================

        @Override
        public final View getView(int position, View convertView, ViewGroup parent) {
            SweetCoverFlow coverFlow = (SweetCoverFlow) parent;

            View wrappedView = null;
            SweetCoverFlowItemWrapper coverFlowItem;

            if (convertView != null) {
                coverFlowItem = (SweetCoverFlowItemWrapper) convertView;
                wrappedView = coverFlowItem.getChildAt(0);
                coverFlowItem.removeAllViews();
            } else {
                coverFlowItem = new SweetCoverFlowItemWrapper(parent.getContext());
            }

            wrappedView = this.getCoverFlowItem(position, wrappedView, parent);

            if (wrappedView == null) {
                throw new NullPointerException("getCoverFlowItem() was expected to return a view, but null was returned.");
            }

            final boolean isReflectionEnabled = coverFlow.isReflectionEnabled();
            coverFlowItem.setReflectionEnabled(isReflectionEnabled);

            if (isReflectionEnabled) {
                coverFlowItem.setReflectionGap(coverFlow.getReflectionGap());
                coverFlowItem.setReflectionRatio(coverFlow.getReflectionRatio());
            }


            coverFlowItem.addView(wrappedView);
            // dnwang;
            //        coverFlowItem.setLayoutParams(wrappedView.getLayoutParams());
            coverFlowItem.setTag(wrappedView.getTag());

            return coverFlowItem;
        }

        // =============================================================================
        // Abstract methods
        // =============================================================================

        public abstract View getCoverFlowItem(int position, View convertView, ViewGroup parent);
    }


    /**
     * This class has only internal use (package scope).
     * <p/>
     * It is responsible for applying additional effects to each coverflow item, that can only be applied at view level
     * (e.g. color saturation).
     * <p/>
     * This is a ViewGroup by intention to enable child views in layouts to stay interactive (like buttons) though
     * transformed.
     * <p/>
     * Since this class is only used within the FancyCoverFlowAdapter it doesn't need to check if there are multiple
     * children or not (there can only be one at all times).
     */
    @SuppressWarnings("ConstantConditions")
    static final class SweetCoverFlowItemWrapper extends ViewGroup {

        // =============================================================================
        // Private members
        // =============================================================================

        private float saturation;

        private boolean isReflectionEnabled = false;

        private float imageReflectionRatio;

        private int reflectionGap;

        private float originalScaledownFactor;

        /**
         * This is a matrix to apply color filters (like saturation) to the wrapped view.
         */
        private ColorMatrix colorMatrix;

        /**
         * This paint is used to draw the wrapped view including any filters.
         */
        private Paint paint;

        /**
         * This is a cache holding the wrapped view's visual representation.
         */
        private Bitmap wrappedViewBitmap;

        /**
         * This canvas is used to let the wrapped view draw it's content.
         */
        private Canvas wrappedViewDrawingCanvas;


        // =============================================================================
        // Constructor
        // =============================================================================

        public SweetCoverFlowItemWrapper(Context context) {
            super(context);
            this.init();
        }

        public SweetCoverFlowItemWrapper(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.init();
        }

        public SweetCoverFlowItemWrapper(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            this.init();
        }

        private void init() {
            this.paint = new Paint();
            this.colorMatrix = new ColorMatrix();
            // TODO: Define a default value for saturation inside an XML.
            this.setSaturation(1);
        }

        // =============================================================================
        // Getters / Setters
        // =============================================================================

        void setReflectionEnabled(boolean hasReflection) {
            if (hasReflection != this.isReflectionEnabled) {
                this.isReflectionEnabled = hasReflection;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    // Turn off hardware acceleration if necessary (reflections won't support it).
                    this.setLayerType(hasReflection ? View.LAYER_TYPE_SOFTWARE : View.LAYER_TYPE_HARDWARE, null);
                }

                this.remeasureChildren();
            }
        }

        void setReflectionRatio(float imageReflectionRatio) {
            if (imageReflectionRatio != this.imageReflectionRatio) {
                this.imageReflectionRatio = imageReflectionRatio;
                this.remeasureChildren();
            }
        }

        void setReflectionGap(int reflectionGap) {
            if (reflectionGap != this.reflectionGap) {
                this.reflectionGap = reflectionGap;
                this.remeasureChildren();
            }
        }

        public void setSaturation(float saturation) {
            if (saturation != this.saturation) {
                this.saturation = saturation;
                this.colorMatrix.setSaturation(saturation);
                this.paint.setColorFilter(new ColorMatrixColorFilter(this.colorMatrix));
            }
        }

        // =============================================================================
        // Supertype overrides
        // =============================================================================

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            this.remeasureChildren();

            // If we have reflection enabled, the original image is scaled down and a reflection is added beneath. Thus,
            // while maintaining the same height the width decreases and we need to adjust measured width.
            // WARNING: This is a hack because we do not obey the EXACTLY MeasureSpec mode that we will get mostly.
            if (this.isReflectionEnabled) {
                this.setMeasuredDimension((int) (this.getMeasuredWidth() * this.originalScaledownFactor), this.getMeasuredHeight());
            } else {
                // dnwang; 添加wrap_content视图大小计算
                int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
                int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
                int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
                int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
                int width = 0;
                int height = 0;
                int lineWidth = 0;
                int lineHeight = 0;
                int cCount = getChildCount();
                for (int i = 0; i < cCount; i++) {
                    View child = getChildAt(i);
                    measureChild(child, widthMeasureSpec, heightMeasureSpec);
                    int childWidth = child.getMeasuredWidth();
                    int childHeight = child.getMeasuredHeight();
                    if (child.getLayoutParams() instanceof MarginLayoutParams) {
                        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                        childWidth += (lp.leftMargin + lp.rightMargin);
                        childHeight += (lp.topMargin + lp.bottomMargin);
                    }
                    if (lineWidth + childWidth > sizeWidth) {
                        width = Math.max(lineWidth, childWidth);
                        lineWidth = childWidth;
                        height += lineHeight;
                        lineHeight = childHeight;
                    } else {
                        lineWidth += childWidth;
                        lineHeight = Math.max(lineHeight, childHeight);
                    }
                    if (i == cCount - 1) {
                        width = Math.max(width, lineWidth);
                        height += lineHeight;
                    }
                }
                setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width, (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);
            }
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (changed) {
                int measuredWidth = this.getMeasuredWidth();
                int measuredHeight = this.getMeasuredHeight();

                if (this.wrappedViewBitmap == null || this.wrappedViewBitmap.getWidth() != measuredWidth || this.wrappedViewBitmap.getHeight() != measuredHeight) {
                    this.wrappedViewBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
                    this.wrappedViewDrawingCanvas = new Canvas(this.wrappedViewBitmap);
                }

                View child = getChildAt(0);
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                int childLeft = (measuredWidth - childWidth) / 2;
                int childRight = measuredWidth - childLeft;
                child.layout(childLeft, 0, childRight, childHeight);
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void dispatchDraw(Canvas canvas) {
            View childView = getChildAt(0);

            if (childView != null) {
                // If on honeycomb or newer, cache the view.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if (childView.isDirty()) {
                        childView.draw(this.wrappedViewDrawingCanvas);

                        if (this.isReflectionEnabled) {
                            this.createReflectedImages();
                        }
                    }
                } else {
                    childView.draw(this.wrappedViewDrawingCanvas);
                }
            }

            canvas.drawBitmap(this.wrappedViewBitmap, (this.getWidth() - childView.getWidth()) / 2, 0, paint);
        }

        // =============================================================================
        // Methods
        // =============================================================================

        private void remeasureChildren() {
            View child = this.getChildAt(0);

            if (child != null) {
                // When reflection is enabled calculate proportional scale down factor.
                final int originalChildHeight = this.getMeasuredHeight();
                this.originalScaledownFactor = this.isReflectionEnabled ? (originalChildHeight * (1 - this.imageReflectionRatio) - reflectionGap) / originalChildHeight : 1.0f;
                final int childHeight = (int) (this.originalScaledownFactor * originalChildHeight);
                final int childWidth = (int) (this.originalScaledownFactor * getMeasuredWidth());

                int heightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST);
                int widthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST);
                this.getChildAt(0).measure(widthSpec, heightSpec);
            }
        }

        /**
         * Creates the reflected images.
         */
        private void createReflectedImages() {

            final int width = this.wrappedViewBitmap.getWidth();
            final int height = this.wrappedViewBitmap.getHeight();


            final Matrix matrix = new Matrix();
            matrix.postScale(1, -1);


            final int scaledDownHeight = (int) (height * originalScaledownFactor);
            final int invertedHeight = height - scaledDownHeight - reflectionGap;
            final int invertedBitmapSourceTop = scaledDownHeight - invertedHeight;
            final Bitmap invertedBitmap = Bitmap.createBitmap(this.wrappedViewBitmap, 0, invertedBitmapSourceTop, width, invertedHeight, matrix, true);

            this.wrappedViewDrawingCanvas.drawBitmap(invertedBitmap, 0, scaledDownHeight + reflectionGap, null);

            final Paint paint = new Paint();
            final LinearGradient shader = new LinearGradient(0, height * imageReflectionRatio + reflectionGap, 0, height, 0x70ffffff, 0x00ffffff, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            this.wrappedViewDrawingCanvas.drawRect(0, height * (1 - imageReflectionRatio), width, height, paint);
        }
    }
}
