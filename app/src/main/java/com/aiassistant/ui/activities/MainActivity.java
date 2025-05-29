package com.aiassistant.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NavController navController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize the AI state manager with context
        AIStateManager.getInstance(this);
        
        // Set up Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        
        // Set up app bar configuration for correct back button behavior
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardFragment, R.id.gamesFragment, R.id.aiModesFragment, 
                R.id.settingsFragment, R.id.learningFragment)
                .build();
        
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        
        // Handle navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dashboardFragment:
                        navController.navigate(R.id.dashboardFragment);
                        return true;
                    case R.id.gamesFragment:
                        navController.navigate(R.id.gamesFragment);
                        return true;
                    case R.id.aiModesFragment:
                        navController.navigate(R.id.aiModesFragment);
                        return true;
                    case R.id.settingsFragment:
                        navController.navigate(R.id.settingsFragment);
                        return true;
                    case R.id.learningFragment:
                        navController.navigate(R.id.learningFragment);
                        return true;
                }
                return false;
            }
        });
        
        Log.d(TAG, "MainActivity created");
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up AI state manager
        AIStateManager.getInstance().stop();
        Log.d(TAG, "MainActivity destroyed");
    }
}
