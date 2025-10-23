package org.levimc.launcher.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import org.levimc.launcher.R;

import java.util.HashMap;

public class SoundPoolUtil {

    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> soundMap;
    private static boolean isLoaded = false;

    /**
     * Khởi tạo SoundPool và load các file âm thanh
     */
    public static void init(Context context) {
        if (soundPool != null) return; // Đã init rồi thì bỏ qua

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundMap = new HashMap<>();

        // 🟢 Load âm thanh (bạn có thể thêm nhiều hơn)
        soundMap.put(R.raw.press, soundPool.load(context, R.raw.press, 1));
        soundMap.put(R.raw.boot_up_2, soundPool.load(context, R.raw.boot_up_2, 2));
        soundMap.put(R.raw.boot_up, soundPool.load(context, R.raw.boot_up, 3));
        soundMap.put(R.raw.scroll, soundPool.load(context, R.raw.scroll, 4));

        // Callback khi load xong
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) isLoaded = true;
        });
    }

    /**
     * Phát âm thanh theo id
     */
    public static void play(Context context, int soundResId) {
        if (soundPool == null) {
            init(context);
        }
        if (isLoaded && soundMap.containsKey(soundResId)) {
            soundPool.play(soundMap.get(soundResId), 1f, 1f, 0, 0, 1f);
        }
    }

    /**
     * Giải phóng tài nguyên khi không cần nữa
     */
    public static void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            soundMap = null;
            isLoaded = false;
        }
    }
}