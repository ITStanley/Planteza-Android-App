package com.example.planteza;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class SellerDetails extends AppCompatActivity {
    TextView sellerName, sellerPhone;
    public static ArrayList<Item> itemList = new ArrayList<>();
    private GridView gridView;
    private String currentCategory, name, phone;
    private String searchQuery;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_details);
        Objects.requireNonNull(getSupportActionBar()).hide();

        sellerName = findViewById(R.id.sellerName);
        sellerPhone = findViewById(R.id.sellerPhone);
        gridView = findViewById(R.id.itemsListView);
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, itemList);
        gridView.setAdapter(itemAdapter);

        Intent intent = getIntent();
        String sellerId = intent.getStringExtra("uid");

        // Fetch sellers's items from Firebase Database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(sellerId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                ReadWriteUserDetails readUserDetails = datasnapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    name = readUserDetails.getName();
                    phone = readUserDetails.getPhone();
                    sellerName.setText(name);
                    sellerPhone.setText(phone);
                }
                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(sellerId).child("items");
                itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            Item item = itemSnapshot.getValue(Item.class);
                            itemList.add(item);
                        }
                        itemAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SellerDetails.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SellerDetails.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle item click event
        setUpOnClickListener();

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

    private void setUpOnClickListener() {
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
