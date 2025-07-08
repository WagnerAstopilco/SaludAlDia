package com.example.saludaldia.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.saludaldia.R;
import com.example.saludaldia.ui.toolbar.AdultToolbar;
import com.example.saludaldia.utils.FontScaleContextWrapper;

public class FAQs extends AppCompatActivity {
    private static final String TAG = "FAQs activity";
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings_prefs", MODE_PRIVATE);
        String currentFontSize = prefs.getString("font_size", "medium");

        Context contextForFont = FontScaleContextWrapper.wrap(newBase);

        super.attachBaseContext(contextForFont);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs);
        AdultToolbar.setup(this);
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setTitle("FAQs");
        }
        Button contactSupportButton = findViewById(R.id.contactSupportButton);

        contactSupportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:wastopilco@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Soporte SaludAlDia - Consulta desde FAQs");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola equipo de soporte,\n\nEscribo desde la sección de FAQs de la aplicación. Tengo una consulta sobre...\n\nGracias.");
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                } else {
                    Toast.makeText(FAQs.this, "No se encontró una aplicación de correo.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
