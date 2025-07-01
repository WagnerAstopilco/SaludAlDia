package com.example.saludaldia.ui.OCR;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.example.saludaldia.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCapture";
    private static final String TESS_DATA = "/tessdata";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private TessBaseAPI tessBaseAPI;
    private PreviewView previewView;
    private TextView resultText;
    private ImageView imageView;
    private ImageCapture imageCapture;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setupCamera();
                    initTesseract();
                } else {
                    Toast.makeText(this, "Permiso de cámara requerido", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_capture);

        previewView = findViewById(R.id.previewView);
        resultText = findViewById(R.id.textResult);
        imageView = findViewById(R.id.imageView);
        Button btnCapture = findViewById(R.id.btnCapture);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            setupCamera();
            initTesseract();
        }
        btnCapture.setOnClickListener(v -> capturePhoto());
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        File photoFile = new File(getFilesDir(), "capture.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        imageView.setImageBitmap(bitmap);
                        Log.e(TAG, "imagen guardada correctamente");
                        runOCR(bitmap);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Error capturando imagen", exception);
                    }
                });
    }

    private void runOCR(Bitmap bitmap) {
        if (tessBaseAPI != null) {
            tessBaseAPI.setImage(bitmap);
            String ocrResult = tessBaseAPI.getUTF8Text();
            resultText.setText(ocrResult);
            OcrToTreatmentParser.processOcrText(ocrResult, new OcrToTreatmentParser.OcrParseCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(OcrCaptureActivity.this, "Datos guardados correctamente", Toast.LENGTH_LONG).show();
                    resultText.setText("Tratamiento, medicamentos y recordatorios guardados correctamente.");
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OcrCaptureActivity.this, "Error al procesar texto OCR", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "OCR parse failed", e);
                }
            });

            tessBaseAPI.clear();
        }
    }

    private String findLineValue(String text, String key) {
        for (String line : text.split("\n")) {
            if (line.toLowerCase().contains(key.toLowerCase())) {
                int index = line.indexOf(":");
                if (index != -1 && index + 1 < line.length()) {
                    return line.substring(index + 1).trim();
                }
            }
        }
        return null;
    }


    private void initTesseract() {
        try {
            File dir = getFilesDir();
            File tessDataDir = new File(dir + TESS_DATA);
            if (!tessDataDir.exists()) {
                tessDataDir.mkdirs();
                copyTessDataFiles("spa.traineddata");
            }

            tessBaseAPI = new TessBaseAPI();
            boolean initSuccess = tessBaseAPI.init(dir.getAbsolutePath(), "spa");
            Log.d(TAG, "Tesseract init success: " + initSuccess);
            if(!initSuccess) {
                Toast.makeText(this, "Fallo al inicializar Tesseract", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al inicializar Tesseract", Toast.LENGTH_LONG).show();
        }
    }

    private void copyTessDataFiles(String fileName) throws IOException {
        File path = new File(getFilesDir() + TESS_DATA);
        File file = new File(path, fileName);

        if (!file.exists()) {
            Log.d(TAG, "Copiando archivo Tesseract: " + file.getAbsolutePath());

            InputStream in = getAssets().open("tessdata/" + fileName);
            FileOutputStream out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

            Log.d(TAG, "Archivo Tesseract copiado");
        } else {
            Log.d(TAG, "Archivo ya existe: " + file.getAbsolutePath());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                runOCR(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onSuccess() {
        Toast.makeText(OcrCaptureActivity.this, "Datos guardados correctamente", Toast.LENGTH_LONG).show();
        finish();
    }
}