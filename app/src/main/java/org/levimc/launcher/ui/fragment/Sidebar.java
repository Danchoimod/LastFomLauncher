package org.levimc.launcher.ui.fragment;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import org.levimc.launcher.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

import static android.widget.Toast.makeText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Sidebar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Sidebar extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LinearLayout javaEdition;
    private LinearLayout btnSettings;
    private LinearLayout bedrockEdition;

    private LinearLayout llHome;

    private LinearLayout llNews;

    private Button launcher_play_button;
    private List<LinearLayout> layouts;

    private ImageView imageAvatar;

    private TextView textUsername;

    public Sidebar() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Sidebar.
     */
    // TODO: Rename and change types and number of parameters
    public static Sidebar newInstance(String param1, String param2) {
        Sidebar fragment = new Sidebar();
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
        View view = inflater.inflate(R.layout.fragment_sidebar, container, false);
        javaEdition = view.findViewById(R.id.javaEdition);
        btnSettings = view.findViewById(R.id.llSettings);
        bedrockEdition = view.findViewById(R.id.bedrockEdition);
        llHome = view.findViewById(R.id.llHome);
        llNews = view.findViewById(R.id.llNews);
        textUsername = view.findViewById(R.id.username);
        imageAvatar = view.findViewById(R.id.avatar);
        layouts = Arrays.asList(javaEdition, btnSettings, bedrockEdition, llHome, llNews);

        // Load avatar and username
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", requireActivity().MODE_PRIVATE);
        String avatarUrl = prefs.getString("avatar_url", "R.drawable.emojime");
        String username = prefs.getString("username", "Guest");
        Glide.with(this).load(avatarUrl).into(imageAvatar);
        textUsername.setText(username);

        // Set background selector and make clickable/focusable
        for (LinearLayout layout : layouts) {
            layout.setBackgroundResource(R.drawable.control_button);
            layout.setClickable(true);
            layout.setFocusable(true);
        }

        // Click listeners
        javaEdition.setOnClickListener(v -> {
//            toggleSelection(javaEdition);
            Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show();

        });
        btnSettings.setOnClickListener(v -> {
            toggleSelection(btnSettings);
            openFragment(new SettingNavigator());
        });
        bedrockEdition.setOnClickListener(v -> {
            toggleSelection(bedrockEdition);
            openFragment(new BedrockContainer());
        });
        llHome.setOnClickListener(v -> {
            toggleSelection(llHome);
            openFragment(new Home());
        });
        llNews.setOnClickListener(v -> {
            openFragment(new What_new());
            toggleSelection(llNews);
        });

        // Select Bedrock by default
        toggleSelection(bedrockEdition);

        // Load BedrockContainer fragment at start
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.gamelayout, new BedrockContainer())
                    .commit();
        }
        return view;
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.gamelayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void toggleSelection(LinearLayout selectedLayout) {
        for (LinearLayout layout : layouts) {
            layout.setSelected(layout == selectedLayout);
        }
    }

}