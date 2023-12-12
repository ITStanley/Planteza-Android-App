package com.example.planteza;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.Manifest;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;
public class AddItem extends AppCompatActivity {
    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private ImageView itemIcon;
    private Uri imageUri;
    private String itemTitle,itemDescription, itemCategory, itemLightening, itemWatering, itemHeight, itemPrice;
    private EditText title,description, height, price;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Spinner categories, lightening, watering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Objects.requireNonNull(getSupportActionBar()).hide();


        firebaseAuth = FirebaseAuth.getInstance();

        itemIcon = findViewById(R.id.itemIcon);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        height = findViewById(R.id.height);
        price = findViewById(R.id.price);
        Button addItemBtn = findViewById(R.id.addItemBtn);
        categories = findViewById(R.id.categories);
        lightening = findViewById(R.id.lightening);
        watering = findViewById(R.id.watering);

        initializeSpinners();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        itemIcon.setOnClickListener(v -> {
            //show dialog to pick image
            showImagePickDialog();
        });

        addItemBtn.setOnClickListener(v -> {
            //Get and validate data
            inputData();
        });
    }

    private void initializeSpinners() {
        //Initializing categories dropdown
        CharSequence[] categoriesArray = getResources().getStringArray(R.array.categories_array);
        CustomArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.spinner_item, categoriesArray);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        categories.setAdapter(adapter);
        categories.setSelection(0);                 // Set the hint item as the initially selected item

        //Initializing lightening dropdown
        CharSequence[] lighteningArray = getResources().getStringArray(R.array.lightening_array);
        CustomArrayAdapter adapter1 = new CustomArrayAdapter(this, R.layout.spinner_item, lighteningArray);
        adapter1.setDropDownViewResource(R.layout.spinner_item_dropdown);
        lightening.setAdapter(adapter1);
        lightening.setSelection(0);                 // Set the hint item as the initially selected item

        //Initializing watering dropdown
        CharSequence[] wateringArray = getResources().getStringArray(R.array.watering_array);
        CustomArrayAdapter adapter2 = new CustomArrayAdapter(this, R.layout.spinner_item, wateringArray);
        adapter2.setDropDownViewResource(R.layout.spinner_item_dropdown);
        watering.setAdapter(adapter2);
        watering.setSelection(0);                   // Set the hint item as the initially selected item
    }
    private void inputData(){
        //Input data
        itemTitle = title.getText().toString().trim();
        itemDescription = description.getText().toString().trim();
        itemCategory= categories.getSelectedItem().toString();
        itemLightening= lightening.getSelectedItem().toString();
        itemWatering= watering.getSelectedItem().toString();
        itemHeight= height.getText().toString().trim();
        itemPrice = price.getText().toString().trim();

        //Validate data
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        if (TextUtils.isEmpty(itemTitle)){
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }
        if (TextUtils.isEmpty(itemDescription)){
            Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }
        if (categories.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select the plant category", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        if (lightening.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select required lightening", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        if (watering.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select watering option", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        if (TextUtils.isEmpty(itemHeight)){
            Toast.makeText(this, "Height is required", Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }
        if (TextUtils.isEmpty(itemPrice)){
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }

        //Add data to db
        addProduct();

    }
    private void addProduct(){
        progressDialog.setMessage("Adding Item...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        //first upload image to storage
        //name and path of image to be uploaded
        String filePathAndName = "item_images/" + "" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            //image uploaded
            //get url of uploaded image
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful());
            Uri downloadImageUri = uriTask.getResult();

            if (uriTask.isSuccessful()){
                //url of image received, upload to db
                //setup data to upload
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("itemId", "" + timestamp);
                hashMap.put("itemImage", "" + downloadImageUri);
                hashMap.put("itemTitle", "" + itemTitle);
                hashMap.put("itemDescription", "" + itemDescription);
                hashMap.put("itemCategory", "" + itemCategory);
                hashMap.put("itemLightening", "" + itemLightening);
                hashMap.put("itemWatering", "" + itemWatering);
                hashMap.put("itemHeight", "" + itemHeight);
                hashMap.put("itemPrice", "" + itemPrice);
                hashMap.put("timestamp", "" + timestamp);
                hashMap.put("uid", "" + firebaseAuth.getUid());

                //add to db
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
                reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("items").child(timestamp).setValue(hashMap).addOnSuccessListener(unused -> {
                    //added to db
                    progressDialog.dismiss();
                    Toast.makeText(AddItem.this, "Item added...", Toast.LENGTH_SHORT).show();
                    clearData();
                }).addOnFailureListener(e -> {
                    //failed adding to db
                    progressDialog.dismiss();
                    Toast.makeText(AddItem.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            //failed uploading image
            progressDialog.dismiss();
            Toast.makeText(AddItem.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void clearData(){
        //clear data after uploading product
        itemIcon.setImageResource(R.drawable.item_icon);
        imageUri = null;
        title.setText("");
        description.setText("");
        categories.setSelection(0);
        lightening.setSelection(0);
        watering.setSelection(0);
        height.setText("");
        price.setText("");
    }
    private void showImagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, (dialog, which) -> {
            //handle item clicks
            if (which==0){
                //camera clicked
                if (checkCameraPermission()){
                    //permission granted
                    pickFromCamera();
                }
                else {
                    //permission no granted, request
                    requestCameraPermission();
                }
            }
            else {
                //gallery clicked
                if (checkStoragePermission()){
                    //permission granted
                    pickFromGallery();
                }
                else {
                    //permission no granted, request
                    requestStoragePermission();
                }
            }
        }).show();
    }
    private void pickFromGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }
    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //both permissions granted
                        pickFromCamera();
                    }
                    else {
                        //both or one of permissions denied
                        Toast.makeText(this, "Camera & Storage permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Storage permissions is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //handle image pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image picked from gallery

                //saved picked image uri
                assert data != null;
                imageUri = data.getData();

                //set image
                itemIcon.setImageURI(imageUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image picked from camera
                itemIcon.setImageURI(imageUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}

