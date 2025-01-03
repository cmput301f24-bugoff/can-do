package com.bugoff.can_do.user;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.List;

/**
 * Fragment for scanning QR codes. This fragment uses the ZXing library to scan QR codes using the device's camera.
 */
public class QrCodeScannerFragment extends Fragment {
    private static final String TAG = "QrCodeScannerFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private DecoratedBarcodeView barcodeView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_code_scanner, container, false);
        barcodeView = view.findViewById(R.id.zxing_barcode_scanner);
        barcodeView.getStatusView().setVisibility(View.GONE);
        barcodeView.getViewFinder().setLaserVisibility(false);

        // Check if the camera permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            initializeScanner();
        }

        return view;
    }

    /**
     * Initializes the barcode scanner with the QR code format and starts scanning for QR codes.
     */
    private void initializeScanner() {
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(List.of(BarcodeFormat.valueOf("QR_CODE"))));
        barcodeView.decodeContinuous(callback);
    }

    /**
     * Called when the user responds to a permission request. If the user grants the camera permission, the barcode scanner
     * is initialized.
     */
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                Log.d(TAG, "Scanned: " + result.getText());
                barcodeView.pause();
                HandleQRScan.processQRCode(result.getText(), getActivity());

            }
        }

        @Override
        public void possibleResultPoints(List resultPoints) {}
    };

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
