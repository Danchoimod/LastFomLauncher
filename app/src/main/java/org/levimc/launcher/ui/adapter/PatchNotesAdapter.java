package org.levimc.launcher.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.levimc.launcher.R;
import org.levimc.launcher.ui.entity.PatchNoteItem;
import org.levimc.launcher.ui.fragment.webview;

import java.io.File;
import java.util.List;

public class PatchNotesAdapter extends RecyclerView.Adapter<PatchNotesAdapter.ViewHolder> {

    private Context context;
    private List<PatchNoteItem> patchNotes;
    private File imagesDir;

    public PatchNotesAdapter(Context context, List<PatchNoteItem> patchNotes, File imagesDir) {
        this.context = context;
        this.patchNotes = patchNotes;
        this.imagesDir = imagesDir;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patchnote, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PatchNoteItem item = patchNotes.get(position);

        holder.tvTitle.setText(item.getTitle());

        // Tìm file ảnh trong thư mục images
        String[] extensions = {".jpg", ".png", ".webp"};
        File imageFile = null;

        for (String ext : extensions) {
            File file = new File(imagesDir, item.getId() + ext);
            if (file.exists()) {
                imageFile = file;
                break;
            }
        }

        // Load ảnh bằng Glide
        if (imageFile != null && imageFile.exists()) {
            Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgPatchNote);
        } else {
            // Nếu không có ảnh local, thử load từ URL
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgPatchNote);
        }

        // Xử lý click để mở fragment webview
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                webview webViewFragment = webview.newInstance(item.getUrl());

                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.patchNoteContent, webViewFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return patchNotes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPatchNote;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPatchNote = itemView.findViewById(R.id.imgPatchNote);
            tvTitle = itemView.findViewById(R.id.tvPatchNoteTitle);
        }
    }
}
