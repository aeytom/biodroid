/**
 * 
 */
package de.taytec.biodroid;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author tay
 * 
 */
public class BioView extends View {

    interface BioHolder {
	public Calendar getStartCalendar();

	public Calendar getEndCalendar();
	public void bioHolderChanged();
    }

    public static final int HIGHT = 0;
    public static final int CRITICAL = 1;
    public static final int LOW = 2;
    
    public static final int INTELECTUAL = 33;
    public static final int EMOTIONAL = 28;
    public static final int PHYSICAL = 23;
    
    private final int SHOWDAYS = 16;

    private BioHolder holder;
    
    private Paint pPhysical;
    private Paint pEmotional;
    private Paint pIntellectual;
    private Paint pBackground;
    private Paint pGrid;
    private Paint pDay;

    private float touchLastX;
    private float trackballScrollX;


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

    public void setBioHolder(BioHolder holder) {
	this.holder = holder;
    }

    private void initialize() {
	setFocusable(true);
	
	pBackground = new Paint();
	pBackground.setColor(Color.WHITE);
	pBackground.setAlpha(100);
	pBackground.setDither(false);
	
	pGrid = new Paint();
	pGrid.setColor(Color.BLACK);
	pGrid.setDither(false);
	
	pDay = new Paint();
	pDay.setColor(Color.GRAY);
	pDay.setAlpha(128);

	pPhysical = new Paint();
	pPhysical.setColor(getResources().getColor(R.color.clr_phy));
	pPhysical.setAlpha(128);
	
	pEmotional = new Paint();
	pEmotional.setColor(getResources().getColor(R.color.clr_emo));
	pEmotional.setAlpha(160);
	
	pIntellectual = new Paint();
	pIntellectual.setColor(getResources().getColor(R.color.clr_int));
	pIntellectual.setAlpha(196);
    }
    
    public int getPhase(int interval) {
	int di = getAge() % interval;
	if (di == 0 || di == interval/2) {
	    return CRITICAL;
	}
	else if (di < interval/2) {
	    return HIGHT;
	}
	else {
	    return LOW;
	}
    }

    public int getAge() {
	return getDaysSinceAD(holder.getEndCalendar()) - getDaysSinceAD(holder.getStartCalendar());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
	// TODO Auto-generated method stub
	super.onDraw(canvas);
	drawBackground(canvas);

	int age = getAge();
	drawRhythm(canvas, age, INTELECTUAL, pIntellectual);
	drawRhythm(canvas, age, EMOTIONAL, pEmotional);
	drawRhythm(canvas, age, PHYSICAL, pPhysical);
	drawCross(canvas);
    }

    /* (non-Javadoc)
     * @see android.view.View#onKeyUp(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
	Calendar cal = holder.getEndCalendar();
	switch (event.getKeyCode()) {
	case KeyEvent.KEYCODE_DPAD_LEFT:
	    cal.add(Calendar.DAY_OF_YEAR, -1);
	    holder.bioHolderChanged();
	    return true;
	case KeyEvent.KEYCODE_DPAD_RIGHT:
	    cal.add(Calendar.DAY_OF_YEAR, 1);
	    holder.bioHolderChanged();
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
	    double diffX = (touchLastX - x) / getPixelsPerDay();
	    if (Math.abs(diffX) >= 1)
	    {
		Calendar cal = holder.getEndCalendar();
		cal.add(Calendar.DAY_OF_YEAR, (int) diffX);
		holder.bioHolderChanged();
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
	    trackballScrollX = event.getX() * event.getXPrecision();
	    if (Math.abs(trackballScrollX) >= 1.0)
	    {
		Calendar cal = holder.getEndCalendar();
		cal.add(Calendar.DAY_OF_YEAR, (int)trackballScrollX);
		holder.bioHolderChanged();
		trackballScrollX -= (int)trackballScrollX;
		return true;
	    }
	}
	return false;
    }

    protected int getPixelsPerDay() {
	return getWidth() / SHOWDAYS / 2;
    }
    
    protected void drawRhythm(Canvas canvas, int age, int interval, Paint paint) {
	
	int width = getWidth();
	int height = getHeight();
	int base = height / 2;
	int pixelsPerDay = getPixelsPerDay();
	
	Path path = new Path();
	path.moveTo(0, base);
	for (int d = -SHOWDAYS-1; d <= SHOWDAYS; d++)
	{
	    double arc = 2 * Math.PI * ((age + d) % interval) / interval;
	    double y = base - base * Math.sin(arc) * interval / INTELECTUAL;
	    float x = width / 2 + d * pixelsPerDay;
	    path.lineTo(x, (float) y);
	}
	path.lineTo(width-1, base);
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
     * 
     * @param canvas
     */
    private void drawBackground(Canvas canvas) {

	int width = getWidth();
	int height = getHeight();
	int dayOfWeek = holder.getEndCalendar().get(Calendar.DAY_OF_WEEK);
	int pixelPerDay = getPixelsPerDay();
	//pDay.setShader(new LinearGradient(0, 0, pixelPerDay, 0, Color.BLACK,
	//	Color.WHITE, Shader.TileMode.REPEAT));

	// fill background
	pBackground.setShader(new LinearGradient(0, height - 1, 0, 0,
		new int[] { Color.RED, Color.WHITE, Color.GREEN }, null,
		Shader.TileMode.CLAMP));
	canvas.drawPaint(pBackground);

	for (int d = -SHOWDAYS-1; d < SHOWDAYS; d++) {
	    boolean we = ((dayOfWeek + d + 7000) % 7) < 2;
	    if (we) {
		int left = width / 2 + d * pixelPerDay;
		canvas.drawRect(left, 0, left + pixelPerDay, height, pDay);
	    }
	}
    }
 
    /**
     * 
     * @param canvas
     */
    private void drawCross(Canvas canvas) {

	int center = getWidth() / 2;
	int base = getHeight() / 2;
	int pixelPerDay = getPixelsPerDay();

	for (int d = -SHOWDAYS-1; d < SHOWDAYS; d++) {
	    int x = center + d * pixelPerDay;
	    canvas.drawLine(x, base - 5, x, base + 5, pGrid);
	}

	// draw cross
	canvas.drawLine(0, base, getWidth() - 1, base, pGrid);
	canvas.drawLine(center, 0, center, getHeight() - 1, pGrid);
    }

}

