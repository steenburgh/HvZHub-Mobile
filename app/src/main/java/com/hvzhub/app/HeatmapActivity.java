package com.hvzhub.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hvzhub.app.Prefs.TagLocationPref;

public class HeatmapActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap tagMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button quit = (Button) findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             finish();
                                         }
                                     }

        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        tagMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng bascom = new LatLng(43.075299, -89.40337299999999);

        tagMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        tagMap.moveCamera(CameraUpdateFactory.newLatLng(bascom));

    }
}