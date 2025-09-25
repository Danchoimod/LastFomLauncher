package org.levimc.launcher.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import org.levimc.launcher.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingNavigator#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingNavigator extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView btnGeneral;

    private TextView btnAccount;

    private TextView btnAbout;
    private LinearLayout settingContainer;

    public SettingNavigator() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingNavigator.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingNavigator newInstance(String param1, String param2) {
        SettingNavigator fragment = new SettingNavigator();
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
        View view = inflater.inflate(R.layout.fragment_setting_navigator, container, false);
        hideGreenViews(view);
        view.post(() -> showGreen(0));
        btnAbout = view.findViewById(R.id.btnAbout);
        btnAccount = view.findViewById(R.id.btnGeneralSettings);
        btnGeneral = view.findViewById(R.id.btnAccounts);
        // Inflate the layout for this fragment

        btnGeneral.setOnClickListener(v -> changeFragment(2));
        btnAccount.setOnClickListener(v -> changeFragment(1));
        btnAbout.setOnClickListener(v -> changeFragment(3));
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingContainer, new general_settings())
                    .commit();
        }

        return view;

    }
    public void changeFragment(int screenOfNumber) {
        Fragment fragment;

        // Ẩn tất cả green views trước
        hideGreenViews(getView());

        switch (screenOfNumber) {
            case 1:
                fragment = new general_settings();
                showGreen(0); // hiển thị green0
                break;
            case 2:
                fragment = new Account_settings();
                showGreen(1); // hiển thị green1
                break;
            case 3:
                fragment = new aboutSettings();
                showGreen(2); // hiển thị green2
                break;
            default:
                throw new IllegalArgumentException("Screen number không hợp lệ: " + screenOfNumber);
        }

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.settingContainer, fragment)
                .commit();
    }
    private void showGreen(int index) {
        int[] greenIds = {R.id.green0, R.id.green1, R.id.green2};
        if (index >= 0 && index < greenIds.length) {
            View v = getView().findViewById(greenIds[index]);
            if (v != null) v.setVisibility(View.VISIBLE);
        }
    }
    private void hideGreenViews(View root) {
        int[] greenIds = {R.id.green0, R.id.green1, R.id.green2};
        for (int id : greenIds) {
            View v = root.findViewById(id);
            if (v != null) v.setVisibility(View.INVISIBLE);
        }
    }

}