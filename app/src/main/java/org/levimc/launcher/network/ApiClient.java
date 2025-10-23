package org.levimc.launcher.network;

import android.content.Context;
import android.content.SharedPreferences;

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

    public interface PurchaseCallback {
        void onResult(boolean success, String message, Integer newBalance, Exception error);
    }

    public static void purchaseAsync(Context ctx, String marketplaceId, final PurchaseCallback cb) {
        String jwt = getJwt(ctx);
        Request.Builder b = new Request.Builder()
                .url(BASE_URL + "/api/marketplace/" + marketplaceId + "/purchase")
                .post(RequestBody.create("{}", JSON));
        if (jwt != null) b.addHeader("Authorization", "Bearer " + jwt);
        getClient().newCall(b.build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onResult(false, e.getMessage(), null, e); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONObject obj = new JSONObject(body);
                    boolean ok = obj.optBoolean("success", response.isSuccessful());
                    String msg = obj.optString("message", response.message());
                    Integer coins = obj.has("coins") ? obj.optInt("coins") : null;
                    // Treat HTTP 402 as not enough coins
                    if (response.code() == 402) ok = false;
                    cb.onResult(ok, msg, coins, null);
                } catch (JSONException e) {
                    cb.onResult(response.isSuccessful(), response.message(), null, null);
                }
            }
        });
    }
}
