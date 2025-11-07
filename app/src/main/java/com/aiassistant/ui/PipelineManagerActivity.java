package com.aiassistant.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.adapters.PipelineAdapter;
import com.aiassistant.adapters.PipelineStageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline Manager - Allows users to create and modify AI component execution sequences
 * Features:
 * - View existing pipelines
 * - Create custom pipelines
 * - Reorder pipeline stages via drag-and-drop
 * - Configure triggers (screen change, voice detected, periodic)
 * - Set component criticality
 */
public class PipelineManagerActivity extends AppCompatActivity {
    private static final String TAG = "PipelineManager";
    
    private RecyclerView pipelinesRecyclerView;
    private RecyclerView stagesRecyclerView;
    private Button createPipelineButton;
    private Button savePipelineButton;
    private Button addStageButton;
    private TextView currentPipelineTextView;
    
    private List<Pipeline> pipelines;
    private List<PipelineStage> currentStages;
    private Pipeline selectedPipeline;
    private PipelineStageAdapter stageAdapter;
    private PipelineAdapter pipelineAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipeline_manager);
        
        initializeViews();
        loadPipelines();
        setupListeners();
    }
    
    private void initializeViews() {
        pipelinesRecyclerView = findViewById(R.id.pipelinesRecyclerView);
        stagesRecyclerView = findViewById(R.id.stagesRecyclerView);
        createPipelineButton = findViewById(R.id.createPipelineButton);
        savePipelineButton = findViewById(R.id.savePipelineButton);
        addStageButton = findViewById(R.id.addStageButton);
        currentPipelineTextView = findViewById(R.id.currentPipelineTextView);
        
        pipelinesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        pipelines = new ArrayList<>();
        currentStages = new ArrayList<>();
        
        pipelineAdapter = new PipelineAdapter(pipelines);
        pipelineAdapter.setOnPipelineClickListener((pipeline, position) -> {
            selectPipeline(pipeline);
            pipelineAdapter.setSelectedPosition(position);
        });
        pipelinesRecyclerView.setAdapter(pipelineAdapter);
        
        stageAdapter = new PipelineStageAdapter(currentStages);
        stagesRecyclerView.setAdapter(stageAdapter);
        
        setupDragAndDrop();
    }
    
    private void setupDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, 
                                RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                
                PipelineStage stage = currentStages.remove(fromPosition);
                currentStages.add(toPosition, stage);
                stageAdapter.notifyItemMoved(fromPosition, toPosition);
                
                Toast.makeText(PipelineManagerActivity.this, 
                    "Reordered: " + stage.componentName, Toast.LENGTH_SHORT).show();
                return true;
            }
            
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }
        };
        
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(stagesRecyclerView);
    }
    
    private void setupListeners() {
        createPipelineButton.setOnClickListener(v -> createNewPipeline());
        savePipelineButton.setOnClickListener(v -> savePipeline());
        addStageButton.setOnClickListener(v -> addStage());
    }
    
    private void loadPipelines() {
        pipelines.clear();
        
        try {
            InputStream is = getAssets().open("orchestration_config.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            
            String json = new String(buffer, "UTF-8");
            JSONObject config = new JSONObject(json);
            JSONArray pipelinesArray = config.getJSONArray("pipelines");
            
            for (int i = 0; i < pipelinesArray.length(); i++) {
                JSONObject pipelineObj = pipelinesArray.getJSONObject(i);
                Pipeline pipeline = new Pipeline();
                pipeline.name = pipelineObj.getString("name");
                pipeline.type = pipelineObj.getString("type");
                
                JSONArray stagesArray = pipelineObj.getJSONArray("stages");
                pipeline.stages = new ArrayList<>();
                
                for (int j = 0; j < stagesArray.length(); j++) {
                    JSONObject stageObj = stagesArray.getJSONObject(j);
                    PipelineStage stage = new PipelineStage();
                    stage.componentName = stageObj.getString("component");
                    stage.critical = stageObj.getBoolean("critical");
                    pipeline.stages.add(stage);
                }
                
                if (pipelineObj.has("triggers")) {
                    JSONArray triggersArray = pipelineObj.getJSONArray("triggers");
                    pipeline.triggers = new ArrayList<>();
                    for (int k = 0; k < triggersArray.length(); k++) {
                        pipeline.triggers.add(triggersArray.getString(k));
                    }
                }
                
                pipelines.add(pipeline);
            }
            
            Log.d(TAG, "Loaded " + pipelines.size() + " pipelines");
            pipelineAdapter.notifyDataSetChanged();
            
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading pipelines: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading pipelines", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void createNewPipeline() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Pipeline");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_pipeline, null);
        EditText nameEditText = dialogView.findViewById(R.id.pipelineNameEditText);
        Spinner typeSpinner = dialogView.findViewById(R.id.pipelineTypeSpinner);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"sequential", "parallel"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = nameEditText.getText().toString();
            String type = typeSpinner.getSelectedItem().toString();
            
            if (!name.isEmpty()) {
                Pipeline pipeline = new Pipeline();
                pipeline.name = name;
                pipeline.type = type;
                pipeline.stages = new ArrayList<>();
                pipeline.triggers = new ArrayList<>();
                
                pipelines.add(pipeline);
                pipelineAdapter.notifyItemInserted(pipelines.size() - 1);
                selectPipeline(pipeline);
                pipelineAdapter.setSelectedPosition(pipelines.size() - 1);
                
                Toast.makeText(this, "Pipeline created: " + name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void selectPipeline(Pipeline pipeline) {
        selectedPipeline = pipeline;
        currentStages.clear();
        currentStages.addAll(pipeline.stages);
        stageAdapter.notifyDataSetChanged();
        
        currentPipelineTextView.setText("Editing: " + pipeline.name + " (" + pipeline.type + ")");
        savePipelineButton.setEnabled(true);
        addStageButton.setEnabled(true);
    }
    
    private void addStage() {
        if (selectedPipeline == null) {
            Toast.makeText(this, "Please select a pipeline first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Stage");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_stage, null);
        EditText componentEditText = dialogView.findViewById(R.id.componentNameEditText);
        Spinner criticalitySpinner = dialogView.findViewById(R.id.criticalitySpinner);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Critical", "Non-Critical"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        criticalitySpinner.setAdapter(adapter);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String componentName = componentEditText.getText().toString();
            boolean critical = criticalitySpinner.getSelectedItemPosition() == 0;
            
            if (!componentName.isEmpty()) {
                PipelineStage stage = new PipelineStage();
                stage.componentName = componentName;
                stage.critical = critical;
                
                currentStages.add(stage);
                stageAdapter.notifyDataSetChanged();
                
                Toast.makeText(this, "Stage added: " + componentName, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void savePipeline() {
        if (selectedPipeline == null) {
            Toast.makeText(this, "No pipeline selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        selectedPipeline.stages.clear();
        selectedPipeline.stages.addAll(currentStages);
        
        try {
            JSONObject config = new JSONObject();
            JSONArray pipelinesArray = new JSONArray();
            
            for (Pipeline pipeline : pipelines) {
                JSONObject pipelineObj = new JSONObject();
                pipelineObj.put("name", pipeline.name);
                pipelineObj.put("type", pipeline.type);
                
                JSONArray stagesArray = new JSONArray();
                for (PipelineStage stage : pipeline.stages) {
                    JSONObject stageObj = new JSONObject();
                    stageObj.put("component", stage.componentName);
                    stageObj.put("critical", stage.critical);
                    stagesArray.put(stageObj);
                }
                pipelineObj.put("stages", stagesArray);
                
                if (pipeline.triggers != null && !pipeline.triggers.isEmpty()) {
                    JSONArray triggersArray = new JSONArray();
                    for (String trigger : pipeline.triggers) {
                        triggersArray.put(trigger);
                    }
                    pipelineObj.put("triggers", triggersArray);
                }
                
                pipelinesArray.put(pipelineObj);
            }
            
            config.put("pipelines", pipelinesArray);
            
            File configFile = new File(getFilesDir(), "custom_orchestration_config.json");
            FileWriter writer = new FileWriter(configFile);
            writer.write(config.toString(2));
            writer.close();
            
            Toast.makeText(this, "Pipeline saved successfully!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Saved pipeline: " + selectedPipeline.name);
            
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Error saving pipeline: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving pipeline", Toast.LENGTH_SHORT).show();
        }
    }
    
    public static class Pipeline {
        public String name;
        public String type;
        public List<PipelineStage> stages;
        public List<String> triggers;
    }
    
    public static class PipelineStage {
        public String componentName;
        public boolean critical;
    }
}
