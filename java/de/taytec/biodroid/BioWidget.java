package de.taytec.biodroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Implementation of App Widget functionality.
 */
public class BioWidget extends AppWidgetProvider {

    public static final String ACTION_CURRENT_BIRTHDAY_CHANGED = "de.taytec.biodroid.CURRENT_BIRTHDAY";
    public static final String EXTRA_CURRENT_BIRTHDAY = "current_birthday";
    private final Core core = new Core();

    public static void sendBirthdayChangedToWidget(ContextWrapper contextWrapper, Core core) {
        BioLog.d(contextWrapper.getClass().getSimpleName(), "sendBirthdayChangedToWidget()");
        Intent intent = new Intent(ACTION_CURRENT_BIRTHDAY_CHANGED);
        contextWrapper.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, options);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        int width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0);
        int height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0);

        BioLog.d(getClass().getSimpleName(), "onAppWidgetOptionsChanged() + " + appWidgetId + " maxW: " + width + " maxH: " + height);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bio_widget);
        SharedPreferences prefs = context.getSharedPreferences(BioDroidActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        long birthTimestamp = prefs.getLong(BioDroidActivity.PREF_KEY_BIRTHDAY, Long.MIN_VALUE);
        if (birthTimestamp != Long.MIN_VALUE) {
            core.setStartTime(new Date(birthTimestamp));
        }

        Bitmap graphBitmap = Bitmap.createBitmap(
                (int) Graph.calculateDpToPixel((width == 0 ? 110 : width), context),
                (int) Graph.calculateDpToPixel((height == 0 ? 40 : height), context),
                Bitmap.Config.ARGB_8888);
        Graph graph = new Graph(context, core);
        graph.setTextEnabled(false).draw(graphBitmap);
        views.setImageViewBitmap(R.id.wdg_image, graphBitmap);

        SimpleDateFormat df = new SimpleDateFormat(
                context.getResources().getString(R.string.format_date));
        views.setTextViewText(R.id.wdg_birthday, df.format(core.getStartCalendar().getTime()));
        views.setTextViewText(R.id.wdg_date, df.format(core.getEndCalendar().getTime()));
        views.setTextViewText(R.id.wdg_age, Integer.toString(core.getAge()));

        Intent intent = new Intent(context, BioDroidActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.wdg_click, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        BioLog.d(getClass().getSimpleName(), "onReceive()");
        if (intent.getAction().equals(ACTION_CURRENT_BIRTHDAY_CHANGED)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            onUpdate(context, appWidgetManager, ids);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}


