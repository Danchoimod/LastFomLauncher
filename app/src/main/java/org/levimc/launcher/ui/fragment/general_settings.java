package org.levimc.launcher.ui.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import org.levimc.launcher.R;
import org.levimc.launcher.settings.FeatureSettings;
import org.levimc.launcher.ui.activities.MainActivity;
import org.levimc.launcher.ui.dialogs.SettingsDialog;
import org.levimc.launcher.util.GithubReleaseUpdater;
import org.levimc.launcher.util.ThemeBackgroundManager;
import org.levimc.launcher.util.ThemeManager;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class general_settings extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextView reviewlauncher;
    private Spinner themeSpinner;
    private ThemeBackgroundManager themeBackgroundManager;
    public general_settings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment general_settings.
     */
    // TODO: Rename and change types and number of parameters
    public static general_settings newInstance(String param1, String param2) {
        general_settings fragment = new general_settings();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general_settings, container, false);

        // Initialize ThemeBackgroundManager
        themeBackgroundManager = new ThemeBackgroundManager(requireContext());

        reviewlauncher = view.findViewById(R.id.reviewlauncher);
        Spinner languageSpinner = view.findViewById(R.id.language_spinner);
        themeSpinner = view.findViewById(R.id.theme_spinner);

        // Setup language spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.language_spinner_array,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Setup theme spinner
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.theme_spinner_array,
                R.layout.spinner_item
        );
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);

        // Set current selected theme
        int savedThemeIndex = themeBackgroundManager.getSelectedThemeIndex();
        themeSpinner.setSelection(savedThemeIndex);

        // Setup theme selection listener
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Save selected theme
                themeBackgroundManager.saveSelectedTheme(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        reviewlauncher.setOnClickListener(v -> {
            showSettingsSafely();
        });

        return view;
    }

    private void showSettingsSafely() {
        try {
            showSettingsDialog();
        } catch (PackageManager.NameNotFoundException e) {
            //Toast.makeText(this, R.string.error_load_setting, Toast.LENGTH_SHORT).show();
        }
    }
    private void showSettingsDialog() throws PackageManager.NameNotFoundException {
        FeatureSettings fs = FeatureSettings.getInstance();
        SettingsDialog dlg = new SettingsDialog(requireContext());

        dlg.addSwitchItem(
                getString(R.string.enable_debug_log),
                fs.isDebugLogDialogEnabled(),
                (btn, check) -> fs.setDebugLogDialogEnabled(check)
        );
        dlg.addSwitchItem(
                getString(R.string.version_isolation),
                fs.isVersionIsolationEnabled(),
                (btn, check) -> fs.setVersionIsolationEnabled(check)
        );
        dlg.show();
    }
}