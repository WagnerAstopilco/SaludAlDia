package com.example.saludaldia.ui.history;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.saludaldia.R;
import com.example.saludaldia.adapter.HistoryEventsAdapter;
import com.example.saludaldia.data.model.History;
import com.example.saludaldia.data.model.HistoryEvent;
import com.example.saludaldia.data.repository.HistoryRepository;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private RecyclerView recyclerView;
    private TextView tvNoHistoryEvents;
    private HistoryEventsAdapter adapter;
    private List<HistoryEvent> historyEventsList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageButton btnGeneratePdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        AdultToolbar.setup(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.history_activity_title));
        }

        recyclerView = findViewById(R.id.rv_history_events);
        tvNoHistoryEvents = findViewById(R.id.tv_no_history_events);
        btnGeneratePdf=findViewById(R.id.btn_generate_pdf);
        historyEventsList = new ArrayList<>();
        adapter = new HistoryEventsAdapter(historyEventsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadHistoryEvents();

        btnGeneratePdf.setOnClickListener(v -> {
            if (historyEventsList.isEmpty()) {
                Toast.makeText(HistoryActivity.this, "No hay eventos para generar un informe PDF.", Toast.LENGTH_SHORT).show();
                v.announceForAccessibility("No hay eventos para generar un informe PDF.");
            } else {
                checkAndRequestPermissions();
            }
        });
    }

    private void loadHistoryEvents() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debe iniciar sesión para ver el historial de eventos.", Toast.LENGTH_LONG).show();
            tvNoHistoryEvents.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnGeneratePdf.setVisibility(View.GONE);
            tvNoHistoryEvents.announceForAccessibility(getString(R.string.no_history_events_message));
            return;
        }

        String userId = currentUser.getUid();

        HistoryRepository.getHistoryByUserId(userId, new HistoryRepository.OnHistoryLoadedListener() {
            @Override
            public void onSuccess(@Nullable History history) {
                if (history != null && history.getEventsIds() != null && !history.getEventsIds().isEmpty()) {
                    List<String> eventIds = history.getEventsIds();
                    Log.d(TAG, "Found " + eventIds.size() + " event IDs in history for user: " + userId);
                    fetchAndDisplayHistoryEvents(userId);

                } else {
                    Log.d(TAG, "No event IDs found in history for user: " + userId);
                    tvNoHistoryEvents.setVisibility(View.VISIBLE);
                    btnGeneratePdf.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    adapter.setEventList(new ArrayList<>());
                    tvNoHistoryEvents.announceForAccessibility(getString(R.string.no_history_events_message));
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error fetching History document for user " + userId + ": " + e.getMessage(), e);
                Toast.makeText(HistoryActivity.this, "Error al cargar historial principal.", Toast.LENGTH_SHORT).show();
                tvNoHistoryEvents.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                btnGeneratePdf.setVisibility(View.GONE);
                tvNoHistoryEvents.announceForAccessibility("Error al cargar historial principal.");
            }
        });
    }

    private void fetchAndDisplayHistoryEvents(String userId) {
        db.collection("historyEvents")
                .whereEqualTo("historyId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyEventsList.clear();
                    for (HistoryEvent event : queryDocumentSnapshots.toObjects(HistoryEvent.class)) {
                        Log.d(TAG,"EVENTO: "+event.getHistoryId());
                        historyEventsList.add(event);
                    }
                    Log.d(TAG,"EVENTO lissta: "+historyEventsList);
                    adapter.setEventList(historyEventsList);
                    Log.d(TAG,"EVENTO lista: "+historyEventsList);

                    if (historyEventsList.isEmpty()) {
                        tvNoHistoryEvents.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        btnGeneratePdf.setVisibility(View.GONE);
                        Log.d(TAG, "No se encontraron eventos detallados en historyEvents para el usuario.");
                        tvNoHistoryEvents.announceForAccessibility(getString(R.string.no_history_events_message));
                    } else {
                        tvNoHistoryEvents.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        btnGeneratePdf.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Eventos detallados cargados. Total: " + historyEventsList.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar eventos detallados de historial: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar eventos de historial.", Toast.LENGTH_SHORT).show();
                    tvNoHistoryEvents.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    btnGeneratePdf.setVisibility(View.GONE);
                    tvNoHistoryEvents.announceForAccessibility("Error al cargar eventos de historial.");
                });
    }
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                generatePdfReport();
            }
        } else {
            generatePdfReport();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdfReport();
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado. No se puede generar el PDF.", Toast.LENGTH_LONG).show();
                getWindow().getDecorView().announceForAccessibility("Permiso de almacenamiento denegado. No se puede generar el PDF.");
            }
        }
    }

    private void generatePdfReport() {
        Log.d(TAG, "Iniciando generación de PDF...");
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        int pageHeight = 1120;
        int pageWidth = 792;
        int margin = 40;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(28);
        titlePaint.setColor(Color.BLACK);
        canvas.drawText("Informe de Historial de Eventos y Tratamientos", pageWidth / 2, margin + 20, titlePaint);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = currentUser != null ? currentUser.getDisplayName() : "Usuario Desconocido";
        String userEmail = currentUser != null ? currentUser.getUid() : "N/A"; // Usamos UID como identificador en el informe
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        paint.setTextSize(14);
        paint.setColor(Color.BLACK);
        canvas.drawText("Generado para: " + userName, margin, margin + 80, paint);
        canvas.drawText("ID de Usuario: " + userEmail, margin, margin + 100, paint);
        canvas.drawText("Fecha de Generación: " + currentDate, margin, margin + 120, paint);

        int yPos = margin + 180;
        paint.setTextSize(12);

        if (historyEventsList.isEmpty()) {
            canvas.drawText("No hay eventos registrados en el historial.", margin, yPos, paint);
        } else {
            for (HistoryEvent event : historyEventsList) {
                if (yPos > pageHeight - margin - 30) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPos = margin + 50;
                }

                canvas.drawText("Tipo: " + event.getEventType(), margin, yPos, paint);
                yPos += 15;
                canvas.drawText("Fecha: " + formatTimestamp(event.getTimestamp()), margin, yPos, paint);
                yPos += 15;
                canvas.drawText("Descripción: " + event.getDetails(), margin, yPos, paint);

                yPos += 30;
            }
        }

        document.finishPage(page);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "InformeSaludAlDia_" + timeStamp + ".pdf";

        File file;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        } else {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            Toast.makeText(this, "PDF generado y guardado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "PDF generado y guardado en: " + file.getAbsolutePath());
            getWindow().getDecorView().announceForAccessibility("Informe PDF generado y guardado.");
            openPdf(file); // Abre el PDF automáticamente
        } catch (IOException e) {
            Log.e(TAG, "Error al generar o guardar PDF: " + e.getMessage(), e);
            Toast.makeText(this, "Error al generar el PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            getWindow().getDecorView().announceForAccessibility("Error al generar el informe PDF");
        }
    }

    private void openPdf(File file) {
        Uri fileUri;
        try {
            fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error: El archivo seleccionado no puede ser compartido. " + e.getMessage());
            Toast.makeText(this, "No se pudo preparar el archivo para abrir. " + e.getMessage(), Toast.LENGTH_LONG).show();
            getWindow().getDecorView().announceForAccessibility("Error al abrir el informe PDF");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No hay ninguna aplicación instalada para abrir PDFs.", Toast.LENGTH_LONG).show();
            getWindow().getDecorView().announceForAccessibility("No hay ninguna aplicación instalada para abrir archivos PDF.");
        }
    }

    private String formatTimestamp(Date timestamp) {
        if (timestamp == null) {
            return "N/A";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(timestamp);
    }
}