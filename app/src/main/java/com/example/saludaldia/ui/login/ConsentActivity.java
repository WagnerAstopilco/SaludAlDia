package com.example.saludaldia.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.saludaldia.R;

public class ConsentActivity extends AppCompatActivity {

    private static final String TAG = "ConsentActivity";
    public static final String RESULT_CONSENT_ACCEPTED = "consent_accepted";
    public static final int REQUEST_CODE_CONSENT = 1001;

    private CheckBox cbAcceptPrivacy;
    private Button btnAccept, btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        TextView tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicyContent);
        cbAcceptPrivacy = findViewById(R.id.cbAcceptPrivacy);
        btnAccept = findViewById(R.id.btnAcceptConsent);
        btnDecline = findViewById(R.id.btnDeclineConsent);


        String privacyPolicyText = "<h1>Política de Privacidad y Términos de Servicio de SaludAlDia</h1>" +
                "<p>Fecha de última actualización: 07 de Julio de 2025</p>" +
                "<p>Bienvenido a SaludAlDia. Su privacidad es de suma importancia para nosotros. " +
                "Esta política describe cómo recopilamos, usamos, protegemos y compartimos " +
                "su información personal, especialmente sus datos de salud, de acuerdo con la " +
                "<b>Ley de Protección de Datos Personales de Perú (Ley N° 29733)</b> y sus " +
                "disposiciones complementarias.</p>" +
                "<h2>1. Información que Recopilamos</h2>" +
                "<p>Recopilamos la información que usted nos proporciona directamente al usar la aplicación, " +
                "incluyendo:</p>" +
                "<ul>" +
                "<li><b>Datos de identificación:</b> Nombre, dirección de correo electrónico (obtenidos de su cuenta de Google).</li>" +
                "<li><b>Datos de usuario:</b> Rol (adulto mayor, cuidador).</li>" +
                "<li><b>Datos de salud (sensibles):</b> Información sobre medicamentos, dosis, " +
                "historial de tratamientos, registro de peso, recordatorios de salud, etc. " +
                "Estos datos son tratados con la máxima confidencialidad.</li>" +
                "</ul>" +
                "<h2>2. Finalidad del Tratamiento de Datos</h2>" +
                "<p>Utilizamos su información para:</p>" +
                "<ul>" +
                "<li>Proporcionarle los servicios y funcionalidades de la aplicación (gestión de tratamientos, recordatorios, seguimiento de progreso).</li>" +
                "<li>Personalizar su experiencia en la aplicación.</li>" +
                "<li>Mejorar y optimizar nuestros servicios.</li>" +
                "<li>Comunicarnos con usted sobre su cuenta y actualizaciones importantes.</li>" +
                "<li>Cumplir con obligaciones legales y regulatorias.</li>" +
                "</ul>" +
                "<h2>3. Consentimiento para Datos Sensibles</h2>" +
                "<p>Al aceptar esta Política de Privacidad, usted otorga su <b>consentimiento expreso e informado</b> " +
                "para el tratamiento de sus datos personales, incluyendo sus <b>datos de salud (sensibles)</b>, " +
                "para las finalidades descritas. Usted tiene el derecho de revocar este consentimiento en cualquier momento.</p>" +
                "<h2>4. Compartir Información con Terceros</h2>" +
                "<p>No compartimos su información personal con terceros para fines de marketing o " +
                "comercialización. Solo compartimos su información con proveedores de servicios " +
                "necesarios para la operación de la aplicación (ej. Google Firebase para almacenamiento " +
                "seguro de datos, autenticación y notificaciones push). Estos terceros están obligados " +
                "contractualmente a proteger su información.</p>" +
                "<p>Si usted es un cuidador, solo podrá acceder a los datos de salud de un adulto mayor " +
                "con el consentimiento explícito de este último y mediante la vinculación dentro de la aplicación.</p>" +
                "<h2>5. Seguridad de la Información</h2>" +
                "<p>Implementamos medidas de seguridad técnicas, organizativas y legales para proteger " +
                "sus datos personales contra el acceso no autorizado, la alteración, la divulgación o la destrucción. " +
                "Esto incluye el uso de cifrado en tránsito y en reposo, controles de acceso estrictos y auditorías regulares.</p>" +
                "<h2>6. Ejercicio de sus Derechos ARCO</h2>" +
                "<p>Usted tiene derecho a ejercer sus derechos de <b>Acceso, Rectificación, Cancelación y Oposición (ARCO)</b> " +
                "respecto al tratamiento de sus datos personales. Para ejercer estos derechos, " +
                "puede contactarnos a través de <a href='mailto:privacidad@saludaldia.com'>privacidad@saludaldia.com</a>. " +
                "Responderemos a su solicitud en los plazos establecidos por la ley.</p>" +
                "<h2>7. Cambios a esta Política</h2>" +
                "<p>Podemos actualizar nuestra Política de Privacidad ocasionalmente. Le notificaremos " +
                "cualquier cambio publicando la nueva política en esta página y, si los cambios son " +
                "significativos, le enviaremos una notificación destacada.</p>" +
                "<p>Al hacer clic en 'Aceptar y Continuar', usted confirma que ha leído, entendido y " +
                "aceptado esta Política de Privacidad y los Términos de Servicio de SaludAlDia.</p>";

        tvPrivacyPolicy.setText(Html.fromHtml(privacyPolicyText, Html.FROM_HTML_MODE_COMPACT));
        tvPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());


        btnAccept.setEnabled(false);


        cbAcceptPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAccept.setEnabled(isChecked);
        });

        btnAccept.setOnClickListener(v -> {
            if (cbAcceptPrivacy.isChecked()) {
                Log.d(TAG, "Consentimiento aceptado.");
                Intent resultIntent = new Intent();
                resultIntent.putExtra(RESULT_CONSENT_ACCEPTED, true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Debe aceptar la Política de Privacidad para continuar.", Toast.LENGTH_SHORT).show();
            }
        });

        btnDecline.setOnClickListener(v -> {
            Log.d(TAG, "Consentimiento rechazado.");
            Intent resultIntent = new Intent();
            resultIntent.putExtra(RESULT_CONSENT_ACCEPTED, false);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Botón de retroceso presionado. Considerado como rechazo de consentimiento.");
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_CONSENT_ACCEPTED, false);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}