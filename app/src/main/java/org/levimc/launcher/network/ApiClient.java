package org.levimc.launcher.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://lflauncher.vercel.app";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client;

    private static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(25, TimeUnit.SECONDS)
                    .writeTimeout(25, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    public static String getJwt(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        return prefs.getString("jwt", null);
    }

    public interface OwnedCallback {
        void onResult(Set<String> ownedIds, Integer balance, Exception error);
    }

    public static void getOwnedAsync(Context ctx, final OwnedCallback cb) {
        String jwt = getJwt(ctx);
        Request.Builder b = new Request.Builder()
                .url(BASE_URL + "/api/marketplace/user/owned")
                .get();
        if (jwt != null) b.addHeader("Authorization", "Bearer " + jwt);
        getClient().newCall(b.build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onResult(null, null, e); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) { cb.onResult(null, null, new IOException("HTTP " + response.code())); return; }
                String body = response.body().string();
                try {
                    Set<String> owned = new HashSet<>();
                    Integer coins = null;
                    // Try parse as JSON array
                    if (body.trim().startsWith("[")) {
                        JSONArray arr = new JSONArray(body);
                        for (int i = 0; i < arr.length(); i++) {
                            Object it = arr.get(i);
                            if (it instanceof String) {
                                owned.add((String) it);
                            } else if (it instanceof JSONObject) {
                                JSONObject o = (JSONObject) it;
                                String id = o.optString("id", null);
                                if (id == null) id = o.optString("marketplaceId", null);
                                if (id != null) owned.add(id);
                                // also accept numeric packid
                                if (id == null && o.has("packid")) {
                                    owned.add(String.valueOf(o.optInt("packid")));
                                }
                                if (o.has("coins")) coins = o.optInt("coins");
                            }
                        }
                    } else {
                        // Maybe wrapped object { owned: [...], coins: 123 }
                        JSONObject obj = new JSONObject(body);
                        if (obj.has("coins")) coins = obj.optInt("coins");
                        JSONArray arr = obj.optJSONArray("owned");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                Object it = arr.get(i);
                                if (it instanceof String) owned.add((String) it);
                                else if (it instanceof JSONObject) {
                                    JSONObject o = (JSONObject) it;
                                    String id = o.optString("id", null);
                                    if (id == null) id = o.optString("marketplaceId", null);
                                    if (id != null) owned.add(id);
                                    if (id == null && o.has("packid")) owned.add(String.valueOf(o.optInt("packid")));
                                }
                            }
                        }
                    }
                    cb.onResult(owned, coins, null);
                } catch (JSONException e) {
                    cb.onResult(null, null, e);
                }
            }
        });
    }

    // Lightweight ownership check for a specific item using new API
    public interface SimpleOwnedCallback { void onResult(Boolean owned, Integer balance, Exception error); }

    public static void checkOwnedAsync(Context ctx, String marketplaceId, Integer packId, final SimpleOwnedCallback cb) {
        SharedPreferences prefs = ctx.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        
        Log.d(TAG, "=== OWNERSHIP CHECK START ===");
        Log.d(TAG, "MarketplaceId: " + marketplaceId);
        Log.d(TAG, "PackId: " + packId);
        Log.d(TAG, "UserId: " + userId);
        
        // Prepare body with uid and marketplaceId for new API
        JSONObject body = new JSONObject();
        try {
            if (userId != null) body.put("uid", userId);
            if (marketplaceId != null) body.put("marketplaceId", marketplaceId);
        } catch (JSONException ignored) {}
        
        // New API endpoint: POST /api/marketplace/ownership
        String url = BASE_URL + "/api/marketplace/ownership";
        String bodyStr = body.toString();
        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "Body: " + bodyStr);
        
        Request.Builder b = new Request.Builder()
                .url(url)
                .post(RequestBody.create(bodyStr, JSON));
        
        getClient().newCall(b.build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ownership check FAILED", e);
                // Fallback to GET /api/marketplace/user/owned
                Log.d(TAG, "Trying fallback to /user/owned");
                getOwnedAsync(ctx, (ownedIds, balance, err) -> {
                    if (err != null) { 
                        Log.e(TAG, "Fallback also failed", err);
                        cb.onResult(null, null, err); 
                        return; 
                    }
                    boolean owned = false;
                    if (ownedIds != null) {
                        if (marketplaceId != null && ownedIds.contains(marketplaceId)) owned = true;
                        if (!owned && packId != null) owned = ownedIds.contains(String.valueOf(packId));
                    }
                    Log.d(TAG, "Fallback result - Owned: " + owned);
                    Log.d(TAG, "=== OWNERSHIP CHECK END (fallback) ===");
                    cb.onResult(owned, balance, null);
                });
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Ownership response code: " + response.code());
                String body = response.body().string();
                Log.d(TAG, "Response body: " + body);
                
                if (!response.isSuccessful()) {
                    Log.w(TAG, "Non-2xx response, trying fallback");
                    // Fallback when server returns non-2xx
                    getOwnedAsync(ctx, (ownedIds, balance, err) -> {
                        if (err != null) { cb.onResult(null, null, err); return; }
                        boolean owned = false;
                        if (ownedIds != null) {
                            if (marketplaceId != null && ownedIds.contains(marketplaceId)) owned = true;
                            if (!owned && packId != null) owned = ownedIds.contains(String.valueOf(packId));
                        }
                        Log.d(TAG, "=== OWNERSHIP CHECK END (fallback) ===");
                        cb.onResult(owned, balance, null);
                    });
                    return;
                }
                
                // Parse new API response format
                try {
                    JSONObject obj = new JSONObject(body);
                    boolean success = obj.optBoolean("success", false);
                    
                    if (!success) {
                        String error = obj.optString("error", "Unknown error");
                        Log.e(TAG, "API returned success=false: " + error);
                        cb.onResult(false, null, new IOException(error));
                        return;
                    }
                    
                    // Get owns field (true/false)
                    Boolean owned = obj.has("owns") ? obj.optBoolean("owns") : null;
                    Integer coins = obj.has("coins") ? obj.optInt("coins") : null;
                    
                    Log.d(TAG, "Parsed success: " + success);
                    Log.d(TAG, "Parsed owns: " + owned);
                    Log.d(TAG, "Parsed coins: " + coins);
                    Log.d(TAG, "=== OWNERSHIP CHECK END (success) ===");
                    
                    cb.onResult(owned, coins, null);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse ownership response", e);
                    Log.e(TAG, "Raw response: " + body);
                    cb.onResult(null, null, e);
                }
            }
        });
    }

    public interface PurchaseCallback {
        void onResult(boolean success, String message, Integer newBalance, Exception error);
    }

    public static void purchaseAsync(Context ctx, String marketplaceId, Integer packId, final PurchaseCallback cb) {
        SharedPreferences prefs = ctx.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        
        Log.d(TAG, "=== PURCHASE REQUEST START ===");
        Log.d(TAG, "MarketplaceId: " + marketplaceId);
        Log.d(TAG, "PackId: " + packId);
        Log.d(TAG, "UserId: " + userId);
        
        // Prepare body with uid and marketplaceId for new API
        JSONObject body = new JSONObject();
        try {
            if (userId != null) body.put("uid", userId);
            if (marketplaceId != null) body.put("marketplaceId", marketplaceId);
        } catch (JSONException ignored) {}
        
        // New API endpoint: POST /api/marketplace/purchase (no {id} in URL)
        String url = BASE_URL + "/api/marketplace/purchase";
        String bodyStr = body.toString();
        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "Body: " + bodyStr);
        
        Request.Builder b = new Request.Builder()
                .url(url)
                .post(RequestBody.create(bodyStr, JSON));
        
        getClient().newCall(b.build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Purchase request FAILED", e);
                Log.e(TAG, "Error message: " + e.getMessage());
                cb.onResult(false, e.getMessage(), null, e);
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Purchase response code: " + response.code());
                Log.d(TAG, "Purchase response message: " + response.message());
                handlePurchaseResponse(response, cb);
            }
        });
    }



    private static void handlePurchaseResponse(Response response, final PurchaseCallback cb) throws IOException {
        String body = response.body().string();
        Log.d(TAG, "Response body: " + body);
        Log.d(TAG, "Response body length: " + body.length());
        
        // Handle empty response
        if (body == null || body.trim().isEmpty()) {
            Log.e(TAG, "Empty response body!");
            Log.d(TAG, "=== PURCHASE REQUEST END (empty response) ===");
            cb.onResult(false, "Server returned empty response", null, new IOException("Empty response"));
            return;
        }
        
        try {
            JSONObject obj = new JSONObject(body);
            boolean ok = obj.optBoolean("success", response.isSuccessful());
            
            // Get message or error field
            String msg = obj.has("message") ? obj.optString("message") : 
                         obj.has("error") ? obj.optString("error") : 
                         response.message();
            
            // Check for newBalance first, then fall back to coins
            Integer coins = obj.has("newBalance") ? obj.optInt("newBalance") :
                           obj.has("coins") ? obj.optInt("coins") : null;
            
            Log.d(TAG, "Parsed success: " + ok);
            Log.d(TAG, "Parsed message: " + msg);
            Log.d(TAG, "Parsed coins/newBalance: " + coins);
            
            if (response.code() == 402) {
                Log.w(TAG, "HTTP 402: Not enough coins");
                ok = false;
            }
            
            Log.d(TAG, "=== PURCHASE REQUEST END (success=" + ok + ") ===");
            cb.onResult(ok, msg, coins, null);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse response JSON", e);
            Log.e(TAG, "Raw response: " + body);
            Log.d(TAG, "=== PURCHASE REQUEST END (parse error) ===");
            
            // Return more helpful error message
            String errorMsg = "Invalid response from server";
            if (body.length() > 0) {
                errorMsg += " (not valid JSON)";
            }
            cb.onResult(false, errorMsg, null, e);
        }
    }
}
