package org.levimc.launcher.ui.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import org.levimc.launcher.R;
import org.levimc.launcher.settings.FeatureSettings;
import org.levimc.launcher.ui.activities.MainActivity;
import org.levimc.launcher.ui.dialogs.SettingsDialog;
import org.levimc.launcher.util.GithubReleaseUpdater;
import org.levimc.launcher.util.ThemeManager;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link general_settings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class general_settings extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView reviewlauncher;
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
        reviewlauncher = view.findViewById(R.id.reviewlauncher);
        Spinner languageSpinner = view.findViewById(R.id.language_spinner);
        Spinner themeSpinner = view.findViewById(R.id.theme_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.language_spinner_array,  // phải trùng tên với strings.xml
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.theme_spinner_array,  // phải trùng tên với strings.xml
                R.layout.spinner_item
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(adapter2);
        reviewlauncher.setOnClickListener(v -> {
            // khi nhấn nút này thì mở debug log như mainActivity
            showSettingsSafely();
        });

        // Inflate the layout for this fragment
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