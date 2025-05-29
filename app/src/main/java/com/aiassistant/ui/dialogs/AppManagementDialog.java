package com.aiassistant.ui.dialogs;

import android.app.Dialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.aiassistant.R;
import com.aiassistant.data.models.AppInfo;
import com.aiassistant.ui.adapters.AppSelectionAdapter;
import com.aiassistant.ui.viewmodels.AppManagementViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dialog for managing which applications the AI is allowed to interact with
 * and what level of permissions each app has.
 */
public class AppManagementDialog extends DialogFragment {

    private AppManagementViewModel viewModel;
    private SearchView searchView;
    private ListView appListView;
    private AppSelectionAdapter adapter;
    private Button selectAllButton;
    private Button clearAllButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AppManagementViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_app_management, null);
        
        initViews(view);
        loadInstalledApps();
        setupListeners();
        
        builder.setView(view)
                .setTitle(R.string.manage_apps)
                .setPositiveButton(R.string.save, (dialog, id) -> saveAppSelections())
                .setNegativeButton(R.string.cancel, (dialog, id) -> dismiss());
        
        return builder.create();
    }
    
    private void initViews(View view) {
        searchView = view.findViewById(R.id.search_apps);
        appListView = view.findViewById(R.id.list_apps);
        selectAllButton = view.findViewById(R.id.button_select_all);
        clearAllButton = view.findViewById(R.id.button_clear_all);
    }
    
    private void loadInstalledApps() {
        List<AppInfo> appInfoList = new ArrayList<>();
        
        PackageManager pm = requireContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        for (ApplicationInfo app : apps) {
            // Only include non-system apps
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String appName = pm.getApplicationLabel(app).toString();
                String packageName = app.packageName;
                appInfoList.add(new AppInfo(packageName, appName, app.loadIcon(pm)));
            }
        }
        
        // Sort by app name
        Collections.sort(appInfoList, (a1, a2) -> 
                a1.getAppName().compareToIgnoreCase(a2.getAppName()));
        
        // Load allowed apps from view model and mark them
        viewModel.getAllowedApps().observe(this, allowedApps -> {
            for (AppInfo appInfo : appInfoList) {
                appInfo.setSelected(allowedApps.contains(appInfo.getPackageName()));
            }
            
            if (adapter == null) {
                adapter = new AppSelectionAdapter(requireContext(), appInfoList);
                appListView.setAdapter(adapter);
            } else {
                adapter.updateData(appInfoList);
            }
        });
    }
    
    private void setupListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        
        selectAllButton.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.selectAll();
            }
        });
        
        clearAllButton.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.clearAll();
            }
        });
    }
    
    private void saveAppSelections() {
        if (adapter != null) {
            List<String> selectedPackages = adapter.getSelectedPackages();
            viewModel.saveAllowedApps(selectedPackages);
            
            Toast.makeText(requireContext(), 
                    getString(R.string.saved_app_selections, selectedPackages.size()), 
                    Toast.LENGTH_SHORT).show();
        }
    }
}