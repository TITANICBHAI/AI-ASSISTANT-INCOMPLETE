package com.aiassistant.ui.learning;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.services.GroqApiService;
import com.aiassistant.core.ai.learning.adaptive.AdaptiveLearningSystem;
import com.aiassistant.core.ai.memory.MemoryManager;
import com.aiassistant.data.repositories.LearningRepository;
import com.aiassistant.data.models.ImageSampleEntity;
import com.aiassistant.data.models.LabelDefinitionEntity;
import com.aiassistant.utils.StorageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Image Labeling Activity - Allows users to label images with AI-assisted learning
 * Features:
 * - Image selection from gallery or camera
 * - Custom label creation with purpose definition
 * - Integration with Groq API for label purpose analysis
 * - Independent learning module for each label category
 * - Automatic pattern recognition and suggestions
 */
public class ImageLabelingActivity extends AppCompatActivity {
    private static final String TAG = "ImageLabelingActivity";
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    
    // UI Components
    private ImageView selectedImageView;
    private TextView imageInfoTextView;
    private TextView labelCountTextView;
    private EditText labelNameEditText;
    private EditText labelPurposeEditText;
    private Button selectImageButton;
    private Button captureImageButton;
    private Button addLabelButton;
    private Button analyzeLabelButton;
    private Button saveLabelsButton;
    private RecyclerView labelsRecyclerView;
    private LinearLayout labeledImagesContainer;
    
    // Services
    private GroqApiService groqService;
    private AdaptiveLearningSystem learningSystem;
    private MemoryManager memoryManager;
    private LearningRepository learningRepository;
    private StorageManager storageManager;
    
    // Data
    private Bitmap currentImage;
    private String currentImagePath;
    private List<ImageLabel> currentLabels;
    private Map<String, IndependentLearningModule> learningModules;
    private List<LabeledImage> labeledImages;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_labeling);
        
        initializeViews();
        initializeServices();
        setupListeners();
        checkPermissions();
        
        Log.d(TAG, "Image Labeling Activity initialized");
    }
    
    private void initializeViews() {
        selectedImageView = findViewById(R.id.selectedImageView);
        imageInfoTextView = findViewById(R.id.imageInfoTextView);
        labelCountTextView = findViewById(R.id.labelCountTextView);
        labelNameEditText = findViewById(R.id.labelNameEditText);
        labelPurposeEditText = findViewById(R.id.labelPurposeEditText);
        selectImageButton = findViewById(R.id.selectImageButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        addLabelButton = findViewById(R.id.addLabelButton);
        analyzeLabelButton = findViewById(R.id.analyzeLabelButton);
        saveLabelsButton = findViewById(R.id.saveLabelsButton);
        labelsRecyclerView = findViewById(R.id.labelsRecyclerView);
        labeledImagesContainer = findViewById(R.id.labeledImagesContainer);
        
        labelsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        updateImageInfo("No image selected");
    }
    
    private void initializeServices() {
        groqService = GroqApiService.getInstance(this);
        learningSystem = new AdaptiveLearningSystem(this);
        memoryManager = MemoryManager.getInstance(this);
        learningRepository = new LearningRepository(this);
        storageManager = new StorageManager(this);
        
        currentLabels = new ArrayList<>();
        learningModules = new HashMap<>();
        labeledImages = new ArrayList<>();
    }
    
    private void setupListeners() {
        selectImageButton.setOnClickListener(v -> selectImage());
        captureImageButton.setOnClickListener(v -> captureImage());
        addLabelButton.setOnClickListener(v -> addLabel());
        analyzeLabelButton.setOnClickListener(v -> analyzeLabelWithGroq());
        saveLabelsButton.setOnClickListener(v -> saveAllLabels());
    }
    
    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };
        
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }
    
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, 
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri selectedImageUri = data.getData();
                loadImageFromUri(selectedImageUri);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                loadCapturedImage(imageBitmap);
            }
        }
    }
    
    private void loadImageFromUri(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            currentImage = BitmapFactory.decodeStream(imageStream);
            selectedImageView.setImageBitmap(currentImage);
            currentImagePath = imageUri.toString();
            
            updateImageInfo("Image loaded: " + currentImage.getWidth() + "x" 
                    + currentImage.getHeight());
            currentLabels.clear();
            updateLabelCount();
            
            // Auto-analyze image with Groq
            autoAnalyzeImage();
            
            Log.d(TAG, "Image loaded from URI");
            
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to load image", e);
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadCapturedImage(Bitmap bitmap) {
        currentImage = bitmap;
        selectedImageView.setImageBitmap(bitmap);
        currentImagePath = "captured_" + System.currentTimeMillis();
        
        updateImageInfo("Image captured: " + currentImage.getWidth() + "x" 
                + currentImage.getHeight());
        currentLabels.clear();
        updateLabelCount();
        
        // Auto-analyze image with Groq
        autoAnalyzeImage();
        
        Log.d(TAG, "Image captured");
    }
    
    private void autoAnalyzeImage() {
        String prompt = "Analyze this image and suggest 3-5 relevant labels and their purposes. " +
                "Consider what objects, concepts, or categories might be useful to label. " +
                "Provide response as JSON array with format: [{\"label\": \"object_name\", " +
                "\"purpose\": \"why this label is useful\", \"category\": \"type\"}]";
        
        updateImageInfo("AI analyzing image...");
        
        groqService.chatCompletion(prompt, new GroqApiService.ChatCompletionCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> handleAutoAnalysis(response));
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    updateImageInfo("Image loaded - Add labels manually");
                    Log.e(TAG, "Auto-analysis failed: " + error);
                });
            }
        });
    }
    
    private void handleAutoAnalysis(String response) {
        try {
            // Try to extract JSON array from response
            int jsonStart = response.indexOf('[');
            int jsonEnd = response.lastIndexOf(']') + 1;
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = response.substring(jsonStart, jsonEnd);
                JSONArray suggestions = new JSONArray(jsonStr);
                
                showLabelSuggestions(suggestions);
                updateImageInfo("AI suggested " + suggestions.length() + " labels");
            } else {
                updateImageInfo("Image loaded - Add labels manually");
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse auto-analysis", e);
            updateImageInfo("Image loaded - Add labels manually");
        }
    }
    
    private void showLabelSuggestions(JSONArray suggestions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AI Label Suggestions");
        
        StringBuilder message = new StringBuilder("Suggested labels:\n\n");
        try {
            for (int i = 0; i < suggestions.length(); i++) {
                JSONObject suggestion = suggestions.getJSONObject(i);
                String label = suggestion.getString("label");
                String purpose = suggestion.getString("purpose");
                message.append((i + 1)).append(". ").append(label)
                        .append("\n   Purpose: ").append(purpose).append("\n\n");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading suggestions", e);
        }
        
        builder.setMessage(message.toString());
        builder.setPositiveButton("Use Suggestions", (dialog, which) -> 
                applySuggestions(suggestions));
        builder.setNegativeButton("Manual Labeling", null);
        builder.show();
    }
    
    private void applySuggestions(JSONArray suggestions) {
        try {
            for (int i = 0; i < suggestions.length(); i++) {
                JSONObject suggestion = suggestions.getJSONObject(i);
                String label = suggestion.getString("label");
                String purpose = suggestion.getString("purpose");
                String category = suggestion.optString("category", "General");
                
                addLabelDirect(label, purpose, category);
            }
            
            Toast.makeText(this, "Applied " + suggestions.length() + " suggested labels", 
                    Toast.LENGTH_SHORT).show();
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to apply suggestions", e);
        }
    }
    
    private void addLabel() {
        String labelName = labelNameEditText.getText().toString().trim();
        String labelPurpose = labelPurposeEditText.getText().toString().trim();
        
        if (currentImage == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (labelName.isEmpty()) {
            Toast.makeText(this, "Please enter a label name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (labelPurpose.isEmpty()) {
            labelPurpose = "User-defined label";
        }
        
        addLabelDirect(labelName, labelPurpose, "Custom");
        
        labelNameEditText.setText("");
        labelPurposeEditText.setText("");
        
        Toast.makeText(this, "Label added: " + labelName, Toast.LENGTH_SHORT).show();
    }
    
    private void addLabelDirect(String labelName, String purpose, String category) {
        ImageLabel label = new ImageLabel(labelName, purpose, category, 
                currentImagePath, System.currentTimeMillis());
        currentLabels.add(label);
        
        updateLabelCount();
        createOrUpdateLearningModule(labelName, purpose, category);
        
        Log.d(TAG, "Label added: " + labelName + " (" + category + ")");
    }
    
    private void analyzeLabelWithGroq() {
        String labelName = labelNameEditText.getText().toString().trim();
        
        if (labelName.isEmpty()) {
            Toast.makeText(this, "Please enter a label to analyze", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String prompt = "Analyze this image label: '" + labelName + "'\n\n" +
                "Provide:\n" +
                "1. The most likely purpose for this label\n" +
                "2. Category (e.g., object, scene, concept, action)\n" +
                "3. Related labels that might be useful\n" +
                "4. How this label can be used in a learning system\n\n" +
                "Respond in JSON format with fields: purpose, category, related_labels, learning_applications";
        
        updateImageInfo("Analyzing label with AI...");
        
        groqService.chatCompletion(prompt, new GroqApiService.ChatCompletionCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> handleLabelAnalysis(response, labelName));
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    updateImageInfo("Analysis failed: " + error);
                    Log.e(TAG, "Label analysis error: " + error);
                });
            }
        });
    }
    
    private void handleLabelAnalysis(String response, String labelName) {
        try {
            JSONObject analysis = new JSONObject(response);
            String purpose = analysis.optString("purpose", "Label purpose");
            String category = analysis.optString("category", "General");
            String relatedLabels = analysis.optString("related_labels", "");
            String learningApps = analysis.optString("learning_applications", "");
            
            // Auto-fill purpose field
            labelPurposeEditText.setText(purpose);
            
            // Show analysis results
            String resultMessage = "Purpose: " + purpose + "\n" +
                    "Category: " + category + "\n\n" +
                    "Related labels: " + relatedLabels + "\n\n" +
                    "Learning applications: " + learningApps;
            
            showAnalysisDialog(labelName, resultMessage, category);
            updateImageInfo("Label analyzed by AI");
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse label analysis", e);
            // Use the raw response as purpose
            labelPurposeEditText.setText(response);
            updateImageInfo("Label analyzed");
        }
    }
    
    private void showAnalysisDialog(String labelName, String analysis, String category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AI Analysis: " + labelName);
        builder.setMessage(analysis);
        builder.setPositiveButton("Add Label", (dialog, which) -> {
            addLabelDirect(labelName, labelPurposeEditText.getText().toString(), category);
            labelNameEditText.setText("");
            labelPurposeEditText.setText("");
        });
        builder.setNegativeButton("Edit", null);
        builder.show();
    }
    
    private void createOrUpdateLearningModule(String labelName, String purpose, String category) {
        String moduleKey = category + "_" + labelName;
        
        IndependentLearningModule module = learningModules.get(moduleKey);
        if (module == null) {
            module = new IndependentLearningModule(labelName, category, purpose);
            learningModules.put(moduleKey, module);
            Log.d(TAG, "Created new learning module: " + moduleKey);
            
            learningRepository.getLabelByName(labelName, new LearningRepository.OnLabelCallback() {
                @Override
                public void onLabelRetrieved(LabelDefinitionEntity label) {
                    if (label == null) {
                        LabelDefinitionEntity newLabel = new LabelDefinitionEntity();
                        newLabel.setName(labelName);
                        newLabel.setPurpose(purpose);
                        newLabel.setCategory(category);
                        newLabel.setCreatedAt(new Date());
                        
                        learningRepository.insertLabel(newLabel, new LearningRepository.OnInsertCallback() {
                            @Override
                            public void onSuccess(long id) {
                                Log.d(TAG, "Created label definition for: " + labelName);
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Failed to create label definition", e);
                            }
                        });
                    } else {
                        learningRepository.incrementLabelUsage(label.getLabelId(), new LearningRepository.OnUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Incremented usage for label: " + labelName);
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Failed to increment label usage", e);
                            }
                        });
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to check label existence", e);
                }
            });
        }
        
        module.addTrainingExample(currentImagePath);
        module.incrementUsageCount();
        
        // Train adaptive learning system
        learningSystem.learn(category, labelName, 0.8f);
        
        // Store in memory
        memoryManager.storeKnowledge("ImageLabel_" + category, labelName, purpose);
    }
    
    private void saveAllLabels() {
        if (currentLabels.isEmpty()) {
            Toast.makeText(this, "No labels to save", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentImage == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }
        
        updateImageInfo("Saving labels to database...");
        
        final AtomicInteger successCount = new AtomicInteger(0);
        final int totalOperations = currentLabels.size();
        
        for (ImageLabel label : currentLabels) {
            learningRepository.getLabelByName(label.name, new LearningRepository.OnLabelCallback() {
                @Override
                public void onLabelRetrieved(LabelDefinitionEntity existingLabel) {
                    final String labelId;
                    
                    if (existingLabel == null) {
                        LabelDefinitionEntity newLabel = new LabelDefinitionEntity();
                        newLabel.setName(label.name);
                        newLabel.setPurpose(label.purpose);
                        newLabel.setCategory(label.category);
                        newLabel.setCreatedAt(new Date());
                        
                        labelId = newLabel.getLabelId();
                        
                        learningRepository.insertLabel(newLabel, new LearningRepository.OnInsertCallback() {
                            @Override
                            public void onSuccess(long id) {
                                runOnUiThread(() -> {
                                    Log.d(TAG, "Created label definition: " + label.name);
                                    saveImageSample(labelId, label, successCount, totalOperations);
                                });
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> {
                                    Log.e(TAG, "Failed to create label", e);
                                    Toast.makeText(ImageLabelingActivity.this, 
                                            "Error creating label: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    } else {
                        labelId = existingLabel.getLabelId();
                        
                        learningRepository.incrementLabelUsage(labelId, new LearningRepository.OnUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    Log.d(TAG, "Incremented usage for label: " + label.name);
                                    saveImageSample(labelId, label, successCount, totalOperations);
                                });
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> {
                                    Log.e(TAG, "Failed to increment label usage", e);
                                    saveImageSample(labelId, label, successCount, totalOperations);
                                });
                            }
                        });
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Failed to check label existence", e);
                        Toast.makeText(ImageLabelingActivity.this, 
                                "Error checking label: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
        
        saveLabelingSession("Labels saved to database");
    }
    
    private void saveImageSample(String labelId, ImageLabel label, AtomicInteger successCount, int totalOperations) {
        String savedImagePath = storageManager.saveImageSample(currentImage, labelId);
        
        if (savedImagePath == null) {
            Toast.makeText(this, "Failed to save image file", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ImageSampleEntity imageSample = new ImageSampleEntity();
        imageSample.setImagePath(savedImagePath);
        imageSample.setLabelId(labelId);
        imageSample.setTimestamp(new Date());
        imageSample.setConfidence(0.8f);
        
        learningRepository.insertImageSample(imageSample, new LearningRepository.OnInsertCallback() {
            @Override
            public void onSuccess(long id) {
                runOnUiThread(() -> {
                    int completed = successCount.incrementAndGet();
                    checkLabelSaveCompletion(completed, totalOperations);
                });
            }
            
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to save image sample", e);
                    Toast.makeText(ImageLabelingActivity.this, 
                            "Error saving image sample: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void checkLabelSaveCompletion(int completed, int total) {
        if (completed == total) {
            Toast.makeText(this, 
                    "Successfully saved " + completed + " labeled images to database!", 
                    Toast.LENGTH_LONG).show();
            updateImageInfo("Session saved successfully!");
            Log.d(TAG, "All labeled images saved successfully");
        }
    }
    
    private String generateLabelingSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Image: ").append(currentImagePath).append("\n");
        summary.append("Labels (").append(currentLabels.size()).append("):\n");
        
        for (ImageLabel label : currentLabels) {
            summary.append("- ").append(label.name).append(" (").append(label.category)
                    .append("): ").append(label.purpose).append("\n");
        }
        
        summary.append("\nLearning Modules: ").append(learningModules.size());
        return summary.toString();
    }
    
    private void saveLabelingSession(String aiSummary) {
        String sessionData = generateLabelingSummary() + "\n\nAI Summary:\n" + aiSummary;
        memoryManager.storeKnowledge("ImageLabelingSession", 
                "Session_" + System.currentTimeMillis(), sessionData);
        
        Toast.makeText(this, "Saved " + currentLabels.size() + " labels with " + 
                learningModules.size() + " learning modules!", Toast.LENGTH_LONG).show();
        
        updateImageInfo("Session saved successfully!");
        
        // Reset for next image
        currentLabels.clear();
        updateLabelCount();
        
        Log.d(TAG, "Labeling session saved");
    }
    
    private void updateImageInfo(String message) {
        imageInfoTextView.setText(message);
    }
    
    private void updateLabelCount() {
        labelCountTextView.setText("Labels: " + currentLabels.size() + 
                " | Learning Modules: " + learningModules.size());
    }
    
    // Data Classes
    private static class ImageLabel {
        String name;
        String purpose;
        String category;
        String imagePath;
        long timestamp;
        
        ImageLabel(String name, String purpose, String category, String imagePath, long timestamp) {
            this.name = name;
            this.purpose = purpose;
            this.category = category;
            this.imagePath = imagePath;
            this.timestamp = timestamp;
        }
    }
    
    private static class IndependentLearningModule {
        String labelName;
        String category;
        String purpose;
        List<String> trainingExamples;
        int usageCount;
        long createdAt;
        float confidence;
        
        IndependentLearningModule(String labelName, String category, String purpose) {
            this.labelName = labelName;
            this.category = category;
            this.purpose = purpose;
            this.trainingExamples = new ArrayList<>();
            this.usageCount = 0;
            this.createdAt = System.currentTimeMillis();
            this.confidence = 0.5f;
        }
        
        void addTrainingExample(String imagePath) {
            trainingExamples.add(imagePath);
            // Increase confidence with more examples
            confidence = Math.min(0.95f, confidence + 0.05f);
        }
        
        void incrementUsageCount() {
            usageCount++;
        }
    }
    
    private static class LabeledImage {
        String imagePath;
        List<ImageLabel> labels;
        long timestamp;
        
        LabeledImage(String imagePath, List<ImageLabel> labels, long timestamp) {
            this.imagePath = imagePath;
            this.labels = labels;
            this.timestamp = timestamp;
        }
    }
}
