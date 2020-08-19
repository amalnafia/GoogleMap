package com.example.googlemap.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class MapHelper {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BW_UPDATES = 3;

    public void setMarker(LatLng latLng, GoogleMap googleMap,String title) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);
        googleMap.addMarker(markerOptions.position(new LatLng(latLng.latitude
                , latLng.longitude)).title(title).draggable(true));

    }


    @SuppressLint("MissingPermission")
    public void getLocation(LocationListener locationListener , Context application) {
        try {
            LocationManager locationManager = (LocationManager) application.getSystemService(LOCATION_SERVICE);
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            locationManager.requestLocationUpdates(locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        } catch (Exception e) {
            Log.e("GET_LOCATION_CRASHED", e.getMessage());
        }
    }

    public List<Address> getLocationName(Marker marker, Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
