package com.example.planteza;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;
public class ResetPassword extends AppCompatActivity {
    EditText resetEmail;
    Button send;
    FirebaseAuth authProfile;
    private final static String TAG = "ResetPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        Objects.requireNonNull(getSupportActionBar()).hide();

        resetEmail = findViewById(R.id.reset_email);
        send = findViewById(R.id.send);
        send.setOnClickListener(v -> {
           String email = resetEmail.getText().toString();
           
           if (TextUtils.isEmpty(email)){
               Toast.makeText(this, "Please enter your registered email", Toast.LENGTH_SHORT).show();
               resetEmail.setError("Email is required");
               resetEmail.requestFocus();
           }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
               Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
               resetEmail.setError("Valid email is required");
               resetEmail.requestFocus();
           }else {
               resetPassword(email);
           }
        });
    }
    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                showAlertDialog();
//                Toast.makeText(this, "Please check your inbox for reset link", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);

//                Intent intent = new Intent(ResetPassword.this, LogIn.class);
//                //Clear stack to prevent user coming back to Profile activity on pressing back button after logging out
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                finish();   //Close Profile Activity
            }else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e){
                    resetEmail.setError("User does not exists or is no longer valid. Please register again");
                } catch (Exception e){
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPassword.this);
        builder.setTitle("Reset Password");
        builder.setMessage("Please check your inbox for reset link");

        //Open Email Apps if user clicks Continue button
        builder.setPositiveButton("Check Emails", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   //Open email app in new window
            startActivity(intent);
        });

        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();
    }
}
