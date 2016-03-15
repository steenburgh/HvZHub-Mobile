package com.hvzhub.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.hvzhub.app.API.API;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnLogoutListener{

    private ChatFragment chatFragment;
    private NewsFragment newsFragment;
    private MyCodeFragment myCodeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        switchToTab(R.id.nav_news); // Open the default tab
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switchToTab(item.getItemId());
        return true;
    }

    public void switchToTab(int id) {
        Fragment toSwitch = null;
        switch (id) {
            case R.id.nav_news:
                if (newsFragment == null) {
                    newsFragment = NewsFragment.newInstance(null, null);
                }
                toSwitch = newsFragment;
                break;
            case R.id.nav_chat:
                if (chatFragment == null) {
                    chatFragment = ChatFragment.newInstance(null, null);
                }
                toSwitch = chatFragment;
                break;
            case R.id.nav_report_tag:
                break;
            case R.id.nav_heatmap:
                Intent i = new Intent(this, HeatMapActivity.class);
                startActivity(i);
                break;
            case R.id.nav_my_code:
                if (myCodeFragment == null) {
                    myCodeFragment = MyCodeFragment.newInstance();
                }
                toSwitch = myCodeFragment;
                break;
            case R.id.nav_settings:
                break;
        }
        if (toSwitch != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, toSwitch)
                    .addToBackStack(null)
                    .commit();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

            // Ensure that the action bar is visible.
            // It might have been hidden by scrolling in a NestedScrollView like in NewsFragment.
            AppBarLayout abl = (AppBarLayout) findViewById(R.id.appbar_layout);
            abl.setExpanded(true, true);
        }
    }

    @Override
    public void onLogout() {
        SharedPreferences.Editor prefs = getSharedPreferences(API.PREFS_API, Context.MODE_PRIVATE).edit();
        prefs.putString(API.PREFS_SESSION_ID, null);
        prefs.apply();

        // Notify the user
        Toast t = Toast.makeText(
                this,
                R.string.you_have_been_logged_out,
                Toast.LENGTH_LONG
        );
        t.show();

        // Load the login screen
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
