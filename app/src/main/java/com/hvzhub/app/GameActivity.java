package com.hvzhub.app;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ChatFragment cf;
    private NewsFragment nf;

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
        switch(id) {
            case R.id.nav_news:
                if (nf == null) {
                    nf = NewsFragment.newInstance(null, null);
                }
                toSwitch = nf;
                break;
            case R.id.nav_chat:
                if (cf == null) {
                    cf = ChatFragment.newInstance(null, null);
                }
                toSwitch = cf;
                break;
            case R.id.nav_report_tag:
                break;
            case R.id.nav_heatmap:
                break;
            case R.id.nav_my_code:
                break;
            case R.id.nav_settings:
                break;
        }

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
