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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
public class LogIn extends AppCompatActivity {
    EditText loginEmail, loginPassword;
    FirebaseAuth authProfile;
    private static final String TAG = "LogIn";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        Objects.requireNonNull(getSupportActionBar()).hide();

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        Button loginBtn = findViewById(R.id.login_btn);
        TextView forgotTxt = findViewById(R.id.forgot_txt);
        TextView signupTxt = findViewById(R.id.signup_txt);

        authProfile = FirebaseAuth.getInstance();

        //Show/Hide Password
        ImageView passwordVisibility = findViewById(R.id.passwordShowHide);
        passwordVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
        passwordVisibility.setOnClickListener(v -> {

            if (loginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                //If password is visible then hide it
                loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                //Change icon
                passwordVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                loginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordVisibility.setImageResource(R.drawable.baseline_visibility_24);
            }

        });

        //Login User
        loginBtn.setOnClickListener(view -> {
            String userEmail = loginEmail.getText().toString();
            String userPassword = loginPassword.getText().toString();

            if (TextUtils.isEmpty(userEmail)){
                Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
                loginEmail.setError("Email is Required");
                loginEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(this, "Please Re-Enter Your Email", Toast.LENGTH_SHORT).show();
                loginEmail.setError("Valid Email is Required");
                loginEmail.requestFocus();
            }else if (TextUtils.isEmpty(userPassword)) {
                Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
                loginPassword.setError("Password is Required");
                loginPassword.requestFocus();
            }else {
                loginUser(userEmail,userPassword);
            }

        });

        forgotTxt.setOnClickListener(view -> {
            Intent intent = new Intent(LogIn.this, ResetPassword.class);
            startActivity(intent);
        });

        signupTxt.setOnClickListener(view -> {
            Intent intent = new Intent(LogIn.this, SignIn.class);
            startActivity(intent);
        });

    }
    private void loginUser(String email, String pwd) {
        authProfile.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                //Get instance of the current user
                FirebaseUser firebaseUser = authProfile.getCurrentUser();

                //check if email is verified before user can access their profile
                assert firebaseUser != null;
                if (firebaseUser.isEmailVerified()){
                    Toast.makeText(LogIn.this,"You are Logged in Now", Toast.LENGTH_SHORT).show();

                    //Start the Profile activity
                    Intent intent = new Intent(LogIn.this, Profile.class);
                    startActivity(intent);
                    finish();   //Close login activity

                }else{
                    firebaseUser.sendEmailVerification();
                    authProfile.signOut();  //Sign out user
                    showAlertDialog();
                }

            }else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e) {
                    loginEmail.setError("User does not exists or is no longer valid. Please register again.");
                    loginEmail.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    loginPassword.setError("Invalid Credentials. Kindly Check and Re-enter");
                    loginPassword.requestFocus();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(LogIn.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LogIn.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification.");

        //Open Email Apps if user clicks Continue button
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //Directed to email app in new window
            startActivity(intent);
        });

        AlertDialog alertDialog = builder.create();             //Create the AlertDialog
        alertDialog.show();                                     //Show the AlertDialog
    }

    //check if user is already logged in.In such case,Straightaway take the user to user's profile
    @Override
    protected void onStart() {
        super.onStart();
        if (authProfile.getCurrentUser() != null){
            //Start the Profile activity
            Intent intent = new Intent(LogIn.this, Profile.class);
            startActivity(intent);
            finish();                   //Close login activity
        }
    }
}
