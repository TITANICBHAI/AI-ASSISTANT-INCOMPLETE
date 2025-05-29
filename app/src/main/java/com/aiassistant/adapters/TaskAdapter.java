package com.aiassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.data.models.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying tasks in a RecyclerView
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    
    private List<Task> tasks;
    private final SimpleDateFormat dateFormat;
    
    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
        this.dateFormat = new SimpleDateFormat("MMM dd HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        
        holder.taskTitleText.setText(task.getTitle());
        holder.taskDescriptionText.setText(task.getDescription());
        
        // Format status text based on task status
        String statusText;
        int statusColor;
        switch (task.getStatus()) {
            case Task.STATUS_COMPLETED:
                statusText = "Completed";
                statusColor = R.color.colorSuccess;
                break;
            case Task.STATUS_IN_PROGRESS:
                statusText = "In Progress";
                statusColor = R.color.colorWarning;
                break;
            case Task.STATUS_FAILED:
                statusText = "Failed";
                statusColor = R.color.colorError;
                break;
            default:
                statusText = "Pending";
                statusColor = R.color.colorPrimary;
                break;
        }
        
        holder.taskStatusText.setText(statusText);
        holder.taskStatusText.setTextColor(holder.itemView.getContext().getResources().getColor(statusColor));
        
        // Format and display date
        String formattedDate = dateFormat.format(new Date(task.getCreatedAt()));
        holder.taskDateText.setText(formattedDate);
        
        // Set priority indicator
        int priorityDrawable;
        switch (task.getPriority()) {
            case Task.PRIORITY_HIGH:
                priorityDrawable = R.drawable.ic_priority_high;
                break;
            case Task.PRIORITY_MEDIUM:
                priorityDrawable = R.drawable.ic_priority_medium;
                break;
            default:
                priorityDrawable = R.drawable.ic_priority_low;
                break;
        }
        holder.taskPriorityIndicator.setBackgroundResource(priorityDrawable);
    }
    
    @Override
    public int getItemCount() {
        return tasks.size();
    }
    
    /**
     * Update the task list and refresh the adapter
     * 
     * @param newTasks The new list of tasks
     */
    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for task items
     */
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitleText;
        TextView taskDescriptionText;
        TextView taskStatusText;
        TextView taskDateText;
        View taskPriorityIndicator;
        
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitleText = itemView.findViewById(R.id.taskTitleText);
            taskDescriptionText = itemView.findViewById(R.id.taskDescriptionText);
            taskStatusText = itemView.findViewById(R.id.taskStatusText);
            taskDateText = itemView.findViewById(R.id.taskDateText);
            taskPriorityIndicator = itemView.findViewById(R.id.taskPriorityIndicator);
        }
    }
}
