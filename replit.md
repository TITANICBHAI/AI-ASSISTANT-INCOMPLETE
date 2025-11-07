# AI Assistant Android Application

## Overview
The AI Assistant is an AI-powered Android application designed to offer advanced assistance through integrated AI/ML capabilities. It includes TensorFlow Lite, voice recognition with emotional intelligence, automated call handling, advanced gaming AI assistance (FPS aim assist, tactical AI, game detection), and educational features like JEE learning and PDF processing. The application also incorporates robust security, anti-detection systems, and real-time screen analysis using OpenCV. The long-term goal is to provide a highly intelligent, adaptive, and versatile AI companion for Android users.

## User Preferences
- Build target: APK for local testing in Android Studio
- Keep all features (no deletions unless duplicate)
- Prefer feature-complete implementations over clean code
- Consolidate duplicates to most feature-rich version

## System Architecture

### Core Application Structure
The application uses a modular architecture with `MainActivity.java` as the entry point and `com.aiassistant.core.ai.AIAssistantApplication` as the primary application class. Distinct packages organize AI, voice, gaming, telephony, education, security, and UI components.

### UI/UX Decisions
The UI adheres to standard Android practices using Activities and Fragments. The `AppTheme` employs a blue color palette (`Primary Color: #2196F3`, `Primary Dark: #1976D2`, `Accent: #FF4081`). All UI strings are externalized for localization.

### Technical Implementations and Feature Specifications

**1. AI & Machine Learning:**
- **Core AI:** Managed by `AIStateManager`, `AIModelManager`, and `TFLiteModelManager` for TensorFlow Lite. Includes `AITaskScheduler`, `DeepRLSystem`, `LearningSystem`, and `PredictiveActionSystem`.
- **ML Models:** Utilizes `.tflite` models for voice behavior, emotional intelligence, combat/enemy/environment detection, depth estimation, game state classification, spatial reasoning, synthetic voice, threat detection, and object detection.
- **Centralized Orchestration:** `CentralAIOrchestrator` manages AI components via an event-driven architecture, including `ComponentRegistry`, `EventRouter`, `DiffEngine`, `HealthMonitor`, `CircuitBreaker`, `ProblemSolvingBroker` (Groq AI integration), and `OrchestrationScheduler`.

**2. Voice & Speech:**
- **Core Voice:** `VoiceManager` integrates `VoiceRecognitionManager` (STT), `SpeechSynthesisManager` (TTS), and `VoiceCommandManager`.
- **Advanced Voice:** Features `EmotionalSpeechSynthesizer`, `VoiceEmotionAnalyzer`, `DynamicDialogueGenerator`, `VoiceBiometricAuthenticator`, and `BehavioralVoiceAnalyzer`.
- **Emotional Intelligence:** Managed by `EmotionalIntelligenceManager` and supporting classes.

**3. Gaming AI:**
- **FPS Features:** `FPSGameModule` integrates `AimAssistant`, `EnemyDetector`, `CombatPatternRecognizer`, `TimingOptimizer`, and `FramePerfectTiming`.
- **Game Analysis:** `GameAnalysisManager` uses `GameDetector`, `EnvironmentAnalyzer`, `PatternAnalyzer`, `SpatialAnalyzer`, and `TacticalAISystem`.
- **Vision & Capture:** `VisionEnhancementManager` provides `VisualThreatRecognition` and `PredictiveVisionModel`. `ScreenCaptureManager` and `HighFPSCaptureManager` support `MultiFrameAnalyzer` and `FrameBufferAnalyzer`.
- **Game Understanding:** `GameUnderstandingEngine` with `RuleExtractor` and `ContextLearningSystem`.

**4. Call Handling & Telephony:**
- **Call Services:** `CallHandlingService`, `AICallScreeningService`, `EmotionalCallHandlingService`, and `DuplexCallHandler`.
- **Call Features:** `BusinessCallHandler` with `BusinessNegotiationEngine`, `ServiceBookingManager`, and `CallerProfileRepository`.
- **Receivers:** `PhoneStateReceiver`, `CallStateReceiver`, and `BootCompletedReceiver`.

**5. Educational Features:**
- **JEE Learning:** `JEELearningActivity` and `PDFLearningActivity` supported by `PDFLearningManager`, `NumericalAnalyzer`, `SymbolicMathEngine`, and `SentientLearningSystem`.
- **Voice Teaching:** `VoiceTeachingActivity` allows teaching AI using voice commands and gestures, integrating Groq API for intent understanding and database persistence.
- **Image Labeling:** `ImageLabelingActivity` facilitates AI-assisted image labeling with camera/gallery integration, Groq API suggestions, and database persistence.
- **Learning Database:** Five new Room entities (`VoiceSampleEntity`, `GestureSampleEntity`, `ImageSampleEntity`, `LabelDefinitionEntity`, `ModelInfoEntity`) with corresponding DAOs and `LearningRepository` for persistence.

**6. Security & Anti-Detection:**
- **Security Systems:** `SecurityProtectionSystem`, `AntiDetectionManager`, `AntiDetectionService`, `AccessControl`, `SignatureVerifier`, and `MLThreatDetectorImpl`.
- **Anti-Cheat:** `AntiCheatSystem`.

**7. Database & Persistence:**
- **Database:** Room Persistence Library (`com.aiassistant.data.AppDatabase`, `ai_assistant_db`).
- **Entities:** Includes `AIAction`, `GameState`, `ScreenActionEntity`, `TouchPath`, `UIElement`, `CallerProfile`, `Game`, `GameProfile`, `GameConfig`, `ActionSequence`, `DetectedEnemy`, `FeedbackRecord`, `LearningSession`, `PerformanceLog`, `PerformanceMetric`, `ScheduledTask`, `Settings`, `Strategy`, `Task`, `TrainingData`, `UserFeedback`, `UserProfile`, and the new learning entities.

**8. Memory & Learning:**
- **Memory Systems:** `MemoryManager` coordinates `LongTermMemory`, `ShortTermMemory`, `EmotionalMemory`, `ConversationHistory`, and `KnowledgeEntry`.
- **Learning Systems:** `AdaptiveLearningSystem`, `PersistentLearningSystem`, `SelfDirectedLearningSystem`, `StructuredKnowledgeSystem`, `SystemAccessLearningManager`, and `InternalReasoningSystem`.

## External Dependencies

### Libraries
- **AndroidX:** `appcompat`, `core`, `constraintlayout`, `material`, `cardview`, `recyclerview`, `lifecycle`, `workmanager`
- **Room Database:** `androidx.room`
- **TensorFlow Lite:** `org.tensorflow:tensorflow-lite`, `tensorflow-lite-metadata`, `tensorflow-lite-support`
- **Google ML Kit:** `com.google.mlkit:language-id`, `com.google.mlkit:translate`
- **JSON Processing:** `com.google.code.gson:gson`
- **OpenCV:** `org.opencv:opencv-android:4.5.3`

### Permissions
- **Normal:** `INTERNET`, `ACCESS_NETWORK_STATE`, `MODIFY_AUDIO_SETTINGS`, `RECEIVE_BOOT_COMPLETED`
- **Dangerous (Runtime Requested):** `READ_CONTACTS`, `READ_CALL_LOG`, `READ_PHONE_STATE`, `PROCESS_OUTGOING_CALLS`, `RECORD_AUDIO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`

### Build Configuration
- **Gradle:** 4.2.2
- **Build Tools:** 30.0.3
- **Compile/Target SDK:** 30
- **Min SDK:** 24
- **Java Compatibility:** 8
- **AAPT Options:** `noCompress "tflite"`