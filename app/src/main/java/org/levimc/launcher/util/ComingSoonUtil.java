package org.levimc.launcher.util;


import android.content.Context;

import org.levimc.launcher.R;
import org.levimc.launcher.ui.dialogs.CustomAlertDialog;

public class ComingSoonUtil {

    public static void show(Context context) {
        // Phát âm thanh boot_up
        SoundPoolUtil.play(context, R.raw.boot_up);

        // Hiển thị hộp thoại "Coming Soon"
        new CustomAlertDialog(context)
                .setTitleText(context.getString(R.string.coming_soon))
                .setMessage(context.getString(R.string.coming_soon_desc))
                .setPositiveButton(context.getString(R.string.ok), null)
                .show();
    }
}

