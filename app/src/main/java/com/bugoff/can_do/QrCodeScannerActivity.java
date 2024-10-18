package com.bugoff.can_do;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.List;

public class QrCodeScannerActivity extends AppCompatActivity {
    private static final String TAG = "QrCodeScannerActivity";
    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scanner);

        // Set up the barcode scanner view
        barcodeView = findViewById(R.id.zxing_barcode_scanner);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(List.of(BarcodeFormat.valueOf("QR_CODE"))));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback);


        // Set up back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());  // Closes the activity and returns to the previous screen
    }

    // Handle the result of continuous scan
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                Log.d(TAG, "Scanned: " + result.getText());
                // Handle the scanned QR code here
                // e.g., display it or process the result
            }
        }

        @Override
        public void possibleResultPoints(java.util.List resultPoints) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();  // Resume scanning
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();  // Pause scanning when the activity is not in focus
    }
}
