package com.example.planteza;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;
public class BlogPost extends AppCompatActivity {

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_post);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Retrieve the clicked item index from the intent
        Intent intent = getIntent();
        int clickedIndex = intent.getIntExtra("clickedIndex", -1);

        // Get the blog topic, image, info arrays from resources
        String[] blogTopic = getResources().getStringArray(R.array.blog_topics);
        int[] blogImage = {R.drawable.blog1, R.drawable.blog2, R.drawable.blog3, R.drawable.blog4, R.drawable.blog5};
        String[] blogInfo = getResources().getStringArray(R.array.blog_info);

        // Check if the clickedIndex is valid
        if (clickedIndex >= 0 && clickedIndex < blogInfo.length) {
            String blogHead = blogTopic[clickedIndex];
            int blogPic = blogImage[clickedIndex];
            String blogText = blogInfo[clickedIndex];

            // Set the blog topic, blog text in a TextView
            TextView tv1 = findViewById(R.id.topic);
            tv1.setText(blogHead);
            ImageView iv = findViewById(R.id.image);
            iv.setImageResource(blogPic);
            TextView tv2 = findViewById(R.id.info);
            tv2.setText(blogText);
        }
    }
}
