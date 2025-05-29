package com.aiassistant.ai.features.education.jee.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.ai.features.education.jee.pdf.PDFProcessManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying PDF documents in a RecyclerView
 */
public class PDFDocumentAdapter extends RecyclerView.Adapter<PDFDocumentAdapter.DocumentViewHolder> {
    
    private List<PDFProcessManager.PDFDocument> documents;
    private PDFDocumentClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    /**
     * Interface for handling document click events
     */
    public interface PDFDocumentClickListener {
        void onPDFDocumentClicked(PDFProcessManager.PDFDocument document);
        void onPDFDocumentLongClicked(PDFProcessManager.PDFDocument document);
    }
    
    /**
     * ViewHolder for document items
     */
    public class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView infoText;
        TextView topicsText;
        
        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.document_title);
            infoText = itemView.findViewById(R.id.document_info);
            topicsText = itemView.findViewById(R.id.document_topics);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPDFDocumentClicked(documents.get(position));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPDFDocumentLongClicked(documents.get(position));
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Constructor
     */
    public PDFDocumentAdapter(List<PDFProcessManager.PDFDocument> documents, PDFDocumentClickListener listener) {
        this.documents = documents;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pdf_document, parent, false);
        return new DocumentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        PDFProcessManager.PDFDocument document = documents.get(position);
        
        // Set title
        holder.titleText.setText(document.title);
        
        // Set info (pages and date)
        String dateString = dateFormat.format(new Date(document.timestamp));
        holder.infoText.setText(holder.itemView.getContext().getString(
                R.string.pdf_document_info, document.pageCount, dateString));
        
        // Set topics
        if (document.keyTopics.isEmpty()) {
            holder.topicsText.setVisibility(View.GONE);
        } else {
            holder.topicsText.setVisibility(View.VISIBLE);
            holder.topicsText.setText(holder.itemView.getContext().getString(
                    R.string.pdf_document_topics, 
                    String.join(", ", document.keyTopics.subList(0, Math.min(3, document.keyTopics.size())))));
        }
    }
    
    @Override
    public int getItemCount() {
        return documents.size();
    }
    
    /**
     * Update the document list and refresh the view
     */
    public void updateDocuments(List<PDFProcessManager.PDFDocument> newDocuments) {
        this.documents = newDocuments;
        notifyDataSetChanged();
    }
}
