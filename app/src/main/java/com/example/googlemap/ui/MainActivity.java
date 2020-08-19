package com.example.googlemap.ui;


import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.googlemap.R;


public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().add(R.id.container, new FusedLocationFragment()).commit();
    }
}