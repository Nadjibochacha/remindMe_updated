package com.example.remindme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import java.util.Random;
import io.objectbox.Box;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long collectionId = intent.getLongExtra("collectionId", -1);
        if (collectionId == -1) return;

        // 1. Get the BoxStore (Using the static instance from MainActivity)
        if (MainActivity.boxStore == null) {
            MainActivity.boxStore = MyObjectBox.builder()
                    .androidContext(context.getApplicationContext())
                    .build();
        }

        Box<Collection> collectionBox = MainActivity.boxStore.boxFor(Collection.class);
        Collection collection = collectionBox.get(collectionId);

        if (collection != null) {
            List<String> words = collection.getWordsList();
            if (!words.isEmpty()) {
                // 2. Pick a random word
                String randomWord = words.get(new Random().nextInt(words.size()));

                // 3. Show the notification
                NotificationHelper.showNotification(
                        context,
                        collection.getName(),
                        randomWord,
                        (int) collectionId
                );
            }
        }
    }
}