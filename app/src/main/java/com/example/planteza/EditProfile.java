package com.example.planteza;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class EditProfile extends AppCompatActivity {
    EditText editName, editPhone, editLat, editLong;
    Button editEmailBtn, resetPswrdBtn, saveBtn;
    ImageButton editDp;
    String name, phone, email, latitudeS, longitudeS;
    double latitude, longitude;
    FirebaseAuth authProfile;
    ImageView dp, locationPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        dp = findViewById(R.id.dp);
        editDp = findViewById(R.id.edit_dp);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editLat = findViewById(R.id.editLat);
        editLong = findViewById(R.id.editLong);
        locationPicker = findViewById(R.id.location_picker);
        editEmailBtn = findViewById(R.id.editEmail_btn);
        resetPswrdBtn = findViewById(R.id.restPassword);
        saveBtn = findViewById(R.id.save_btn);
        
        //show profile data
        assert firebaseUser != null;
        showProfile(firebaseUser);

        //Upload Dp
        editDp.setOnClickListener(view ->{
            Intent intent = new Intent(this,UpdateProfilePic.class);
            startActivity(intent);
        });

        // location picker click listener
        locationPicker();

        //Update Email
        editEmailBtn.setOnClickListener(view ->{
            Intent intent = new Intent(this,UpdateEmail.class);
            startActivity(intent);
        });

        //Reset Password
        resetPswrdBtn.setOnClickListener(view ->{
            Intent intent = new Intent(this,ResetPassword.class);
            startActivity(intent);
        });

        //Update Profile
        saveBtn.setOnClickListener(view -> updateProfile(firebaseUser));
    }
    private void updateProfile(FirebaseUser firebaseUser) {
        //Obtain the data entered by user
        name = editName.getText().toString();
        email = firebaseUser.getEmail();
        phone = editPhone.getText().toString();
        latitudeS = editLat.getText().toString();
        longitudeS = editLong.getText().toString();

        // Check if name, latitude and longitude fields are empty and validate phone number using Matcher and Pattern
        String phoneRegex = "[0][0-9]{9}";    // First no. can be {6,7,8,9} and rest 9no.s can be any no.
        Matcher phoneMatcher;
        Pattern phonePattern = Pattern.compile(phoneRegex);
        phoneMatcher = phonePattern.matcher(phone);

        if (TextUtils.isEmpty(latitudeS)) {
            Toast.makeText(this, "Please Enter Latitude", Toast.LENGTH_SHORT).show();
            editLat.setError("Latitude is Required");
            editLat.requestFocus();
        } else if (TextUtils.isEmpty(longitudeS)) {
            Toast.makeText(this, "Please Enter Longitude", Toast.LENGTH_SHORT).show();
            editLong.setError("Longitude is Required");
            editLong.requestFocus();
        } else if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please Enter a Name", Toast.LENGTH_SHORT).show();
            editName.setError("Name is Required");
            editName.requestFocus();
        }else if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please Enter a Phone Number", Toast.LENGTH_SHORT).show();
            editPhone.setError("Phone Number is Required");
            editPhone.requestFocus();
        } else if (phone.length() != 10) {
            Toast.makeText(this, "Please Re-Enter Your  Phone Number", Toast.LENGTH_SHORT).show();
            editPhone.setError("Phone Number Should be 10 Digits");
            editPhone.requestFocus();
        } else if (!phoneMatcher.find()) {
            Toast.makeText(this, "Please Re-Enter Your  Phone Number", Toast.LENGTH_SHORT).show();
            editPhone.setError("Phone Number is Not Valid");
            editPhone.requestFocus();
        }else {

            // Convert latitude and longitude strings to double values
            try {
                latitude = Double.parseDouble(latitudeS);
                longitude = Double.parseDouble(longitudeS);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Latitude or Longitude", Toast.LENGTH_SHORT).show();
                return; // Return early if latitude or longitude cannot be parsed as double
            }

            //Extract user reference from database for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userID = firebaseUser.getUid();

            // Update user data in the database
            referenceProfile.child(userID).child("name").setValue(name, (error, ref) -> {
                if (error != null) {
                    Toast.makeText(EditProfile.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                }
            });

            referenceProfile.child(userID).child("phone").setValue(phone, (error, ref) -> {
                if (error != null) {
                    Toast.makeText(EditProfile.this, "Failed to update phone number", Toast.LENGTH_SHORT).show();
                }
            });

            referenceProfile.child(userID).child("latitude").setValue(latitude, (error, ref) -> {
                if (error != null) {
                    Toast.makeText(EditProfile.this, "Failed to update latitude", Toast.LENGTH_SHORT).show();
                }
            });

            referenceProfile.child(userID).child("longitude").setValue(longitude, (error, ref) -> {
                if (error != null) {
                    Toast.makeText(EditProfile.this, "Failed to update longitude", Toast.LENGTH_SHORT).show();
                }
            });

            Toast.makeText(EditProfile.this, "Update Successful!", Toast.LENGTH_SHORT).show();

            //To prevent user from returning back to EditProfile Activity on pressing back and close activity
            Intent intent = new Intent(EditProfile.this, Profile.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
    //Fetch data from Firebase and display
    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        //Extracting user references from database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userIDofRegistered).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null){
                    name = readUserDetails.getName();
                    phone = readUserDetails.getPhone();
                    latitude = readUserDetails.getLatitude();
                    longitude = readUserDetails.getLongitude();

                    editName.setText(name);
                    editPhone.setText(phone);
                    editLat.setText(String.valueOf(latitude));
                    editLong.setText(String.valueOf(longitude));

                    //Set user DP after user has uploaded pic
                    Uri uri = firebaseUser.getPhotoUrl();
                    //set image uri
                    Picasso.get().load(uri).into(dp);

                    //set default dp if user don't upload dp
                    if (uri == null){
                        dp.setImageResource(R.drawable.dp);
                    }

                }else {
                    Toast.makeText(EditProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void locationPicker() {
        locationPicker.setOnClickListener(v -> {
            //setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
            builder.setTitle("Add Location");
            builder.setMessage("To update the coordinates in your preferred location on Google Maps, follow these steps:\n\n" +
                    "1. Go to Google Maps.\n" +
                    "2. Find your preferred location on the map.\n" +
                    "3. Hold down on the map for a moment until a marker appears.\n" +
                    "4. The coordinates will be displayed in the search bar.\n" +
                    "5. Copy the coordinates.\n" +
                    "6. Paste the first coordinate as latitude and the second coordinate as longitude.\n\n" +
                    "Please ensure that you follow these steps accurately to extract the correct coordinates.");

            //Open Email Apps if user clicks Continue button
            builder.setPositiveButton("Continue", (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MAPS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   //To maps app in new window and not within our app
                startActivity(intent);
            });
            //Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }
}
