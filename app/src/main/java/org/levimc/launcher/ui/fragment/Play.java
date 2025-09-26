package org.levimc.launcher.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.levimc.launcher.R;
import org.levimc.launcher.core.minecraft.MinecraftLauncher;
import org.levimc.launcher.core.mods.FileHandler;
import org.levimc.launcher.core.versions.GameVersion;
import org.levimc.launcher.core.versions.VersionManager;
import org.levimc.launcher.databinding.FragmentPlayBinding;
import org.levimc.launcher.settings.FeatureSettings;
import org.levimc.launcher.ui.activities.MainActivity;
import org.levimc.launcher.ui.dialogs.GameVersionSelectDialog;
import org.levimc.launcher.ui.dialogs.gameversionselect.BigGroup;
import org.levimc.launcher.ui.dialogs.gameversionselect.VersionUtil;
import org.levimc.launcher.ui.views.MainViewModel;
import org.levimc.launcher.ui.views.MainViewModelFactory;
import org.levimc.launcher.util.ApkImportManager;
import org.levimc.launcher.util.LanguageManager;
import org.levimc.launcher.util.PermissionsHandler;
import org.levimc.launcher.util.UIHelper;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Play extends Fragment {
    static {
        System.loadLibrary("leviutils");
    }
    public interface PlayListener {
        void onPlayClicked();
    }
    private MainViewModel viewModel;
    private PlayListener listener;
    private VersionManager versionManager;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;

    private PermissionsHandler permissionsHandler;
    private ActivityResultLauncher<Intent> permissionResultLauncher;
    private String mParam2;
    private FragmentPlayBinding binding;
    private MinecraftLauncher minecraftLauncher;

    public Play() {}

    public static Play newInstance(String param1, String param2) {
        Play fragment = new Play();
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
        binding = FragmentPlayBinding.inflate(inflater, container, false);
        // Khởi tạo managers tại đây nếu cần
        versionManager = VersionManager.get(getContext());
        versionManager.loadAllVersions();
        minecraftLauncher = new MinecraftLauncher(requireActivity(), requireActivity().getClassLoader());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListeners();
        setTextMinecraftVersion();
        updateViewModelVersion();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void launchGame() {
        binding.launchButton.setEnabled(false);
        binding.progressLoader.setVisibility(View.VISIBLE);

        GameVersion version = versionManager != null ? versionManager.getSelectedVersion() : null;
        minecraftLauncher.launch(requireActivity().getIntent(), version);

        requireActivity().runOnUiThread(() -> {
            if (binding != null) {
                binding.progressLoader.setVisibility(View.GONE);
                binding.launchButton.setEnabled(true);
            }
        });
    }


    @SuppressLint({"ClickableViewAccessibility", "UnsafeIntentLaunch"})
    private void initListeners() {
        binding.launchButton.setOnClickListener(v -> {launchGame();
            Toast.makeText(getContext(), "launch", Toast.LENGTH_SHORT).show();
        });
        binding.selectVersionButton.setOnClickListener(v -> showVersionSelectDialog());

    }
    private void setupManagersAndHandlers() {
        // Nếu muốn ViewModel scope theo Fragment:
        viewModel = new ViewModelProvider(this, new MainViewModelFactory(requireActivity().getApplication()))
                .get(MainViewModel.class);

        // VersionManager lấy context của Fragment
        versionManager = VersionManager.get(requireContext());
        versionManager.loadAllVersions();

        // MinecraftLauncher nhận context là activity, classloader cũng từ activity
        minecraftLauncher = new MinecraftLauncher(requireActivity(), requireActivity().getClassLoader());

        // ActivityResultLauncher cho Fragment
        permissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (permissionsHandler != null)
                        permissionsHandler.onActivityResult(result.getResultCode(), result.getData());
                }
        );

        permissionsHandler = PermissionsHandler.getInstance();
        permissionsHandler.setActivity(requireActivity(), permissionResultLauncher);

        initListeners();
    }
    private void updateViewModelVersion() {
        GameVersion selectedVersion = versionManager.getSelectedVersion();
        if (selectedVersion != null && viewModel != null) {
            viewModel.setCurrentVersion(selectedVersion);
        }
    }

    private void showVersionSelectDialog() {
        if (versionManager == null) return;
        versionManager.loadAllVersions();
        List<BigGroup> bigGroups = VersionUtil.buildBigGroups(
                versionManager.getInstalledVersions(),
                versionManager.getCustomVersions()
        );
        GameVersionSelectDialog dialog = new GameVersionSelectDialog(getContext(), bigGroups);
        dialog.setOnVersionSelectListener(version -> {
            versionManager.selectVersion(version);
            if (viewModel != null)
                viewModel.setCurrentVersion(version);
            setTextMinecraftVersion();
        });
        dialog.show();
    }

    public void setTextMinecraftVersion() {
        if (binding == null) return;
        String display = versionManager.getSelectedVersion() != null ?
                versionManager.getSelectedVersion().displayName : getString(R.string.not_found_version);
        binding.textMinecraftVersion.setText(TextUtils.isEmpty(display) ? getString(R.string.not_found_version) : display);
        updateAbiLabel();
    }

    private void updateAbiLabel() {
        if (binding == null) return;
        TextView abiLabel = binding.abiLabel;
        String abiList = (versionManager.getSelectedVersion() != null) ? versionManager.getSelectedVersion().abiList : null;
        String abiToShow = "unknown";
        if (!TextUtils.isEmpty(abiList) && !"unknown".equals(abiList)) {
            abiToShow = abiList.split("\\n")[0].trim();
        }
        abiLabel.setText(abiToShow);
        int bgRes = switch (abiToShow) {
            case "arm64-v8a" -> R.drawable.bg_abi_arm64_v8a;
            case "armeabi-v7a" -> R.drawable.bg_abi_armeabi_v7a;
            case "x86" -> R.drawable.bg_abi_x86;
            case "x86_64" -> R.drawable.bg_abi_x86_64;
            default -> R.drawable.bg_abi_default;
        };
//        abiLabel.setBackgroundResource(bgRes);
    }
}