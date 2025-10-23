package org.levimc.launcher.ui.fragment;

import android.os.Bundle;
import org.levimc.launcher.R;
import org.levimc.launcher.databinding.FragmentBedrockContainerBinding;
import org.levimc.launcher.ui.dialogs.CustomAlertDialog;
import org.levimc.launcher.util.ComingSoonUtil;
import org.levimc.launcher.util.SoundPoolUtil;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BedrockContainer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BedrockContainer extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
//
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView btnNavPlay;

    private TextView btnNavInstallation;

    private TextView btnNavFAQ;

    private TextView btnNavPatchNotes;

    private TextView btnMarketplace;

    private FragmentBedrockContainerBinding binding;

    private TextView btnModules;
    public BedrockContainer() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BedrockContainer.
     */
    // TODO: Rename and change types and number of parameters
    public static BedrockContainer newInstance(String param1, String param2) {
        BedrockContainer fragment = new BedrockContainer();
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
        View view = inflater.inflate(R.layout.fragment_bedrock_container, container, false);
        hideGreenViews(view);
        view.post(() -> showGreen(0));
        btnNavPlay = view.findViewById(R.id.btnNavPlay);
        btnNavFAQ = view.findViewById(R.id.btnNavFAQ);
        btnNavPatchNotes = view.findViewById(R.id.btnNavPatchNotes);
        btnNavInstallation = view.findViewById(R.id.btnNavInstallation);
        btnMarketplace = view.findViewById(R.id.btnMarketplace);
        btnModules = view.findViewById(R.id.btnModules);
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bedrockContent, new Play())
                    .commit();
            btnNavPlay.setOnClickListener(v -> changeFragment(1));
            btnNavInstallation.setOnClickListener(v -> changeFragment(2));
            btnNavFAQ.setOnClickListener(v -> changeFragment(3));
            btnNavPatchNotes.setOnClickListener(v -> changeFragment(4));
            btnMarketplace.setOnClickListener(v -> changeFragment(5));
            btnModules.setOnClickListener(v -> ComingSoonUtil.show(getContext()));
        }
        return view;
    }
    public void changeFragment(int screenOfNumber) {
        Fragment fragment;

        hideGreenViews(getView());

        switch (screenOfNumber) {
            case 1:
                fragment = new Play();
                showGreen(0);
                break;
            case 2:
                fragment = new javaInstalltion();
                showGreen(1);
                break;
            case 3:
                fragment = new faq();
                showGreen(2);
                break;
            case 4:
                fragment = new patchnotes();
                showGreen(3);
                break;
            case 5:
                fragment = new Marketplace();
                showGreen(4);
                break;
            case 6:
//                showGreen(5);
//                return; // No fragment to show, exit early
                ComingSoonUtil.show(getContext());
            default:
                throw new IllegalArgumentException("Screen number không hợp lệ: " + screenOfNumber);
        }

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.bedrockContent, fragment)
                .commit();
    }


    private void hideGreenViews(View root) {
        int[] greenIds = {R.id.green0, R.id.green1, R.id.green2, R.id.green3, R.id.green4, R.id.green5};
        for (int id : greenIds) {
            View v = root.findViewById(id);
            if (v != null) v.setVisibility(View.INVISIBLE);
        }
    }
    private void showGreen(int index) {
        int[] greenIds = {R.id.green0, R.id.green1, R.id.green2, R.id.green3, R.id.green4 ,R.id.green5};
        if (index >= 0 && index < greenIds.length) {
            View v = getView().findViewById(greenIds[index]);
            if (v != null) v.setVisibility(View.VISIBLE);
        }
    }



}