package com.example.remindme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<Collection> collectionList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(Collection collection);
        void onDelete(Collection collection);
    }

    public CollectionAdapter(List<Collection> collectionList, OnItemClickListener listener) {
        this.collectionList = collectionList;
        this.listener = listener;
    }

    public void updateData(List<Collection> newList) {
        this.collectionList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collectionList.get(position);

        holder.tvName.setText(collection.getName());

        // Formatting the frequency for better UI
        String freqText = formatFrequency(collection.getFrequency());
        holder.tvFrequency.setText(freqText);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(collection));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(collection));
    }

    private String formatFrequency(long minutes) {
        if (minutes >= 60) {
            long hours = minutes / 60;
            return "Every " + hours + (hours == 1 ? " hour" : " hours");
        }
        return "Every " + minutes + " min";
    }

    @Override
    public int getItemCount() {
        return collectionList != null ? collectionList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvFrequency;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCollectionName);
            tvFrequency = itemView.findViewById(R.id.tvCollectionFrequency);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}