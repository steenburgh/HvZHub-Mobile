package com.hvzhub.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.hvzhub.app.API.API;
import com.hvzhub.app.API.ErrorUtils;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.APIError;
import com.hvzhub.app.API.model.Games.HeatmapTagContainer;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.GamePrefs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HeatmapActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap heatMap;
    HeatmapTileProvider mProvider;
    TileOverlay mOverlay;
    ArrayList<LatLng> tagList = new ArrayList<>();

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
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        heatMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng bascom = new LatLng(43.075299, -89.40337299999999);

        heatMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        heatMap.moveCamera(CameraUpdateFactory.newLatLng(bascom));
        loadData();
    }

    private void loadData() {

        int gameId = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).getInt(GamePrefs.PREFS_GAME_ID, -1);
        String uuid = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).getString(GamePrefs.PREFS_SESSION_ID, null);

        HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
        Call<HeatmapTagContainer> call = client.getHeatmap(new Uuid(uuid), gameId);
        call.enqueue(new Callback<HeatmapTagContainer>() {
            @Override
            public void onResponse(Call<HeatmapTagContainer> call, Response<HeatmapTagContainer> response) {
                //TODO
                if (response.isSuccessful()) {
                    for (int i = 0; i < response.body().tags.size(); i++) {
                        tagList.add(new LatLng(response.body().tags.get(i).lat, response.body().tags.get(i).lon));
                    }

                    addHeatMap(tagList);

                } else {

                    APIError apiError = ErrorUtils.parseError(response);
                    String err = apiError.error.toLowerCase();
                    if (err.contains(getString(R.string.invalid_session_id))) {
                        // Notify the parent activity that the user should be logged out
                        // Don't bother stopping the loading animation
                        Toast.makeText(getApplicationContext(), "Invalid Session ID. Logging Out...", Toast.LENGTH_SHORT);
                        logout();
                    } else {
                        AlertDialog.Builder b = new AlertDialog.Builder(HeatmapActivity.this);
                        b.setTitle(getString(R.string.unexpected_response))
                                .setMessage(getString(R.string.unexpected_response_hint))
                                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                }
            }

            @Override
            public void onFailure(Call<HeatmapTagContainer> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error Loading Game Tags. Alert HVZ Hub dev team.", Toast.LENGTH_SHORT);
                //TODO
            }
        });


    }

    private void addHeatMap(List<LatLng> list) {

//        ArrayList<LatLng> list = new ArrayList<>();
//        list.add(new LatLng(43.075299, -89.40337299999999));

        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);
        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .gradient(gradient)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = heatMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        mOverlay.clearTileCache();

    }


    private void logout() {
        // Clear *all* GamePrefs
        SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Show the login screen again
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}