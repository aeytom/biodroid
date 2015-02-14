/**
 *
 */
package de.taytec.biodroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author tay
 */
public class BioView extends View {


    public static final float STROKE_WIDTH = 6.0f;
    private float textSize = 12f;

    public void setTextSize(float textSize) {
        this.textSize = textSize * 0.8f;
    }

    interface BioCalendarProvider {
        /**
         * get the birthday date
         *
         * @return
         */
        public Calendar getStartCalendar();

        /**
         * get the destination date
         *
         * @return
         */
        public Calendar getEndCalendar();

        /**
         * called if the calendars changed by the view (scrolling around)
         */
        public void onScroll();
    }

    /**
     * dummy calender provider for the view
     */
    private class BioCalendarDefault implements BioCalendarProvider {
        final Calendar start;
        final Calendar end;
        public BioCalendarDefault() {
            start = Calendar.getInstance();
            start.set(1968,9,27);
            end = Calendar.getInstance();
        }
        @Override
        public Calendar getStartCalendar() {
            return start;
        }

        @Override
        public Calendar getEndCalendar() {
            return end;
        }

        @Override
        public void onScroll() {
        }
    }

    public static final int HEIGHT = 0;
    public static final int CRITICAL = 1;
    public static final int LOW = 2;

    public static final int IVAL_INTELECTUAL = 33;
    public static final int IVAL_EMOTIONAL = 28;
    public static final int IVAL_PHYSICAL = 23;

    private BioCalendarProvider calendarProvider;

    private Paint pPhysical;
    private Paint pEmotional;
    private Paint pIntellectual;
    private Paint pBackground;
    private Paint pGrid;
    private Paint pDay;
    private Paint pDate;

    private int mAge;
    private int mShowDays = 16;
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

    public void setCalendarProvider(BioCalendarProvider holder) {
        this.calendarProvider = holder;
    }

    private void initialize() {
        setFocusable(true);

        calendarProvider = new BioCalendarDefault();
        mAge = computeAge();

        pBackground = new Paint();
        pBackground.setColor(Color.WHITE);
        pBackground.setAlpha(100);
        pBackground.setDither(false);

        pGrid = new Paint();
        pGrid.setColor(Color.BLACK);
        pGrid.setDither(false);
        pGrid.setStrokeWidth(3.0f);

        pDate = new Paint(pGrid);
        pDate.setStrokeWidth(0);
        pDate.setColor(Color.WHITE);

        pDay = new Paint();
        pDay.setColor(Color.GRAY);
        pDay.setAlpha(128);

        pPhysical = new Paint();
        pPhysical.setColor(getResources().getColor(R.color.clr_phy));
        pPhysical.setStyle(Paint.Style.STROKE);
        pPhysical.setStrokeWidth(STROKE_WIDTH);

        pEmotional = new Paint();
        pEmotional.setColor(getResources().getColor(R.color.clr_emo));
        pEmotional.setStyle(Paint.Style.STROKE);
        pEmotional.setStrokeWidth(STROKE_WIDTH);

        pIntellectual = new Paint();
        pIntellectual.setColor(getResources().getColor(R.color.clr_int));
        pIntellectual.setStyle(Paint.Style.STROKE);
        pIntellectual.setStrokeWidth(STROKE_WIDTH);
    }

    public int getPhase(int interval) {
        int di = mAge % interval;
        if (di == 0 || di == interval / 2) {
            return CRITICAL;
        } else if (di < interval / 2) {
            return HEIGHT;
        } else {
            return LOW;
        }
    }

    protected int computeAge() {
        return getDaysSinceAD(calendarProvider.getEndCalendar()) - getDaysSinceAD(calendarProvider.getStartCalendar());
    }

    public int getAge() {
        return mAge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawCross(canvas);

        mAge = computeAge();
        drawRhythm(canvas, mAge, IVAL_INTELECTUAL, pIntellectual, getResources().getString(R.string.tab_int));
        drawRhythm(canvas, mAge, IVAL_EMOTIONAL, pEmotional, getResources().getString(R.string.tab_emo));
        drawRhythm(canvas, mAge, IVAL_PHYSICAL, pPhysical, getResources().getString(R.string.tab_phy));
    }

    /* (non-Javadoc)
     * @see android.view.View#onKeyUp(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Calendar cal = calendarProvider.getEndCalendar();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                cal.add(Calendar.DAY_OF_YEAR, -1);
                onCalendarChanged();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                cal.add(Calendar.DAY_OF_YEAR, 1);
                onCalendarChanged();
                return true;
        }
        return false;
    }

    private void onCalendarChanged() {
        mAge = computeAge();
        calendarProvider.onScroll();
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
                double diffX = (touchLastX - x) / getPixelsPerDay();
                if (Math.abs(diffX) >= 1) {
                    Calendar cal = calendarProvider.getEndCalendar();
                    cal.add(Calendar.DAY_OF_YEAR, (int) diffX);
                    onCalendarChanged();
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
                Calendar cal = calendarProvider.getEndCalendar();
                cal.add(Calendar.DAY_OF_YEAR, (int) trackballScrollX);
                onCalendarChanged();
                trackballScrollX -= (int) trackballScrollX;
                return true;
            }
        }
        return false;
    }

    protected int getPixelsPerDay() {
        mShowDays = 10 * getWidth() / getHeight();
        return getWidth() / mShowDays / 2;
    }

    protected void drawRhythm(Canvas canvas, int age, int interval, Paint paint, String text) {

        int width = getWidth();
        int height = getHeight();
        int base = height / 2;
        int pixelsPerDay = getPixelsPerDay();
        Paint textPaint = new Paint(paint);
        textPaint.setStrokeWidth(0);
        textPaint.setTextSize(textSize);

        Path path = new Path();
        path.moveTo(0, base);
        for (int d = -mShowDays - 1; d <= mShowDays; d++) {
            double arc = 2 * Math.PI * ((age + d) % interval) / interval;
            double y = base - base * Math.sin(arc) * interval / IVAL_INTELECTUAL * 0.9;
            float x = width / 2 + d * pixelsPerDay;
            path.lineTo(x, (float) y);
            if (0 == d) {
                canvas.drawText(text, width/2 + 5, (float) y, textPaint);
            }
        }
//        path.lineTo(width - 1, base);
        canvas.drawPath(path, paint);
    }

    /**
     * Anzahl der Tage nach dem 1.1.0000 zurückgeben
     */
    public int getDaysSinceAD(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int month = 1 + cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (month < 3) {
            year--;
            month += 12;
        }
        return day + (int) ((month + 1) * 30.6) + (int) (year * 365.25);
    }

    /**
     * @param canvas
     */
    private void drawBackground(Canvas canvas) {

        int width = getWidth();
        int height = getHeight();
        int dayOfWeek = calendarProvider.getEndCalendar().get(Calendar.DAY_OF_WEEK);
        int pixelPerDay = getPixelsPerDay();
        pDate.setTextSize(textSize);
        SimpleDateFormat df = new SimpleDateFormat(
                getResources().getString(R.string.format_date_short));
        Calendar cal = Calendar.getInstance();
        cal.setTime(calendarProvider.getEndCalendar().getTime());
        cal.add(Calendar.DATE, -mShowDays-1);

        // fill background
        pBackground.setShader(new LinearGradient(0, height - 1, 0, 0,
                new int[]{Color.RED, Color.WHITE, Color.GREEN}, null,
                Shader.TileMode.CLAMP));
        canvas.drawPaint(pBackground);

        for (int d = -mShowDays - 1; d < mShowDays; d++) {
            boolean we = ((dayOfWeek + d + 7000) % 7) < 2;
            if (we) {
                int left = width / 2 + d * pixelPerDay;
                canvas.drawRect(left, 0, left + pixelPerDay, height - pixelPerDay - 6, pDay);
                if (0 == ((dayOfWeek + d + 7000) % 7)) {
                    canvas.drawText(df.format(cal.getTime()), left, height - 3, pDate);
                }
            }
            cal.add(Calendar.DATE, 1);
        }
    }

    /**
     * @param canvas
     */
    private void drawCross(Canvas canvas) {

        int center = getWidth() / 2;
        int base = getHeight() / 2;
        int pixelPerDay = getPixelsPerDay();

        for (int d = -mShowDays - 1; d < mShowDays; d++) {
            int x = center + d * pixelPerDay;
            canvas.drawLine(x, base - 10, x, base + 10, pGrid);
        }

        // draw cross
        canvas.drawLine(0, base, getWidth() - 1, base, pGrid);
        canvas.drawLine(center, 0, center, getHeight() - 1, pGrid);
    }

}

