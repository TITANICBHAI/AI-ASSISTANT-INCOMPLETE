package com.aiassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.ui.OrchestrationDemoActivity;

import java.util.List;

public class ComponentStatusAdapter extends RecyclerView.Adapter<ComponentStatusAdapter.ViewHolder> {
    
    private final List<OrchestrationDemoActivity.ComponentStatus> components;
    
    public ComponentStatusAdapter(List<OrchestrationDemoActivity.ComponentStatus> components) {
        this.components = components;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_component_status, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrchestrationDemoActivity.ComponentStatus status = components.get(position);
        holder.bind(status);
    }
    
    @Override
    public int getItemCount() {
        return components.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView componentIdTextView;
        private final TextView componentTypeTextView;
        private final TextView healthStatusTextView;
        
        ViewHolder(View itemView) {
            super(itemView);
            componentIdTextView = itemView.findViewById(R.id.componentIdTextView);
            componentTypeTextView = itemView.findViewById(R.id.componentTypeTextView);
            healthStatusTextView = itemView.findViewById(R.id.healthStatusTextView);
        }
        
        void bind(OrchestrationDemoActivity.ComponentStatus status) {
            componentIdTextView.setText(status.id);
            componentTypeTextView.setText(status.type);
            healthStatusTextView.setText(status.healthy ? "✓ Healthy" : "✗ Unhealthy");
            healthStatusTextView.setTextColor(status.healthy ? 
                    0xFF4CAF50 : 0xFFF44336);
        }
    }
}
