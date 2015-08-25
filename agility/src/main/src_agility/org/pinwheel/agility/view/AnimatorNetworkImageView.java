package org.pinwheel.agility.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.volley.toolbox.NetworkImageView;
import org.pinwheel.agility.animation.TouchAnimator;

/**
 * 版权所有 (C), 2014, 北京视达科科技有限公司<br>
 * <br>
 * All rights reserved
 *
 * @author dnwang
 * @date 2015/3/11 14:15
 * @description
 */
public class AnimatorNetworkImageView extends NetworkImageView {

    protected TouchAnimator touchAnimator;

    public AnimatorNetworkImageView(Context context) {
        super(context);
        init(context);
    }

    public AnimatorNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnimatorNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        touchAnimator = new TouchAnimator(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (touchAnimator != null) {
            touchAnimator.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

}