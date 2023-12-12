package com.example.planteza;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class Profile extends AppCompatActivity {
    FirebaseAuth authProfile;
    TextView profileName, profileEmail, profilePhone, profileAddress, logoutBtn;
    String name, email, phone;
    double latitude, longitude;
    Button editBtn, itemsBtn,deleteBtn;
    ImageView profilePic;

    @SuppressLint({"NonConstantResourceId", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();

        profilePic = findViewById(R.id.profile_pic);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profilePhone = findViewById(R.id.profile_phone);
        profileAddress = findViewById(R.id.profile_address);
        editBtn = findViewById(R.id.edit_btn);
        itemsBtn = findViewById(R.id.items_btn);
        logoutBtn = findViewById(R.id.logout_btn);
        deleteBtn = findViewById(R.id.delete_btn);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null){
            Toast.makeText(this, "Something went wrong! User's details are not available ath the moment",
                    Toast.LENGTH_SHORT).show();
        }else {
            showUserProfile(firebaseUser);
        }

        editBtn.setOnClickListener(view -> {
            startActivity(new Intent(this,EditProfile.class));
        });
        itemsBtn.setOnClickListener(view -> {
            startActivity(new Intent(this,UserItems.class));
        });
        logoutBtn.setOnClickListener(view -> {
            authProfile.signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,LogIn.class);
            startActivity(intent);
            finish();
        });
        deleteBtn.setOnClickListener(view -> deleteAccount());

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.items:
                    startActivity(new Intent(getApplicationContext(),Home.class));
                    overridePendingTransition(0,0);
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
                    return true;
            }
            return false;
        });
    }
    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        // Extracting User Reference from database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    name = readUserDetails.getName();
                    email = readUserDetails.getEmail();
                    phone = readUserDetails.getPhone();
                    latitude = readUserDetails.getLatitude();
                    longitude = readUserDetails.getLongitude();

                    profileName.setText(name);
                    profileEmail.setText(email);
                    profilePhone.setText(phone);

                    // Convert latitude and longitude into an address
                    Geocoder geocoder = new Geocoder(Profile.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String fullAddress = address.getAddressLine(0);
                            profileAddress.setText(fullAddress);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Set user DP after the user has uploaded a pic
                    Uri uri = firebaseUser.getPhotoUrl();
                    Picasso.get().load(uri).into(profilePic);

                    //set default dp if user don't upload dp
                    if (uri == null){
                        profilePic.setImageResource(R.drawable.dp);
                    }

                    // Check email verification only after successfully showing the user profile
                    checkIfEmailVerified(firebaseUser);
                } else {
                    Toast.makeText(Profile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        firebaseUser.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!firebaseUser.isEmailVerified()) {
                    showAlertDialog();
                }
            } else {
                Toast.makeText(Profile.this, "Failed to reload user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification next time.");

        //Open Email Apps if user clicks Continue button
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   //To email app in new window and not within our app
            startActivity(intent);
        });
        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();
    }
    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            FirebaseUser user = authProfile.getCurrentUser();
            assert user != null;
            String userID = user.getUid();

            // delete user data from Firebase
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users").child(userID);
            reference.removeValue();

            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Profile.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Profile.this, LogIn.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Profile.this, "Failed to delete account", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> {
            // Do nothing
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

}
