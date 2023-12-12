package com.example.planteza;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class Home extends AppCompatActivity {
    public static ArrayList<Item> itemList = new ArrayList<>();
    private GridView gridView;
    private String currentCategory;
    private String searchQuery;
    private ItemAdapter itemAdapter;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Objects.requireNonNull(getSupportActionBar()).hide();

        gridView = findViewById(R.id.itemsListView);
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, itemList);
        gridView.setAdapter(itemAdapter);

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.items);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.items:
                    return true;
                case R.id.sellers:
                    startActivity(new Intent(getApplicationContext(),Sellers.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.blog:
                    startActivity(new Intent(getApplicationContext(),Blog.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(),Profile.class));
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });

        // Fetch items from all users in Firebase Database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Registered Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    assert userId != null;
                    DatabaseReference userItemsRef = usersRef.child(userId).child("items");
                    userItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot itemsSnapshot) {
                            for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                                Item item = itemSnapshot.getValue(Item.class);
                                itemList.add(item);
                            }
                            itemAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Handle item click event
        setUpOnItemClickListener();

        // Filter button click listeners
        Button all = findViewById(R.id.allFilter);
        all.setOnClickListener(v -> {
            currentCategory = "All";
            applyFilterAndSearch();
        });

        Button ornamental = findViewById(R.id.ornamentalFilter);
        ornamental.setOnClickListener(v -> {
            currentCategory = "Ornamental";
            applyFilterAndSearch();
        });

        Button edible = findViewById(R.id.edibleFilter);
        edible.setOnClickListener(v -> {
            currentCategory = "Edible";
            applyFilterAndSearch();
        });

        Button herbal = findViewById(R.id.herbalFilter);
        herbal.setOnClickListener(v -> {
            currentCategory = "Herbal";
            applyFilterAndSearch();
        });

        // Search input field listener

        SearchView searchView = findViewById(R.id.itemsListSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update the searchQuery variable with the current text in the SearchView
                searchQuery = newText.trim();
                applyFilterAndSearch();
                return true;
            }
        });
    }
    private void setUpOnItemClickListener() {
        gridView.setOnItemClickListener((adapterView, view, position, l) -> {
            Item selectedItem = (Item) gridView.getItemAtPosition(position);
            Intent detailsIntent = new Intent(getApplicationContext(), PlantDetails.class);
            detailsIntent.putExtra("itemId", selectedItem.getItemId());
            startActivity(detailsIntent);
        });
    }
    private void applyFilterAndSearch() {
        List<Item> filteredList = new ArrayList<>();

        // Apply category filter
        if (currentCategory == null || currentCategory.equals("All")) {
            filteredList.addAll(itemList);
        } else {
            for (Item item : itemList) {
                if (item.getItemCategory().equals(currentCategory)) {
                    filteredList.add(item);
                }
            }
        }

        // Apply search query
        if (searchQuery != null && !searchQuery.isEmpty()) {
            List<Item> searchResults = new ArrayList<>();
            for (Item item : filteredList) {
                if (item.getItemTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                    searchResults.add(item);
                }
            }
            filteredList = searchResults;
        }

        // Update the itemAdapter with the filtered and searched items
        itemAdapter.setItems(filteredList);
        itemAdapter.notifyDataSetChanged();
    }

}

