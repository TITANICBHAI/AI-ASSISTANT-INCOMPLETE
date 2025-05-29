package com.aiassistant.core.game;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.aiassistant.data.models.GameType;

import java.util.HashMap;
import java.util.Map;

/**
 * Detects game types based on package names and other heuristics
 */
public class GameDetector {
    private static final String TAG = "GameDetector";
    
    // Context
    private final Context context;
    
    // Cache of detected game types
    private final Map<String, GameType> gameTypeCache = new HashMap<>();
    
    // Known game packages
    private final Map<String, GameType> knownGames = new HashMap<>();
    
    /**
     * Constructor
     */
    public GameDetector(Context context) {
        this.context = context.getApplicationContext();
        
        // Initialize known games
        initializeKnownGames();
    }
    
    /**
     * Initialize database of known games
     */
    private void initializeKnownGames() {
        // FPS games
        knownGames.put("com.dts.freefireth", GameType.FPS);
        knownGames.put("com.dts.freefiremax", GameType.FPS);
        knownGames.put("com.tencent.ig", GameType.FPS);
        knownGames.put("com.pubg.imobile", GameType.FPS);
        knownGames.put("com.activision.callofduty.shooter", GameType.FPS);
        knownGames.put("com.tencent.tmgp.pubgmhd", GameType.FPS);
        knownGames.put("com.garena.game.codm", GameType.FPS);
        
        // MOBA games
        knownGames.put("com.mobile.legends", GameType.MOBA);
        knownGames.put("com.tencent.tmgp.sgame", GameType.MOBA);
        knownGames.put("com.riotgames.league.wildrift", GameType.MOBA);
        knownGames.put("com.supercell.brawlstars", GameType.MOBA);
        
        // RPG games
        knownGames.put("com.miHoYo.GenshinImpact", GameType.RPG);
        knownGames.put("com.netease.nmhshjlc", GameType.RPG);
        knownGames.put("com.netmarble.revolutionthm", GameType.RPG);
        
        // Racing games
        knownGames.put("com.ea.game.nfs14_row", GameType.RACING);
        knownGames.put("com.gameloft.android.ANMP.GloftA9HM", GameType.RACING);
        knownGames.put("com.naturalmotion.customstreetracer2", GameType.RACING);
        
        // Sports games
        knownGames.put("com.ea.gp.fifamobile", GameType.SPORTS);
        knownGames.put("com.firsttouchgames.dls7", GameType.SPORTS);
        
        // Strategy games
        knownGames.put("com.supercell.clashofclans", GameType.STRATEGY);
        knownGames.put("com.lilithgame.roc.gp", GameType.STRATEGY);
    }
    
    /**
     * Detect the type of a game based on package name
     * @param packageName Package name of the app
     * @return Game type
     */
    public GameType detectGameType(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return GameType.UNKNOWN;
        }
        
        // Check cache first
        if (gameTypeCache.containsKey(packageName)) {
            return gameTypeCache.get(packageName);
        }
        
        // Check known games
        if (knownGames.containsKey(packageName)) {
            GameType type = knownGames.get(packageName);
            gameTypeCache.put(packageName, type);
            Log.d(TAG, "Detected known game: " + packageName + " as " + type);
            return type;
        }
        
        // Try to infer game type from package name or category
        GameType inferredType = inferGameType(packageName);
        gameTypeCache.put(packageName, inferredType);
        
        return inferredType;
    }
    
    /**
     * Infer game type from package name and app metadata
     * @param packageName Package name
     * @return Inferred game type
     */
    private GameType inferGameType(String packageName) {
        // Try to get app info
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            
            // Check app name and category
            String appName = pm.getApplicationLabel(appInfo).toString().toLowerCase();
            
            // Check for keywords in package name or app name
            if (containsKeyword(packageName, appName, "fps", "shooter", "shoot", "gun", "battle", "fire", "combat")) {
                Log.d(TAG, "Inferred FPS game: " + packageName);
                return GameType.FPS;
            } else if (containsKeyword(packageName, appName, "moba", "arena", "legend", "hero", "clash")) {
                Log.d(TAG, "Inferred MOBA game: " + packageName);
                return GameType.MOBA;
            } else if (containsKeyword(packageName, appName, "rpg", "role", "adventure", "quest", "fantasy", "impact")) {
                Log.d(TAG, "Inferred RPG game: " + packageName);
                return GameType.RPG;
            } else if (containsKeyword(packageName, appName, "race", "racing", "speed", "car", "asphalt", "drift")) {
                Log.d(TAG, "Inferred RACING game: " + packageName);
                return GameType.RACING;
            } else if (containsKeyword(packageName, appName, "sport", "football", "soccer", "basketball", "fifa", "nba")) {
                Log.d(TAG, "Inferred SPORTS game: " + packageName);
                return GameType.SPORTS;
            } else if (containsKeyword(packageName, appName, "strategy", "kingdom", "clash", "castle", "tower", "empire")) {
                Log.d(TAG, "Inferred STRATEGY game: " + packageName);
                return GameType.STRATEGY;
            } else if (containsKeyword(packageName, appName, "puzzle", "casual", "match", "candy", "farm", "ville")) {
                Log.d(TAG, "Inferred CASUAL game: " + packageName);
                return GameType.CASUAL;
            }
            
            // Check if it's classified as a game in the Play Store
            if ((appInfo.category >= ApplicationInfo.CATEGORY_GAME && appInfo.category <= ApplicationInfo.CATEGORY_ARCADE) ||
                    appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                Log.d(TAG, "Detected generic game: " + packageName);
                return GameType.UNKNOWN;
            }
            
        } catch (PackageManager.NameNotFoundException | Exception e) {
            Log.e(TAG, "Error getting app info for: " + packageName + ", " + e.getMessage());
        }
        
        return GameType.UNKNOWN;
    }
    
    /**
     * Check if package name or app name contains any of the given keywords
     */
    private boolean containsKeyword(String packageName, String appName, String... keywords) {
        for (String keyword : keywords) {
            if (packageName.toLowerCase().contains(keyword) || 
                    appName.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clear the game type cache
     */
    public void clearCache() {
        gameTypeCache.clear();
    }
}
