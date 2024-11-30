package com.bugoff.can_do;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility class for handling image compression and conversion operations.
 * Provides methods to compress images and convert between different formats.
 */
public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_DIMENSION = 400;
    private static final int COMPRESSION_QUALITY = 50; // JPEG compression quality (0-100)

    /**
     * Compresses and encodes an image URI to a Base64 string.
     * The image is scaled down and compressed to reduce size while maintaining reasonable quality.
     *
     * @param context Context needed to access content resolver
     * @param imageUri URI of the image to compress
     * @return Base64 encoded string of the compressed image, or null if compression fails
     */
    public static String compressAndEncodeImage(Context context, Uri imageUri) {
        try {
            // Get bitmap from Uri
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            return compressAndEncodeBitmap(bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    /**
     * Compresses and encodes a Bitmap to a Base64 string.
     *
     * @param bitmap Bitmap to compress and encode
     * @return Base64 encoded string of the compressed image, or null if compression fails
     */
    public static String compressAndEncodeBitmap(Bitmap bitmap) {
        try {
            // Scale down the image
            float scale = Math.min(
                    ((float) MAX_DIMENSION) / bitmap.getWidth(),
                    ((float) MAX_DIMENSION) / bitmap.getHeight()
            );

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            // Create scaled bitmap
            Bitmap scaledBitmap = Bitmap.createBitmap(
                    bitmap,
                    0, 0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
            );

            // Convert to low quality JPEG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream);

            // Convert to Base64
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (Exception e) {
            Log.e(TAG, "Error compressing bitmap", e);
            return null;
        }
    }

    /**
     * Decodes a Base64 string back to a Bitmap.
     *
     * @param base64Image Base64 encoded string of the image
     * @return Decoded Bitmap, or null if decoding fails
     */
    public static Bitmap decodeBase64Image(String base64Image) {
        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding base64 image", e);
            return null;
        }
    }

    /**
     * Gets the approximate size in bytes of a Base64 encoded image string.
     *
     * @param base64Image Base64 encoded string of the image
     * @return Approximate size in bytes
     */
    public static int getBase64ImageSize(String base64Image) {
        if (base64Image == null) return 0;
        return base64Image.length() * 3/4;
    }
}