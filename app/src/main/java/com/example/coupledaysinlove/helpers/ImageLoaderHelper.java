package com.example.coupledaysinlove.helpers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.util.TypedValue;
import android.widget.ImageView;

import com.example.coupledaysinlove.R;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoaderHelper {

    private static final int MAX_MEMORY = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int CACHE_SIZE = MAX_MEMORY / 8; // Use 1/8th of available memory for cache

    private static final LruCache<String, Bitmap> BITMAP_CACHE = new LruCache<String, Bitmap>(CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    public static void loadPhotoInto(Context context, String path, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (path == null) {
            showDefaultAvatar(context, imageView);
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            showDefaultAvatar(context, imageView);
            return;
        }

        String cacheKey = path + "_" + file.lastModified();
        Bitmap cached = BITMAP_CACHE.get(cacheKey);

        if (cached != null && !cached.isRecycled()) {
            imageView.setImageBitmap(cached);
            imageView.setImageTintList(null);
            imageView.setPadding(0, 0, 0, 0);
            return;
        }

        // Decode asynchronously or on background thread
        EXECUTOR.execute(() -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    BITMAP_CACHE.put(cacheKey, bitmap);
                    imageView.post(() -> {
                        imageView.setImageBitmap(bitmap);
                        imageView.setImageTintList(null);
                        imageView.setPadding(0, 0, 0, 0);
                    });
                } else {
                    imageView.post(() -> showDefaultAvatar(context, imageView));
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.post(() -> showDefaultAvatar(context, imageView));
            }
        });
    }

    public static void clearCacheForPath(String path) {
        if (path == null) return;
        BITMAP_CACHE.remove(path);
    }

    public static void loadEventPhotoInto(Context context, String path, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (path == null) {
            imageView.setImageDrawable(null);
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            imageView.setImageDrawable(null);
            return;
        }

        String cacheKey = path + "_" + file.lastModified();
        Bitmap cached = BITMAP_CACHE.get(cacheKey);

        if (cached != null && !cached.isRecycled()) {
            imageView.setImageBitmap(cached);
            imageView.setImageTintList(null);
            imageView.setPadding(0, 0, 0, 0);
            return;
        }

        EXECUTOR.execute(() -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    BITMAP_CACHE.put(cacheKey, bitmap);
                    imageView.post(() -> {
                        imageView.setImageBitmap(bitmap);
                        imageView.setImageTintList(null);
                        imageView.setPadding(0, 0, 0, 0);
                    });
                } else {
                    imageView.post(() -> imageView.setImageDrawable(null));
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.post(() -> imageView.setImageDrawable(null));
            }
        });
    }

    private static void showDefaultAvatar(Context context, ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_default_avatar);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.appPrimaryColor, typedValue, true);
        imageView.setImageTintList(ColorStateList.valueOf(typedValue.data));
        int p = (int) (12 * context.getResources().getDisplayMetrics().density);
        imageView.setPadding(p, p, p, p);
    }
}