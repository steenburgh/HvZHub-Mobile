package com.hvzhub.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.hvzhub.app.Prefs.GamePrefs;
import com.hvzhub.app.Prefs.TagLocationPref;

public class TagMapActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap tagMap;
    Marker tag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button reportTag = (Button) findViewById(R.id.reportT);
        reportTag.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             LatLng tagPos = tag.getPosition();
                                             SharedPreferences.Editor prefs = getSharedPreferences(TagLocationPref.NAME, Context.MODE_PRIVATE).edit();
                                             prefs.putString(TagLocationPref.Latitude, String.valueOf(tagPos.latitude));
                                             prefs.putString(TagLocationPref.Longitude, String.valueOf(tagPos.longitude));
                                             prefs.apply();
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
        tag = tagMap.addMarker(new MarkerOptions()
                        .position(bascom)
                        .title("Tag Marker")
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hand))
        );
        tagMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });
        tagMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        tagMap.moveCamera(CameraUpdateFactory.newLatLng(bascom));

    }
}
