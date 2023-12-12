package com.example.planteza;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;
public class Blog extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        Objects.requireNonNull(getSupportActionBar()).hide();

        String[] blogTopics = getResources().getStringArray(R.array.blog_topics);

        ListView listView = findViewById(R.id.blogTopicsList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.blog_topic_item, blogTopics);
        listView.setAdapter(adapter);

        // Set item click listener for the ListView
        listView.setOnItemClickListener((parent, view, position, id) -> {

            // Start the BlogPost activity and pass the clicked item index
            Intent intent = new Intent(Blog.this, BlogPost.class);
            intent.putExtra("clickedIndex", position);
            startActivity(intent);

        });

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.blog);
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
                    return true;
                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(),Profile.class));
                    overridePendingTransition(0,0);
                    return true;
            }return false;
        });
    }
}
