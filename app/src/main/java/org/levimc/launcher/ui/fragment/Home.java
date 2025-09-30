package org.levimc.launcher.ui.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.levimc.launcher.R;
import org.levimc.launcher.databinding.FragmentHomeBinding;

import java.security.PublicKey;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class Home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentHomeBinding binding;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Home() {
        // Required empty public constructor
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
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        //logic
        changeBg(1);
        // Inflate the layout for this fragment
        binding.news1.setOnClickListener(v -> {
            changeBg(1);
            binding.notfiTitle.setText(R.string.event1);
            binding.notfiDecs.setText(R.string.event1_desc);
            binding.launcherlayoutImageView1.setImageResource(R.drawable.mclive2025);
        });
        binding.news2.setOnClickListener(v -> {
            changeBg(2);
            binding.launcherlayoutImageView1.setImageResource(R.drawable.thumnail0);
            binding.notfiTitle.setText(R.string.event2);
            binding.notfiDecs.setText(R.string.event2_desc);
        });
        binding.news3.setOnClickListener(v -> {
            changeBg(3);
        binding.launcherlayoutImageView1.setImageResource(R.drawable.thunail1);
            binding.notfiTitle.setText(R.string.event3);
            binding.notfiDecs.setText(R.string.event3_desc);
        });
        binding.news4.setOnClickListener(v -> {
            changeBg(4);
            binding.launcherlayoutImageView1.setImageResource(R.drawable.thumnail2);
            binding.notfiTitle.setText(R.string.event4);
            binding.notfiDecs.setText(R.string.event4_desc);
        });
        return binding.getRoot();

    }
    public void changeBg(int number) {
        fullAlpha();
        View target = null;

        switch (number) {
            case 1: target = binding.news1; break;
            case 2: target = binding.news2; break;
            case 3: target = binding.news3; break;
            case 4: target = binding.news4; break;
        }

        if (target != null) target.setAlpha(0.5f);
    }

    public void fullAlpha(){
        binding.news1.setAlpha(1f);
        binding.news2.setAlpha(1f);
        binding.news3.setAlpha(1f);
        binding.news4.setAlpha(1f);
    }
}