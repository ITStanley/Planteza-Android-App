package com.example.planteza;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
public class EditItem extends AppCompatActivity {
    // permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    // image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    // permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri imageUri;
    private ImageView itemIcon;
    private EditText title, description, height, price;
    private ProgressDialog progressDialog;
    private Spinner categories, lightening, watering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        Objects.requireNonNull(getSupportActionBar()).hide();

        itemIcon = findViewById(R.id.itemIcon);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        height = findViewById(R.id.height);
        price = findViewById(R.id.price);
        Button saveBtn = findViewById(R.id.saveBtn);
        categories = findViewById(R.id.categories);
        lightening = findViewById(R.id.lightening);
        watering = findViewById(R.id.watering);

        // Initialize spinners
        initializeSpinners();

        // Get the item ID from the intent
        Intent intent = getIntent();
        if (intent != null) {
            String itemId = intent.getStringExtra("itemId");
            if (itemId != null) {
                // Retrieve the existing item data from the database using the item ID
                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("items").child(itemId);
                itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get the item details from the snapshot
                            String itemTitle = snapshot.child("itemTitle").getValue(String.class);
                            String itemDescription = snapshot.child("itemDescription").getValue(String.class);
                            String itemHeight = snapshot.child("itemHeight").getValue(String.class);
                            String itemPrice = snapshot.child("itemPrice").getValue(String.class);
                            String itemCategory = snapshot.child("itemCategory").getValue(String.class);
                            String itemLightening = snapshot.child("itemLightening").getValue(String.class);
                            String itemWatering = snapshot.child("itemWatering").getValue(String.class);
                            String itemImage = snapshot.child("itemImage").getValue(String.class);

                            // Set the retrieved data to the respective views
                            title.setText(itemTitle);
                            description.setText(itemDescription);
                            height.setText(itemHeight);
                            price.setText(itemPrice);

                            // Set the category spinner selection
                            if (itemCategory != null) {
                                int categoryIndex = getCategoryIndex(itemCategory);
                                categories.setSelection(categoryIndex);
                            }

                            // Set the lightening spinner selection
                            if (itemLightening != null) {
                                int lighteningIndex = getLighteningIndex(itemLightening);
                                lightening.setSelection(lighteningIndex);
                            }

                            // Set the watering spinner selection
                            if (itemWatering != null) {
                                int wateringIndex = getWateringIndex(itemWatering);
                                watering.setSelection(wateringIndex);
                            }

                            // Set the item image using Picasso library
                            try {
                                Picasso.get().load(itemImage).into(itemIcon);
                            } catch (Exception e) {
                                // Error loading image
                                Picasso.get().load(R.drawable.choose_img).into(itemIcon);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditItem.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Handle click on itemIcon
        itemIcon.setOnClickListener(v -> {
            // Show image pick dialog
            showImagePickDialog();
        });

        // Handle click on saveBtn
        saveBtn.setOnClickListener(v -> {
            // Validate data
            validateData();
        });
    }
    private void initializeSpinners() {
        // Categories spinner
        CharSequence[] categoriesArray = getResources().getStringArray(R.array.categories_array);
        CustomArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.spinner_item, categoriesArray);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        categories.setAdapter(adapter);

        // Lightening spinner
        CharSequence[] lighteningArray = getResources().getStringArray(R.array.lightening_array);
        CustomArrayAdapter adapter1 = new CustomArrayAdapter(this, R.layout.spinner_item, lighteningArray);
        adapter1.setDropDownViewResource(R.layout.spinner_item_dropdown);
        lightening.setAdapter(adapter1);

        // Watering spinner
        CharSequence[] wateringArray = getResources().getStringArray(R.array.watering_array);
        CustomArrayAdapter adapter2 = new CustomArrayAdapter(this, R.layout.spinner_item, wateringArray);
        adapter2.setDropDownViewResource(R.layout.spinner_item_dropdown);
        watering.setAdapter(adapter2);
    }
    private int getCategoryIndex(String category) {
        String[] categoriesArray = getResources().getStringArray(R.array.categories_array);
        return Arrays.asList(categoriesArray).indexOf(category);
    }
    private int getLighteningIndex(String lightening) {
        String[] lighteningArray = getResources().getStringArray(R.array.lightening_array);
        return Arrays.asList(lighteningArray).indexOf(lightening);
    }
    private int getWateringIndex(String watering) {
        String[] wateringArray = getResources().getStringArray(R.array.watering_array);
        return Arrays.asList(wateringArray).indexOf(watering);
    }
    private void showImagePickDialog() {
        // Options to display in dialog
        String[] options = {"Camera", "Gallery"};
        // Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, (dialog, which) -> {
                    // Handle item click
                    if (which == 0) {
                        // Camera clicked
                        if (checkCameraPermission()) {
                            // Camera permission granted
                            pickFromCamera();
                        } else {
                            // Camera permission not granted, request it
                            requestCameraPermission();
                        }
                    } else {
                        // Gallery clicked
                        if (checkStoragePermission()) {
                            // Storage permission granted
                            pickFromGallery();
                        } else {
                            // Storage permission not granted, request it
                            requestStoragePermission();
                        }
                    }
                })
                .show();
    }
    private boolean checkCameraPermission() {
        // Check if camera permission is enabled or not
        // Return true if enabled, false otherwise
        boolean resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return resultCamera && resultStorage;
    }
    private void requestCameraPermission() {
        // Request runtime camera permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }
    private boolean checkStoragePermission() {
        // Check if storage permission is enabled or not
        // Return true if enabled, false otherwise
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }
    private void requestStoragePermission() {
        // Request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }
    private void pickFromCamera() {
        // Intent to pick image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        // Put image uri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }
    private void pickFromGallery() {
        // Pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }
    private void validateData() {
        // Get data from EditTexts
        String itemTitle = title.getText().toString().trim();
        String itemDescription = description.getText().toString().trim();
        String itemHeight = height.getText().toString().trim();
        String itemPrice = price.getText().toString().trim();
        String itemCategory = categories.getSelectedItem().toString();
        String itemLightening = lightening.getSelectedItem().toString();
        String itemWatering = watering.getSelectedItem().toString();

        // Validate data
        if (TextUtils.isEmpty(itemTitle)) {
            Toast.makeText(this, "Enter item title...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(itemDescription)) {
            Toast.makeText(this, "Enter item description...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(itemHeight)) {
            Toast.makeText(this, "Enter item height...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(itemPrice)) {
            Toast.makeText(this, "Enter item price...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(itemCategory)) {
            Toast.makeText(this, "Select item category...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(itemLightening)) {
            Toast.makeText(this, "Select item lightening...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(itemWatering)) {
            Toast.makeText(this, "Select item watering...", Toast.LENGTH_SHORT).show();
        } else {
            // All data is validated, now update item
            updateItem(itemTitle, itemDescription, itemHeight, itemPrice, itemCategory, itemLightening, itemWatering);
        }
    }
    private void updateItem(String itemTitle, String itemDescription, String itemHeight, String itemPrice, String itemCategory, String itemLightening, String itemWatering) {
        progressDialog.setMessage("Updating item...");
        progressDialog.show();

        if (imageUri == null) {
            // Update without image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("itemTitle", itemTitle);
            hashMap.put("itemDescription", itemDescription);
            hashMap.put("itemHeight", itemHeight);
            hashMap.put("itemPrice", itemPrice);
            hashMap.put("itemCategory", itemCategory);
            hashMap.put("itemLightening", itemLightening);
            hashMap.put("itemWatering", itemWatering);

            DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .child("items")
                    .child(Objects.requireNonNull(getIntent().getStringExtra("itemId")));
            itemRef.updateChildren(hashMap)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditItem.this, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditItem.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Update with image
            String filePathAndName = "item_images/" + getIntent().getStringExtra("itemId");

            // Upload image to Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded, now get the image url
                        storageReference.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    // Url of the uploaded image
                                    String itemImage = uri.toString();

                                    // Update item with image
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("itemTitle", itemTitle);
                                    hashMap.put("itemDescription", itemDescription);
                                    hashMap.put("itemHeight", itemHeight);
                                    hashMap.put("itemPrice", itemPrice);
                                    hashMap.put("itemCategory", itemCategory);
                                    hashMap.put("itemLightening", itemLightening);
                                    hashMap.put("itemWatering", itemWatering);
                                    hashMap.put("itemImage", itemImage);

                                    DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("Registered Users")
                                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                            .child("items")
                                            .child(Objects.requireNonNull(getIntent().getStringExtra("itemId")));
                                    itemRef.updateChildren(hashMap)
                                            .addOnSuccessListener(aVoid -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditItem.this, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditItem.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditItem.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditItem.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // This method will be called after picking image from Camera or Gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Image is picked from gallery, get uri of image
                assert data != null;
                imageUri = data.getData();
                // Set to ImageView
                itemIcon.setImageURI(imageUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // Image is picked from camera, get uri of image
                itemIcon.setImageURI(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // This method will be called when user presses Allow or Deny from permission request dialog
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        // Both permission granted
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera and Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Camera and Storage permissions are required", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        // Storage permission granted
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
