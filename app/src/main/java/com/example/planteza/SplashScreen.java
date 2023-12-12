package com.example.planteza;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Objects.requireNonNull(getSupportActionBar()).hide();

        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreen.this,LogIn.class);
            startActivity(i);
            finish();
        }, 7000);

        Typing txtView = findViewById(R.id.texttyp);
        txtView.setText("");
        txtView.setCharacterAdder(80);
        txtView.animateText("A Beautiful Plant Is Like Having A Friend Around The House...");
    }
}
