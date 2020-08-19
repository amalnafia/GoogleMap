package com.example.googlemap.ui;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.googlemap.R;
import com.example.googlemap.helper.MapHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import butterknife.OnClick;

import static android.widget.Toast.LENGTH_SHORT;

public class LocationFragment extends Fragment implements OnMapReadyCallback {
    public LocationFragment() {
    }

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    GoogleMap googleMap;
    private boolean locationPermissionGranted;
    private LatLng myCurrentLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMapClickListener(onMapClickListener);
        new MapHelper().getLocation(locationListener, getContext());
        this.googleMap.setOnMarkerDragListener(onMarkerDragListener);
        this.googleMap.setOnMarkerClickListener(onMarkerClickListener);
        this.googleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        this.googleMap.setOnMyLocationClickListener(onMyLocationClickListener);
        updateLocationUI();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            myCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            new MapHelper().setMarker(myCurrentLocation, googleMap,"My Clicked Location");
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myCurrentLocation));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            myCurrentLocation = latLng;
            googleMap.clear();
            new MapHelper().setMarker(myCurrentLocation, googleMap,"My new Marker Location");
        }
    };

    private GoogleMap.OnMarkerDragListener onMarkerDragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            myCurrentLocation = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        }
    };

    private GoogleMap.OnMarkerClickListener onMarkerClickListener = marker -> {
        showDialog(marker);
        return true;
    };

    private void showDialog(Marker marker) {
        MessageDialogFragment messageDialogFragment = new MessageDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("message", " CountryName is: " + new MapHelper().getLocationName(marker, getContext()).get(0).getCountryName());
        messageDialogFragment.setArguments(bundle);
        messageDialogFragment.show(getFragmentManager(), "TAG");
    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            Toast.makeText(getContext(), "MyLocation button clicked", LENGTH_SHORT).show();
            return false;
        }
    };
    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener = new GoogleMap.OnMyLocationClickListener() {
        @Override
        public void onMyLocationClick(@NonNull Location location) {
            Toast.makeText(getContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();

        }
    };

    @OnClick(R.id.button)
    void onClick() {
    }
}