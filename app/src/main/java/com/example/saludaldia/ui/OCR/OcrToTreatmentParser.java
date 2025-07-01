package com.example.saludaldia.ui.OCR;

import android.util.Log;
import com.example.saludaldia.data.model.Medication;
import com.example.saludaldia.data.model.Reminder;
import com.example.saludaldia.data.model.Treatment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrToTreatmentParser {

    private static final String TAG = "OcrParser";
    public interface OcrParseCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static void processOcrText(String ocrText, OcrParseCallback callback) {
        try {
            String[] rawLines = ocrText.split("\n");
            List<String> cleanedLinesList = new ArrayList<>();

            for (String line : rawLines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    cleanedLinesList.add(trimmedLine);
                }
            }

            String[] lines = cleanedLinesList.toArray(new String[0]);
            String treatmentName = "";
            Date startDate = new Date();
            int maxDays = 0;
            List<Medication> medications = new ArrayList<>();

            Pattern autogPattern = Pattern.compile("AUTOG\\.?:\\s*(\\S+)");
            Pattern medLinePattern = Pattern.compile("^[\\d\\w]+\\s+(.*?)\\s*,\\s*(.+)$");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                Log.d(TAG, "linea detectada" +line);
            }
            for (int i = 0; i < lines.length-2; i++) {
                String line = lines[i].trim();
                Matcher autogMatcher = autogPattern.matcher(line);
                if (autogMatcher.find()) {
                    treatmentName = autogMatcher.group(1);
                }

                Matcher medMatcher = medLinePattern.matcher(line);
                if (medMatcher.find()) {
                    String name = medMatcher.group(1).replaceFirst("^\\d+\\s*", "").trim();
                    String presentacion = medMatcher.group(2).trim();
                    String viaLine = lines[i + 1].trim();
                    String indLine = lines[i + 2].trim();
                    int days = extractDaysFromLine(line);
                    maxDays = Math.max(maxDays, days);

                    Medication med = createMedication(name, presentacion, viaLine, indLine, days, startDate);
                    medications.add(med);
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.DATE, maxDays);
            Date endDate = cal.getTime();

            String treatmentId = UUID.randomUUID().toString();
            Treatment treatment = new Treatment();
            treatment.setTreatmentId(treatmentId);
            treatment.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            treatment.setName(treatmentName);
            treatment.setStartDate(startDate);
            treatment.setEndDate(endDate);
            treatment.setDescription(null);
            treatment.setState("activo");

            saveTreatmentWithMedications(treatment, medications, callback);

        } catch (Exception e) {
            callback.onFailure(e);
        }
    }


    private static int extractDaysFromLine(String line) {
        Matcher matcher = Pattern.compile("\\s(\\d{1,2})\\s+FR").matcher(line);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1;
    }

    private static Medication createMedication(String name, String presentacion, String viaLine, String indLine, int days, Date startDate) {
        String medId = UUID.randomUUID().toString();

        String via = viaLine.toLowerCase().contains("via admin.") ? viaLine.substring(viaLine.indexOf(".") + 1).trim() : "";
        String dose = "", intervalRaw = "", notes = "";


        if (indLine.toLowerCase().contains("ind")) {
            String[] parts = indLine.split(",");
            if (parts.length >= 1) dose = parts[0].replaceAll("(?i)ind:", "").trim();
            if (parts.length >= 2) intervalRaw = parts[1].trim();
            if (parts.length >= 3) notes = parts[2].trim();
        }
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(startDate);
        endCal.add(Calendar.DATE, days);
        Date endDate = endCal.getTime();

        Reminder reminder = new Reminder();
        reminder.setReminderId(UUID.randomUUID().toString());
        reminder.setStartDate(startDate);
        reminder.setEndDate(endDate);
        reminder.setIsRecurring(true);
        reminder.setFrequency("Diario");
        reminder.setIsActive(true);
        List<String> generatedTimes = generateScheduleTimes(intervalRaw);
        reminder.setScheduleTimes(generatedTimes);

        Medication med = new Medication();
        med.setMedicationId(medId);
        med.setName(name);
        med.setPresentation(presentacion);
        med.setVia(via);
        med.setDose(dose);
        med.setNotes(notes);
        med.setNumber_days(days);
        med.setIsActive(true);
        med.setReminder(reminder);

        return med;
    }

    private static List<String> generateScheduleTimes(String intervalRaw) {
        List<String> times = new ArrayList<>();
        String normalizedIntervalRaw = intervalRaw.toUpperCase().replaceAll("\\s+", "");
        Matcher matcher = Pattern.compile("C/(\\d{1,2})\\s*HS").matcher(normalizedIntervalRaw.toUpperCase());
        if (!matcher.find()) return times;

        int interval = Integer.parseInt(matcher.group(1));
        if (interval <= 0) {
            return times;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = (Calendar) cal.clone();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        while (cal.before(endOfDay)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            times.add(sdf.format(cal.getTime()));
            cal.add(Calendar.HOUR_OF_DAY, interval);
        }

        return times;
    }

    private static void saveTreatmentWithMedications(Treatment treatment, List<Medication> medications, OcrParseCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("treatments").document(treatment.getTreatmentId())
                .set(treatment)
                .addOnSuccessListener(treatmentTask -> {
                    for (Medication med : medications) {
                        med.setTreatmentId(treatment.getTreatmentId());

                        db.collection("medications").document(med.getMedicationId())
                                .set(med)
                                .addOnSuccessListener(medTask -> {
                                    Reminder rem = med.getReminder();
                                    rem.setMedicationId(med.getMedicationId());

                                    db.collection("reminders").document(rem.getReminderId())
                                            .set(rem)
                                            .addOnSuccessListener(reminderTask ->
                                                    Log.d(TAG, "Reminder saved: " + rem.getReminderId()))
                                            .addOnFailureListener(callback::onFailure);
                                })
                                .addOnFailureListener(callback::onFailure);
                    }

                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }
}
