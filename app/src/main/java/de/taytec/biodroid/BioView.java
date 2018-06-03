/**
 *
 */
package de.taytec.biodroid;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

/**
 * @author tay
 */
public class BioView extends View {

    private Graph graph;

    private Core core = new Core();
    private float touchLastX;


    public BioView(Context context) {
        super(context);
        initialize();
    }

    public BioView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public BioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void setCore(Core holder) {
        this.core = holder;
    }

    private void initialize() {
        setFocusable(true);
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        getGraph().draw(this, canvas);
    }

    /* (non-Javadoc)
     * @see android.view.View#onKeyUp(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Calendar cal = core.getEndCalendar();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                core.add(Calendar.DAY_OF_YEAR, -1);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                core.add(Calendar.DAY_OF_YEAR, 1);
                return true;
        }
        return false;
    }

    /* (non-Javadoc)
         * @see android.view.View#onTouchEvent(android.view.MotionEvent)
         */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchLastX = x;
                return true;
            case MotionEvent.ACTION_MOVE:
                double diffX = (touchLastX - x) / graph.getPixelsPerDay();
                if (Math.abs(diffX) >= 1) {
                    core.add(Calendar.DAY_OF_YEAR, (int) diffX);
                    touchLastX = x;
                    return true;
                }
                break;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see android.view.View#onTrackballEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float trackballScrollX = event.getX() * event.getXPrecision();
            if (Math.abs(trackballScrollX) >= 1.0) {
                core.add(Calendar.DAY_OF_YEAR, (int) trackballScrollX);
                trackballScrollX -= (int) trackballScrollX;
                return true;
            }
        }
        return false;
    }

    public void setTextSize(float textSize) {
        getGraph().setTextSize(textSize);
    }

    protected Graph getGraph() {
        if (null == graph) {
            graph = new Graph(getContext(), core);
        }
        return graph;
    }

}

