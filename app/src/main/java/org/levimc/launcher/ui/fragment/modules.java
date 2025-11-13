package org.levimc.launcher.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.levimc.launcher.R;
import org.levimc.launcher.core.mods.FileHandler;
import org.levimc.launcher.core.mods.Mod;
import org.levimc.launcher.core.versions.VersionManager;
import org.levimc.launcher.ui.adapter.ModsAdapter;
import org.levimc.launcher.ui.dialogs.CustomAlertDialog;
import org.levimc.launcher.ui.views.MainViewModel;
import org.levimc.launcher.ui.views.MainViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class modules extends Fragment {

    private RecyclerView modulesRecycler;
    private ImageButton addModuleButton;
    private TextView modulesTitleText;
    private ModsAdapter modsAdapter;
    private MainViewModel viewModel;
    private FileHandler fileHandler;
    private VersionManager versionManager;
    private ActivityResultLauncher<Intent> soImportResultLauncher;

    public modules() {
        // Required empty public constructor
    }

    public static modules newInstance() {
        return new modules();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        versionManager = VersionManager.get(requireContext());

        // Initialize ActivityResultLauncher
        soImportResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null && fileHandler != null) {
                        fileHandler.processIncomingFilesWithConfirmation(result.getData(), new FileHandler.FileOperationCallback() {
                            @Override
                            public void onSuccess(int processedFiles) {
                                if (processedFiles > 0) {
                                    Toast.makeText(getContext(), getString(R.string.files_processed, processedFiles), Toast.LENGTH_SHORT).show();
                                    if (viewModel != null) {
                                        viewModel.refreshMods();
                                    }
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                if (errorMessage != null && !errorMessage.equals(getString(R.string.user_cancelled))) {
                                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onProgressUpdate(int progress) {
                                // Update progress if needed
                            }
                        }, true);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_module, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        modulesRecycler = view.findViewById(R.id.modules_recycler);
        addModuleButton = view.findViewById(R.id.add_module_button);
        modulesTitleText = view.findViewById(R.id.modules_title_text);

        initViewModel();
        initModulesRecycler();
        initListeners();
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this, new MainViewModelFactory(requireActivity().getApplication()))
                .get(MainViewModel.class);
        viewModel.getModsLiveData().observe(getViewLifecycleOwner(), this::updateModulesUI);

        // Initialize FileHandler after viewModel is ready
        fileHandler = new FileHandler(requireContext(), viewModel, versionManager);
    }

    private void initModulesRecycler() {
        modsAdapter = new ModsAdapter(new ArrayList<>());
        modulesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        modulesRecycler.setAdapter(modsAdapter);

        modsAdapter.setOnModEnableChangeListener((mod, enabled) -> {
            if (viewModel != null) {
                viewModel.setModEnabled(mod.getFileName(), enabled);
            }
        });

        modsAdapter.setOnModReorderListener(reorderedMods -> {
            if (viewModel != null) {
                viewModel.reorderMods(reorderedMods);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), R.string.project_id, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                modsAdapter.moveItem(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Mod mod = modsAdapter.getItem(pos);
                new CustomAlertDialog(requireContext())
                        .setTitleText(getString(R.string.dialog_title_delete_mod))
                        .setMessage(getString(R.string.dialog_message_delete_mod))
                        .setPositiveButton(getString(R.string.dialog_positive_delete), v -> {
                            viewModel.removeMod(mod);
                            modsAdapter.removeAt(pos);
                        })
                        .setNegativeButton(getString(R.string.dialog_negative_cancel), v -> {
                            modsAdapter.notifyItemChanged(pos);
                        })
                        .show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(modulesRecycler);
    }

    private void initListeners() {
        addModuleButton.setOnClickListener(v -> startFilePicker());
    }

    private void startFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        soImportResultLauncher.launch(intent);
    }

    private void updateModulesUI(List<Mod> mods) {
        modsAdapter.updateMods(mods != null ? mods : new ArrayList<>());
        int moduleCount = (mods != null) ? mods.size() : 0;
        modulesTitleText.setText(getString(R.string.modules_title, moduleCount));
    }
}