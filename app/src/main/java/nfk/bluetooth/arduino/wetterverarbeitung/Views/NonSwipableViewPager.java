package nfk.bluetooth.arduino.wetterverarbeitung.Views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Niklas on 03.03.2017.
 */

public class NonSwipableViewPager extends ViewPager {

    private boolean swipeable;


    public NonSwipableViewPager(Context context) {
        super(context);
    }

    public NonSwipableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
    }

    public boolean isSwipeable() {
        return this.swipeable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return (isSwipeable()) && super.onInterceptTouchEvent(arg0);
    }
}
