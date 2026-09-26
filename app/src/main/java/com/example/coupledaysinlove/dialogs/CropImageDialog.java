package com.example.coupledaysinlove.dialogs;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.coupledaysinlove.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CropImageDialog {

    private static final String TAG = "CropImageDialog";

    public interface OnPhotoCroppedListener {
        void onPhotoCropped(String savedFilePath);
    }

    public static void show(Context context, Uri sourceUri, String outputFileName, OnPhotoCroppedListener listener) {
        show(context, sourceUri, outputFileName, 260, 260, listener);
    }

    public static void show(Context context, Uri sourceUri, String outputFileName, int frameWidthDp, int frameHeightDp, OnPhotoCroppedListener listener) {
        File tempFile = null;
        try {
            if (sourceUri == null) {
                Toast.makeText(context, "Imagen no válida", Toast.LENGTH_SHORT).show();
                return;
            }

            // Copy URI stream ONCE to a temporary cache file to avoid multi-open ContentProvider security exceptions
            tempFile = copyUriToCacheFile(context, sourceUri);
            if (tempFile == null || !tempFile.exists() || tempFile.length() == 0) {
                Toast.makeText(context, "No se pudo acceder al archivo de imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap loadedBitmap = decodeSampledBitmapFromFile(tempFile, 1200, 1200);

            if (loadedBitmap == null) {
                Toast.makeText(context, "Formato de imagen no soportado", Toast.LENGTH_SHORT).show();
                if (tempFile.exists()) tempFile.delete();
                return;
            }

            final Bitmap originalBitmap = rotateBitmapIfRequired(tempFile, loadedBitmap);

            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_crop_image, null);

            View cardCropFrame = dialogView.findViewById(R.id.cardCropFrame);
            if (cardCropFrame != null && frameWidthDp > 0 && frameHeightDp > 0) {
                float density = context.getResources().getDisplayMetrics().density;
                ViewGroup.LayoutParams lp = cardCropFrame.getLayoutParams();
                if (lp != null) {
                    lp.width = (int) (frameWidthDp * density);
                    lp.height = (int) (frameHeightDp * density);
                    cardCropFrame.setLayoutParams(lp);
                }
            }

            TouchCropImageView ivPreview = dialogView.findViewById(R.id.ivCropPreview);
            View btnRotate = dialogView.findViewById(R.id.btnRotateCrop);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelCrop);
            MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveCrop);

            ivPreview.setBitmap(originalBitmap);

            if (btnRotate != null) {
                btnRotate.setOnClickListener(v -> ivPreview.rotateImage90());
            }

            final File finalTempFile = tempFile;

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(true)
                    .setOnDismissListener(d -> {
                        if (finalTempFile != null && finalTempFile.exists()) {
                            finalTempFile.delete();
                        }
                    })
                    .create();

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnSave.setOnClickListener(v -> {
                Bitmap croppedBitmap = ivPreview.getCroppedBitmap();
                if (croppedBitmap == null) {
                    croppedBitmap = originalBitmap;
                }

                try {
                    File file = new File(context.getFilesDir(), outputFileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos);
                    fos.flush();
                    fos.close();

                    if (listener != null) {
                        listener.onPhotoCropped(file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error saving cropped image", e);
                    Toast.makeText(context, "Error al guardar foto", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });

            dialog.show();

        } catch (Throwable t) {
            Log.e(TAG, "Error processing image", t);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            Toast.makeText(context, "No se pudo procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private static File copyUriToCacheFile(Context context, Uri uri) {
        InputStream inputStream = null;
        ParcelFileDescriptor pfd = null;
        AssetFileDescriptor afd = null;

        try {
            ContentResolver resolver = context.getContentResolver();

            // Method 1: openInputStream
            try {
                inputStream = resolver.openInputStream(uri);
            } catch (Exception ignored) {}

            // Method 2: openFileDescriptor
            if (inputStream == null) {
                try {
                    pfd = resolver.openFileDescriptor(uri, "r");
                    if (pfd != null) {
                        inputStream = new FileInputStream(pfd.getFileDescriptor());
                    }
                } catch (Exception ignored) {}
            }

            // Method 3: openAssetFileDescriptor
            if (inputStream == null) {
                try {
                    afd = resolver.openAssetFileDescriptor(uri, "r");
                    if (afd != null) {
                        inputStream = afd.createInputStream();
                    }
                } catch (Exception ignored) {}
            }

            if (inputStream == null) {
                Log.e(TAG, "All input stream resolution methods failed for URI: " + uri);
                return null;
            }

            File tempFile = new File(context.getCacheDir(), "temp_crop_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[16384];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            if (pfd != null) pfd.close();
            if (afd != null) afd.close();

            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy URI to cache file", e);
            if (pfd != null) try { pfd.close(); } catch (Exception ignored) {}
            if (afd != null) try { afd.close(); } catch (Exception ignored) {}
            if (inputStream != null) try { inputStream.close(); } catch (Exception ignored) {}
            return null;
        }
    }

    private static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight) {
        if (file == null || !file.exists()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null;
        }

        int sampleSize = 1;
        int width = options.outWidth;
        int height = options.outHeight;

        while (width / sampleSize > reqWidth || height / sampleSize > reqHeight) {
            sampleSize *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    private static Bitmap rotateBitmapIfRequired(File file, Bitmap bitmap) {
        if (file == null || !file.exists() || bitmap == null) return bitmap;

        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                rotationAngle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                rotationAngle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                rotationAngle = 270;
            }

            if (rotationAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to check EXIF rotation", e);
        }
        return bitmap;
    }
}