package com.example.historymaster.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.historymaster.R;
import com.example.historymaster.models.Entity;
import com.example.historymaster.models.Period;
import java.util.Collections;
import java.util.List;

public class LearningTopicAdapter extends RecyclerView.Adapter<LearningTopicAdapter.Holder> {
    private final List<Period> periods;

    public LearningTopicAdapter(List<Period> periods) {
        this.periods = periods == null ? Collections.emptyList() : periods;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_period, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Period period = periods.get(position);
        holder.title.setText(period.getUpperLevel());
        StringBuilder text = new StringBuilder();
        appendEntities(period.getMiddleLevelEntities(), text, "");
        holder.entities.setText(text.toString());
    }

    private void appendEntities(List<Entity> entities, StringBuilder text, String indent) {
        if (entities == null) return;
        for (Entity entity : entities) {
            text.append(indent).append("・").append(entity.getName()).append("\n");
            if (entity.getTags() != null) {
                for (String tag : entity.getTags()) text.append(indent).append("  ").append(tag).append("\n");
            }
            appendEntities(entity.getSubEntities(), text, indent + "  ");
        }
    }

    @Override
    public int getItemCount() { return periods.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title, entities;
        Holder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_period_name);
            entities = view.findViewById(R.id.tv_entities);
        }
    }
}
