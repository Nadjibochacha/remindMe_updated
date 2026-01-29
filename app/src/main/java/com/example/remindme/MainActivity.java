package com.example.remindme;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class MainActivity extends AppCompatActivity {
    public static BoxStore boxStore;
    private Box<Collection> collectionBox;
    private CollectionAdapter adapter;
    private static final int REQUEST_NOTIF_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 1. Initialize ObjectBox securely
        initObjectBox();

        // 2. Setup Notification Channel and Check Permissions
        NotificationHelper.createNotificationChannel(this);
        checkNotificationPermission();

        // 3. UI Setup
        setupWindowInsets();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initObjectBox() {
        if (boxStore == null) {
            boxStore = MyObjectBox.builder()
                    .androidContext(getApplicationContext())
                    .build();
        }
        collectionBox = boxStore.boxFor(Collection.class);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClickListeners() {
        TextView tvCopy = findViewById(R.id.tvCopyright);
        tvCopy.setOnClickListener(v -> {
            Intent goPortfolio = new Intent(Intent.ACTION_VIEW, Uri.parse("https://nadjib-chacha.vercel.app/"));
            startActivity(goPortfolio);
        });
        ExtendedFloatingActionButton btnAddCol = findViewById(R.id.btnAddCollection);
        btnAddCol.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddEditCollection.class));
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerCollections);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CollectionAdapter(collectionBox.getAll(), new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Collection collection) {
                Intent editIntent = new Intent(MainActivity.this, AddEditCollection.class);
                editIntent.putExtra("collectionId", collection.getId());
                startActivity(editIntent);
            }

            @Override
            public void onDelete(Collection collection) {
                collectionBox.remove(collection);
                refreshCollectionList();
                Toast.makeText(MainActivity.this, "Collection deleted", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIF_PERMISSION);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCollectionList();
    }

    private void refreshCollectionList() {
        if (adapter != null && collectionBox != null) {
            adapter.updateData(collectionBox.getAll());
        }
    }
}