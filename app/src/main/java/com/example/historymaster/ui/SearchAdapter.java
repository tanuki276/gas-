package com.example.historymaster.ui;

import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.historymaster.models.Entity;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Holder> {
    private List<Entity> entities;

    public SearchAdapter(List<Entity> entities) {
        this.entities = new ArrayList<>(entities);
    }

    public void updateData(List<Entity> entities) {
        this.entities = new ArrayList<>(entities);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView text = new TextView(parent.getContext());
        text.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int padding = (int) (16 * parent.getResources().getDisplayMetrics().density);
        text.setPadding(padding, padding, padding, padding);
        text.setTextSize(16);
        return new Holder(text);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Entity entity = entities.get(position);
        StringBuilder text = new StringBuilder(entity.getName());
        if (entity.getTags() != null) {
            for (String tag : entity.getTags()) text.append("\n・").append(tag);
        }
        holder.text.setText(text);
    }

    @Override
    public int getItemCount() { return entities.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView text;
        Holder(TextView text) { super(text); this.text = text; }
    }
}
