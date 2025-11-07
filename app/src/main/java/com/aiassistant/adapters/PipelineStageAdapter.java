package com.aiassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.ui.PipelineManagerActivity;

import java.util.List;

public class PipelineStageAdapter extends RecyclerView.Adapter<PipelineStageAdapter.ViewHolder> {
    
    private final List<PipelineManagerActivity.PipelineStage> stages;
    private OnStageActionListener listener;
    
    public interface OnStageActionListener {
        void onStageRemove(int position);
        void onStageEdit(int position);
    }
    
    public PipelineStageAdapter(List<PipelineManagerActivity.PipelineStage> stages) {
        this.stages = stages;
    }
    
    public void setOnStageActionListener(OnStageActionListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pipeline_stage, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PipelineManagerActivity.PipelineStage stage = stages.get(position);
        holder.bind(stage, position);
    }
    
    @Override
    public int getItemCount() {
        return stages.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView stageNumberTextView;
        private final TextView componentNameTextView;
        private final TextView criticalityTextView;
        private final ImageButton removeButton;
        
        ViewHolder(View itemView) {
            super(itemView);
            stageNumberTextView = itemView.findViewById(R.id.stageNumberTextView);
            componentNameTextView = itemView.findViewById(R.id.componentNameTextView);
            criticalityTextView = itemView.findViewById(R.id.criticalityTextView);
            removeButton = itemView.findViewById(R.id.removeStageButton);
        }
        
        void bind(PipelineManagerActivity.PipelineStage stage, int position) {
            stageNumberTextView.setText(String.valueOf(position + 1));
            componentNameTextView.setText(stage.componentName);
            criticalityTextView.setText(stage.critical ? "CRITICAL" : "Optional");
            criticalityTextView.setTextColor(stage.critical ? 0xFFF44336 : 0xFF4CAF50);
            
            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStageRemove(position);
                } else {
                    stages.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, stages.size());
                }
            });
        }
    }
}
