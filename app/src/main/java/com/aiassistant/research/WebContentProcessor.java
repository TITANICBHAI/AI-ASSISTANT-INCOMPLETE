package com.aiassistant.research;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 * WebContentProcessor handles fetching, parsing, and extracting information
 * from web pages. It provides functionality to clean HTML, extract meaningful
 * content, and analyze web page structure.
 */
public class WebContentProcessor {
    private static final String TAG = "WebContentProcessor";
    
    // Default user agent
    private static final String DEFAULT_USER_AGENT = 
            "Mozilla/5.0 (Android) AIAssistantApp/1.0";
    
    // Connection settings
    private int connectionTimeout = 10000; // 10 seconds
    private int readTimeout = 15000; // 15 seconds
    private String userAgent = DEFAULT_USER_AGENT;
    
    // Content processing settings
    private int maxContentLength = 500000; // Characters
    private boolean followRedirects = true;
    private boolean extractImages = false;
    
    /**
     * Extracted web page content
     */
    public static class WebPageContent {
        public String url;
        public String title;
        public String mainContent;
        public List<String> paragraphs = new ArrayList<>();
        public List<String> headings = new ArrayList<>();
        public Map<String, String> metadata = new HashMap<>();
        public List<String> imageUrls = new ArrayList<>();
        public int statusCode;
        public long fetchTime;
        public String contentType;
        public int contentLength;
        
        public WebPageContent(String url) {
            this.url = url;
        }
    }
    
    public WebContentProcessor() {
        // Default constructor
    }
    
    /**
     * Set connection timeout
     */
    public void setConnectionTimeout(int timeoutMs) {
        this.connectionTimeout = timeoutMs;
    }
    
    /**
     * Set read timeout
     */
    public void setReadTimeout(int timeoutMs) {
        this.readTimeout = timeoutMs;
    }
    
    /**
     * Set user agent string
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    /**
     * Set maximum content length
     */
    public void setMaxContentLength(int maxLength) {
        this.maxContentLength = maxLength;
    }
    
    /**
     * Set whether to follow HTTP redirects
     */
    public void setFollowRedirects(boolean follow) {
        this.followRedirects = follow;
    }
    
    /**
     * Set whether to extract image URLs
     */
    public void setExtractImages(boolean extract) {
        this.extractImages = extract;
    }
    
    /**
     * Fetch and process web page content
     */
    public WebPageContent fetchAndProcessUrl(String url) throws IOException {
        WebPageContent content = new WebPageContent(url);
        long startTime = System.currentTimeMillis();
        
        HttpURLConnection connection = null;
        try {
            // Create connection
            URL urlObj = new URL(url);
            if (url.startsWith("https")) {
                connection = (HttpsURLConnection) urlObj.openConnection();
            } else {
                connection = (HttpURLConnection) urlObj.openConnection();
            }
            
            // Configure connection
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setInstanceFollowRedirects(followRedirects);
            
            // Get response
            content.statusCode = connection.getResponseCode();
            
            if (content.statusCode == HttpURLConnection.HTTP_OK) {
                // Get content type and length
                content.contentType = connection.getContentType();
                content.contentLength = connection.getContentLength();
                
                // Only process HTML content
                if (content.contentType != null && content.contentType.toLowerCase().contains("text/html")) {
                    // Read content
                    String htmlContent = readStream(connection.getInputStream());
                    
                    // Process HTML content
                    processHtmlContent(htmlContent, content);
                } else {
                    Log.w(TAG, "Skipping non-HTML content: " + content.contentType);
                }
            } else {
                Log.w(TAG, "HTTP error: " + content.statusCode + " for URL: " + url);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            content.fetchTime = System.currentTimeMillis() - startTime;
        }
        
        return content;
    }
    
    /**
     * Read input stream to string
     */
    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder content = new StringBuilder();
        String line;
        int totalLength = 0;
        
        while ((line = reader.readLine()) != null && totalLength < maxContentLength) {
            content.append(line).append("\n");
            totalLength += line.length();
        }
        
        reader.close();
        return content.toString();
    }
    
    /**
     * Process HTML content and extract relevant information
     */
    private void processHtmlContent(String htmlContent, WebPageContent content) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return;
        }
        
        // Extract title
        content.title = extractTitle(htmlContent);
        
        // Extract metadata
        extractMetadata(htmlContent, content);
        
        // Extract headings
        extractHeadings(htmlContent, content);
        
        // Extract main content
        content.mainContent = extractMainContent(htmlContent);
        
        // Extract paragraphs
        extractParagraphs(htmlContent, content);
        
        // Extract images if enabled
        if (extractImages) {
            extractImages(htmlContent, content);
        }
    }
    
    /**
     * Extract page title
     */
    private String extractTitle(String htmlContent) {
        Pattern titlePattern = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = titlePattern.matcher(htmlContent);
        
        if (matcher.find()) {
            String title = matcher.group(1).trim();
            // Clean up title (remove extra whitespace, etc.)
            title = title.replaceAll("\\s+", " ");
            return title;
        }
        
        return "";
    }
    
    /**
     * Extract metadata from meta tags
     */
    private void extractMetadata(String htmlContent, WebPageContent content) {
        // Extract meta tags
        Pattern metaPattern = Pattern.compile("<meta[^>]*\\s+(?:name|property)\\s*=\\s*[\"']([^\"']*)[\"'][^>]*\\s+content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>|" +
                                             "<meta[^>]*\\s+content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*\\s+(?:name|property)\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>",
                                             Pattern.CASE_INSENSITIVE);
        
        Matcher matcher = metaPattern.matcher(htmlContent);
        while (matcher.find()) {
            String name;
            String metaContent;
            
            if (matcher.group(1) != null) {
                name = matcher.group(1).trim().toLowerCase();
                metaContent = matcher.group(2).trim();
            } else {
                name = matcher.group(4).trim().toLowerCase();
                metaContent = matcher.group(3).trim();
            }
            
            // Store important metadata
            if (name.equals("description") || name.equals("keywords") ||
                name.startsWith("og:") || name.startsWith("twitter:")) {
                content.metadata.put(name, metaContent);
            }
        }
    }
    
    /**
     * Extract headings (h1-h6)
     */
    private void extractHeadings(String htmlContent, WebPageContent content) {
        Pattern headingPattern = Pattern.compile("<h([1-6])[^>]*>(.*?)</h\\1>", 
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        Matcher matcher = headingPattern.matcher(htmlContent);
        while (matcher.find()) {
            String headingText = matcher.group(2).trim();
            
            // Clean up heading text
            headingText = cleanHtml(headingText);
            
            if (!headingText.isEmpty()) {
                content.headings.add(headingText);
            }
        }
    }
    
    /**
     * Extract paragraphs
     */
    private void extractParagraphs(String htmlContent, WebPageContent content) {
        Pattern paragraphPattern = Pattern.compile("<p[^>]*>(.*?)</p>", 
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        Matcher matcher = paragraphPattern.matcher(htmlContent);
        while (matcher.find()) {
            String paragraphText = matcher.group(1).trim();
            
            // Clean up paragraph text
            paragraphText = cleanHtml(paragraphText);
            
            if (!paragraphText.isEmpty()) {
                content.paragraphs.add(paragraphText);
            }
        }
    }
    
    /**
     * Extract image URLs
     */
    private void extractImages(String htmlContent, WebPageContent content) {
        Pattern imagePattern = Pattern.compile("<img[^>]*\\s+src\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>", 
                Pattern.CASE_INSENSITIVE);
        
        Matcher matcher = imagePattern.matcher(htmlContent);
        while (matcher.find()) {
            String imageUrl = matcher.group(1).trim();
            
            // Convert relative URLs to absolute
            if (!imageUrl.startsWith("http")) {
                imageUrl = resolveRelativeUrl(content.url, imageUrl);
            }
            
            content.imageUrls.add(imageUrl);
        }
    }
    
    /**
     * Clean HTML content (remove tags, fix entities, etc.)
     */
    private String cleanHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        
        // Remove HTML tags
        String text = html.replaceAll("<[^>]*>", "");
        
        // Decode HTML entities
        text = decodeHtmlEntities(text);
        
        // Normalize whitespace
        text = text.replaceAll("\\s+", " ").trim();
        
        return text;
    }
    
    /**
     * Decode common HTML entities
     */
    private String decodeHtmlEntities(String text) {
        // Replace common HTML entities
        text = text.replace("&nbsp;", " ")
                   .replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&apos;", "'")
                   .replace("&#39;", "'");
        
        // Replace numeric entities
        Pattern numericEntity = Pattern.compile("&#(\\d+);");
        Matcher matcher = numericEntity.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            int charCode = Integer.parseInt(matcher.group(1));
            matcher.appendReplacement(result, String.valueOf((char) charCode));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Extract main content from HTML using heuristics
     */
    private String extractMainContent(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }
        
        // This is a simplified content extraction algorithm
        // In a real implementation, you'd use more sophisticated techniques
        
        // First, remove parts of the page that are unlikely to be main content
        String cleaned = removeUnlikelyContent(htmlContent);
        
        // Extract what is likely the main content area
        String mainContentHtml = extractContentArea(cleaned);
        
        // Clean up the extracted content
        return cleanHtml(mainContentHtml);
    }
    
    /**
     * Remove unlikely content areas like navigation, ads, etc.
     */
    private String removeUnlikelyContent(String html) {
        // Remove script tags and content
        html = html.replaceAll("<script[^>]*>.*?</script>", "", Pattern.DOTALL);
        
        // Remove style tags and content
        html = html.replaceAll("<style[^>]*>.*?</style>", "", Pattern.DOTALL);
        
        // Remove comments
        html = html.replaceAll("<!--.*?-->", "", Pattern.DOTALL);
        
        // Remove likely navigation areas
        html = html.replaceAll("<nav[^>]*>.*?</nav>", "", Pattern.DOTALL);
        html = html.replaceAll("<header[^>]*>.*?</header>", "", Pattern.DOTALL);
        html = html.replaceAll("<footer[^>]*>.*?</footer>", "", Pattern.DOTALL);
        
        // Remove likely ad containers
        html = html.replaceAll("<div[^>]*(?:id|class)[^>]*(?:ad|banner|widget|sidebar)[^>]*>.*?</div>", "", 
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        return html;
    }
    
    /**
     * Extract likely content area based on content density
     */
    private String extractContentArea(String html) {
        // Look for likely content containers
        String[] contentContainers = {
            "<article[^>]*>(.*?)</article>",
            "<section[^>]*>(.*?)</section>",
            "<div[^>]*(?:id|class)[^>]*(?:content|main|article|post|body)[^>]*>(.*?)</div>",
            "<div[^>]*>(.*?)</div>"  // Fallback to any div with content
        };
        
        for (String containerPattern : contentContainers) {
            Pattern pattern = Pattern.compile(containerPattern, 
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);
            
            String bestMatch = "";
            int bestScore = 0;
            
            while (matcher.find()) {
                String match = matcher.group(1);
                
                // Simple scoring based on text length and paragraph count
                int paragraphCount = countMatches(match, "<p");
                int textLength = cleanHtml(match).length();
                int score = textLength + (paragraphCount * 50);  // Weight paragraphs
                
                if (score > bestScore && textLength > 200) {  // Require minimum content
                    bestScore = score;
                    bestMatch = match;
                }
            }
            
            if (!bestMatch.isEmpty()) {
                return bestMatch;
            }
        }
        
        // If no good content area found, return cleaned full HTML
        return html;
    }
    
    /**
     * Count pattern matches in text
     */
    private int countMatches(String text, String pattern) {
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        
        return count;
    }
    
    /**
     * Resolve relative URL to absolute URL
     */
    private String resolveRelativeUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl.startsWith("http")) {
            return relativeUrl;  // Already absolute
        }
        
        if (relativeUrl.startsWith("//")) {
            // Protocol-relative URL
            if (baseUrl.startsWith("https:")) {
                return "https:" + relativeUrl;
            } else {
                return "http:" + relativeUrl;
            }
        }
        
        try {
            URL base = new URL(baseUrl);
            
            if (relativeUrl.startsWith("/")) {
                // Absolute path
                return new URL(base.getProtocol(), base.getHost(), base.getPort(), relativeUrl).toString();
            } else {
                // Relative path
                String path = base.getPath();
                if (!path.endsWith("/")) {
                    // Get parent directory
                    int lastSlash = path.lastIndexOf('/');
                    if (lastSlash >= 0) {
                        path = path.substring(0, lastSlash + 1);
                    } else {
                        path = "/";
                    }
                }
                
                return new URL(base.getProtocol(), base.getHost(), base.getPort(), path + relativeUrl).toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resolving relative URL: " + e.getMessage());
            return relativeUrl;  // Return unchanged if error
        }
    }
}
