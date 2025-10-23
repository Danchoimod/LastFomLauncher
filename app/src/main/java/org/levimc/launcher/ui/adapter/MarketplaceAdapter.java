package org.levimc.launcher.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.levimc.launcher.R;
import org.levimc.launcher.data.MarketplaceItem;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_LOADING = 2;

    private final List<MarketplaceItem> items = new ArrayList<>();
    private boolean showLoading = false;

    public interface OnItemClickListener {
        void onItemClick(MarketplaceItem item);
    }

    private final OnItemClickListener listener;

    public MarketplaceAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < items.size()) return TYPE_ITEM;
        return TYPE_LOADING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ITEM) {
            View v = inflater.inflate(R.layout.item_marketplace, parent, false);
            return new ItemVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_loading_footer, parent, false);
            return new LoadingVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemVH) {
            MarketplaceItem item = items.get(position);
            ((ItemVH) holder).bind(item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + (showLoading ? 1 : 0);
    }

    public void setItems(List<MarketplaceItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItems(List<MarketplaceItem> more) {
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    public void showLoadingFooter(boolean show) {
        if (show == showLoading) return;
        showLoading = show;
        if (show) {
            notifyItemInserted(items.size());
        } else {
            notifyItemRemoved(items.size());
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle, txtDesc, txtMeta, txtTypeChip, txtPrice;
        public ItemVH(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDesc = itemView.findViewById(R.id.txtDesc);
            txtMeta = itemView.findViewById(R.id.txtMeta);
            txtTypeChip = itemView.findViewById(R.id.txtTypeChip);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
        void bind(final MarketplaceItem item, final OnItemClickListener listener) {
            txtTitle.setText(item.name == null ? "(no title)" : item.name);
            txtDesc.setText(item.description == null ? "" : item.description);
            String meta = "";
            if (item.owner != null) meta += "Author: " + item.owner + "   ";
            if (item.createdAt != null) meta += "Created: " + item.createdAt;
            txtMeta.setText(meta);
            txtTypeChip.setText(item.type == null ? "" : item.type);

            // Price formatting
            if (txtPrice != null) {
                String priceText;
                if (item.price == null || item.price <= 0) {
                    priceText = "Free";
                } else {
                    double p = item.price;
                    if (Math.floor(p) == p) {
                        priceText = ((int) p) + " LFC";
                    } else {
                        priceText = p + " LFC";
                    }
                }
                txtPrice.setText(priceText);
            }

            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                Glide.with(imgCover.getContext())
                        .load(item.imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imgCover);
            } else {
                imgCover.setImageResource(R.drawable.ic_launcher_background);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }

    static class LoadingVH extends RecyclerView.ViewHolder {
        public LoadingVH(@NonNull View itemView) { super(itemView); }
    }
}
