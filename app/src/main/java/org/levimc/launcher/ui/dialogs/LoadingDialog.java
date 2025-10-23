package org.levimc.launcher.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.levimc.launcher.R;

public class LoadingDialog extends Dialog {
    public LoadingDialog(Context context) {
        super(context);
    }
    private ImageView loadingicon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_loading, null);
        setContentView(view);
        loadingicon = findViewById(R.id.loadingicon);

        Glide.with(getContext())
                .asGif()
                .load(Uri.parse("file:///android_asset/loading.gif"))
                .into(loadingicon);

        setCancelable(false);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}
