package de.taytec.biodroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by tay on 13.03.15.
 */
public class Graph {

    public static final float STROKE_WIDTH = 1.5f;

    private Context context = null;
    private Core core = null;

    private final Paint pBackground = new Paint();
    private final Paint pGrid = new Paint();
    private final Paint pDate = new Paint();
    private final Paint pDay = new Paint();
    private final Paint pPhysical = new Paint();
    private final Paint pEmotional = new Paint();
    private final Paint pIntellectual = new Paint();
    private View view = null;

    private Canvas canvas;
    private int width;
    private int height;

    private int mShowDays = 16;
    private float textSize = 12f;
    private boolean isTextEnabled = true;

    public Graph(Context context, Core core) {
        initialize(context, core);
    }

    public Graph(Context context, Core core, View view) {
        initialize(context, core);
        this.view = view;
    }

    public static float calculateDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    private void initialize(Context context, Core core) {
        this.context = context;
        this.core = core;

        pBackground.setColor(Color.WHITE);
        pBackground.setAlpha(100);
        pBackground.setDither(false);

        pGrid.setColor(Color.BLACK);
        pGrid.setDither(false);
        pGrid.setStrokeWidth(3.0f);

        pDate.setColor(Color.BLACK);
        pDate.setDither(false);
        pDate.setStrokeWidth(0);
        pDate.setColor(Color.WHITE);

        pDay.setColor(Color.GRAY);
        pDay.setAlpha(128);

        pPhysical.setColor(context.getResources().getColor(R.color.clr_phy));
        pPhysical.setStyle(Paint.Style.STROKE);
        pPhysical.setStrokeWidth(calculateDpToPixel(STROKE_WIDTH, context));

        pEmotional.setColor(context.getResources().getColor(R.color.clr_emo));
        pEmotional.setStyle(Paint.Style.STROKE);
        pEmotional.setStrokeWidth(calculateDpToPixel(STROKE_WIDTH, context));

        pIntellectual.setColor(context.getResources().getColor(R.color.clr_int));
        pIntellectual.setStyle(Paint.Style.STROKE);
        pIntellectual.setStrokeWidth(calculateDpToPixel(STROKE_WIDTH, context));
    }

    public void draw(View view, Canvas canvas) {
        this.canvas = canvas;
        height = view.getHeight();
        width = view.getWidth();
        draw();
    }

    public void draw(Bitmap bitmap) {
        canvas = new Canvas(bitmap);
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        draw();
    }

    private void draw() {
        drawBackground();
        drawCross();
        drawRhythm(core.getAge(), Core.IVAL_INTELECTUAL, pIntellectual, context.getResources().getString(R.string.tab_int));
        drawRhythm(core.getAge(), Core.IVAL_EMOTIONAL, pEmotional, context.getResources().getString(R.string.tab_emo));
        drawRhythm(core.getAge(), Core.IVAL_PHYSICAL, pPhysical, context.getResources().getString(R.string.tab_phy));
    }

    public void drawRhythm(int age, int interval, Paint paint, String text) {
        int baseHeight = getGraphHeight() / 2;
        int pixelsPerDay = getPixelsPerDay();
        Paint textPaint = new Paint(paint);
        textPaint.setStrokeWidth(0);
        textPaint.setTextSize(textSize);

        Path path = new Path();
        path.moveTo(0, baseHeight);
        for (int d = -mShowDays - 1; d <= mShowDays; d++) {
            double arc = 2 * Math.PI * ((age + d) % interval) / interval;
            double y = baseHeight - baseHeight * Math.sin(arc) * interval / Core.IVAL_INTELECTUAL * 0.9;
            float x = getWidth() / 2 + d * pixelsPerDay;
            path.lineTo(x, (float) y);
            if (0 == d && isTextEnabled()) {
                canvas.drawText(text, getWidth()/2 + 5, (float) y, textPaint);
            }
        }
        canvas.drawPath(path, paint);

    }

    public void setTextSize(float textSize) {
        this.textSize = textSize * 0.8f;
    }

    protected int getPixelsPerDay() {
        mShowDays = 10 * getWidth() / getHeight();
        return getWidth() / mShowDays / 2;
    }

    private void drawBackground() {
        int dayOfWeek = core.getEndCalendar().get(Calendar.DAY_OF_WEEK);
        int pixelPerDay = getPixelsPerDay();
        int graphHeight = getGraphHeight() - 1;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = null;

        if (isTextEnabled()) {
            cal.setTime(core.getEndCalendar().getTime());
            cal.add(Calendar.DATE, -mShowDays - 1);
            pDate.setTextSize(textSize);
            df = new SimpleDateFormat(
                    context.getResources().getString(R.string.format_date_short));
        }

        // fill background
        pBackground.setShader(new LinearGradient(0, graphHeight, 0, 0,
                new int[]{Color.RED, Color.WHITE, Color.GREEN}, null,
                Shader.TileMode.CLAMP));
        canvas.drawPaint(pBackground);

        for (int d = -mShowDays - 1; d < mShowDays; d++) {
            boolean we = ((dayOfWeek + d + 7000) % 7) < 2;
            if (we) {
                int left = getWidth() / 2 + d * pixelPerDay;
                canvas.drawRect(left, 0, left + pixelPerDay, graphHeight, pDay);
                if (isTextEnabled() && 0 == ((dayOfWeek + d + 7000) % 7)) {
                    canvas.drawText(df.format(cal.getTime()), left, getHeight() - 3, pDate);
                }
            }
            cal.add(Calendar.DATE, 1);
        }
    }

    private void drawCross() {

        int center = getWidth() / 2;
        int baseHeight = getGraphHeight() / 2;
        int pixelPerDay = getPixelsPerDay();

        if (pixelPerDay > 15) {
            for (int d = -mShowDays - 1; d < mShowDays; d++) {
                int x = center + d * pixelPerDay;
                canvas.drawLine(x, baseHeight - 10, x, baseHeight + 10, pGrid);
            }
        }

        // draw cross
        canvas.drawLine(0, baseHeight, getWidth() - 1, baseHeight, pGrid);
        canvas.drawLine(center, 0, center, getGraphHeight() - 1, pGrid);
    }

    protected final int getWidth() {
        return view == null ? width : view.getWidth();
    }

    protected final int getHeight() {
        return view == null ? height :view.getHeight();
    }

    protected int getGraphHeight() {
        return getHeight() - (isTextEnabled() ? (int)textSize + 6 : 0);
    }

    boolean isTextEnabled() {
        return isTextEnabled && getWidth() > 400 && getHeight() > 300;
    }

    public Graph setTextEnabled(boolean isTextEnabled) {
        this.isTextEnabled = isTextEnabled;
        return this;
    }

    public Graph setBackgroundAlpha(int alpha) {
        pBackground.setAlpha(alpha);
        return this;
    }
}
