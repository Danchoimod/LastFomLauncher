package org.levimc.launcher.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.levimc.launcher.R;
import org.levimc.launcher.data.MarketplaceItem;
import org.levimc.launcher.databinding.FragmentMarketplaceBinding;
import org.levimc.launcher.ui.adapter.MarketplaceAdapter;

import java.util.ArrayList;
import java.util.List;

public class Marketplace extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Only load 5 items per page
    private static final int PAGE_SIZE = 5;

    private String mParam1;
    private String mParam2;

    private FragmentMarketplaceBinding binding;
    private MarketplaceAdapter adapter;
    private FirebaseFirestore db;
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private boolean hasMore = true;

    private String selectedType = "all"; // from spinner
    private String searchQuery = "";

    private boolean firstPageLoading = false;

    public Marketplace() { }

    public static Marketplace newInstance(String param1, String param2) {
        Marketplace fragment = new Marketplace();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMarketplaceBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Spinner adapter (filter by type)
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.pack_type_spinenr,
                R.layout.spinner_item
        );


        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.packSpinner.setAdapter(spinAdapter);

        // RecyclerView setup
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.ItemAnimator animator = binding.recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        adapter = new MarketplaceAdapter(item -> {
            // Open detail page
            Intent i = new Intent(requireContext(), org.levimc.launcher.ui.activities.MarketplaceDetailActivity.class);
            i.putExtra("id", item.id);
            if (item.packId != null) i.putExtra("packid", item.packId);
            i.putExtra("name", item.name);
            i.putExtra("description", item.description);
            i.putExtra("imageUrl", item.imageUrl);
            i.putExtra("owner", item.owner);
            i.putExtra("ownerUrl", item.ownerUrl);
            i.putExtra("price", item.price == null ? -1 : item.price);
            i.putExtra("type", item.type);
            i.putExtra("url", item.url);
            startActivity(i);
        });
        binding.recyclerView.setAdapter(adapter);

        // Pagination on scroll
        binding.recyclerView.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && !isLoading && hasMore && !recyclerView.canScrollVertically(1)) {
                    loadMore();
                }
            }
        });

        // Search action on keyboard
        binding.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    searchQuery = v.getText().toString().trim();
                    resetAndLoad();
                    return true;
                }
                return false;
            }
        });

        // Spinner filter change
        binding.packSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String value = parent.getItemAtPosition(position).toString();
                String oldType = selectedType;
                selectedType = mapSpinnerToType(value);
                Log.d("Marketplace", "Spinner selected: '" + value + "' -> mapped to: '" + selectedType + "'");
                if (!selectedType.equals(oldType)) {
                    resetAndLoad();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Add LFCoin button - Navigate to getCoin fragment
        binding.addLfCoinText.setOnClickListener(v -> {
            getCoin getCoinFragment = new getCoin();
            getParentFragmentManager().beginTransaction()
                .replace(R.id.bedrockContent, getCoinFragment)
                .addToBackStack(null)
                .commit();
        });

        // Initial load
        resetAndLoad();
        // Fetch user coin for the footer
        fetchUserCoinFromFirestore();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh coin when returning from detail/purchase screens
        fetchUserCoinFromFirestore();
    }

    private void resetAndLoad() {
        lastVisible = null;
        hasMore = true;
        adapter.setItems(new ArrayList<>());
        // First-page loading state
        firstPageLoading = true;
        binding.recyclerView.setVisibility(View.INVISIBLE);
        binding.leftLoading.setVisibility(View.VISIBLE);
        loadMore();
    }

    private void loadMore() {
        if (isLoading || !hasMore) return;
        isLoading = true;

        // Only show adapter footer if not first page
        if (!firstPageLoading) {
            adapter.showLoadingFooter(true);
        }

        Query q = db.collection("marketplace");
        boolean hasTypeFilter = !TextUtils.isEmpty(selectedType) && !"all".equalsIgnoreCase(selectedType);

        if (hasTypeFilter) {
            Log.d("Marketplace", "Filtering by type: " + selectedType);
            q = q.whereEqualTo("type", selectedType);
        } else {
            Log.d("Marketplace", "Loading all items (no type filter)");
        }

        if (!TextUtils.isEmpty(searchQuery)) {
            q = q.orderBy("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff");
        } else if (!hasTypeFilter) {
            // Only sort by createdAt when NOT filtering by type to avoid requiring composite index
            q = q.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        if (lastVisible != null) {
            q = q.startAfter(lastVisible);
        }
        q = q.limit(PAGE_SIZE);

        Task<QuerySnapshot> task = q.get();
        task.addOnSuccessListener(snap -> {
            List<MarketplaceItem> page = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                MarketplaceItem item = MarketplaceItem.from(d);
                page.add(item);
            }

            if (!page.isEmpty()) {
                lastVisible = snap.getDocuments().get(snap.size() - 1);
                if (adapter.getItemCount() == 0) {
                    adapter.setItems(page);
                } else {
                    adapter.addItems(page);
                }
                hasMore = page.size() >= PAGE_SIZE;
            } else {
                hasMore = false;
            }
        }).addOnFailureListener(e -> {
            Log.e("Marketplace", "Error loading items", e);
            if (isAdded()) {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(done -> {
            isLoading = false;
            if (firstPageLoading) {
                firstPageLoading = false;
                binding.leftLoading.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            } else {
                adapter.showLoadingFooter(false);
            }
        });
    }

    private String mapSpinnerToType(String value) {
        if (value == null) return "all";
        String v = value.trim().toLowerCase();
        // Map display text to exact Firestore field values
        if (v.contains("all") || v.contains("tất cả")) return "all";
        if (v.contains("map")) return "maps";                    // "Maps" -> "maps"
        if (v.contains("skin")) return "skin";                   // "Skin" -> "skin"
        if (v.contains("texture") || v.contains("resource")) return "texture pack"; // "Texture Pack" -> "texture pack"
        if (v.contains("addon") || v.contains("add-on")) return "addon"; // "Addon" -> "addon"
        return v; // fallback: use as-is
    }

    private void fetchUserCoinFromFirestore() {
        if (!isAdded()) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("user_info", android.content.Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null || userId.isEmpty()) return;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    Number coin = (Number) snapshot.get("coin");
                    String text = coin == null ? "0" : String.valueOf(coin.intValue());
                    binding.lfCoinAmount.setText(text);
                })
                .addOnFailureListener(e -> {
                    // ignore silently
                });
    }
}
