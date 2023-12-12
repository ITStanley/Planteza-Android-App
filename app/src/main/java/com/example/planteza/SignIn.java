package com.example.planteza;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class SignIn extends AppCompatActivity {
    private EditText signupName , signupEmail, signupPhone, signupLat, signupLong, signupPassword, signupConPassword;
    private ImageView locationPicker;
    private static final String TAG = "SignIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Objects.requireNonNull(getSupportActionBar()).hide();

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPhone = findViewById(R.id.signup_phone);
        signupLat = findViewById(R.id.signup_lat);
        signupLong = findViewById(R.id.signup_long);
        signupPassword = findViewById(R.id.signup_password);
        signupConPassword = findViewById(R.id.signup_con_password);
        locationPicker = findViewById(R.id.location_picker);
        Button signupBtn = findViewById(R.id.signup_btn);
        TextView loginTxt = findViewById(R.id.login_txt);

        // location picker click listener
        locationPicker();

        //Show/Hide Password
        ImageView passwordVisibility = findViewById(R.id.passwordShowHide);
        passwordVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
        passwordVisibility.setOnClickListener(v -> {
            if (signupPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                //If password is visible then hide it
                signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                //Change icon
                passwordVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordVisibility.setImageResource(R.drawable.baseline_visibility_24);
            }
        });
        //Show/Hide Con_Password
        ImageView con_passwordVisibility = findViewById(R.id.con_passwordShowHide);
        con_passwordVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
        con_passwordVisibility.setOnClickListener(v -> {
            if (signupConPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                //If password is visible then hide it
                signupConPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                //Change icon
                con_passwordVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                signupConPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                con_passwordVisibility.setImageResource(R.drawable.baseline_visibility_24);
            }
        });

        loginTxt.setOnClickListener(v -> Login());

        signupBtn.setOnClickListener(v -> {

            //Obtain the entered Data
            String userName = signupName.getText().toString();
            String userEmail = signupEmail.getText().toString();
            String userPhone = signupPhone.getText().toString();
            String userLatitude = signupLat.getText().toString();
            String userLongitude = signupLong.getText().toString();
            String userPassword = signupPassword.getText().toString();
            String userConPassword = signupConPassword.getText().toString();

            //Validate Phone Number using Matcher and Pattern
            String phoneRegex = "[0][0-9]{9}";    // First no. can be {6,7,8,9} and rest 9no.s can be any no.
            Matcher phoneMatcher;
            Pattern phonePattern = Pattern.compile(phoneRegex);
            phoneMatcher = phonePattern.matcher(userPhone);

            //Input fields validation
            if (TextUtils.isEmpty(userName)) {
                Toast.makeText(this, "Please Enter a Name", Toast.LENGTH_SHORT).show();
                signupName.setError("Name is Required");
                signupName.requestFocus();
            } else if (TextUtils.isEmpty(userEmail)) {
                Toast.makeText(this, "Please Enter a Email", Toast.LENGTH_SHORT).show();
                signupEmail.setError("Email is Required");
                signupEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(this, "Please Re-Enter Your Email", Toast.LENGTH_SHORT).show();
                signupEmail.setError("Valid Email is Required");
                signupEmail.requestFocus();
            } else if (TextUtils.isEmpty(userPhone)) {
                Toast.makeText(this, "Please Enter a Phone Number", Toast.LENGTH_SHORT).show();
                signupPhone.setError("Phone Number is Required");
                signupPhone.requestFocus();
            } else if (userPhone.length() != 10) {
                Toast.makeText(this, "Please Re-Enter Your  Phone Number", Toast.LENGTH_SHORT).show();
                signupPhone.setError("Phone Number Should be 10 Digits");
                signupPhone.requestFocus();
            } else if (!phoneMatcher.find()) {
                Toast.makeText(this, "Please Re-Enter Your  Phone Number", Toast.LENGTH_SHORT).show();
                signupPhone.setError("Phone Number is Not Valid");
                signupPhone.requestFocus();
            } else if (TextUtils.isEmpty(userLatitude)) {
                Toast.makeText(this, "Please Enter Latitude", Toast.LENGTH_SHORT).show();
                signupLat.setError("Latitude is Required");
                signupLat.requestFocus();
            } else if (TextUtils.isEmpty(userLongitude)) {
                Toast.makeText(this, "Please Enter Longitude", Toast.LENGTH_SHORT).show();
                signupLong.setError("Longitude is Required");
                signupLong.requestFocus();
            } else if (TextUtils.isEmpty(userPassword)) {
                Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
                signupPassword.setError("Password is Required");
                signupPassword.requestFocus();
            }else if (TextUtils.isEmpty(userConPassword)) {
                Toast.makeText(this, "Please Confirm Your Password", Toast.LENGTH_SHORT).show();
                signupConPassword.setError("Password Confirmation is Required");
                signupConPassword.requestFocus();
            }else if (!userPassword.equals(userConPassword)) {
                Toast.makeText(this, "Please Enter The Same Password", Toast.LENGTH_SHORT).show();
                signupConPassword.setError("Password Confirmation is Required");
                signupConPassword.requestFocus();
                // Clear entered passwords
                signupConPassword.clearComposingText();
                signupPassword.clearComposingText();
            }else {
                // Convert latitude and longitude strings to double values
                double latitude = Double.parseDouble(userLatitude);
                double longitude = Double.parseDouble(userLongitude);

                registerUser(userName, userEmail, userPhone, latitude, longitude, userPassword);
            }
        });
    }
    private void registerUser(String userName, String userEmail, String userPhone, double latitude, double longitude, String userPassword){
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //Create Usr Profile
        auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(SignIn.this, task -> {
            if (task.isSuccessful()){
                FirebaseUser firebaseUser = auth.getCurrentUser();

                //Update Display Name of User
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(userName).build();
                assert firebaseUser != null;
                firebaseUser.updateProfile(profileChangeRequest);

                //Enter user data into the database
                ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(userName,userEmail,userPhone, latitude, longitude);

                //Extracting user reference from Database for "Registered Users"
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        //send verification email
                        firebaseUser.sendEmailVerification();

                        //Open user profile after successful sign in
                        Intent intent = new Intent(SignIn.this, Profile.class);
                        //To prevent user from returning back to SignIn Activity on pressing back button after signup
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); //Closing SignIn Activity

                    }else {
                        Toast.makeText(SignIn.this, "User SignIn Failed.Please Try Again",Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                try {
                    throw Objects.requireNonNull(task.getException());
                }catch (FirebaseAuthWeakPasswordException e){
                    signupPassword.setError("Your password is too weak. Kindly use a mix of alphabets,numbers and special characters");
                    signupPassword.requestFocus();
                }catch (FirebaseAuthInvalidCredentialsException e){
                    signupEmail.setError("Your email is invalid or already in use.Kindly re-enter.");
                    signupEmail.requestFocus();
                }catch (FirebaseAuthUserCollisionException e){
                    signupEmail.setError("User is already registered with this email. use another email.");
                    signupEmail.requestFocus();
                }catch (Exception e){
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void Login(){
        Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }
    private void locationPicker() {
        locationPicker.setOnClickListener(v -> {
            //setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
            builder.setTitle("Add Location");
            builder.setMessage("To get the coordinates in your preferred location on Google Maps, follow these steps:\n\n" +
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
