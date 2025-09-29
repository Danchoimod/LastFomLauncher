package org.levimc.launcher.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VersionAdapter extends RecyclerView.Adapter<VersionAdapter.VersionViewHolder> {
    private List<String> versionList;

    public VersionAdapter(List<String> versionList) {
        this.versionList = versionList;
    }

    @NonNull
    @Override
    public VersionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VersionViewHolder holder, int position) {
        holder.versionName.setText(versionList.get(position));
    }

    @Override
    public int getItemCount() {
        return versionList != null ? versionList.size() : 0;
    }

    public static class VersionViewHolder extends RecyclerView.ViewHolder {
        TextView versionName;
        public VersionViewHolder(@NonNull View itemView) {
            super(itemView);
            versionName = itemView.findViewById(android.R.id.text1);
        }
    }

    public void setVersionList(List<String> versionList) {
        this.versionList = versionList;
        notifyDataSetChanged();
    }
}

