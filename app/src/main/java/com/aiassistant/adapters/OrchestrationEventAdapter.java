package com.aiassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.core.orchestration.OrchestrationEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrchestrationEventAdapter extends RecyclerView.Adapter<OrchestrationEventAdapter.ViewHolder> {
    
    private final List<OrchestrationEvent> events;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    
    public OrchestrationEventAdapter(List<OrchestrationEvent> events) {
        this.events = events;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_orchestration_event, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrchestrationEvent event = events.get(position);
        holder.bind(event, timeFormat);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventTypeTextView;
        private final TextView eventSourceTextView;
        private final TextView eventTimeTextView;
        
        ViewHolder(View itemView) {
            super(itemView);
            eventTypeTextView = itemView.findViewById(R.id.eventTypeTextView);
            eventSourceTextView = itemView.findViewById(R.id.eventSourceTextView);
            eventTimeTextView = itemView.findViewById(R.id.eventTimeTextView);
        }
        
        void bind(OrchestrationEvent event, SimpleDateFormat timeFormat) {
            eventTypeTextView.setText(event.getEventType());
            eventSourceTextView.setText(event.getSource());
            eventTimeTextView.setText(timeFormat.format(new Date(event.getTimestamp())));
            
            int color = getColorForEventType(event.getEventType());
            eventTypeTextView.setTextColor(color);
        }
        
        private int getColorForEventType(String eventType) {
            if (eventType.contains("error") || eventType.contains("failed")) {
                return 0xFFF44336;
            } else if (eventType.contains("success") || eventType.contains("completed")) {
                return 0xFF4CAF50;
            } else if (eventType.contains("warning") || eventType.contains("degraded")) {
                return 0xFFFF9800;
            } else {
                return 0xFF2196F3;
            }
        }
    }
}
