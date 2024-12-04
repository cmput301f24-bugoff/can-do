package com.bugoff.can_do;

import static com.bugoff.can_do.ImageUtils.calculateScaleForDimensions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

import android.graphics.Bitmap;
import android.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageUtilsTest {
    @Mock
    private Bitmap mockBitmap;

    @Test
    void testGetBase64ImageSize() {
        // Test null input
        assertEquals(0, ImageUtils.getBase64ImageSize(null));
        // Test empty string
        assertEquals(0, ImageUtils.getBase64ImageSize(""));
        // Test known size string
        String testString = "SGVsbG8gV29ybGQ="; // "Hello World" in base64
        assertEquals(12, ImageUtils.getBase64ImageSize(testString)); // Base64 encoded "Hello World" has length 16
    }

    @Test
    void testDecodeBase64Image() {
        // Test invalid base64 string
        assertNull(ImageUtils.decodeBase64Image("invalid-base64"));

        // Test null input
        assertNull(ImageUtils.decodeBase64Image(null));

        // Test valid but non-image base64 string
        String validBase64 = Base64.encodeToString("not an image".getBytes(), Base64.DEFAULT);
        assertNull(ImageUtils.decodeBase64Image(validBase64));
    }

    @Test
    void testGenerateDefaultAvatar() {
        // First, mock the static Bitmap.createBitmap method
        try (MockedStatic<Bitmap> mockedBitmap = mockStatic(Bitmap.class)) {
            // Set up the static mock to return our mockBitmap
            mockedBitmap.when(() ->
                    Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            ).thenReturn(mockBitmap);

            Bitmap result = ImageUtils.generateDefaultAvatar("A");

            // Verify result
            assertNotNull(result);
            assertEquals(mockBitmap, result);

            // verify correct parameters
            mockedBitmap.verify(() ->
                    Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            );
        }
    }

    @Test
    void testGenerateDefaultAvatarWithNullLetter() {
        try (MockedStatic<Bitmap> mockedBitmap = mockStatic(Bitmap.class)) {
            mockedBitmap.when(() ->
                    Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            ).thenReturn(mockBitmap);

            Bitmap result = ImageUtils.generateDefaultAvatar(null);

            assertNotNull(result);
            assertEquals(mockBitmap, result);
        }
    }

    @Test
    void testGenerateDefaultAvatarWithEmptyLetter() {
        try (MockedStatic<Bitmap> mockedBitmap = mockStatic(Bitmap.class)) {
            mockedBitmap.when(() ->
                    Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            ).thenReturn(mockBitmap);

            Bitmap result = ImageUtils.generateDefaultAvatar("");

            assertNotNull(result);
            assertEquals(mockBitmap, result);
        }
    }

    @Test
    void testCalculateScaleForDimensions() {
        // Test scale calculation
        int width = 1600;
        int height = 1200;
        float expectedScale = 0.5f; // 800/1600 = 0.5

        float scale = calculateScaleForDimensions(width, height);

        assertEquals(expectedScale, scale, 0.001f);
    }
}