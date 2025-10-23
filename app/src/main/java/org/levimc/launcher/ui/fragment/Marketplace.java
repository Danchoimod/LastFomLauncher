package org.levimc.launcher.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.levimc.launcher.R;
import org.levimc.launcher.databinding.FragmentMarketplaceBinding;

public class Marketplace extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private FragmentMarketplaceBinding binding;
    private String mParam2;

    public Marketplace() {
        // Required empty public constructor
    }
    public static Marketplace newInstance(String param1, String param2) {
        Marketplace fragment = new Marketplace();
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
        // Khởi tạo binding
        binding = FragmentMarketplaceBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Adapter cho Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.pack_type_spinenr,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.packSpinner.setAdapter(adapter);

        return view;
    }
}