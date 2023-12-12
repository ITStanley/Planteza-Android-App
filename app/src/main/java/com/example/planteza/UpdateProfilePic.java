package com.example.planteza;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;
public class UpdateProfilePic extends AppCompatActivity {
    ImageView uploadPic;
    Button chooseBtn, uploadBtn;
    FirebaseAuth authProfile;
    StorageReference storageReference;
    FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_pic);
        Objects.requireNonNull(getSupportActionBar()).hide();

        uploadPic = findViewById(R.id.dp);
        chooseBtn = findViewById(R.id.choose_btn);
        uploadBtn = findViewById(R.id.upload_btn);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();

        //Set user's current DP in ImageView
        Picasso.get().load(uri).into(uploadPic);

        //Choosing image to upload
        chooseBtn.setOnClickListener(v -> openFileChooser());

        //Upload Pic
        uploadBtn.setOnClickListener(v -> upload());
    }
    private void upload() {
        if (uriImage != null){
            //Save image with uid of the currently logged user
            StorageReference fileReference = storageReference.child(Objects.requireNonNull(authProfile.getCurrentUser()).getUid() + "." + getFileExtension(uriImage));

            //Upload Pic to Storage
            fileReference.putFile(uriImage).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    firebaseUser = authProfile.getCurrentUser();

                    //Set displayPic of the user after upload
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
                    firebaseUser.updateProfile(profileUpdates);
                });
                Toast.makeText(this, "Image Upload Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this,EditProfile.class);
                startActivity(intent);
                finish();

            }).addOnFailureListener(e -> Toast.makeText(UpdateProfilePic.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }else {
            Toast.makeText(UpdateProfilePic.this, "No File Selected!", Toast.LENGTH_SHORT).show();
        }
    }
    //Obtain File Extension of the image
    private  String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            uploadPic.setImageURI(uriImage);
        }
    }
}