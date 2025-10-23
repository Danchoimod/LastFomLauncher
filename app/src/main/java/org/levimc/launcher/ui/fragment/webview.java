package org.levimc.launcher.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.levimc.launcher.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link webview#newInstance} factory method to
 * create an instance of this fragment.
 */
public class webview extends Fragment {

    private static final String ARG_URL = "url";
    private String url;
    private WebView webView;

    public webview() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param url Parameter 1.
     * @return A new instance of fragment webview.
     */
    public static webview newInstance(String url) {
        webview fragment = new webview();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString(ARG_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);

        webView = view.findViewById(R.id.webview);

        // Cấu hình WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);

        // Đảm bảo WebView không mở browser ngoài
        webView.setWebViewClient(new WebViewClient());

        // Load URL
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }

        return view;
    }
}