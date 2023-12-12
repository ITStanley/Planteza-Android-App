package com.example.planteza;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
public class UpdateEmail extends AppCompatActivity {
    FirebaseAuth authProfile;
    FirebaseUser firebaseUser;
    TextView textViewAuthenticated;
    EditText  editTextNewEmail, editTextPwd;
    Button buttonUpdateEmail;
    String userOldEmail, userNewEmail, userPwd;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        Objects.requireNonNull(getSupportActionBar()).hide();

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        editTextPwd = findViewById(R.id.email_verify_password);
        editTextNewEmail = findViewById(R.id.email_new);
        textViewAuthenticated = findViewById(R.id.update_email_authenticated);
        buttonUpdateEmail = findViewById(R.id.update_email_btn);

        //Show/Hide Password
        ImageView passwordVisibility = findViewById(R.id.passwordShowHide);
        passwordVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
        passwordVisibility.setOnClickListener(v -> {

            if (editTextPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                //If password is visible then hide it
                editTextPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                //Change icon
                passwordVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                editTextPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordVisibility.setImageResource(R.drawable.baseline_visibility_24);
            }

        });

        buttonUpdateEmail.setEnabled(false);    //Make button disabled in the beginning until the user is authenticated
        editTextNewEmail.setEnabled(false);

//      Set old email ID on TextView
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail= findViewById(R.id.update_email_old);
        textViewOldEmail.setText(userOldEmail);

        if (firebaseUser == null){
            Toast.makeText(this, "Something went wrong! User's details not available", Toast.LENGTH_LONG).show();
        }else {
            reAuthenticate(firebaseUser);
        }
    }
    //Re-authenticate/verify user before updating email
    @SuppressLint("SetTextI18n")
    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button authenticateBtn = findViewById(R.id.authenticate_user_btn);
        authenticateBtn.setOnClickListener(v -> {

            //Obtain password for authentication
            userPwd = editTextPwd.getText().toString();

            if (TextUtils.isEmpty(userPwd)){
                Toast.makeText(this, "Password is needed to continue", Toast.LENGTH_SHORT).show();
                editTextPwd.setError("Please enter your password for authentication");
                editTextPwd.requestFocus();
            } else {
                AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail,userPwd);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(this, "Password has been verified." + "You can update email now", Toast.LENGTH_LONG).show();

                        //Set TextView to show that user is authenticated
                        textViewAuthenticated.setText("You are authenticated.you can update your email now");

                        //Disable EditText for password, button to verify and enable EditText for new Email and update button
                        editTextNewEmail.setEnabled(true);
                        editTextPwd.setEnabled(false);
                        buttonUpdateEmail.setEnabled(true);
                        authenticateBtn.setEnabled(false);

                        //Change color of  update email button
                        buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmail.this, R.color.logo_d));

                        buttonUpdateEmail.setOnClickListener(v1 -> {
                            userNewEmail = editTextNewEmail.getText().toString();
                            if (TextUtils.isEmpty(userNewEmail)){
                                Toast.makeText(this, "New Email is Required", Toast.LENGTH_SHORT).show();
                                editTextNewEmail.setError("Please enter new email");
                                editTextNewEmail.requestFocus();
                            } else if(!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                                editTextNewEmail.setError("Please provide valid email");
                                editTextNewEmail.requestFocus();
                            }else if(userOldEmail.matches(userNewEmail)) {
                                Toast.makeText(this, "New email cannot be same as old email", Toast.LENGTH_SHORT).show();
                                editTextNewEmail.setError("Please enter valid email");
                                editTextNewEmail.requestFocus();
                            }else {
                                updateEmail(firebaseUser);
                            }
                        });
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e){
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(task -> {
            if (task.isComplete()){
                // Update email in Realtime Database
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                String userID = firebaseUser.getUid();
                referenceProfile.child(userID).child("email").setValue(userNewEmail, (error, ref) -> {
                    if (error != null) {
                        Toast.makeText(UpdateEmail.this, "Failed to update email in database", Toast.LENGTH_SHORT).show();
                    }
                });

                //Verify email
                firebaseUser.sendEmailVerification();

                showAlertDialog();
                finish();
            }else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (Exception e){
                    Toast.makeText(UpdateEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateEmail.this);
        builder.setTitle("Email Updated");
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
}
