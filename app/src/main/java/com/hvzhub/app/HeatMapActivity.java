package com.hvzhub.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class HeatMapActivity extends Activity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHeatMap();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng madison = new LatLng(43.077563837, -89.4162797898);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(madison));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(11);

        mMap.animateCamera(zoom);

    }

    public void addHeatMap() {

        // Create the gradient.
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.3f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        double pointY[]={43.077563837,44.4322019105, 43.076529389, 43.0761610585, 43.1041321932, 43.0775560003, 43.0763569793, 43.0706182023, 43.0758828501, 43.0722581876, 43.0732613589, 43.0734181029, 43.0765685729, 43.0750482171, 43.0768428599, 43.0763491424, 43.0760689756, 43.0749463362, 43.0739823781, 43.0777832631, 43.0750952389, 43.0775677553, 43.0755537004, 43.0775187762, 43.0746328555, 43.072121034, 43.0724149342, 43.0779909335, 43.077599102, 43.0766469407, 43.0775560003, 43.0751540162, 43.0761140375, 43.0775324903, 43.076278611, 43.0751814456, 43.0764980418, 43.0751226683, 43.0747817591, 43.0769212274, 43.0761336296, 43.0774306136, 43.071482286, 43.0751383423, 43.0764431842, 43.0750952389, 43.074288025, 43.0714156677, 43.0717565958, 43.0773718385, 43.0733554053, 43.0762707742, 43.0773052266, 43.0712549999, 43.0742174912, 43.0733671611, 43.0719760427};
        double pointX[]={-89.4162797898, -120.563346145, -89.4068598717, -89.406183955, -89.4068920582, -89.4168269604, -89.4097673863, -89.4076833099, -89.4050091475, -89.4051754445, -89.4058835476, -89.4011843174, -89.4118487805, -89.4064199894, -89.4111728638, -89.4103574723, -89.4058889121, -89.4063878029, -89.400562045, -89.4146798521, -89.4024503201, -89.4168162316, -89.4120418995, -89.4167920917, -89.4005084008, -89.4061893195, -89.4056475132, -89.4167679518, -89.4155824154, -89.4103682011, -89.4167840451, -89.4057440728, -89.4056689709, -89.4167920917, -89.3886691302, -89.4061893195, -89.415872094, -89.4061785906, -89.4010448426, -89.4097673863, -89.4058567255, -89.4127714604, -89.3990063637, -89.4067686766, -89.4130825966, -89.4056689709, -89.4014847249, -89.4005459517, -89.3991672963, -89.4160839885, -89.4027399987, -89.4061356753, -89.4144666165, -89.406645295, -89.4005405873, -89.4073614448, -89.3939477175};

        List<LatLng> list = new ArrayList<>();
        for (int i = 0 ; i < pointX.length; i++){
            list.add(new LatLng(pointX[i],pointY[i]));
        }
        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .opacity(0.8)
                .gradient(gradient)
                .radius(40)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        TileOverlayOptions HeatMapOptions = new TileOverlayOptions().tileProvider(mProvider);
        mOverlay = mMap.addTileOverlay(HeatMapOptions);
        mOverlay.clearTileCache();
    }
}
