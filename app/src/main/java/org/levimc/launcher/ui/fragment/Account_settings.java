package org.levimc.launcher.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import org.levimc.launcher.R;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Account_settings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Account_settings extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView userName;

    public Account_settings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Account_settings.
     */
    // TODO: Rename and change types and number of parameters
    public static Account_settings newInstance(String param1, String param2) {
        Account_settings fragment = new Account_settings();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        // Lấy SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("user_info", 0);
        String avatarUrl = prefs.getString("avatar_url", "");
        String username = prefs.getString("username", "Guest");

        // Ví dụ: set vào TextView và ImageView (nếu có)
        TextView userName = view.findViewById(R.id.username);

        userName.setText(username);

        return view;
    }
}