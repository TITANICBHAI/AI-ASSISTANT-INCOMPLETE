package com.aiassistant.examples;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.orchestration.CentralAIOrchestrator;
import com.aiassistant.core.orchestration.ComponentStateSnapshot;
import com.aiassistant.core.orchestration.EventRouter;
import com.aiassistant.core.orchestration.OrchestrationEvent;
import com.aiassistant.core.orchestration.StateDiff;

import java.util.HashMap;
import java.util.Map;

public class CoordinatedLoopDemo {
    private static final String TAG = "CoordinatedLoopDemo";
    
    public static void runDemo(Context context) {
        Log.i(TAG, "========== Starting Coordinated Loop Demo ==========");
        
        CentralAIOrchestrator orchestrator = CentralAIOrchestrator.getInstance();
        
        if (orchestrator == null || !orchestrator.isInitialized()) {
            Log.e(TAG, "Central AI Orchestrator not available");
            return;
        }
        
        ExampleCoordinatedComponent component1 = new ExampleCoordinatedComponent(
            context, "demo_analyzer", "Demo Analyzer"
        );
        ExampleCoordinatedComponent component2 = new ExampleCoordinatedComponent(
            context, "demo_processor", "Demo Processor"
        );
        ExampleCoordinatedComponent component3 = new ExampleCoordinatedComponent(
            context, "demo_responder", "Demo Responder"
        );
        
        component1.initialize();
        component2.initialize();
        component3.initialize();
        
        component1.start();
        component2.start();
        component3.start();
        
        Log.i(TAG, "Components started - demonstrating coordinated execution");
        
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("demo_data", "test input");
        initialData.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> result1 = component1.execute(initialData);
        Log.d(TAG, "Component 1 result: " + result1);
        
        Map<String, Object> result2 = component2.execute(result1);
        Log.d(TAG, "Component 2 result: " + result2);
        
        Map<String, Object> result3 = component3.execute(result2);
        Log.d(TAG, "Component 3 result: " + result3);
        
        demonstrateStateDiffDetection(component1);
        
        demonstrateCircuitBreaker(component2);
        
        demonstrateGroqEscalation(orchestrator);
        
        component1.stop();
        component2.stop();
        component3.stop();
        
        Log.i(TAG, "========== Coordinated Loop Demo Complete ==========");
    }
    
    private static void demonstrateStateDiffDetection(ExampleCoordinatedComponent component) {
        Log.i(TAG, "--- Demonstrating State Diff Detection ---");
        
        ComponentStateSnapshot snapshot = component.captureState();
        
        Map<String, Object> expectedState = new HashMap<>(snapshot.getState());
        expectedState.put("health_status", "excellent");
        
        ComponentStateSnapshot expectedSnapshot = new ComponentStateSnapshot(
            component.getComponentId(),
            snapshot.getVersion() + 1,
            expectedState
        );
        
        Log.d(TAG, "Expected state differs from actual state - diff will be detected");
    }
    
    private static void demonstrateCircuitBreaker(ExampleCoordinatedComponent component) {
        Log.i(TAG, "--- Demonstrating Circuit Breaker ---");
        
        for (int i = 0; i < 6; i++) {
            Map<String, Object> input = new HashMap<>();
            input.put("iteration", i);
            
            try {
                component.execute(input);
                Log.d(TAG, "Execution " + i + " completed");
            } catch (Exception e) {
                Log.w(TAG, "Execution " + i + " failed (intentional for demo)");
            }
        }
        
        Log.d(TAG, "After multiple failures, circuit breaker should be open");
    }
    
    private static void demonstrateGroqEscalation(CentralAIOrchestrator orchestrator) {
        Log.i(TAG, "--- Demonstrating Groq Problem Escalation ---");
        
        Map<String, Object> context = new HashMap<>();
        context.put("demo", "groq_escalation");
        context.put("description", "This is a simulated complex problem that needs AI assistance");
        
        OrchestrationEvent problemEvent = new OrchestrationEvent(
            "component.error",
            "demo_component",
            context
        );
        
        orchestrator.getEventRouter().publish(problemEvent);
        
        Log.d(TAG, "Problem event published - should be escalated to Groq if unresolved");
    }
}
