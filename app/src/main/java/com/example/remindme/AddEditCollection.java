package com.example.remindme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import io.objectbox.Box;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class AddEditCollection extends AppCompatActivity {

    private Box<Collection> collectionBox;
    private long existingId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_collection);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            newMethod();
        }
        collectionBox = MainActivity.boxStore.boxFor(Collection.class);

        // Handle Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addCollection), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        TextInputEditText etCollectionName = findViewById(R.id.etCollectionName);
        TextInputEditText etWords = findViewById(R.id.etWords);
        AutoCompleteTextView frequencyDropdown = findViewById(R.id.dropdownFrequency);
        Button saveButton = findViewById(R.id.btnSaveCollection);

        // Setup Dropdown
        String[] frequencyOptions = {"Every 5 minutes", "Every 10 minutes", "Every 30 minutes", "Every hour", "Every 2 hours", "Every 6 hours"};
        frequencyDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, frequencyOptions));

        // Check if Editing
        existingId = getIntent().getLongExtra("collectionId", 0);
        if (existingId != 0) {
            Collection c = collectionBox.get(existingId);
            if (c != null) {
                etCollectionName.setText(c.getName());
                etWords.setText(String.join("\n", c.getWordsList()));
                // Set the dropdown to match existing frequency
                frequencyDropdown.setText(getFrequencyLabel(c.getFrequency()), false);
            }
        }

        saveButton.setOnClickListener(v -> {
            String name = etCollectionName.getText().toString().trim();
            String freqLabel = frequencyDropdown.getText().toString().trim();
            String wordsRaw = etWords.getText().toString().trim();

            if (name.isEmpty() || freqLabel.isEmpty() || wordsRaw.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Prepare Data
            List<String> words = Arrays.asList(wordsRaw.split("\\n"));
            Collection collection = (existingId != 0) ? collectionBox.get(existingId) : new Collection();
            collection.setName(name);
            collection.setFrequency(parseFrequency(freqLabel));
            collection.setWordsList(words);

            // 2. Save to ObjectBox
            collectionBox.put(collection);

            // 3. Request Alarm Permission & Schedule
            checkAlarmPermissionAndSchedule(collection);

            Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void checkAlarmPermissionAndSchedule(Collection collection) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return; // User needs to grant permission before scheduling works
            }
        }
        scheduleNotification(collection);
    }

    private void scheduleNotification(Collection collection) {
        long intervalMillis = (long) collection.getFrequency() * 60 * 1000;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("collectionId", collection.id); // Send ID, not the word!

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) collection.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long triggerAt = System.currentTimeMillis() + intervalMillis;

        if (alarmManager != null) {
            // Use setInexactRepeating for better battery, or setExactAndAllowWhileIdle for precision
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, intervalMillis, pendingIntent);
        }
    }

    private int parseFrequency(String label) {
        switch (label) {
            case "Every 5 minutes": return 5;
            case "Every 10 minutes": return 10;
            case "Every 30 minutes": return 30;
            case "Every hour": return 60;
            case "Every 2 hours": return 120;
            case "Every 6 hours": return 360;
            default: return 60;
        }
    }

    private String getFrequencyLabel(int mins) {
        if (mins == 5) return "Every 5 minutes";
        if (mins == 10) return "Every 10 minutes";
        if (mins == 30) return "Every 30 minutes";
        if (mins == 60) return "Every hour";
        if (mins == 120) return "Every 2 hours";
        return "Every 6 hours";
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void newMethod() {// Better way to request the permission
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.setData(Uri.parse("package:" + getPackageName())); // This takes the user directly to YOUR app's toggle
        startActivity(intent);
    }
}