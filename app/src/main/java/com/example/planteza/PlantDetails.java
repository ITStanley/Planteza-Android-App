package com.example.planteza;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class PlantDetails extends AppCompatActivity {
    private DatabaseReference databaseRef;
    private ImageView icon;
    private TextView name, description, category, lightening, watering, height, ownName, ownAdd, ownPhone, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_details);
        Objects.requireNonNull(getSupportActionBar()).hide();

        icon = findViewById(R.id.itemIcon);
        name = findViewById(R.id.itemName);
        description = findViewById(R.id.itemDescription);
        category = findViewById(R.id.itemCategory);
        lightening = findViewById(R.id.itemLightening);
        watering = findViewById(R.id.itemWatering);
        height = findViewById(R.id.itemHeight);
        price = findViewById(R.id.itemPrice);
        ownName = findViewById(R.id.sellerName);
        ownAdd = findViewById(R.id.sellerAdd);
        ownPhone = findViewById(R.id.sellerPhone);

        // Initialize the Firebase Realtime Database reference
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Get the item ID from the previous activity
        Intent intent = getIntent();
        String itemId = intent.getStringExtra("itemId");

        // Retrieve the item details from the Firebase Realtime Database
        retrieveItemDetails(itemId);

    }
    private void retrieveItemDetails(String itemId) {
        // Create a query to retrieve the item with the given item ID
        databaseRef.child("Registered Users").orderByChild("items/" + itemId + "/itemId").equalTo(itemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String categoryName = userSnapshot.child("items/" + itemId + "/itemCategory").getValue(String.class);
                            String itemDescription = userSnapshot.child("items/" + itemId + "/itemDescription").getValue(String.class);
                            String itemHeight = userSnapshot.child("items/" + itemId + "/itemHeight").getValue(String.class);
                            String itemIcon = userSnapshot.child("items/" + itemId + "/itemImage").getValue(String.class);
                            String itemLightening = userSnapshot.child("items/" + itemId + "/itemLightening").getValue(String.class);
                            String itemPrice = userSnapshot.child("items/" + itemId + "/itemPrice").getValue(String.class);
                            String itemTitle = userSnapshot.child("items/" + itemId + "/itemTitle").getValue(String.class);
                            String itemWatering = userSnapshot.child("items/" + itemId + "/itemWatering").getValue(String.class);
                            String userName = userSnapshot.child("name").getValue(String.class);
                            String userPhone = userSnapshot.child("phone").getValue(String.class);
                            Double latitude = userSnapshot.child("latitude").getValue(Double.class);
                            Double longitude = userSnapshot.child("longitude").getValue(Double.class);

                            // Update the UI with the retrieved data
                            name.setText(itemTitle);
                            description.setText(itemDescription);
                            category.setText(categoryName);
                            lightening.setText(itemLightening);
                            watering.setText(itemWatering);
                            height.setText(itemHeight + " ft");
                            price.setText("Rs. " + itemPrice);
                            ownName.setText(userName);
                            ownPhone.setText(userPhone);

                            // Convert latitude and longitude into an address
                            Geocoder geocoder = new Geocoder(PlantDetails.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (!addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String fullAddress = address.getAddressLine(0);
                                    ownAdd.setText(fullAddress);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Load the item icon using Picasso or any other image loading library
                            Picasso.get().load(itemIcon).into(icon);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle the error, if any
                        Toast.makeText(PlantDetails.this, "Error retrieving item details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}