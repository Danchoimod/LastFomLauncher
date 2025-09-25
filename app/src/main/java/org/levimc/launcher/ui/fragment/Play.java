package org.levimc.launcher.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import org.levimc.launcher.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Play#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Play extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageView title;

    private Button playButton;

    public Play() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Play.
     */
    // TODO: Rename and change types and number of parameters
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
        // Inflate layout fragment trước, lấy View root
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);

        // Tìm spinner trên View root
//        Spinner spinner = rootView.findViewById(R.id.planets_spinner);
        title = rootView.findViewById(R.id.minecraft_logo);
        playButton = rootView.findViewById(R.id.launcher_play_button);
        // Tạo adapter và gán cho spinner
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//                getContext(),  // Hoặc getActivity()
//                R.array.planets_array,
//                R.layout.spinner_item
//        );
        playButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Button clicked", Toast.LENGTH_SHORT).show();
        });
        
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
        return rootView;
    }

}