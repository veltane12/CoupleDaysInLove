package com.example.coupledaysinlove.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.example.coupledaysinlove.R;
import com.example.coupledaysinlove.helpers.DateUtils;
import com.example.coupledaysinlove.helpers.PreferencesHelper;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoupleWidgetProvider extends AppWidgetProvider {

    private static final ExecutorService WIDGET_EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Context appContext = context.getApplicationContext();
        WIDGET_EXECUTOR.execute(() -> {
            for (int appWidgetId : appWidgetIds) {
                updateWidget(appContext, appWidgetManager, appWidgetId);
            }
        });
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        PreferencesHelper prefs = new PreferencesHelper(context);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_couple_counter);

        // 1. Determine Theme Mode strictly based on widget dark mode switch
        boolean isDark = prefs.isWidgetDarkModeEnabled();

        // Set widget background & explicit non-dynamic text colors
        views.setInt(R.id.widget_root, "setBackgroundResource", isDark ? R.drawable.bg_widget_card_dark : R.drawable.bg_widget_card);
        views.setTextColor(R.id.tvWidgetCoupleNames, isDark ? 0xFFFCE4EC : 0xFF2C0E18);
        views.setTextColor(R.id.tvWidgetDaysCount, isDark ? 0xFFFF2A6D : 0xFFD81B60);
        views.setTextColor(R.id.tvWidgetDetailedDuration, isDark ? 0xFFF8BBD0 : 0xFF884A5E);

        // 2. Set Names & Counter Text
        String p1 = prefs.getPartner1Name();
        String p2 = prefs.getPartner2Name();
        views.setTextViewText(R.id.tvWidgetCoupleNames, p1 + " & " + p2);

        if (prefs.hasStartDate()) {
            long startMillis = prefs.getStartDateMillis();
            long daysTogether = DateUtils.getDaysTogether(startMillis);
            String detailed = DateUtils.getDetailedDuration(startMillis);

            views.setTextViewText(R.id.tvWidgetDaysCount, daysTogether + " Días");
            views.setTextViewText(R.id.tvWidgetDetailedDuration, detailed);
        } else {
            views.setTextViewText(R.id.tvWidgetDaysCount, "0 Días");
            views.setTextViewText(R.id.tvWidgetDetailedDuration, "Selecciona fecha de inicio");
        }

        // 3. Apply Text Size Scale
        float textScale = prefs.getWidgetTextScale();
        views.setTextViewTextSize(R.id.tvWidgetCoupleNames, TypedValue.COMPLEX_UNIT_SP, 15f * textScale);
        views.setTextViewTextSize(R.id.tvWidgetDaysCount, TypedValue.COMPLEX_UNIT_SP, 28f * textScale);
        views.setTextViewTextSize(R.id.tvWidgetDetailedDuration, TypedValue.COMPLEX_UNIT_SP, 12f * textScale);

        // 4. Apply Photo Size
        int photoSizeDp = prefs.getWidgetPhotoSize();
        int photoSizePx = (int) (photoSizeDp * context.getResources().getDisplayMetrics().density);

        Bitmap p1Bitmap = loadCircularBitmap(context, prefs.getPartner1PhotoPath(), photoSizePx);
        Bitmap p2Bitmap = loadCircularBitmap(context, prefs.getPartner2PhotoPath(), photoSizePx);

        if (p1Bitmap != null) {
            views.setImageViewBitmap(R.id.ivWidgetPartner1Photo, p1Bitmap);
        }
        if (p2Bitmap != null) {
            views.setImageViewBitmap(R.id.ivWidgetPartner2Photo, p2Bitmap);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllWidgets(Context context) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();
        WIDGET_EXECUTOR.execute(() -> {
            try {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(appContext);
                ComponentName componentName = new ComponentName(appContext, CoupleWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    for (int appWidgetId : appWidgetIds) {
                        updateWidget(appContext, appWidgetManager, appWidgetId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Bitmap loadCircularBitmap(Context context, String path, int sizePx) {
        Bitmap rawBitmap = null;
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                rawBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        }

        if (rawBitmap == null) {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_default_avatar);
            if (drawable != null) {
                rawBitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(rawBitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }
        }

        if (rawBitmap == null) return null;

        return getCircleBitmap(rawBitmap, sizePx);
    }

    private static Bitmap getCircleBitmap(Bitmap bitmap, int targetPx) {
        int minSide = Math.min(bitmap.getWidth(), bitmap.getHeight());
        if (targetPx <= 0) targetPx = minSide;

        Bitmap output = Bitmap.createBitmap(targetPx, targetPx, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, targetPx, targetPx);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(targetPx / 2f, targetPx / 2f, targetPx / 2f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        int srcLeft = (bitmap.getWidth() - minSide) / 2;
        int srcTop = (bitmap.getHeight() - minSide) / 2;
        Rect srcRect = new Rect(srcLeft, srcTop, srcLeft + minSide, srcTop + minSide);

        canvas.drawBitmap(bitmap, srcRect, rect, paint);

        return output;
    }
}