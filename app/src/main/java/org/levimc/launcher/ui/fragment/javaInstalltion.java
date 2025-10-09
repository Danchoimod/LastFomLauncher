package org.levimc.launcher.ui.fragment;

import static android.app.Activity.RESULT_OK;
import org.levimc.launcher.core.mods.FileHandler;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import org.levimc.launcher.R;
import org.levimc.launcher.core.minecraft.MinecraftLauncher;
import org.levimc.launcher.core.versions.VersionManager;
import org.levimc.launcher.databinding.ActivityMainBinding;
import org.levimc.launcher.databinding.FragmentJavaInstalltionBinding;
import org.levimc.launcher.databinding.FragmentPlayBinding;
import org.levimc.launcher.settings.FeatureSettings;
import org.levimc.launcher.ui.activities.MainActivity;
import org.levimc.launcher.ui.adapter.VersionAdapter;
import org.levimc.launcher.ui.dialogs.CustomAlertDialog;
import org.levimc.launcher.ui.dialogs.gameversionselect.BigGroup;
import org.levimc.launcher.ui.dialogs.gameversionselect.UltimateVersionAdapter;
import org.levimc.launcher.ui.dialogs.gameversionselect.VersionGroup;
import org.levimc.launcher.ui.views.MainViewModel;
import org.levimc.launcher.ui.views.MainViewModelFactory;
import org.levimc.launcher.util.ApkImportManager;
import org.levimc.launcher.util.LanguageManager;
import org.levimc.launcher.util.PermissionsHandler;
import org.levimc.launcher.util.UIHelper;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.levimc.launcher.core.versions.GameVersion;

import java.util.ArrayList;
import java.util.List;

//import java.util.logging.FileHandler;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link javaInstalltion#newInstance} factory method to
 * create an instance of this fragment.
 */
public class javaInstalltion extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FileHandler fileHandler;
    private ActivityResultLauncher<Intent> apkImportResultLauncher;
    private ActivityResultLauncher<Intent> soImportResultLauncher;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ApkImportManager apkImportManager;
    private TextView importApk;
    private ActivityResultLauncher<Intent> permissionResultLauncher;
    private VersionManager versionManager;
    private PermissionsHandler permissionsHandler;
    private MainViewModel viewModel;
    private UltimateVersionAdapter ultimateVersionAdapter;
    public javaInstalltion() {
        // Required empty public constructor
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(GameVersion gameVersion);
    }
    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
    static {
        System.loadLibrary("leviutils");
    }
    private FragmentJavaInstalltionBinding binding;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment javaInstalltion.
     */
    // TODO: Rename and change types and number of parameters
    public static javaInstalltion newInstance(String param1, String param2) {
        javaInstalltion fragment = new javaInstalltion();
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
        binding = FragmentJavaInstalltionBinding.inflate(inflater, container, false);
        setupManagersAndHandlers();
        binding.tvVersions.setVisibility(View.INVISIBLE);
        binding.llVersions.setVisibility(View.INVISIBLE);
        binding.tvNewInstallations.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new installVersion())
                    .commit();
        });

        return binding.getRoot();


    }


    @SuppressLint({"ClickableViewAccessibility", "UnsafeIntentLaunch"})
    private void initListeners() {
        binding.importApkButton.setOnClickListener(v -> startFilePicker("application/vnd.android.package-archive", apkImportResultLauncher));
//        binding.deleteVersionButton.setOnClickListener(v -> showDeleteVersionDialog());
    }
    private void startFilePicker(String type, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        launcher.launch(intent);
    }

    private void setupManagersAndHandlers() {
        // Nếu muốn ViewModel scope theo Fragment:
        viewModel = new ViewModelProvider(this, new MainViewModelFactory(requireActivity().getApplication()))
                .get(MainViewModel.class);

        // VersionManager lấy context của Fragment
        versionManager = VersionManager.get(requireContext());
        versionManager.loadAllVersions();
        // Nhóm các phiên bản thành BigGroup/VersionGroup giống dialog
        List<GameVersion> installed = versionManager.getInstalledVersions();
        List<GameVersion> custom = versionManager.getCustomVersions();
        List<BigGroup> bigGroups = new ArrayList<>();
        // Nhóm chính: Phiên bản cài đặt (Official)
        BigGroup installedGroup = new BigGroup(R.string.installed_versions);
        VersionGroup installedVerGroup = new VersionGroup("Auto-detected, read-only info.");
        installedVerGroup.versions.addAll(installed);
        installedGroup.versionGroups.add(installedVerGroup);
        bigGroups.add(installedGroup);
        // Nhóm phụ: Phiên bản tuỳ chỉnh (Custom)
        if (!custom.isEmpty()) {
            BigGroup customGroup = new BigGroup(R.string.custom_versions);
            VersionGroup customVerGroup = new VersionGroup("Import APK and launchable.");
            customVerGroup.versions.addAll(custom);
            customGroup.versionGroups.add(customVerGroup);
            bigGroups.add(customGroup);
        }
        // Khởi tạo UltimateVersionAdapter và set cho RecyclerView
        ultimateVersionAdapter = new UltimateVersionAdapter(requireContext(), bigGroups);
        RecyclerView rv = binding.rvInstallations;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(ultimateVersionAdapter);
        // ActivityResultLauncher cho Fragment
        permissionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (permissionsHandler != null)
                        permissionsHandler.onActivityResult(result.getResultCode(), result.getData());
                }
        );
        ultimateVersionAdapter.setOnVersionLongClickListener(version -> showDeleteVersionDialog(version));
        // Khởi tạo ApkImportManager đúng chuẩn: truyền requireActivity() thay vì requireContext()
        // Đăng ký ActivityResultLauncher cho import APK
        apkImportManager = new ApkImportManager(requireActivity(), viewModel);        apkImportResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (apkImportManager != null)
                        apkImportManager.handleActivityResult(result.getResultCode(), result.getData());
                }
        );
        // Gắn sự kiện cho nút import APK
        soImportResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && fileHandler != null) {
                        fileHandler.processIncomingFilesWithConfirmation(result.getData(), new org.levimc.launcher.core.mods.FileHandler.FileOperationCallback() {
                            @Override
                            public void onSuccess(int processedFiles) {
                                UIHelper.showToast(getContext(), getString(R.string.files_processed, processedFiles));
                            }

                            @Override
                            public void onError(String errorMessage) {
                            }

                            @Override
                            public void onProgressUpdate(int progress) {
                                if (binding != null) binding.progressLoader.setProgress(progress);
                            }
                        }, true);
                    }
                }
        );

        permissionsHandler = PermissionsHandler.getInstance();
        permissionsHandler.setActivity(requireActivity(), permissionResultLauncher);
        initListeners();
    }

//    private void handleIncomingFiles() {
//        if (fileHandler == null) return;
//        fileHandler.processIncomingFilesWithConfirmation(getContext(), new org.levimc.launcher.core.mods.FileHandler.FileOperationCallback() {
//            @Override
//            public void onSuccess(int processedFiles) {
//                if (processedFiles > 0)
//                    UIHelper.showToast(getContext(), getString(R.string.files_processed, processedFiles));
//            }
//
//            @Override
//            public void onError(String errorMessage) {
//            }
//
//            @Override
//            public void onProgressUpdate(int progress) {
//                if (binding != null) binding.progressLoader.setProgress(progress);
//            }
//        }, false);
//    }
private void showDeleteVersionDialog(GameVersion version) {
    new CustomAlertDialog(requireContext())
            .setTitleText(getString(R.string.dialog_title_delete_version))
            .setMessage(getString(R.string.dialog_message_delete_version))
            .setPositiveButton(getString(R.string.dialog_positive_delete), v2 -> {
                VersionManager.get(requireContext()).deleteCustomVersion(version,
                        new VersionManager.OnDeleteVersionCallback() {
                            @Override
                            public void onDeleteCompleted(boolean success) {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), getString(R.string.toast_delete_success), Toast.LENGTH_SHORT).show();
                                    viewModel.setCurrentVersion(versionManager.getSelectedVersion());
                                });
                            }
                            @Override
                            public void onDeleteFailed(Exception e) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), getString(R.string.toast_delete_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
                            }
                        });
            })
            .setNegativeButton(getString(R.string.dialog_negative_cancel), null)
            .show();
}


}
