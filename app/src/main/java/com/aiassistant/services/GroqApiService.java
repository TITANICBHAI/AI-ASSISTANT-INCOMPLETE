package com.aiassistant.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroqApiService {
    private static final String TAG = "GroqApiService";
    
    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";
    private static final int TIMEOUT_MS = 30000;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    
    private static GroqApiService instance;
    private final Context context;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final GroqApiKeyManager apiKeyManager;
    
    private GroqApiService(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(3);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.apiKeyManager = GroqApiKeyManager.getInstance(context);
    }
    
    public static synchronized GroqApiService getInstance(Context context) {
        if (instance == null) {
            instance = new GroqApiService(context);
        }
        return instance;
    }
    
    public interface ChatCompletionCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public interface StreamingCallback {
        void onChunk(String chunk);
        void onComplete();
        void onError(String error);
    }
    
    public void chatCompletion(String prompt, ChatCompletionCallback callback) {
        chatCompletion(prompt, DEFAULT_MODEL, callback);
    }
    
    public void chatCompletion(String prompt, String model, ChatCompletionCallback callback) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", prompt));
        chatCompletion(messages, model, callback);
    }
    
    public void chatCompletion(List<ChatMessage> messages, String model, ChatCompletionCallback callback) {
        executorService.execute(() -> {
            String apiKey = apiKeyManager.getApiKey();
            
            if (apiKey == null || apiKey.isEmpty()) {
                notifyError(callback, "API key not configured. Please set your Groq API key in settings.");
                return;
            }
            
            String response = chatCompletionSync(messages, model, apiKey, false);
            
            if (response != null) {
                notifySuccess(callback, response);
            } else {
                notifyError(callback, "Failed to get response from Groq API");
            }
        });
    }
    
    public void chatCompletionStreaming(String prompt, StreamingCallback callback) {
        chatCompletionStreaming(prompt, DEFAULT_MODEL, callback);
    }
    
    public void chatCompletionStreaming(String prompt, String model, StreamingCallback callback) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", prompt));
        chatCompletionStreaming(messages, model, callback);
    }
    
    public void chatCompletionStreaming(List<ChatMessage> messages, String model, StreamingCallback callback) {
        executorService.execute(() -> {
            String apiKey = apiKeyManager.getApiKey();
            
            if (apiKey == null || apiKey.isEmpty()) {
                notifyStreamError(callback, "API key not configured. Please set your Groq API key in settings.");
                return;
            }
            
            streamChatCompletion(messages, model, apiKey, callback);
        });
    }
    
    private String chatCompletionSync(List<ChatMessage> messages, String model, String apiKey, boolean stream) {
        HttpURLConnection connection = null;
        int retries = 0;
        
        while (retries < MAX_RETRIES) {
            try {
                URL url = new URL(BASE_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setDoOutput(true);
                
                JSONObject requestBody = buildRequestBody(messages, model, stream);
                
                BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)
                );
                writer.write(requestBody.toString());
                writer.flush();
                writer.close();
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readStream(connection.getInputStream());
                    return parseResponse(response);
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.e(TAG, "Invalid API key");
                    return null;
                } else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE || 
                           responseCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
                    retries++;
                    if (retries < MAX_RETRIES) {
                        Log.w(TAG, "Server error, retrying... (" + retries + "/" + MAX_RETRIES + ")");
                        Thread.sleep(RETRY_DELAY_MS * retries);
                        continue;
                    }
                } else {
                    String errorResponse = readStream(connection.getErrorStream());
                    Log.e(TAG, "API error: " + responseCode + " - " + errorResponse);
                    return null;
                }
                
            } catch (IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage(), e);
                retries++;
                if (retries < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * retries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON error: " + e.getMessage(), e);
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Interrupted: " + e.getMessage(), e);
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        
        Log.e(TAG, "Max retries reached");
        return null;
    }
    
    private void streamChatCompletion(List<ChatMessage> messages, String model, String apiKey, StreamingCallback callback) {
        HttpURLConnection connection = null;
        
        try {
            URL url = new URL(BASE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS * 2);
            connection.setDoOutput(true);
            
            JSONObject requestBody = buildRequestBody(messages, model, true);
            
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)
            );
            writer.write(requestBody.toString());
            writer.flush();
            writer.close();
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        
                        if (data.equals("[DONE]")) {
                            notifyStreamComplete(callback);
                            break;
                        }
                        
                        try {
                            JSONObject jsonData = new JSONObject(data);
                            JSONArray choices = jsonData.getJSONArray("choices");
                            
                            if (choices.length() > 0) {
                                JSONObject choice = choices.getJSONObject(0);
                                JSONObject delta = choice.getJSONObject("delta");
                                
                                if (delta.has("content")) {
                                    String content = delta.getString("content");
                                    notifyStreamChunk(callback, content);
                                }
                                
                                String finishReason = choice.optString("finish_reason", null);
                                if ("stop".equals(finishReason)) {
                                    notifyStreamComplete(callback);
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing streaming data: " + e.getMessage());
                        }
                    }
                }
                
                reader.close();
                
            } else {
                String errorResponse = readStream(connection.getErrorStream());
                Log.e(TAG, "API error: " + responseCode + " - " + errorResponse);
                notifyStreamError(callback, "API error: " + responseCode);
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Streaming error: " + e.getMessage(), e);
            notifyStreamError(callback, "Network error: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "JSON error: " + e.getMessage(), e);
            notifyStreamError(callback, "JSON error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private JSONObject buildRequestBody(List<ChatMessage> messages, String model, boolean stream) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("stream", stream);
        
        JSONArray messagesArray = new JSONArray();
        for (ChatMessage message : messages) {
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", message.getRole());
            messageObj.put("content", message.getContent());
            messagesArray.put(messageObj);
        }
        requestBody.put("messages", messagesArray);
        
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2048);
        
        return requestBody;
    }
    
    private String parseResponse(String jsonResponse) {
        try {
            JSONObject response = new JSONObject(jsonResponse);
            JSONArray choices = response.getJSONArray("choices");
            
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                return message.getString("content");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        
        reader.close();
        return result.toString();
    }
    
    private void notifySuccess(ChatCompletionCallback callback, String response) {
        mainHandler.post(() -> callback.onSuccess(response));
    }
    
    private void notifyError(ChatCompletionCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }
    
    private void notifyStreamChunk(StreamingCallback callback, String chunk) {
        mainHandler.post(() -> callback.onChunk(chunk));
    }
    
    private void notifyStreamComplete(StreamingCallback callback) {
        mainHandler.post(callback::onComplete);
    }
    
    private void notifyStreamError(StreamingCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
    
    public static class ChatMessage {
        private final String role;
        private final String content;
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getContent() {
            return content;
        }
    }
}
