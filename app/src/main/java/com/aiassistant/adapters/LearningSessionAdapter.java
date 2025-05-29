package com.aiassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.models.LearningSession;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the learning sessions recycler view
 */
public class LearningSessionAdapter extends RecyclerView.Adapter<LearningSessionAdapter.ViewHolder> {

    private final List<LearningSession> sessions;
    private final OnSessionClickListener clickListener;
    private final SimpleDateFormat dateFormat;
    
    /**
     * Interface for handling session clicks
     */
    public interface OnSessionClickListener {
        void onSessionClick(LearningSession session);
    }
    
    /**
     * Constructor
     */
    public LearningSessionAdapter(List<LearningSession> sessions, OnSessionClickListener clickListener) {
        this.sessions = sessions;
        this.clickListener = clickListener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_learning_session, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LearningSession session = sessions.get(position);
        
        holder.tvName.setText(session.getName());
        holder.tvApp.setText(session.getAppName());
        holder.tvPatterns.setText(String.valueOf(session.getPatternCount()) + " patterns");
        holder.tvDate.setText(dateFormat.format(session.getCreatedDate()));
        
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSessionClick(session);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return sessions.size();
    }
    
    /**
     * ViewHolder for learning session items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvApp;
        public TextView tvPatterns;
        public TextView tvDate;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvName = itemView.findViewById(R.id.tv_session_name);
            tvApp = itemView.findViewById(R.id.tv_session_app);
            tvPatterns = itemView.findViewById(R.id.tv_session_patterns);
            tvDate = itemView.findViewById(R.id.tv_session_date);
        }
    }
}