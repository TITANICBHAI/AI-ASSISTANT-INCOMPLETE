package com.aiassistant.ai.features.voice.multilingual;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.util.Log;

import com.aiassistant.ai.features.voice.VoiceCommandManager;
import com.aiassistant.ai.features.voice.VoiceResponseManager;
import com.aiassistant.ai.features.voice.emotional.advanced.SoulfulVoiceSystem;
import com.aiassistant.ai.features.voice.adaptive.HumanVoiceAdaptationManager;
import com.aiassistant.security.SecurityContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Advanced multilingual voice support system that enables the AI assistant to
 * communicate naturally in multiple languages, including voice recognition and
 * synthesis capabilities with natural accent adaptation and cultural context awareness.
 */
public class MultilingualVoiceSupport {
    private static final String TAG = "MultilingualVoiceSupport";
    
    // Singleton instance
    private static MultilingualVoiceSupport instance;
    
    // Context reference
    private Context context;
    
    // Text-to-speech engines for different languages
    private Map<String, TextToSpeech> ttsEngines = new HashMap<>();
    
    // Currently active language
    private String activeLanguage = "en";
    
    // Available languages
    private List<LanguageProfile> supportedLanguages = new ArrayList<>();
    
    // Language detection confidence threshold
    private static final float LANGUAGE_DETECTION_THRESHOLD = 0.75f;
    
    // Executor for background tasks
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    // Handler for main thread callbacks
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Language detection patterns
    private Map<String, List<Pattern>> languagePatterns = new HashMap<>();
    
    // Component references
    private VoiceCommandManager voiceCommandManager;
    private VoiceResponseManager voiceResponseManager;
    private HumanVoiceAdaptationManager adaptationManager;
    private SoulfulVoiceSystem soulfulVoiceSystem;
    
    /**
     * Language profile including all settings for a supported language
     */
    public static class LanguageProfile {
        public final String languageCode;
        public final String displayName;
        public final Locale locale;
        public final List<String> commonPhrases = new ArrayList<>();
        public final Map<String, String> customPronunciations = new HashMap<>();
        public Voice preferredVoice;
        public float speechRate = 1.0f;
        public float pitch = 1.0f;
        public boolean enabled = true;
        
        public LanguageProfile(String code, String name) {
            this.languageCode = code;
            this.displayName = name;
            this.locale = new Locale(code);
        }
    }
    
    /**
     * Interface for language detection callbacks
     */
    public interface LanguageDetectionCallback {
        void onLanguageDetected(String languageCode, float confidence);
    }
    
    /**
     * Interface for speech synthesis callbacks
     */
    public interface SpeechSynthesisCallback {
        void onSpeechStarted();
        void onSpeechFinished();
        void onSpeechError(String errorMessage);
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized MultilingualVoiceSupport getInstance(Context context) {
        if (instance == null) {
            instance = new MultilingualVoiceSupport(context);
        }
        return instance;
    }
    
    /**
     * Private constructor
     */
    private MultilingualVoiceSupport(Context context) {
        this.context = context.getApplicationContext();
        initialize();
    }
    
    /**
     * Initialize the multilingual support system
     */
    private void initialize() {
        Log.d(TAG, "Initializing multilingual voice support");
        
        // Initialize component references
        voiceCommandManager = VoiceCommandManager.getInstance(context);
        voiceResponseManager = VoiceResponseManager.getInstance(context);
        adaptationManager = HumanVoiceAdaptationManager.getInstance(context);
        soulfulVoiceSystem = SoulfulVoiceSystem.getInstance(context);
        
        // Set up supported languages
        initializeSupportedLanguages();
        
        // Initialize language detection patterns
        initializeLanguagePatterns();
        
        // Initialize TTS engines for each language
        initializeTTSEngines();
    }
    
    /**
     * Initialize supported languages
     */
    private void initializeSupportedLanguages() {
        // Add English (US)
        LanguageProfile english = new LanguageProfile("en", "English");
        english.commonPhrases.add("Hello, how can I help you?");
        english.commonPhrases.add("I'm your AI assistant.");
        english.commonPhrases.add("Would you like me to explain that further?");
        supportedLanguages.add(english);
        
        // Add Spanish
        LanguageProfile spanish = new LanguageProfile("es", "Spanish");
        spanish.commonPhrases.add("Hola, ¿cómo puedo ayudarte?");
        spanish.commonPhrases.add("Soy tu asistente de IA.");
        spanish.commonPhrases.add("¿Te gustaría que te explicara más sobre esto?");
        supportedLanguages.add(spanish);
        
        // Add French
        LanguageProfile french = new LanguageProfile("fr", "French");
        french.commonPhrases.add("Bonjour, comment puis-je vous aider?");
        french.commonPhrases.add("Je suis votre assistant IA.");
        french.commonPhrases.add("Souhaitez-vous que je développe davantage?");
        supportedLanguages.add(french);
        
        // Add German
        LanguageProfile german = new LanguageProfile("de", "German");
        german.commonPhrases.add("Hallo, wie kann ich Ihnen helfen?");
        german.commonPhrases.add("Ich bin Ihr KI-Assistent.");
        german.commonPhrases.add("Möchten Sie, dass ich das näher erläutere?");
        supportedLanguages.add(german);
        
        // Add Mandarin Chinese
        LanguageProfile chinese = new LanguageProfile("zh", "Mandarin Chinese");
        chinese.commonPhrases.add("你好，我能帮你什么忙？");
        chinese.commonPhrases.add("我是你的 AI 助手。");
        chinese.commonPhrases.add("你想让我进一步解释吗？");
        supportedLanguages.add(chinese);
        
        // Add Japanese
        LanguageProfile japanese = new LanguageProfile("ja", "Japanese");
        japanese.commonPhrases.add("こんにちは、どのようにお手伝いできますか？");
        japanese.commonPhrases.add("私はあなたのAIアシスタントです。");
        japanese.commonPhrases.add("さらに詳しく説明しましょうか？");
        supportedLanguages.add(japanese);
        
        // Add Hindi
        LanguageProfile hindi = new LanguageProfile("hi", "Hindi");
        hindi.commonPhrases.add("नमस्ते, मैं आपकी कैसे मदद कर सकता हूँ?");
        hindi.commonPhrases.add("मैं आपका AI सहायक हूँ।");
        hindi.commonPhrases.add("क्या आप चाहेंगे कि मैं इसे और विस्तार से समझाऊं?");
        supportedLanguages.add(hindi);
    }
    
    /**
     * Initialize language detection patterns
     */
    private void initializeLanguagePatterns() {
        // English patterns
        List<Pattern> englishPatterns = new ArrayList<>();
        englishPatterns.add(Pattern.compile("\\b(the|a|an|and|or|but|if|of|at|by|for|with|about)\\b", 
                                          Pattern.CASE_INSENSITIVE));
        englishPatterns.add(Pattern.compile("\\b(hello|hi|hey|thanks|thank you|please|sorry|excuse me)\\b", 
                                          Pattern.CASE_INSENSITIVE));
        languagePatterns.put("en", englishPatterns);
        
        // Spanish patterns
        List<Pattern> spanishPatterns = new ArrayList<>();
        spanishPatterns.add(Pattern.compile("\\b(el|la|los|las|un|una|unos|unas|y|o|pero|si|de|en|por|para|con|sobre)\\b", 
                                          Pattern.CASE_INSENSITIVE));
        spanishPatterns.add(Pattern.compile("\\b(hola|adiós|gracias|por favor|lo siento|disculpe)\\b", 
                                          Pattern.CASE_INSENSITIVE));
        languagePatterns.put("es", spanishPatterns);
        
        // French patterns
        List<Pattern> frenchPatterns = new ArrayList<>();
        frenchPatterns.add(Pattern.compile("\\b(le|la|les|un|une|des|et|ou|mais|si|de|à|par|pour|avec|sur)\\b", 
                                         Pattern.CASE_INSENSITIVE));
        frenchPatterns.add(Pattern.compile("\\b(bonjour|salut|merci|s'il vous plaît|désolé|excusez-moi)\\b", 
                                         Pattern.CASE_INSENSITIVE));
        languagePatterns.put("fr", frenchPatterns);
        
        // German patterns
        List<Pattern> germanPatterns = new ArrayList<>();
        germanPatterns.add(Pattern.compile("\\b(der|die|das|ein|eine|und|oder|aber|wenn|von|bei|für|mit|über)\\b", 
                                         Pattern.CASE_INSENSITIVE));
        germanPatterns.add(Pattern.compile("\\b(hallo|auf wiedersehen|danke|bitte|entschuldigung)\\b", 
                                         Pattern.CASE_INSENSITIVE));
        languagePatterns.put("de", germanPatterns);
        
        // Chinese patterns (simplified)
        List<Pattern> chinesePatterns = new ArrayList<>();
        chinesePatterns.add(Pattern.compile("[\\u4e00-\\u9fa5]+"));
        languagePatterns.put("zh", chinesePatterns);
        
        // Japanese patterns
        List<Pattern> japanesePatterns = new ArrayList<>();
        japanesePatterns.add(Pattern.compile("[\\u3040-\\u309F\\u30A0-\\u30FF]+"));
        languagePatterns.put("ja", japanesePatterns);
        
        // Hindi patterns
        List<Pattern> hindiPatterns = new ArrayList<>();
        hindiPatterns.add(Pattern.compile("[\\u0900-\\u097F]+"));
        languagePatterns.put("hi", hindiPatterns);
    }
    
    /**
     * Initialize TTS engines for each language
     */
    private void initializeTTSEngines() {
        for (LanguageProfile language : supportedLanguages) {
            initializeTTSForLanguage(language);
        }
    }
    
    /**
     * Initialize TTS engine for a specific language
     */
    private void initializeTTSForLanguage(final LanguageProfile language) {
        Log.d(TAG, "Initializing TTS for language: " + language.displayName);
        
        TextToSpeech tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(language.locale);
                    
                    if (result == TextToSpeech.LANG_MISSING_DATA || 
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Language not supported: " + language.languageCode);
                    } else {
                        Log.d(TAG, "TTS initialized for: " + language.displayName);
                        
                        // Find best voice for this language
                        findBestVoiceForLanguage(tts, language);
                    }
                } else {
                    Log.e(TAG, "Failed to initialize TTS for: " + language.displayName);
                }
            }
        });
        
        // Store TTS engine
        ttsEngines.put(language.languageCode, tts);
    }
    
    /**
     * Find the best voice for a language
     */
    private void findBestVoiceForLanguage(TextToSpeech tts, LanguageProfile language) {
        Set<Voice> voices = tts.getVoices();
        if (voices == null) return;
        
        Voice bestVoice = null;
        for (Voice voice : voices) {
            if (voice.getLocale().getLanguage().equals(language.locale.getLanguage())) {
                // Prefer female voices that are not network voices for reliability
                if (bestVoice == null || 
                    (!voice.isNetworkConnectionRequired() && bestVoice.isNetworkConnectionRequired()) ||
                    (!voice.getName().toLowerCase().contains("male") && 
                     bestVoice.getName().toLowerCase().contains("male"))) {
                    bestVoice = voice;
                }
            }
        }
        
        if (bestVoice != null) {
            language.preferredVoice = bestVoice;
            tts.setVoice(bestVoice);
            Log.d(TAG, "Set preferred voice for " + language.displayName + ": " + bestVoice.getName());
        }
    }
    
    /**
     * Set active language
     */
    public void setActiveLanguage(String languageCode) {
        if (ttsEngines.containsKey(languageCode)) {
            activeLanguage = languageCode;
            Log.d(TAG, "Active language set to: " + getLanguageNameFromCode(languageCode));
        } else {
            Log.e(TAG, "Language not supported: " + languageCode);
        }
    }
    
    /**
     * Get language name from code
     */
    private String getLanguageNameFromCode(String languageCode) {
        for (LanguageProfile language : supportedLanguages) {
            if (language.languageCode.equals(languageCode)) {
                return language.displayName;
            }
        }
        return languageCode;
    }
    
    /**
     * Speak text in the active language
     */
    public void speak(String text, final SpeechSynthesisCallback callback) {
        speak(text, activeLanguage, callback);
    }
    
    /**
     * Speak text in a specific language
     */
    public void speak(String text, String languageCode, final SpeechSynthesisCallback callback) {
        if (text == null || text.isEmpty()) {
            if (callback != null) {
                callback.onSpeechError("Empty text");
            }
            return;
        }
        
        final TextToSpeech tts = ttsEngines.get(languageCode);
        if (tts == null) {
            if (callback != null) {
                callback.onSpeechError("Language not supported: " + languageCode);
            }
            return;
        }
        
        // Apply emotional enhancement if soulful voice is available
        if (soulfulVoiceSystem != null) {
            text = soulfulVoiceSystem.enhanceTextWithEmotion(text);
        }
        
        // Get language profile
        LanguageProfile languageProfile = null;
        for (LanguageProfile profile : supportedLanguages) {
            if (profile.languageCode.equals(languageCode)) {
                languageProfile = profile;
                break;
            }
        }
        
        // Apply custom pronunciations if available
        if (languageProfile != null && !languageProfile.customPronunciations.isEmpty()) {
            for (Map.Entry<String, String> entry : languageProfile.customPronunciations.entrySet()) {
                text = text.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
            }
        }
        
        // Prepare speech parameters
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");
        
        // Set voice parameters
        if (languageProfile != null) {
            tts.setSpeechRate(languageProfile.speechRate);
            tts.setPitch(languageProfile.pitch);
            
            if (languageProfile.preferredVoice != null) {
                tts.setVoice(languageProfile.preferredVoice);
            }
        }
        
        // Set up utterance progress listener
        tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (callback != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSpeechStarted();
                        }
                    });
                }
            }
            
            @Override
            public void onDone(String utteranceId) {
                if (callback != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSpeechFinished();
                        }
                    });
                }
            }
            
            @Override
            public void onError(String utteranceId) {
                if (callback != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSpeechError("Speech synthesis error");
                        }
                    });
                }
            }
        });
        
        // Speak the text
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }
    
    /**
     * Detect the language of input text
     */
    public void detectLanguage(final String text, final LanguageDetectionCallback callback) {
        if (text == null || text.isEmpty()) {
            if (callback != null) {
                callback.onLanguageDetected("en", 0.0f);
            }
            return;
        }
        
        // Run detection in background
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Scores for each language
                Map<String, Float> scores = new HashMap<>();
                
                // Initialize scores
                for (LanguageProfile language : supportedLanguages) {
                    scores.put(language.languageCode, 0.0f);
                }
                
                // Check for language-specific patterns
                for (Map.Entry<String, List<Pattern>> entry : languagePatterns.entrySet()) {
                    String langCode = entry.getKey();
                    List<Pattern> patterns = entry.getValue();
                    
                    int matches = 0;
                    for (Pattern pattern : patterns) {
                        if (pattern.matcher(text).find()) {
                            matches++;
                        }
                    }
                    
                    if (patterns.size() > 0) {
                        scores.put(langCode, (float) matches / patterns.size());
                    }
                }
                
                // Find language with highest score
                String detectedLanguage = "en"; // Default to English
                float highestScore = 0.0f;
                
                for (Map.Entry<String, Float> entry : scores.entrySet()) {
                    if (entry.getValue() > highestScore) {
                        highestScore = entry.getValue();
                        detectedLanguage = entry.getKey();
                    }
                }
                
                // If score is too low, default to active language
                if (highestScore < LANGUAGE_DETECTION_THRESHOLD) {
                    detectedLanguage = activeLanguage;
                    highestScore = Math.max(0.5f, highestScore);
                }
                
                // Return result on main thread
                final String finalLanguage = detectedLanguage;
                final float finalConfidence = highestScore;
                
                if (callback != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onLanguageDetected(finalLanguage, finalConfidence);
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Get a greeting in the active language
     */
    public String getGreeting() {
        return getGreeting(activeLanguage);
    }
    
    /**
     * Get a greeting in a specific language
     */
    public String getGreeting(String languageCode) {
        switch (languageCode) {
            case "en":
                return generateEnglishGreeting();
                
            case "es":
                return generateSpanishGreeting();
                
            case "fr":
                return generateFrenchGreeting();
                
            case "de":
                return generateGermanGreeting();
                
            case "zh":
                return generateChineseGreeting();
                
            case "ja":
                return generateJapaneseGreeting();
                
            case "hi":
                return generateHindiGreeting();
                
            default:
                return generateEnglishGreeting();
        }
    }
    
    /**
     * Check if text is in Hindi
     * @param text Text to check
     * @return true if Hindi
     */
    public boolean isHindi(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Use language detector to check
        String detectedLanguage = detectLanguageSync(text);
        return detectedLanguage.equals(new Locale("hi"));
    }
    
    /**
     * Generate a greeting in Hindi based on time of day
     */
    public String generateHindiGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "सुप्रभात! मैं आपकी कैसे सहायता कर सकता हूँ?";
        } else if (hour < 17) {
            return "नमस्ते! मैं आपकी कैसे सहायता कर सकता हूँ?";
        } else {
            return "शुभ संध्या! मैं आपकी कैसे सहायता कर सकता हूँ?";
        }
    }
    
    /**
     * Generate a greeting in English based on time of day
     */
    public String generateEnglishGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "Good morning! How can I assist you today?";
        } else if (hour < 17) {
            return "Good afternoon! How can I assist you today?";
        } else {
            return "Good evening! How can I assist you today?";
        }
    }
    
    /**
     * Generate a greeting in Spanish based on time of day
     */
    public String generateSpanishGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "¡Buenos días! ¿En qué puedo ayudarte hoy?";
        } else if (hour < 17) {
            return "¡Buenas tardes! ¿En qué puedo ayudarte hoy?";
        } else {
            return "¡Buenas noches! ¿En qué puedo ayudarte hoy?";
        }
    }
    
    /**
     * Generate a greeting in French based on time of day
     */
    public String generateFrenchGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "Bonjour! Comment puis-je vous aider aujourd'hui?";
        } else if (hour < 17) {
            return "Bon après-midi! Comment puis-je vous aider aujourd'hui?";
        } else {
            return "Bonsoir! Comment puis-je vous aider aujourd'hui?";
        }
    }
    
    /**
     * Generate a greeting in German based on time of day
     */
    public String generateGermanGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "Guten Morgen! Wie kann ich Ihnen heute helfen?";
        } else if (hour < 17) {
            return "Guten Tag! Wie kann ich Ihnen heute helfen?";
        } else {
            return "Guten Abend! Wie kann ich Ihnen heute helfen?";
        }
    }
    
    /**
     * Generate a greeting in Chinese based on time of day
     */
    public String generateChineseGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "早上好！今天我能帮您什么忙？";
        } else if (hour < 17) {
            return "下午好！今天我能帮您什么忙？";
        } else {
            return "晚上好！今天我能帮您什么忙？";
        }
    }
    
    /**
     * Generate a greeting in Japanese based on time of day
     */
    public String generateJapaneseGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "おはようございます！今日はどのようにお手伝いできますか？";
        } else if (hour < 17) {
            return "こんにちは！今日はどのようにお手伝いできますか？";
        } else {
            return "こんばんは！今日はどのようにお手伝いできますか？";
        }
    }
    
    /**
     * Synchronous language detection (simplified)
     */
    private String detectLanguageSync(String text) {
        // Simple implementation
        for (Map.Entry<String, List<Pattern>> entry : languagePatterns.entrySet()) {
            String langCode = entry.getKey();
            List<Pattern> patterns = entry.getValue();
            
            for (Pattern pattern : patterns) {
                if (pattern.matcher(text).find()) {
                    return new Locale(langCode).toString();
                }
            }
        }
        
        return new Locale("en").toString(); // Default to English
    }
    
    /**
     * Add a new supported language
     */
    public boolean addLanguage(String languageCode, String displayName) {
        // Check if already supported
        for (LanguageProfile language : supportedLanguages) {
            if (language.languageCode.equals(languageCode)) {
                Log.d(TAG, "Language already supported: " + displayName);
                return false;
            }
        }
        
        // Create new language profile
        LanguageProfile newLanguage = new LanguageProfile(languageCode, displayName);
        supportedLanguages.add(newLanguage);
        
        // Initialize TTS for new language
        initializeTTSForLanguage(newLanguage);
        
        Log.d(TAG, "Added new language support: " + displayName);
        return true;
    }
    
    /**
     * Get list of supported languages
     */
    public List<LanguageProfile> getSupportedLanguages() {
        return new ArrayList<>(supportedLanguages);
    }
    
    /**
     * Set speech rate for a language
     */
    public void setSpeechRate(String languageCode, float rate) {
        for (LanguageProfile language : supportedLanguages) {
            if (language.languageCode.equals(languageCode)) {
                language.speechRate = Math.max(0.1f, Math.min(2.0f, rate));
                
                TextToSpeech tts = ttsEngines.get(languageCode);
                if (tts != null) {
                    tts.setSpeechRate(language.speechRate);
                }
                
                Log.d(TAG, "Set speech rate for " + language.displayName + ": " + rate);
                break;
            }
        }
    }
    
    /**
     * Set speech pitch for a language
     */
    public void setSpeechPitch(String languageCode, float pitch) {
        for (LanguageProfile language : supportedLanguages) {
            if (language.languageCode.equals(languageCode)) {
                language.pitch = Math.max(0.1f, Math.min(2.0f, pitch));
                
                TextToSpeech tts = ttsEngines.get(languageCode);
                if (tts != null) {
                    tts.setPitch(language.pitch);
                }
                
                Log.d(TAG, "Set speech pitch for " + language.displayName + ": " + pitch);
                break;
            }
        }
    }
    
    /**
     * Add custom pronunciation for a word in a specific language
     */
    public void addCustomPronunciation(String languageCode, String word, String pronunciation) {
        for (LanguageProfile language : supportedLanguages) {
            if (language.languageCode.equals(languageCode)) {
                language.customPronunciations.put(word, pronunciation);
                Log.d(TAG, "Added custom pronunciation for '" + word + "' in " + 
                          language.displayName + ": " + pronunciation);
                break;
            }
        }
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        Log.d(TAG, "Shutting down multilingual voice support");
        
        // Shutdown TTS engines
        for (TextToSpeech tts : ttsEngines.values()) {
            tts.stop();
            tts.shutdown();
        }
        
        ttsEngines.clear();
        
        // Shutdown executor
        executor.shutdown();
    }
}
