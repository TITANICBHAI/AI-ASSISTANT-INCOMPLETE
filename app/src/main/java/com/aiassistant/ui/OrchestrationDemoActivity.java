package com.aiassistant.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.core.orchestration.CentralAIOrchestrator;
import com.aiassistant.core.orchestration.ComponentInterface;
import com.aiassistant.core.orchestration.ComponentRegistry;
import com.aiassistant.core.orchestration.EventRouter;
import com.aiassistant.core.orchestration.HealthMonitor;
import com.aiassistant.core.orchestration.OrchestrationEvent;
import com.aiassistant.core.orchestration.ProblemSolvingBroker;
import com.aiassistant.adapters.OrchestrationEventAdapter;
import com.aiassistant.adapters.ComponentStatusAdapter;
import com.aiassistant.services.GroqApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrchestrationDemoActivity extends AppCompatActivity {
    private static final String TAG = "OrchestrationDemo";
    
    private TextView orchestrationStatusTextView;
    private TextView componentCountTextView;
    private TextView eventCountTextView;
    private TextView healthScoreTextView;
    private Button startOrchestrationButton;
    private Button stopOrchestrationButton;
    private Button testComponentButton;
    private Button testProblemSolvingButton;
    private ProgressBar healthProgressBar;
    private RecyclerView componentsRecyclerView;
    private RecyclerView eventsRecyclerView;
    
    private CentralAIOrchestrator orchestrator;
    private boolean isBound = false;
    private Handler updateHandler;
    private Runnable updateRunnable;
    
    private List<ComponentStatus> componentStatuses;
    private List<OrchestrationEvent> recentEvents;
    private ComponentStatusAdapter componentAdapter;
    private OrchestrationEventAdapter eventAdapter;
    
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CentralAIOrchestrator.LocalBinder binder = (CentralAIOrchestrator.LocalBinder) service;
            orchestrator = binder.getService();
            isBound = true;
            
            Log.d(TAG, "Connected to CentralAIOrchestrator");
            updateOrchestrationStatus();
            subscribeToOrchestrationEvents();
            startPeriodicUpdates();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            orchestrator = null;
            isBound = false;
            Log.d(TAG, "Disconnected from CentralAIOrchestrator");
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orchestration_demo);
        
        initializeViews();
        initializeData();
        setupListeners();
        bindToOrchestratorService();
    }
    
    private void initializeViews() {
        orchestrationStatusTextView = findViewById(R.id.orchestrationStatusTextView);
        componentCountTextView = findViewById(R.id.componentCountTextView);
        eventCountTextView = findViewById(R.id.eventCountTextView);
        healthScoreTextView = findViewById(R.id.healthScoreTextView);
        startOrchestrationButton = findViewById(R.id.startOrchestrationButton);
        stopOrchestrationButton = findViewById(R.id.stopOrchestrationButton);
        testComponentButton = findViewById(R.id.testComponentButton);
        testProblemSolvingButton = findViewById(R.id.testProblemSolvingButton);
        healthProgressBar = findViewById(R.id.healthProgressBar);
        componentsRecyclerView = findViewById(R.id.componentsRecyclerView);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        
        componentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        updateHandler = new Handler(Looper.getMainLooper());
    }
    
    private void initializeData() {
        componentStatuses = new ArrayList<>();
        recentEvents = new ArrayList<>();
        
        componentAdapter = new ComponentStatusAdapter(componentStatuses);
        eventAdapter = new OrchestrationEventAdapter(recentEvents);
        
        componentsRecyclerView.setAdapter(componentAdapter);
        eventsRecyclerView.setAdapter(eventAdapter);
    }
    
    private void setupListeners() {
        startOrchestrationButton.setOnClickListener(v -> startOrchestration());
        stopOrchestrationButton.setOnClickListener(v -> stopOrchestration());
        testComponentButton.setOnClickListener(v -> testComponent());
        testProblemSolvingButton.setOnClickListener(v -> testProblemSolving());
    }
    
    private void bindToOrchestratorService() {
        Intent intent = new Intent(this, CentralAIOrchestrator.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    
    private void subscribeToOrchestrationEvents() {
        if (orchestrator == null) return;
        
        EventRouter eventRouter = orchestrator.getEventRouter();
        if (eventRouter == null) return;
        
        eventRouter.subscribe("*", event -> {
            runOnUiThread(() -> addEvent(event));
        });
        
        Log.d(TAG, "Subscribed to orchestration events");
    }
    
    private void startOrchestration() {
        if (orchestrator != null) {
            orchestrator.start();
            Toast.makeText(this, R.string.orchestration_running, Toast.LENGTH_SHORT).show();
            updateOrchestrationStatus();
        } else {
            Toast.makeText(this, "Orchestrator not connected", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopOrchestration() {
        if (orchestrator != null) {
            orchestrator.stop();
            Toast.makeText(this, "Orchestration stopped", Toast.LENGTH_SHORT).show();
            updateOrchestrationStatus();
        }
    }
    
    private void testComponent() {
        if (orchestrator == null) return;
        
        ComponentRegistry registry = orchestrator.getComponentRegistry();
        if (registry == null) return;
        
        Map<String, ComponentInterface> components = registry.getAllComponents();
        
        if (components.isEmpty()) {
            Toast.makeText(this, "No components registered yet", Toast.LENGTH_SHORT).show();
            registerTestComponents();
            return;
        }
        
        String firstComponentId = components.keySet().iterator().next();
        ComponentInterface component = components.get(firstComponentId);
        
        if (component != null) {
            component.execute(null);
            Toast.makeText(this, "Tested component: " + firstComponentId, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void testProblemSolving() {
        if (orchestrator == null) return;
        
        ProblemSolvingBroker broker = orchestrator.getProblemSolvingBroker();
        if (broker == null) {
            Toast.makeText(this, "Problem solving broker not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        GroqApiService groqService = GroqApiService.getInstance(this);
        groqService.chatCompletion(
            "Test: How would you optimize an AI system for mobile devices?",
            new GroqApiService.ChatCompletionCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        Toast.makeText(OrchestrationDemoActivity.this, 
                            R.string.groq_api_success, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Groq response: " + response);
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(OrchestrationDemoActivity.this, 
                            R.string.groq_api_error, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Groq error: " + error);
                    });
                }
            }
        );
        
        Toast.makeText(this, R.string.groq_api_processing, Toast.LENGTH_SHORT).show();
    }
    
    private void registerTestComponents() {
        if (orchestrator == null) return;
        
        ComponentRegistry registry = orchestrator.getComponentRegistry();
        if (registry == null) return;
        
        registry.registerComponent("TestComponent1", new ComponentInterface() {
            @Override
            public String getComponentId() {
                return "TestComponent1";
            }
            
            @Override
            public String getComponentType() {
                return "TestType";
            }
            
            @Override
            public void initialize() {
                Log.d(TAG, "TestComponent1 initialized");
            }
            
            @Override
            public void execute(Map<String, Object> params) {
                Log.d(TAG, "TestComponent1 executed");
            }
            
            @Override
            public void shutdown() {
                Log.d(TAG, "TestComponent1 shutdown");
            }
            
            @Override
            public boolean isHealthy() {
                return true;
            }
            
            @Override
            public Map<String, Object> getState() {
                return new java.util.HashMap<>();
            }
        });
        
        Toast.makeText(this, "Test component registered", Toast.LENGTH_SHORT).show();
        updateComponentList();
    }
    
    private void updateOrchestrationStatus() {
        if (orchestrator == null) {
            orchestrationStatusTextView.setText("Disconnected");
            startOrchestrationButton.setEnabled(false);
            stopOrchestrationButton.setEnabled(false);
            return;
        }
        
        boolean isRunning = orchestrator.isRunning();
        orchestrationStatusTextView.setText(isRunning ? "Running" : "Stopped");
        startOrchestrationButton.setEnabled(!isRunning);
        stopOrchestrationButton.setEnabled(isRunning);
        
        updateComponentList();
        updateHealthScore();
    }
    
    private void updateComponentList() {
        if (orchestrator == null) return;
        
        ComponentRegistry registry = orchestrator.getComponentRegistry();
        if (registry == null) return;
        
        Map<String, ComponentInterface> components = registry.getAllComponents();
        
        componentStatuses.clear();
        for (Map.Entry<String, ComponentInterface> entry : components.entrySet()) {
            ComponentStatus status = new ComponentStatus(
                entry.getKey(),
                entry.getValue().getComponentType(),
                entry.getValue().isHealthy()
            );
            componentStatuses.add(status);
        }
        
        componentCountTextView.setText(String.valueOf(components.size()));
        componentAdapter.notifyDataSetChanged();
    }
    
    private void updateHealthScore() {
        if (orchestrator == null) return;
        
        HealthMonitor healthMonitor = orchestrator.getHealthMonitor();
        if (healthMonitor == null) {
            healthScoreTextView.setText("--");
            healthProgressBar.setProgress(0);
            return;
        }
        
        double healthScore = healthMonitor.getOverallHealthScore();
        int healthPercent = (int) (healthScore * 100);
        
        healthScoreTextView.setText(healthPercent + "%");
        healthProgressBar.setProgress(healthPercent);
    }
    
    private void addEvent(OrchestrationEvent event) {
        recentEvents.add(0, event);
        
        if (recentEvents.size() > 50) {
            recentEvents.remove(recentEvents.size() - 1);
        }
        
        eventCountTextView.setText(String.valueOf(recentEvents.size()));
        eventAdapter.notifyDataSetChanged();
    }
    
    private void startPeriodicUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateOrchestrationStatus();
                updateHandler.postDelayed(this, 2000);
            }
        };
        updateHandler.post(updateRunnable);
    }
    
    private void stopPeriodicUpdates() {
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
    
    @Override
    protected void onDestroy() {
        stopPeriodicUpdates();
        
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        
        super.onDestroy();
    }
    
    public static class ComponentStatus {
        public final String id;
        public final String type;
        public final boolean healthy;
        
        public ComponentStatus(String id, String type, boolean healthy) {
            this.id = id;
            this.type = type;
            this.healthy = healthy;
        }
    }
}
