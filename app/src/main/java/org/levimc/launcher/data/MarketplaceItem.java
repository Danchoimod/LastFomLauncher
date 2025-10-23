package org.levimc.launcher.data;

import com.google.firebase.firestore.DocumentSnapshot;

public class MarketplaceItem {
    public String id;
    public String name;
    public String description;
    public String imageUrl; // image url field unnamed in schema -> use first anonymous string as image
    public String owner;
    public String ownerUrl;
    public Double price; // store as double to accept Firestore number
    public Integer packId; // numeric backend id for ownership/purchase
    public String type;
    public String url;
    public String createdAt; // ISO8601 string

    public static MarketplaceItem from(DocumentSnapshot doc) {
        MarketplaceItem item = new MarketplaceItem();
        item.id = doc.getId();
        item.name = doc.getString("name");
        item.description = doc.getString("description");
        // Try common keys for cover image
        String cover = doc.getString("image");
        if (cover == null) cover = doc.getString("cover");
        if (cover == null) {
            // In sample, the cover url seems to be stored as a top-level unnamed example. Use key 'thumbnail' as alternative.
            cover = doc.getString("thumbnail");
        }
        if (cover == null) {
            // Some schema may use 'img'
            cover = doc.getString("img");
        }
        // If your Firestore uses a fixed key, replace the above with the exact key and remove fallbacks.
        // Normalize localhost dev URLs to production to avoid Glide failing on device
        if (cover != null) {
            String c = cover;
            c = c.replace("http://localhost:3000", "https://lflauncher.vercel.app");
            c = c.replace("https://localhost:3000", "https://lflauncher.vercel.app");
            c = c.replace("http://127.0.0.1:3000", "https://lflauncher.vercel.app");
            c = c.replace("http://10.0.2.2:3000", "https://lflauncher.vercel.app");
            item.imageUrl = c;
        } else {
            item.imageUrl = null;
        }
        item.owner = doc.getString("owner");
        item.ownerUrl = doc.getString("ownerurl");
        Number p = (Number) doc.get("price");
        item.price = p == null ? null : p.doubleValue();
        Number pid = (Number) doc.get("packid");
        item.packId = pid == null ? null : pid.intValue();
        item.type = doc.getString("type");
        item.url = doc.getString("url");
        item.createdAt = doc.getString("createdAt");
        return item;
    }
}
