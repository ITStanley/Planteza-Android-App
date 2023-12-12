package com.example.planteza;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class UserItems extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private GridView gridView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_items);
        Objects.requireNonNull(getSupportActionBar()).hide();

        gridView = findViewById(R.id.itemsListView);
        ImageView addBtn = findViewById(R.id.addBtn);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        //Add Btn click listener
        addBtn.setOnClickListener(view ->{
            Intent intent = new Intent(this,AddItem.class);
            startActivity(intent);
        });

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, itemList);
        gridView.setAdapter(itemAdapter);

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(this);

        // Fetch user's items from Firebase Database
        fetchUserItems();

        // Handle item click event
        setUpOnClickListener();
    }

    @Override
    public void onRefresh() {
        // Clear the item list and fetch user's items again
        itemList.clear();
        fetchUserItems();
    }
    private void fetchUserItems() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference("Registered Users").child(userId).child("items")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            Item item = itemSnapshot.getValue(Item.class);
                            itemList.add(item);
                        }
                        itemAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
                    }
                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
                        // Handle error
                        swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
                    }
                });
    }

    private void setUpOnClickListener() {
        gridView.setOnItemClickListener((adapterView, view, position, l) -> {
            Item selectedItem = (Item) gridView.getItemAtPosition(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(UserItems.this);
            builder.setTitle("Options")
                    .setMessage("Do you want to Edit or Delete the item?")
                    .setPositiveButton("Edit", (dialog, which) -> {
                        // Handle edit action
                        Intent editIntent = new Intent(getApplicationContext(), EditItem.class);
                        editIntent.putExtra("itemId", selectedItem.getItemId());
                        startActivity(editIntent);
                    })
                    .setNegativeButton("Delete", (dialog, which) -> {
                        // Handle delete action
                        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("items").child(selectedItem.getItemId());
                        itemRef.removeValue().addOnSuccessListener(aVoid -> {
                            // Delete the itemIcon from Firebase Storage
                            StorageReference itemIconRef = FirebaseStorage.getInstance().getReference().child("item_images").child(selectedItem.getItemId()); //selectedItem.getItemId() + ".jpeg"
                            itemIconRef.delete().addOnSuccessListener(aVoid1 -> {
                                // Remove item from the list
                                itemList.remove(selectedItem);
                                // Refresh the GridView
                                ((ItemAdapter) gridView.getAdapter()).notifyDataSetChanged();
                            }).addOnFailureListener(e -> {
                                // Failed to delete the itemIcon from Firebase Storage, show an error message
                                Toast.makeText(UserItems.this, "Failed to delete item imageA: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }).addOnFailureListener(e -> {
                            // Failed to delete the item from the database, show an error message
                            Toast.makeText(UserItems.this, "Failed to delete itemB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }
}
