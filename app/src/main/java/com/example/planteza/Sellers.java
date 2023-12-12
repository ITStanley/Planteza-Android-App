package com.example.planteza;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class Sellers extends FragmentActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_CODE = 101;
    private GoogleMap mMap;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellers);

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.sellers);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            switch(item.getItemId())
            {
                case R.id.items:
                    startActivity(new Intent(getApplicationContext(),Home.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.sellers:
                    return true;
                case R.id.blog:
                    startActivity(new Intent(getApplicationContext(),Blog.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(),Profile.class));
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });

        if (isLocationPermissionGranted()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            assert mapFragment != null;
            mapFragment.getMapAsync(this);
        }
        else{
            requestLocationPermission();
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Retrieve user locations and item details from Firebase and mark them on the map
        retrieveUserLocations();

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(marker -> {
            String markerSnippet = marker.getSnippet();
            Intent i = new Intent(Sellers.this,SellerDetails.class);
            i.putExtra("uid",markerSnippet);
            startActivity(i);
            return false;
        });

        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }
    }
    private void retrieveUserLocations() {
        FirebaseDatabase.getInstance().getReference("Registered Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            ReadWriteUserDetails user = userSnapshot.getValue(ReadWriteUserDetails.class);

                            if (user != null && userSnapshot.child("items").exists()) {
                                double latitude = user.getLatitude();
                                double longitude = user.getLongitude();
                                String name = user.getName();

                                if (userSnapshot.child("items").getChildrenCount() != 0) {
                                    for (DataSnapshot itemSnapshot : userSnapshot.child("items").getChildren()) {
                                        Item item = itemSnapshot.getValue(Item.class);
                                        if (item != null) {
                                            String uid = item.getUid();
                                            markLocationOnMap(latitude, longitude, name, uid);
                                            break; // Break after the first item
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });
    }
    private void markLocationOnMap(double latitude, double longitude, String name, String uid) {
        LatLng location = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(location).title(name).snippet(uid);
        Marker marker = mMap.addMarker(markerOptions);
        assert marker != null;
        marker.setTag(location);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
    }
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }
}

