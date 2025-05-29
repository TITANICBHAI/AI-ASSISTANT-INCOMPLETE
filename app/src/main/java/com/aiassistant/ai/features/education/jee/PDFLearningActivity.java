package com.aiassistant.ai.features.education.jee;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.ai.features.education.jee.adapters.PDFDocumentAdapter;
import com.aiassistant.ai.features.education.jee.pdf.PDFProcessManager;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.features.voice.SentientVoiceSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for PDF Learning that allows the AI to gain knowledge
 * from educational PDFs uploaded by the user.
 * 
 * This activity provides:
 * 1. PDF upload and processing
 * 2. Document management
 * 3. Document learning status
 * 4. Integration with AI's knowledge base
 */
public class PDFLearningActivity extends AppCompatActivity implements 
        PDFProcessManager.ProcessCallback, PDFDocumentAdapter.PDFDocumentClickListener {
    
    private static final String TAG = "PDFLearningActivity";
    
    // UI Components
    private Button selectPdfButton;
    private TextView statusText;
    private ProgressBar progressBar;
    private RecyclerView documentsRecyclerView;
    private PDFDocumentAdapter documentAdapter;
    
    // Managers
    private PDFProcessManager pdfManager;
    private AIStateManager aiStateManager;
    private SentientVoiceSystem voiceSystem;
    private Handler mainHandler;
    
    // Document tracking
    private String currentProcessingId;
    
    // File picker
    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_learning);
        
        // Initialize managers
        pdfManager = PDFProcessManager.getInstance(this);
        aiStateManager = AIStateManager.getInstance(this);
        voiceSystem = SentientVoiceSystem.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize UI components
        initializeUI();
        
        // Initialize file picker
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedPdfUri = data.getData();
                            processPdf(selectedPdfUri);
                        }
                    }
                });
        
        // Load existing documents
        loadDocuments();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeUI() {
        selectPdfButton = findViewById(R.id.select_pdf_button);
        statusText = findViewById(R.id.status_text);
        progressBar = findViewById(R.id.progress_bar);
        documentsRecyclerView = findViewById(R.id.documents_recycler_view);
        
        // Set layout manager for recycler view
        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize adapter
        documentAdapter = new PDFDocumentAdapter(new ArrayList<>(), this);
        documentsRecyclerView.setAdapter(documentAdapter);
        
        // Set button click listener
        selectPdfButton.setOnClickListener(v -> openPdfPicker());
        
        // Initial state
        progressBar.setVisibility(View.GONE);
        statusText.setText(R.string.pdf_learning_intro);
        
        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.pdf_learning_title);
        }
    }
    
    /**
     * Open PDF file picker
     */
    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        
        pdfPickerLauncher.launch(intent);
    }
    
    /**
     * Process the selected PDF file
     */
    private void processPdf(Uri pdfUri) {
        if (pdfUri == null) {
            showToast("No PDF file selected");
            return;
        }
        
        // Update UI
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText(R.string.pdf_processing_started);
        selectPdfButton.setEnabled(false);
        
        // Start processing
        pdfManager.processPdfFromUri(pdfUri, this);
        
        // Voice feedback
        voiceSystem.speak("I'm starting to learn from the PDF document. This will help me understand and assist with related topics.", "enthusiastic", 0.8f);
    }
    
    /**
     * Load existing documents
     */
    private void loadDocuments() {
        List<PDFProcessManager.PDFDocument> documents = pdfManager.getAllDocuments();
        documentAdapter.updateDocuments(documents);
        
        if (documents.isEmpty()) {
            statusText.setText(R.string.no_pdf_documents);
        }
    }
    
    // ProcessCallback implementation
    
    @Override
    public void onProcessStarted(String documentId) {
        currentProcessingId = documentId;
        
        mainHandler.post(() -> {
            progressBar.setProgress(0);
            statusText.setText(R.string.pdf_processing_started);
        });
    }
    
    @Override
    public void onProcessProgress(String documentId, int progress, int total) {
        if (!documentId.equals(currentProcessingId)) return;
        
        int progressPercent = (progress * 100) / total;
        
        mainHandler.post(() -> {
            progressBar.setProgress(progressPercent);
            statusText.setText(getString(R.string.pdf_processing_progress, progress, total));
        });
    }
    
    @Override
    public void onProcessComplete(String documentId, PDFProcessManager.PDFDocument document) {
        if (!documentId.equals(currentProcessingId)) return;
        
        mainHandler.post(() -> {
            // Update UI
            progressBar.setVisibility(View.GONE);
            selectPdfButton.setEnabled(true);
            statusText.setText(getString(R.string.pdf_processing_complete, document.title));
            
            // Reload document list
            loadDocuments();
            
            // Communicate success to the user
            String successMessage = "I've successfully learned from the document " + document.title + 
                    ". I now understand " + document.conceptRelevance.size() + 
                    " new concepts that I can use to help you.";
            
            voiceSystem.speak(successMessage, "happy", 0.9f);
            
            // Store document learning experience in AI state
            aiStateManager.storeMemoryWithSentiment(
                    "pdf_learning_" + documentId,
                    "Learned from document: " + document.title + 
                            " covering topics: " + String.join(", ", document.keyTopics),
                    0.9
            );
            
            // Process emotional response to learning
            aiStateManager.processInteraction("learning", 0.8);
        });
    }
    
    @Override
    public void onProcessError(String documentId, String error) {
        if (!documentId.equals(currentProcessingId)) return;
        
        mainHandler.post(() -> {
            // Update UI
            progressBar.setVisibility(View.GONE);
            selectPdfButton.setEnabled(true);
            statusText.setText(getString(R.string.pdf_processing_error, error));
            
            // Notify user of the issue
            voiceSystem.speak("I'm sorry, but I encountered an issue processing the PDF document. " + 
                    "The error was: " + error, "concerned", 0.7f);
            
            showToast("Error processing PDF: " + error);
            
            // Process emotional response to failure
            aiStateManager.processInteraction("frustration", -0.4);
        });
    }
    
    // PDFDocumentClickListener implementation
    
    @Override
    public void onPDFDocumentClicked(PDFProcessManager.PDFDocument document) {
        // Show document details or learning status
        showDocumentDetails(document);
    }
    
    @Override
    public void onPDFDocumentLongClicked(PDFProcessManager.PDFDocument document) {
        // Show delete confirmation dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Document");
        builder.setMessage("Are you sure you want to delete the document: " + document.title + "?");
        
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteDocument(document);
        });
        
        builder.setNegativeButton("Cancel", null);
        
        builder.show();
    }
    
    /**
     * Show document details
     */
    private void showDocumentDetails(PDFProcessManager.PDFDocument document) {
        // Build the detail message
        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(document.title).append("\n");
        details.append("Pages: ").append(document.pageCount).append("\n\n");
        
        details.append("Key Topics:\n");
        for (String topic : document.keyTopics) {
            details.append("• ").append(topic).append("\n");
        }
        
        details.append("\nKey Concepts:\n");
        for (String concept : document.conceptRelevance.keySet()) {
            double relevance = document.conceptRelevance.get(concept);
            details.append("• ").append(concept).append(" (").
                    append(String.format("%.1f", relevance * 100)).append("%)\n");
        }
        
        // Show dialog with details
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(document.title);
        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
        
        // Speak a summary of the document
        String summary = "This document about " + document.title + 
                " covers key topics including " + 
                String.join(", ", document.keyTopics.subList(0, Math.min(3, document.keyTopics.size()))) +
                ". I've learned valuable concepts from it that I can apply to help you.";
        
        voiceSystem.speak(summary, "thoughtful", 0.7f);
    }
    
    /**
     * Delete a document
     */
    private void deleteDocument(PDFProcessManager.PDFDocument document) {
        pdfManager.deleteDocument(document.id);
        
        // Update UI
        loadDocuments();
        showToast("Document deleted: " + document.title);
        
        // Notify with voice
        voiceSystem.speak("I've removed the document " + document.title + 
                " from my learning library. The related concepts may still be part of my understanding.", 
                "neutral", 0.5f);
    }
    
    /**
     * Show a toast message
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Save document state
        pdfManager.saveAllDocuments();
    }
}
