package com.aiassistant.research;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * InformationVerifier validates and fact-checks information gathered from the web,
 * helping ensure the reliability and credibility of research results presented to users.
 */
public class InformationVerifier {
    private static final String TAG = "InformationVerifier";
    
    // Core components
    private Context context;
    
    // Verification settings
    private double minConfidenceThreshold = 0.6;    // Minimum confidence for inclusion
    private double trustworthyDomainBonus = 0.2;    // Boost for reputable domains
    private double recentInfoBonus = 0.1;          // Boost for recent information
    private double multiSourceBonus = 0.15;         // Boost for info found in multiple sources
    private double contradictionPenalty = 0.3;      // Penalty for contradictory information
    
    // Domain reputation data
    private Map<String, Double> domainReputationScores = new HashMap<>();
    private Set<String> knownReliableDomains = new HashSet<>();
    private Set<String> knownUnreliableDomains = new HashSet<>();
    
    /**
     * Verified claim with confidence score
     */
    public static class VerifiedClaim {
        public String claim;
        public double confidence;
        public List<String> sources = new ArrayList<>();
        public List<String> supportingEvidence = new ArrayList<>();
        public List<String> contradictingEvidence = new ArrayList<>();
        public boolean isVerified;
        
        public VerifiedClaim(String claim) {
            this.claim = claim;
            this.confidence = 0.5;  // Default neutral confidence
            this.isVerified = false;
        }
    }
    
    public InformationVerifier(Context context) {
        this.context = context;
        initializeDomainReputationData();
    }
    
    /**
     * Initialize reputation data for known domains
     */
    private void initializeDomainReputationData() {
        // Add known reliable domains
        String[] reliableDomains = {
            "bbc.com", "bbc.co.uk", "reuters.com", "apnews.com", "npr.org", 
            "nytimes.com", "washingtonpost.com", "theguardian.com", 
            "economist.com", "scientificamerican.com", "nature.com", "science.org",
            "nih.gov", "who.int", "cdc.gov", "harvard.edu", "stanford.edu",
            "mit.edu", "ox.ac.uk", "cam.ac.uk", "nasa.gov", "noaa.gov",
            "espncricinfo.com", "icc-cricket.com", "cricket.com.au"
        };
        
        for (String domain : reliableDomains) {
            knownReliableDomains.add(domain);
            domainReputationScores.put(domain, 0.9);  // High reputation score
        }
        
        // Add domains with moderate reputation
        String[] moderateDomains = {
            "medium.com", "forbes.com", "businessinsider.com", "timesofindia.indiatimes.com",
            "hindustantimes.com", "ndtv.com", "wikipedia.org"
        };
        
        for (String domain : moderateDomains) {
            domainReputationScores.put(domain, 0.7);  // Moderate reputation score
        }
        
        // Add known unreliable domains
        String[] unreliableDomains = {
            "theonion.com", "clickbait.com", "fakesportsupdate.com"
        };
        
        for (String domain : unreliableDomains) {
            knownUnreliableDomains.add(domain);
            domainReputationScores.put(domain, 0.1);  // Low reputation score
        }
    }
    
    /**
     * Verify a set of claims using multiple sources
     */
    public List<VerifiedClaim> verifyClaims(List<String> claims, List<ResearchManager.SourceInfo> sources) {
        List<VerifiedClaim> verifiedClaims = new ArrayList<>();
        
        // Process each claim
        for (String claim : claims) {
            VerifiedClaim verifiedClaim = new VerifiedClaim(claim);
            
            // Check claim against sources
            verifyClaimAgainstSources(verifiedClaim, sources);
            
            // Only include claims that meet minimum confidence
            if (verifiedClaim.confidence >= minConfidenceThreshold) {
                verifiedClaims.add(verifiedClaim);
            }
        }
        
        return verifiedClaims;
    }
    
    /**
     * Verify a claim against multiple sources
     */
    private void verifyClaimAgainstSources(VerifiedClaim claim, List<ResearchManager.SourceInfo> sources) {
        int supportingSourceCount = 0;
        int contradictingSourceCount = 0;
        double confidenceSum = 0;
        
        for (ResearchManager.SourceInfo source : sources) {
            // Check if source content supports or contradicts the claim
            SourceEvaluation evaluation = evaluateSourceForClaim(claim.claim, source);
            
            if (evaluation.supports) {
                supportingSourceCount++;
                claim.supportingEvidence.add("From " + extractDomain(source.url) + ": " + evaluation.relevantText);
                claim.sources.add(source.url);
                confidenceSum += source.credibilityScore;
            } else if (evaluation.contradicts) {
                contradictingSourceCount++;
                claim.contradictingEvidence.add("From " + extractDomain(source.url) + ": " + evaluation.relevantText);
            }
        }
        
        // Calculate base confidence from supporting sources
        double baseConfidence = supportingSourceCount > 0 ? 
                confidenceSum / supportingSourceCount : 0.0;
        
        // Apply penalties for contradicting sources
        if (contradictingSourceCount > 0) {
            baseConfidence -= (contradictionPenalty * contradictingSourceCount);
        }
        
        // Apply bonus for multiple supporting sources
        if (supportingSourceCount > 1) {
            baseConfidence += multiSourceBonus * (1.0 - (1.0 / supportingSourceCount));
        }
        
        // Check source domain reputation
        for (String sourceUrl : claim.sources) {
            String domain = extractDomain(sourceUrl);
            Double reputationScore = domainReputationScores.get(domain);
            
            if (reputationScore != null && reputationScore > 0.8) {
                // Add reputation bonus for trustworthy sources
                baseConfidence += trustworthyDomainBonus;
                break; // Only apply once
            }
        }
        
        // Ensure confidence stays in valid range
        claim.confidence = Math.max(0.0, Math.min(1.0, baseConfidence));
        
        // Mark as verified if confidence is high enough and has multiple sources
        claim.isVerified = claim.confidence > 0.7 && supportingSourceCount >= 2;
    }
    
    /**
     * Source evaluation result
     */
    private static class SourceEvaluation {
        boolean supports;
        boolean contradicts;
        String relevantText;
    }
    
    /**
     * Evaluate if a source supports or contradicts a claim
     */
    private SourceEvaluation evaluateSourceForClaim(String claim, ResearchManager.SourceInfo source) {
        SourceEvaluation result = new SourceEvaluation();
        result.supports = false;
        result.contradicts = false;
        
        if (source.content == null || source.content.isEmpty()) {
            return result;
        }
        
        // Extract key terms from claim (simplified approach)
        List<String> keyTerms = extractKeyTerms(claim);
        
        // Check if a meaningful number of key terms are present in the source
        int termMatches = 0;
        for (String term : keyTerms) {
            if (source.content.toLowerCase().contains(term.toLowerCase())) {
                termMatches++;
            }
        }
        
        // Require at least 50% of terms to match
        double matchRatio = (double) termMatches / keyTerms.size();
        if (matchRatio < 0.5) {
            return result;
        }
        
        // Find paragraphs that have a high density of matching terms
        String[] paragraphs = source.content.split("\n+");
        String bestParagraph = "";
        int maxMatches = 0;
        
        for (String paragraph : paragraphs) {
            if (paragraph.length() < 20) continue; // Skip very short paragraphs
            
            int matches = 0;
            for (String term : keyTerms) {
                if (paragraph.toLowerCase().contains(term.toLowerCase())) {
                    matches++;
                }
            }
            
            if (matches > maxMatches) {
                maxMatches = matches;
                bestParagraph = paragraph;
            }
        }
        
        // Check if the paragraph supports or contradicts the claim
        if (!bestParagraph.isEmpty()) {
            result.relevantText = bestParagraph;
            
            // Check for contradiction markers
            boolean hasContradictionMarkers = containsContradictionMarkers(bestParagraph);
            
            // Look for semantic similarity or contradiction
            if (hasContradictionMarkers && isOpposite(claim, bestParagraph)) {
                result.contradicts = true;
            } else if (!hasContradictionMarkers) {
                result.supports = true;
            }
        }
        
        return result;
    }
    
    /**
     * Extract key terms from a text
     */
    private List<String> extractKeyTerms(String text) {
        List<String> terms = new ArrayList<>();
        
        // Split into words
        String[] words = text.split("\\s+");
        
        // Keep meaningful words (filter out common stopwords)
        for (String word : words) {
            // Remove punctuation
            word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            
            // Filter out short words and common stopwords
            if (word.length() > 3 && !isStopword(word)) {
                terms.add(word);
            }
        }
        
        return terms;
    }
    
    /**
     * Check if a word is a common stopword
     */
    private boolean isStopword(String word) {
        String[] stopwords = {
            "the", "and", "that", "have", "for", "not", "with", "you", "this", "but",
            "his", "her", "she", "they", "from", "will", "would", "could", "should",
            "what", "when", "where", "who", "how", "been", "were", "are", "was", "well"
        };
        
        for (String stopword : stopwords) {
            if (word.equals(stopword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if text contains contradiction markers
     */
    private boolean containsContradictionMarkers(String text) {
        String[] contradictionMarkers = {
            "however", "but", "although", "though", "yet", "nevertheless",
            "on the contrary", "in contrast", "despite", "in spite of",
            "not", "never", "no", "false", "incorrect", "untrue", "debunked"
        };
        
        String lowerText = text.toLowerCase();
        
        for (String marker : contradictionMarkers) {
            if (lowerText.contains(" " + marker + " ") || 
                    lowerText.startsWith(marker + " ") || 
                    lowerText.endsWith(" " + marker)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if two texts represent semantically opposite statements
     * This is a simplified implementation - actual semantic analysis would be more complex
     */
    private boolean isOpposite(String text1, String text2) {
        // Convert to lowercase
        text1 = text1.toLowerCase();
        text2 = text2.toLowerCase();
        
        // Check for simple negation patterns
        boolean text1HasNegation = hasNegation(text1);
        boolean text2HasNegation = hasNegation(text2);
        
        // If one has negation and the other doesn't, they may be opposites
        if (text1HasNegation != text2HasNegation) {
            // Check if they share key terms
            List<String> terms1 = extractKeyTerms(text1);
            List<String> terms2 = extractKeyTerms(text2);
            
            int sharedTerms = 0;
            for (String term : terms1) {
                if (terms2.contains(term)) {
                    sharedTerms++;
                }
            }
            
            // If they share a significant number of terms, they may be opposites
            return sharedTerms >= Math.min(terms1.size(), terms2.size()) / 2;
        }
        
        return false;
    }
    
    /**
     * Check if text contains negation markers
     */
    private boolean hasNegation(String text) {
        String[] negationMarkers = {
            " not ", " no ", " never ", " isn't ", " aren't ", " wasn't ", " weren't ",
            " hasn't ", " haven't ", " hadn't ", " doesn't ", " don't ", " didn't ",
            " won't ", " wouldn't ", " can't ", " cannot ", " couldn't ", " shouldn't "
        };
        
        for (String marker : negationMarkers) {
            if (text.contains(marker)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Extract domain from URL
     */
    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        // Remove protocol
        url = url.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)", "");
        
        // Extract domain (everything before first slash)
        int slashIndex = url.indexOf('/');
        if (slashIndex > 0) {
            url = url.substring(0, slashIndex);
        }
        
        return url;
    }
    
    /**
     * Check if a fact mentions a specific entity
     */
    public boolean factMentionsEntity(String fact, String entity) {
        if (fact == null || entity == null) {
            return false;
        }
        
        return fact.toLowerCase().contains(entity.toLowerCase());
    }
    
    /**
     * Check if a claim is a prediction rather than a fact
     */
    public boolean isPrediction(String claim) {
        if (claim == null || claim.isEmpty()) {
            return false;
        }
        
        // Check for prediction markers
        String[] predictionMarkers = {
            "will", "would", "going to", "predict", "forecast", "expect", "likely",
            "may", "might", "could", "projected", "anticipated", "future"
        };
        
        for (String marker : predictionMarkers) {
            if (claim.toLowerCase().contains(" " + marker + " ") || 
                    claim.toLowerCase().startsWith(marker + " ")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Classify claim type
     */
    public String classifyClaimType(String claim) {
        if (claim == null || claim.isEmpty()) {
            return "unknown";
        }
        
        // Check for different claim types
        if (isPrediction(claim)) {
            return "prediction";
        }
        
        if (claim.contains("?")) {
            return "question";
        }
        
        if (claim.toLowerCase().contains("should") || 
                claim.toLowerCase().contains("must") || 
                claim.toLowerCase().contains("ought to")) {
            return "opinion";
        }
        
        // Default to factual claim
        return "fact";
    }
    
    /**
     * Calculate confidence adjustment based on domain reputation
     */
    public double getDomainReputationAdjustment(String domain) {
        Double score = domainReputationScores.get(domain);
        if (score != null) {
            // Map 0.1-0.9 reputation to -0.2 to +0.2 adjustment
            return (score - 0.5) * 0.4;
        }
        
        return 0.0;  // Neutral for unknown domains
    }
    
    /**
     * Validate a sports prediction
     */
    public VerifiedClaim validateSportsPrediction(String prediction) {
        VerifiedClaim claim = new VerifiedClaim(prediction);
        
        // Sports predictions should always be marked as unverified
        claim.isVerified = false;
        claim.confidence = 0.0;
        
        // Add standard disclaimer
        claim.contradictingEvidence.add(
                "Sports predictions involve many variables and randomness that cannot be predicted with certainty. " +
                "Even with all available data, predicting sports outcomes is speculative.");
        
        return claim;
    }
}
