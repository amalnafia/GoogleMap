package com.example.googlemap.ui;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.googlemap.R;
import com.example.googlemap.helper.MapHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FusedLocationFragment extends Fragment implements OnMapReadyCallback {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location myCurrentLocation;

    @Nullable
    @BindView(R.id.search_view_map)
    SearchView searchView;
    private String locationSearch;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initGoogleMap();
        searchLocation(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fused_location, container, false);
        getFusedLocation();
        createLocationRequest();
        locationCallBack();
        return view;
    }

    private void initGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.fused_map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    private void getFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        myCurrentLocation = location;
                        Toast.makeText(getContext(), "Last Location \n" + location.getLatitude() + "  " + location.getLongitude(), Toast.LENGTH_LONG).show();
                        new MapHelper().setMarker(new LatLng(location.getLatitude(), location.getLongitude()), googleMap, "Last Known Location");
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                    }
                });
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        ///////////////
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(locationSettingsResponse -> Toast.makeText(getContext(), "locationSettingsResponse \n"
                        + locationSettingsResponse.getLocationSettingsStates().isNetworkLocationPresent()
                , Toast.LENGTH_LONG).show());
    }

    private void locationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
//                for (Location location : locationResult.getLocations()) {
//                    if (location != null) {
//                        myCurrentLocation = location;
//
//                        if (fusedLocationClient != null) {
//                            fusedLocationClient.removeLocationUpdates(locationCallback);
//                        }
//                    }
//                }
                Log.d("TAG", "onLocationResult: " + locationResult.getLastLocation().getLongitude() + "," + locationResult.getLastLocation().getLatitude());
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFusedLocation();
                }
            }
        }
    }

    public void searchLocation(View view) {
        searchView.setOnQueryTextListener(onQueryTextListener);

    }

    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            locationSearch = query;
            submitSearch();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            locationSearch = newText;
            return true;
        }
    };

    private void submitSearch() {
        List<Address> addressList = null;
        Geocoder geocoder = new Geocoder(getContext());
        if (locationSearch != null) {
            try {
                addressList = geocoder.getFromLocationName(locationSearch, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address addressSearch = addressList.get(0);
            LatLng latLng = new LatLng(addressSearch.getLatitude(), addressSearch.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(latLng).title(locationSearch));
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            Toast.makeText(getContext(), "Found Your Search Location", Toast.LENGTH_LONG).show();
        }
    }
}