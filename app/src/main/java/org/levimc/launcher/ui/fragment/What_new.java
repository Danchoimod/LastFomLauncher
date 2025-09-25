package org.levimc.launcher.ui.fragment;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import org.levimc.launcher.R;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;
import org.levimc.launcher.ui.adapter.WhatsNewAdapter;
import org.levimc.launcher.ui.entity.WhatsNewItem;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link What_new#newInstance} factory method to
 * create an instance of this fragment.
 */
public class What_new extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public What_new() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment whatnew.
     */
    // TODO: Rename and change types and number of parameters
    public static What_new newInstance(String param1, String param2) {
        What_new fragment = new What_new();
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
        View root = inflater.inflate(R.layout.fragment_whatnew, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.whatsNewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<WhatsNewItem> items = new ArrayList<>();
        try {
            AssetManager assetManager = requireContext().getAssets();
            InputStream is = assetManager.open("whats_new.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                items.add(new WhatsNewItem(
                    obj.getString("date"),
                    obj.getString("version"),
                    obj.getString("description")
                ));
            }
        } catch (Exception e) {
            Log.e("WhatNew", "Error loading whats_new.json", e);
        }
        WhatsNewAdapter adapter = new WhatsNewAdapter(items);
        recyclerView.setAdapter(adapter);
        return root;
    }
}