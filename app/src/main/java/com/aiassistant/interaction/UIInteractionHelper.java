package com.aiassistant.interaction;

import android.content.Context;
import android.util.Log;

import com.aiassistant.data.models.GameType;
import com.aiassistant.data.models.UIElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for UI interaction detection and analysis.
 * Provides methods for categorizing UI elements, determining game types,
 * and analyzing UI hierarchies.
 */
public class UIInteractionHelper {
    private static final String TAG = "UIInteractionHelper";
    
    // Categories of UI elements
    public static final String CATEGORY_BUTTON = "button";
    public static final String CATEGORY_JOYSTICK = "joystick";
    public static final String CATEGORY_SLIDER = "slider";
    public static final String CATEGORY_TEXT = "text";
    public static final String CATEGORY_HEALTH_BAR = "health_bar";
    public static final String CATEGORY_AMMO_DISPLAY = "ammo_display";
    public static final String CATEGORY_MINIMAP = "minimap";
    public static final String CATEGORY_MENU = "menu";
    public static final String CATEGORY_OTHER = "other";
    
    // Text patterns for identifying game elements
    private static final Map<String, String> BUTTON_PATTERNS = new HashMap<>();
    private static final Map<String, String> MENU_PATTERNS = new HashMap<>();
    private static final Map<String, String> GAME_TYPE_PATTERNS = new HashMap<>();
    
    private Context context;
    
    // Initialize pattern maps
    static {
        // Button identification patterns
        BUTTON_PATTERNS.put("start", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("play", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("exit", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("settings", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("options", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("cancel", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("confirm", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("ok", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("yes", CATEGORY_BUTTON);
        BUTTON_PATTERNS.put("no", CATEGORY_BUTTON);
        
        // Menu identification patterns
        MENU_PATTERNS.put("main menu", CATEGORY_MENU);
        MENU_PATTERNS.put("settings", CATEGORY_MENU);
        MENU_PATTERNS.put("options", CATEGORY_MENU);
        MENU_PATTERNS.put("inventory", CATEGORY_MENU);
        MENU_PATTERNS.put("loadout", CATEGORY_MENU);
        MENU_PATTERNS.put("character", CATEGORY_MENU);
        
        // Game type identification patterns
        GAME_TYPE_PATTERNS.put("pubg", "PUBG_MOBILE");
        GAME_TYPE_PATTERNS.put("playerunknown", "PUBG_MOBILE");
        GAME_TYPE_PATTERNS.put("battlegrounds", "PUBG_MOBILE");
        GAME_TYPE_PATTERNS.put("free fire", "FREE_FIRE");
        GAME_TYPE_PATTERNS.put("freefire", "FREE_FIRE");
        GAME_TYPE_PATTERNS.put("call of duty", "COD_MOBILE");
        GAME_TYPE_PATTERNS.put("cod", "COD_MOBILE");
        GAME_TYPE_PATTERNS.put("call of duty mobile", "COD_MOBILE");
    }
    
    /**
     * Constructor with context
     * @param context Application context
     */
    public UIInteractionHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Categorize a UI element based on its properties and content
     * @param element The UI element to categorize
     * @return The category of the element
     */
    public String categorizeElement(UIElement element) {
        // Check by element type first
        if (element.getElementType() == UIElement.ELEMENT_TYPE_BUTTON) {
            return CATEGORY_BUTTON;
        } else if (element.getElementType() == UIElement.ELEMENT_TYPE_JOYSTICK) {
            return CATEGORY_JOYSTICK;
        } else if (element.getElementType() == UIElement.ELEMENT_TYPE_SLIDER) {
            return CATEGORY_SLIDER;
        } else if (element.getElementType() == UIElement.ELEMENT_TYPE_TEXT) {
            return CATEGORY_TEXT;
        } else if (element.getElementType() == UIElement.ELEMENT_TYPE_HEALTH_BAR) {
            return CATEGORY_HEALTH_BAR;
        } else if (element.getElementType() == UIElement.ELEMENT_TYPE_AMMO_DISPLAY) {
            return CATEGORY_AMMO_DISPLAY;
        } else if (element.getElementType() == UIElement.ELEMENT_TYPE_MINIMAP) {
            return CATEGORY_MINIMAP;
        } else if (element.getElementType() == UIElement.ELEMENT_TYPE_MENU) {
            return CATEGORY_MENU;
        }
        
        // Check by text content if available
        String text = element.getText();
        if (text != null && !text.isEmpty()) {
            text = text.toLowerCase();
            for (Map.Entry<String, String> entry : BUTTON_PATTERNS.entrySet()) {
                if (text.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
            
            for (Map.Entry<String, String> entry : MENU_PATTERNS.entrySet()) {
                if (text.contains(entry.getKey())) {
                    return CATEGORY_MENU;
                }
            }
        }
        
        // Check by properties
        if (element.isClickable() && element.getWidth() < 300 && element.getHeight() < 300) {
            return CATEGORY_BUTTON;
        }
        
        // Default category
        return CATEGORY_OTHER;
    }
    
    /**
     * Try to infer the game type from UI elements
     * @param elements Collection of UI elements
     * @return The inferred game type or UNKNOWN
     */
    public GameType inferGameType(Collection<UIElement> elements) {
        // Check text content of elements for game-specific patterns
        for (UIElement element : elements) {
            String text = element.getText();
            if (text != null && !text.isEmpty()) {
                text = text.toLowerCase();
                for (Map.Entry<String, String> entry : GAME_TYPE_PATTERNS.entrySet()) {
                    if (text.contains(entry.getKey())) {
                        try {
                            return GameType.valueOf(entry.getValue());
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Invalid game type: " + entry.getValue());
                        }
                    }
                }
            }
        }
        
        // If no match found, try to infer from other properties
        int pubgScore = 0;
        int freeFireScore = 0;
        int codScore = 0;
        
        // Count elements with game-specific colors or patterns
        for (UIElement element : elements) {
            String primaryColor = element.getPrimaryColor();
            if (primaryColor != null) {
                // PUBG tends to use green, yellow, and military colors
                if (primaryColor.equals("#4CAF50") || primaryColor.equals("#CDDC39")) {
                    pubgScore++;
                }
                // Free Fire uses more bright colors
                else if (primaryColor.equals("#FF5722") || primaryColor.equals("#F44336")) {
                    freeFireScore++;
                }
                // COD uses more dark and red colors
                else if (primaryColor.equals("#D32F2F") || primaryColor.equals("#212121")) {
                    codScore++;
                }
            }
        }
        
        // Return the game type with the highest score
        if (pubgScore > freeFireScore && pubgScore > codScore) {
            return GameType.PUBG_MOBILE;
        } else if (freeFireScore > pubgScore && freeFireScore > codScore) {
            return GameType.FREE_FIRE;
        } else if (codScore > pubgScore && codScore > freeFireScore) {
            return GameType.COD_MOBILE;
        }
        
        // Default to unknown
        return GameType.UNKNOWN;
    }
    
    /**
     * Check if the current screen is likely in a menu based on UI elements
     * @param elements Collection of UI elements
     * @return True if likely in a menu
     */
    public boolean isLikelyInMenu(Collection<UIElement> elements) {
        int menuElements = 0;
        int gameplayElements = 0;
        
        for (UIElement element : elements) {
            String category = categorizeElement(element);
            
            // Count menu-related elements
            if (category.equals(CATEGORY_MENU) || category.equals(CATEGORY_BUTTON)) {
                menuElements++;
            }
            
            // Count gameplay-related elements
            if (category.equals(CATEGORY_JOYSTICK) || 
                category.equals(CATEGORY_HEALTH_BAR) ||
                category.equals(CATEGORY_AMMO_DISPLAY) ||
                category.equals(CATEGORY_MINIMAP)) {
                gameplayElements++;
            }
            
            // Check for menu-specific text
            String text = element.getText();
            if (text != null && !text.isEmpty()) {
                text = text.toLowerCase();
                for (String menuPattern : MENU_PATTERNS.keySet()) {
                    if (text.contains(menuPattern)) {
                        menuElements += 2; // Extra weight for text matches
                    }
                }
            }
        }
        
        // If we have significantly more menu elements than gameplay elements, likely in a menu
        return menuElements > gameplayElements * 2;
    }
    
    /**
     * Find all elements matching a specific category
     * @param elements Collection of elements to search
     * @param category Category to match
     * @return List of matching elements
     */
    public List<UIElement> findElementsByCategory(Collection<UIElement> elements, String category) {
        List<UIElement> results = new ArrayList<>();
        
        for (UIElement element : elements) {
            if (categorizeElement(element).equals(category)) {
                results.add(element);
            }
        }
        
        return results;
    }
    
    /**
     * Find all elements containing specific text
     * @param elements Collection of elements to search
     * @param text Text to match (case insensitive)
     * @return List of matching elements
     */
    public List<UIElement> findElementsByText(Collection<UIElement> elements, String text) {
        List<UIElement> results = new ArrayList<>();
        String lowercaseText = text.toLowerCase();
        
        for (UIElement element : elements) {
            String elementText = element.getText();
            if (elementText != null && elementText.toLowerCase().contains(lowercaseText)) {
                results.add(element);
            }
        }
        
        return results;
    }
    
    /**
     * Find the most prominent element (by size and position) matching a category
     * @param elements Collection of elements to search
     * @param category Category to match
     * @return The most prominent matching element, or null if none found
     */
    public UIElement findMostProminentElement(Collection<UIElement> elements, String category) {
        List<UIElement> matching = findElementsByCategory(elements, category);
        UIElement mostProminent = null;
        int highestProminence = 0;
        
        for (UIElement element : matching) {
            // Calculate prominence score based on size and position
            int prominence = element.getWidth() * element.getHeight();
            
            // Bonus for elements near the center of the screen
            int centerX = 540; // Assuming 1080p screen
            int centerY = 960;
            int distanceFromCenter = (int) Math.sqrt(
                Math.pow(element.getCenterX() - centerX, 2) + 
                Math.pow(element.getCenterY() - centerY, 2)
            );
            prominence -= distanceFromCenter;
            
            if (prominence > highestProminence) {
                highestProminence = prominence;
                mostProminent = element;
            }
        }
        
        return mostProminent;
    }
}
