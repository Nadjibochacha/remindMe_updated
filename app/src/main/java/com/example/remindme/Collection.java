package com.example.remindme;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
public class Collection {
    @Id
    public long id;

    private String name;
    private int frequency;
    private String wordJson;

    // We mark these as @Transient so ObjectBox doesn't try to save them to the DB
    @Transient
    private static final Gson gson = new Gson();
    @Transient
    private static final Type listType = new TypeToken<List<String>>(){}.getType();

    public Collection() {}

    // Convenience method to handle the JSON conversion automatically
    public List<String> getWordsList() {
        if (wordJson == null || wordJson.isEmpty()) return new ArrayList<>();
        return gson.fromJson(wordJson, listType);
    }

    public void setWordsList(List<String> words) {
        this.wordJson = gson.toJson(words);
    }

    // Standard Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }

    public String getWordJson() { return wordJson; }
    public void setWordJson(String wordJson) { this.wordJson = wordJson; }
}