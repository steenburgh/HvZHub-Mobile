package com.hvzhub.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.hvzhub.app.Prefs.GamePrefs;
import com.hvzhub.app.Prefs.TagLocationPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class HeatmapActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap heatMap;
    HeatmapTileProvider mProvider;
    TileOverlay mOverlay;
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
        heatMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng bascom = new LatLng(43.075299, -89.40337299999999);

        heatMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        heatMap.moveCamera(CameraUpdateFactory.newLatLng(bascom));
        addHeatMap();

    }

    private void addHeatMap() {
        ArrayList<Double> lats = new ArrayList<>();
        ArrayList<Double> longs = new ArrayList<>();
        lats.add(43.125299);
        longs.add(-89.40337299999999);
        lats.add(43.095299);
        longs.add(-89.40357299999999);
        lats.add(43.075299);
        longs.add(-89.40207299999999);

        ArrayList<LatLng> list = new ArrayList<LatLng>();
        for (int i = 0; i < lats.size(); i++){
            list.add(new LatLng(lats.get(i), longs.get(i)));
        }

        int gameId = getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE).getInt(GamePrefs.PREFS_GAME_ID, -1);
        String chapterUrl = getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE).getString(GamePrefs.PREFS_CHAPTER_URL, null);

        //try {
        //    list = readItems(gameId, chapterUrl);
        //} catch (JSONException e) {
        //   Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
        //}

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

    private ArrayList<LatLng> readItems(int gameId, String chapterUrl) throws JSONException {

        //for testing purposes. change path to reflect game id and chapter url
        String path = "hvzhub.com/heatmap.json";

        //need to query website for json of tags
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        try{
            JSONObject json = getJSONFromUrl(path);

        // get the array of users
            JSONArray dataJsonArr = json.getJSONArray("Tags");

        // loop through all users
            for (int i = 0; i < dataJsonArr.length(); i++) {

                JSONObject c = dataJsonArr.getJSONObject(i);

            // Storing each json item in variable
                Double lat = c.getDouble("lat");
                Double lon = c.getDouble("lon");

                list.add(new LatLng(lat, lon));
            }

        } catch (JSONException e) {
        e.printStackTrace();
        }
        return list;
    }

    public JSONObject getJSONFromUrl(String url) {

        JSONObject jObj = null;
        String json = "";

        StringBuilder jsonFile = new StringBuilder();

        try {
            URL urlReq = new URL(url);

            // make HTTP request
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) urlReq.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonFile.append(line);
                    }
                    json = jsonFile.toString();

                } finally {
                    urlConnection.disconnect();
                }

                // try parse the string to a JSON object
                try {
                    jObj = new JSONObject(json);
                } catch (JSONException e) {

                }
            }catch (IOException e){

            }
        } catch (MalformedURLException e) {

        }

        // return JSON Object
        return jObj;
    }
}