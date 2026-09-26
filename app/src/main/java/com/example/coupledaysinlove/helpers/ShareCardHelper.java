package com.example.coupledaysinlove.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.coupledaysinlove.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShareCardHelper {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static void generateAndShareCard(Context context, PreferencesHelper prefsHelper) {
        if (context == null || prefsHelper == null) return;

        if (!prefsHelper.hasStartDate()) {
            Toast.makeText(context, R.string.share_card_no_date_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String partner1Name = prefsHelper.getPartner1Name();
        String partner2Name = prefsHelper.getPartner2Name();
        String partner1PhotoPath = prefsHelper.getPartner1PhotoPath();
        String partner2PhotoPath = prefsHelper.getPartner2PhotoPath();
        long startDateMillis = prefsHelper.getStartDateMillis();

        boolean isDarkMode = prefsHelper.isDarkModeEnabled() ||
                (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        EXECUTOR.execute(() -> {
            try {
                // Resolve dynamic Theme Colors from active theme
                TypedValue tvPrimary = new TypedValue();
                TypedValue tvPrimaryDark = new TypedValue();
                TypedValue tvCardBg = new TypedValue();
                TypedValue tvCardStroke = new TypedValue();
                TypedValue tvTextDark = new TypedValue();
                TypedValue tvTextMuted = new TypedValue();

                context.getTheme().resolveAttribute(R.attr.appPrimaryColor, tvPrimary, true);
                context.getTheme().resolveAttribute(R.attr.appPrimaryDarkColor, tvPrimaryDark, true);
                context.getTheme().resolveAttribute(R.attr.appCardBgColor, tvCardBg, true);
                context.getTheme().resolveAttribute(R.attr.appCardStrokeColor, tvCardStroke, true);
                context.getTheme().resolveAttribute(R.attr.appTextDarkColor, tvTextDark, true);
                context.getTheme().resolveAttribute(R.attr.appTextMutedColor, tvTextMuted, true);

                int primaryColor = tvPrimary.data;
                int primaryDarkColor = tvPrimaryDark.data;
                int cardBgColor = tvCardBg.data;
                int cardStrokeColor = tvCardStroke.data;
                int textDarkColor = tvTextDark.data;
                int textMutedColor = tvTextMuted.data;

                // Inflate card view programmatically
                View cardView = LayoutInflater.from(context).inflate(R.layout.card_anniversary_share, null);

                ShapeableImageView ivPartner1 = cardView.findViewById(R.id.ivSharePartner1Photo);
                ShapeableImageView ivPartner2 = cardView.findViewById(R.id.ivSharePartner2Photo);
                TextView tvNames = cardView.findViewById(R.id.tvShareCoupleNames);
                MaterialCardView cardMilestone = cardView.findViewById(R.id.cardMilestone);
                TextView tvDaysCount = cardView.findViewById(R.id.tvShareDaysCount);
                TextView tvHeadline = cardView.findViewById(R.id.tvShareHeadline);
                TextView tvDetailedDuration = cardView.findViewById(R.id.tvShareDetailedDuration);
                TextView tvStartDate = cardView.findViewById(R.id.tvShareStartDate);

                // Set Photos
                loadPhotoIntoView(context, partner1PhotoPath, ivPartner1, cardBgColor, primaryColor);
                loadPhotoIntoView(context, partner2PhotoPath, ivPartner2, cardBgColor, primaryColor);

                // Set Texts
                tvNames.setText(partner1Name + " & " + partner2Name);

                long daysTogether = DateUtils.getDaysTogether(startDateMillis);
                tvDaysCount.setText(String.valueOf(daysTogether));

                String headline = context.getString(R.string.share_card_headline, daysTogether);
                tvHeadline.setText(headline);

                String detailed = DateUtils.getDetailedDuration(startDateMillis);
                tvDetailedDuration.setText(detailed);

                String formattedDate = DateUtils.getFormattedDate(startDateMillis);
                tvStartDate.setText(context.getString(R.string.since_date_prefix) + " " + formattedDate);

                // Apply Dark Mode or Light Mode theme styling dynamically based on palette
                if (isDarkMode) {
                    cardView.setBackgroundResource(R.drawable.bg_share_card_gradient_dark);

                    if (tvNames != null) {
                        tvNames.setTextColor(Color.parseColor("#FFFFFF"));
                    }

                    if (cardMilestone != null) {
                        cardMilestone.setCardBackgroundColor(cardBgColor);
                        cardMilestone.setStrokeColor(ColorStateList.valueOf(cardStrokeColor));
                    }

                    if (ivPartner1 != null) {
                        ivPartner1.setStrokeColor(ColorStateList.valueOf(cardBgColor));
                    }
                    if (ivPartner2 != null) {
                        ivPartner2.setStrokeColor(ColorStateList.valueOf(cardBgColor));
                    }

                    if (tvDaysCount != null) {
                        tvDaysCount.setTextColor(primaryColor);
                    }
                    if (tvHeadline != null) {
                        tvHeadline.setTextColor(primaryDarkColor);
                    }
                    if (tvDetailedDuration != null) {
                        tvDetailedDuration.setTextColor(textDarkColor);
                    }
                    if (tvStartDate != null) {
                        tvStartDate.setTextColor(textMutedColor);
                    }
                } else {
                    cardView.setBackgroundResource(R.drawable.bg_share_card_gradient);

                    if (tvNames != null) {
                        tvNames.setTextColor(textDarkColor);
                    }

                    if (cardMilestone != null) {
                        cardMilestone.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                        cardMilestone.setStrokeColor(ColorStateList.valueOf(cardStrokeColor));
                    }

                    if (ivPartner1 != null) {
                        ivPartner1.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }
                    if (ivPartner2 != null) {
                        ivPartner2.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }

                    if (tvDaysCount != null) {
                        tvDaysCount.setTextColor(primaryColor);
                    }
                    if (tvHeadline != null) {
                        tvHeadline.setTextColor(primaryDarkColor);
                    }
                    if (tvDetailedDuration != null) {
                        tvDetailedDuration.setTextColor(textDarkColor);
                    }
                    if (tvStartDate != null) {
                        tvStartDate.setTextColor(textMutedColor);
                    }
                }

                // Measure & Layout to compact 1080x1000 pixels
                cardView.measure(
                        View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
                );
                cardView.layout(0, 0, 1080, 1000);

                // Create Bitmap & Draw View
                Bitmap bitmap = Bitmap.createBitmap(1080, 1000, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                cardView.draw(canvas);

                // Save to cache directory for sharing
                File cacheDir = new File(context.getCacheDir(), "shared_images");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }

                File imageFile = new File(cacheDir, "tarjeta_aniversario.png");
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                // Get Uri via FileProvider
                Uri contentUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".fileprovider",
                        imageFile
                );

                // Build Share Intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent chooserIntent = Intent.createChooser(
                        shareIntent,
                        context.getString(R.string.share_card_chooser_title)
                );
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Launch chooser on main UI thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    context.startActivity(chooserIntent);
                });

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Error al generar la tarjeta de aniversario", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private static void loadPhotoIntoView(Context context, String photoPath, ImageView imageView, int cardBgColor, int primaryColor) {
        Bitmap circularBitmap = null;

        if (photoPath != null) {
            File file = new File(photoPath);
            if (file.exists()) {
                Bitmap original = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (original != null) {
                    circularBitmap = getCircularBitmap(original);
                }
            }
        }

        if (circularBitmap != null) {
            imageView.setImageBitmap(circularBitmap);
            imageView.setImageTintList(null);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            // Generate circular default avatar bitmap with matching background
            int size = 280;
            Bitmap avatarBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(avatarBitmap);

            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(cardBgColor);
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint);

            Drawable avatarDrawable = ContextCompat.getDrawable(context, R.drawable.ic_default_avatar);
            if (avatarDrawable != null) {
                Drawable tinted = avatarDrawable.mutate();
                tinted.setTint(primaryColor);
                int padding = 60;
                tinted.setBounds(padding, padding, size - padding, size - padding);
                tinted.draw(canvas);
            }

            imageView.setImageBitmap(avatarBitmap);
            imageView.setImageTintList(null);
            imageView.setPadding(0, 0, 0, 0);
        }
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int x = (bitmap.getWidth() - size) / 2;
        int y = (bitmap.getHeight() - size) / 2;
        Rect srcRect = new Rect(x, y, x + size, y + size);
        Rect dstRect = new Rect(0, 0, size, size);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        return output;
    }
}