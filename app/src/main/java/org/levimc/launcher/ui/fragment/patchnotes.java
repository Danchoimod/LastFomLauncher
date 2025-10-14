package org.levimc.launcher.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.levimc.launcher.R;
import org.levimc.launcher.ui.adapter.PatchNotesAdapter;
import org.levimc.launcher.ui.entity.PatchNoteItem;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link patchnotes#newInstance} factory method to
 * create an instance of this fragment.
 */
public class patchnotes extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PatchNotesAdapter adapter;
    private List<PatchNoteItem> patchNotesList;

    public patchnotes() {
        // Required empty public constructor
    }

    public static patchnotes newInstance() {
        return new patchnotes();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_patchnotes, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPatchNotes);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView với GridLayoutManager (4 cột)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);

        patchNotesList = new ArrayList<>();

        // Load data
        loadPatchNotes();

        return view;
    }

    private void loadPatchNotes() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // Đọc file patchnotes.json
                File appDir = requireContext().getExternalFilesDir(null);
                File patchnotesFile = new File(appDir, "patchnotes.json");
                File imagesDir = new File(appDir, "images");

                if (!patchnotesFile.exists()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Patchnotes file not found", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                    return;
                }

                // Đọc nội dung file
                FileInputStream fis = new FileInputStream(patchnotesFile);
                byte[] buffer = new byte[(int) patchnotesFile.length()];
                fis.read(buffer);
                fis.close();
                String jsonContent = new String(buffer, StandardCharsets.UTF_8);

                // Parse JSON
                JSONObject root = new JSONObject(jsonContent);
                JSONArray patchnotes = root.getJSONArray("patchnotes");

                List<PatchNoteItem> items = new ArrayList<>();
                for (int i = 0; i < patchnotes.length(); i++) {
                    JSONObject item = patchnotes.getJSONObject(i);
                    String id = item.getString("id");
                    String title = item.getString("title");
                    String img = item.getString("img");
                    String url = item.getString("url");

                    items.add(new PatchNoteItem(id, title, img, url));
                }

                // Cập nhật UI
                requireActivity().runOnUiThread(() -> {
                    patchNotesList.clear();
                    patchNotesList.addAll(items);

                    adapter = new PatchNotesAdapter(getContext(), patchNotesList, imagesDir);
                    recyclerView.setAdapter(adapter);

                    progressBar.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error loading patchnotes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }
}