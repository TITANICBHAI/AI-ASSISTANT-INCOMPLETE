package com.aiassistant.core.external;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.aiassistant.core.external.business.BusinessCallHandler;
import com.aiassistant.core.external.business.BusinessNegotiationEngine;
import com.aiassistant.core.external.business.ServiceBookingManager;
import com.aiassistant.core.external.pdf.PDFLearningManager;
import com.aiassistant.core.external.security.AntiDetectionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central manager for external integrations including business calling,
 * service booking, PDF learning, and enhanced security features.
 */
public class ExternalIntegrationManager {
    private static final String TAG = "ExternalIntegrationMgr";
    
    private static ExternalIntegrationManager instance;
    
    private Context context;
    private boolean isInitialized = false;
    
    // Integration components
    private BusinessCallHandler businessCallHandler;
    private BusinessNegotiationEngine negotiationEngine;
    private ServiceBookingManager serviceBookingManager;
    private PDFLearningManager pdfLearningManager;
    private AntiDetectionManager antiDetectionManager;
    
    // Enabled integrations
    private Map<IntegrationType, Boolean> enabledIntegrations = new HashMap<>();
    
    /**
     * Private constructor for singleton pattern
     */
    private ExternalIntegrationManager(Context context) {
        this.context = context.getApplicationContext();
        
        // Initialize enabled integrations map
        for (IntegrationType type : IntegrationType.values()) {
            enabledIntegrations.put(type, false);
        }
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ExternalIntegrationManager getInstance(Context context) {
        if (instance == null) {
            instance = new ExternalIntegrationManager(context);
        }
        
        return instance;
    }
    
    /**
     * Initialize the external integration manager and its components
     */
    public boolean initialize() {
        try {
            Log.d(TAG, "Initializing external integration manager");
            
            // Create integration components (lazy initialization)
            businessCallHandler = new BusinessCallHandler(context);
            negotiationEngine = new BusinessNegotiationEngine(context);
            serviceBookingManager = new ServiceBookingManager(context);
            pdfLearningManager = new PDFLearningManager(context);
            antiDetectionManager = new AntiDetectionManager(context);
            
            isInitialized = true;
            Log.d(TAG, "External integration manager initialized successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize external integration manager: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if the manager is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Enable a specific integration type
     */
    public boolean enableIntegration(IntegrationType type) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot enable integration, manager not initialized");
            return false;
        }
        
        try {
            Log.d(TAG, "Enabling integration: " + type);
            
            // Initialize the specific integration
            switch (type) {
                case BUSINESS_CALLING:
                    businessCallHandler.initialize();
                    break;
                    
                case BUSINESS_NEGOTIATION:
                    negotiationEngine.initialize();
                    break;
                    
                case SERVICE_BOOKING:
                    serviceBookingManager.initialize();
                    break;
                    
                case PDF_LEARNING:
                    pdfLearningManager.initialize();
                    break;
                    
                case ANTI_DETECTION:
                    antiDetectionManager.initialize();
                    break;
            }
            
            enabledIntegrations.put(type, true);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable integration " + type + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disable a specific integration type
     */
    public boolean disableIntegration(IntegrationType type) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot disable integration, manager not initialized");
            return false;
        }
        
        Log.d(TAG, "Disabling integration: " + type);
        enabledIntegrations.put(type, false);
        return true;
    }
    
    /**
     * Check if an integration is enabled
     */
    public boolean isIntegrationEnabled(IntegrationType type) {
        return enabledIntegrations.getOrDefault(type, false);
    }
    
    /**
     * Execute a business call
     */
    public void executeBusinessCall(BusinessCallRequest request, BusinessCallCallback callback) {
        if (!checkIntegrationEnabled(IntegrationType.BUSINESS_CALLING, callback)) {
            return;
        }
        
        Log.d(TAG, "Executing business call to " + request.phoneNumber);
        
        // Create simple object to pass to the business call handler
        Map<String, Object> callData = new HashMap<>();
        callData.put("phone_number", request.phoneNumber);
        callData.put("purpose", request.purpose);
        callData.put("call_type", request.callType.toString());
        callData.put("parameters", request.parameters);
        
        // Execute the call
        businessCallHandler.executeCall(callData, new BusinessCallHandler.CallListener() {
            @Override
            public void onCallInitiated() {
                if (callback != null) {
                    callback.onCallInitiated();
                }
            }
            
            @Override
            public void onCallConnected() {
                if (callback != null) {
                    callback.onCallConnected();
                }
            }
            
            @Override
            public void onCallDisconnected(boolean successful, Map<String, String> results) {
                if (callback != null) {
                    callback.onCallCompleted(successful, results);
                }
            }
            
            @Override
            public void onCallError(String reason) {
                if (callback != null) {
                    callback.onCallFailed(reason);
                }
            }
            
            @Override
            public void onNegotiationUpdate(String status) {
                if (callback != null) {
                    callback.onNegotiationProgress(status);
                }
            }
        });
    }
    
    /**
     * Book a service
     */
    public void bookService(ServiceBookingRequest request, ServiceBookingCallback callback) {
        if (!checkIntegrationEnabled(IntegrationType.SERVICE_BOOKING, callback)) {
            return;
        }
        
        Log.d(TAG, "Booking service: " + request.serviceType + " with " + request.providerName);
        
        // Create simple object to pass to the service booking manager
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("service_type", request.serviceType);
        bookingData.put("provider_name", request.providerName);
        bookingData.put("scheduled_time", request.scheduledTime);
        bookingData.put("parameters", request.parameters);
        
        // Execute the booking
        serviceBookingManager.bookService(bookingData, new ServiceBookingManager.BookingListener() {
            @Override
            public void onBookingInitiated() {
                if (callback != null) {
                    callback.onBookingInitiated();
                }
            }
            
            @Override
            public void onBookingProgress(String status) {
                if (callback != null) {
                    callback.onBookingProgress(status);
                }
            }
            
            @Override
            public void onBookingConfirmed(Map<String, String> confirmationDetails) {
                if (callback != null) {
                    callback.onBookingConfirmed(confirmationDetails);
                }
            }
            
            @Override
            public void onBookingFailed(String reason) {
                if (callback != null) {
                    callback.onBookingFailed(reason);
                }
            }
        });
    }
    
    /**
     * Learn from a PDF document
     */
    public void learnFromPDF(Uri pdfUri, PDFLearningCallback callback) {
        if (!checkIntegrationEnabled(IntegrationType.PDF_LEARNING, callback)) {
            return;
        }
        
        Log.d(TAG, "Learning from PDF: " + pdfUri);
        
        // Process the PDF document
        pdfLearningManager.processPDFDocument(pdfUri, new PDFLearningManager.PDFProcessListener() {
            @Override
            public void onProcessingStarted() {
                if (callback != null) {
                    callback.onLearningStarted();
                }
            }
            
            @Override
            public void onProcessingProgress(int progress, String stage) {
                if (callback != null) {
                    callback.onLearningProgress(progress, stage);
                }
            }
            
            @Override
            public void onProcessingComplete(PDFLearningManager.PDFLearningResult result) {
                if (callback != null) {
                    PDFLearningResult adaptedResult = adaptPDFLearningResult(result);
                    callback.onLearningComplete(adaptedResult);
                }
            }
            
            @Override
            public void onProcessingFailed(String reason) {
                if (callback != null) {
                    callback.onLearningFailed(reason);
                }
            }
        });
    }
    
    /**
     * Adapts the internal PDF learning result to the external interface
     */
    private PDFLearningResult adaptPDFLearningResult(PDFLearningManager.PDFLearningResult internalResult) {
        PDFLearningResult result = new PDFLearningResult(
            internalResult.getDocumentTitle(),
            internalResult.getPageCount()
        );
        
        // Copy metadata
        for (Map.Entry<String, String> entry : internalResult.getDocumentMetadata().entrySet()) {
            result.addMetadata(entry.getKey(), entry.getValue());
        }
        
        // Copy concepts
        for (String concept : internalResult.getExtractedConcepts()) {
            result.addExtractedConcept(concept);
        }
        
        // Copy sections
        for (PDFLearningManager.PDFLearningResult.PDFSection internalSection : internalResult.getSections()) {
            PDFLearningResult.PDFSection section = new PDFLearningResult.PDFSection(
                internalSection.getTitle(),
                internalSection.getStartPage(),
                internalSection.getEndPage(),
                internalSection.getSummary()
            );
            
            // Copy key points
            for (String keyPoint : internalSection.getKeyPoints()) {
                section.addKeyPoint(keyPoint);
            }
            
            result.addSection(section);
        }
        
        return result;
    }
    
    /**
     * Helper to check if integration is enabled
     */
    private <T> boolean checkIntegrationEnabled(IntegrationType type, T callback) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot perform operation, manager not initialized");
            handleError(callback, "External integration manager not initialized");
            return false;
        }
        
        if (!isIntegrationEnabled(type)) {
            Log.e(TAG, "Cannot perform operation, integration not enabled: " + type);
            handleError(callback, "Integration not enabled: " + type);
            return false;
        }
        
        return true;
    }
    
    /**
     * Helper to handle error callbacks
     */
    private <T> void handleError(T callback, String error) {
        if (callback instanceof BusinessCallCallback) {
            ((BusinessCallCallback) callback).onCallFailed(error);
        } else if (callback instanceof ServiceBookingCallback) {
            ((ServiceBookingCallback) callback).onBookingFailed(error);
        } else if (callback instanceof PDFLearningCallback) {
            ((PDFLearningCallback) callback).onLearningFailed(error);
        }
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        if (!isInitialized) {
            return;
        }
        
        Log.d(TAG, "Shutting down external integration manager");
        
        if (businessCallHandler != null) {
            businessCallHandler.shutdown();
        }
        
        if (negotiationEngine != null) {
            negotiationEngine.shutdown();
        }
        
        if (serviceBookingManager != null) {
            serviceBookingManager.shutdown();
        }
        
        if (pdfLearningManager != null) {
            pdfLearningManager.shutdown();
        }
        
        if (antiDetectionManager != null) {
            antiDetectionManager.shutdown();
        }
        
        isInitialized = false;
    }
    
    /**
     * Integration types
     */
    public enum IntegrationType {
        BUSINESS_CALLING,
        BUSINESS_NEGOTIATION,
        SERVICE_BOOKING,
        PDF_LEARNING,
        ANTI_DETECTION
    }
    
    /**
     * Business call request builder
     */
    public static class BusinessCallRequest {
        final String phoneNumber;
        final String purpose;
        final CallType callType;
        final Map<String, String> parameters;
        
        private BusinessCallRequest(Builder builder) {
            this.phoneNumber = builder.phoneNumber;
            this.purpose = builder.purpose;
            this.callType = builder.callType;
            this.parameters = builder.parameters;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public enum CallType {
            GENERAL,
            RESERVATION,
            APPOINTMENT,
            INFORMATION,
            SUPPORT
        }
        
        public static class Builder {
            private String phoneNumber;
            private String purpose;
            private CallType callType = CallType.GENERAL;
            private Map<String, String> parameters = new HashMap<>();
            
            public Builder setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
                return this;
            }
            
            public Builder setPurpose(String purpose) {
                this.purpose = purpose;
                return this;
            }
            
            public Builder setCallType(CallType callType) {
                this.callType = callType;
                return this;
            }
            
            public Builder addParameter(String key, String value) {
                this.parameters.put(key, value);
                return this;
            }
            
            public BusinessCallRequest build() {
                return new BusinessCallRequest(this);
            }
        }
    }
    
    /**
     * Service booking request builder
     */
    public static class ServiceBookingRequest {
        final String serviceType;
        final String providerName;
        final String scheduledTime;
        final Map<String, String> parameters;
        
        private ServiceBookingRequest(Builder builder) {
            this.serviceType = builder.serviceType;
            this.providerName = builder.providerName;
            this.scheduledTime = builder.scheduledTime;
            this.parameters = builder.parameters;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String serviceType;
            private String providerName;
            private String scheduledTime;
            private Map<String, String> parameters = new HashMap<>();
            
            public Builder setServiceType(String serviceType) {
                this.serviceType = serviceType;
                return this;
            }
            
            public Builder setProviderName(String providerName) {
                this.providerName = providerName;
                return this;
            }
            
            public Builder setScheduledTime(String scheduledTime) {
                this.scheduledTime = scheduledTime;
                return this;
            }
            
            public Builder addParameter(String key, String value) {
                this.parameters.put(key, value);
                return this;
            }
            
            public ServiceBookingRequest build() {
                return new ServiceBookingRequest(this);
            }
        }
    }
    
    /**
     * PDF learning result data
     */
    public static class PDFLearningResult {
        private String documentTitle;
        private int pageCount;
        private List<String> extractedConcepts;
        private Map<String, String> documentMetadata;
        private List<PDFSection> sections;
        
        public PDFLearningResult(String documentTitle, int pageCount) {
            this.documentTitle = documentTitle;
            this.pageCount = pageCount;
            this.extractedConcepts = new ArrayList<>();
            this.documentMetadata = new HashMap<>();
            this.sections = new ArrayList<>();
        }
        
        public String getDocumentTitle() {
            return documentTitle;
        }
        
        public int getPageCount() {
            return pageCount;
        }
        
        public List<String> getExtractedConcepts() {
            return extractedConcepts;
        }
        
        public void addExtractedConcept(String concept) {
            this.extractedConcepts.add(concept);
        }
        
        public Map<String, String> getDocumentMetadata() {
            return documentMetadata;
        }
        
        public void addMetadata(String key, String value) {
            this.documentMetadata.put(key, value);
        }
        
        public List<PDFSection> getSections() {
            return sections;
        }
        
        public void addSection(PDFSection section) {
            this.sections.add(section);
        }
        
        /**
         * Section of a PDF document
         */
        public static class PDFSection {
            private String title;
            private int startPage;
            private int endPage;
            private String summary;
            private List<String> keyPoints;
            
            public PDFSection(String title, int startPage, int endPage, String summary) {
                this.title = title;
                this.startPage = startPage;
                this.endPage = endPage;
                this.summary = summary;
                this.keyPoints = new ArrayList<>();
            }
            
            public String getTitle() {
                return title;
            }
            
            public int getStartPage() {
                return startPage;
            }
            
            public int getEndPage() {
                return endPage;
            }
            
            public String getSummary() {
                return summary;
            }
            
            public List<String> getKeyPoints() {
                return keyPoints;
            }
            
            public void addKeyPoint(String keyPoint) {
                this.keyPoints.add(keyPoint);
            }
        }
    }
    
    /**
     * Callback for business call operations
     */
    public interface BusinessCallCallback {
        void onCallInitiated();
        void onCallConnected();
        void onNegotiationProgress(String status);
        void onCallCompleted(boolean successful, Map<String, String> results);
        void onCallFailed(String reason);
    }
    
    /**
     * Callback for service booking operations
     */
    public interface ServiceBookingCallback {
        void onBookingInitiated();
        void onBookingProgress(String status);
        void onBookingConfirmed(Map<String, String> confirmationDetails);
        void onBookingFailed(String reason);
    }
    
    /**
     * Callback for PDF learning operations
     */
    public interface PDFLearningCallback {
        void onLearningStarted();
        void onLearningProgress(int progress, String stage);
        void onLearningComplete(PDFLearningResult result);
        void onLearningFailed(String reason);
    }
}
