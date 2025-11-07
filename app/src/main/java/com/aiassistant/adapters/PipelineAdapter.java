package com.aiassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.ui.PipelineManagerActivity;

import java.util.List;

public class PipelineAdapter extends RecyclerView.Adapter<PipelineAdapter.ViewHolder> {
    
    private final List<PipelineManagerActivity.Pipeline> pipelines;
    private OnPipelineClickListener listener;
    private int selectedPosition = -1;
    
    public interface OnPipelineClickListener {
        void onPipelineClick(PipelineManagerActivity.Pipeline pipeline, int position);
    }
    
    public PipelineAdapter(List<PipelineManagerActivity.Pipeline> pipelines) {
        this.pipelines = pipelines;
    }
    
    public void setOnPipelineClickListener(OnPipelineClickListener listener) {
        this.listener = listener;
    }
    
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        
        // Only notify if previous position was valid
        if (previousPosition >= 0 && previousPosition < pipelines.size()) {
            notifyItemChanged(previousPosition);
        }
        
        // Only notify if new position is valid
        if (selectedPosition >= 0 && selectedPosition < pipelines.size()) {
            notifyItemChanged(selectedPosition);
        }
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pipeline, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PipelineManagerActivity.Pipeline pipeline = pipelines.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(pipeline, position, isSelected);
    }
    
    @Override
    public int getItemCount() {
        return pipelines.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView pipelineNameTextView;
        private final TextView pipelineTypeTextView;
        private final TextView stageCountTextView;
        private final View selectionIndicator;
        
        ViewHolder(View itemView) {
            super(itemView);
            pipelineNameTextView = itemView.findViewById(R.id.pipelineNameTextView);
            pipelineTypeTextView = itemView.findViewById(R.id.pipelineTypeTextView);
            stageCountTextView = itemView.findViewById(R.id.stageCountTextView);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
        }
        
        void bind(PipelineManagerActivity.Pipeline pipeline, int position, boolean isSelected) {
            pipelineNameTextView.setText(pipeline.name);
            pipelineTypeTextView.setText(pipeline.type.toUpperCase());
            stageCountTextView.setText(pipeline.stages.size() + " stages");
            
            selectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
            itemView.setBackgroundColor(isSelected ? 0xFFE3F2FD : 0xFFFFFFFF);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPipelineClick(pipeline, position);
                }
            });
        }
    }
}
