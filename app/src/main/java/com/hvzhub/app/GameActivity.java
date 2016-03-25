package com.hvzhub.app;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.hvzhub.app.DB.DB;
import com.hvzhub.app.Prefs.ChatPrefs;
import com.hvzhub.app.Prefs.GCMRegistationPrefs;
import com.hvzhub.app.Prefs.GamePrefs;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnLogoutListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GameActivity";
    public static final String ARG_FRAGMENT_NAME = "fragmentName";


    private ModUpdatesFragment modUpdatesFragment;
    public static final int MOD_UPDATES_FRAGMENT = 1;
    private GameNewsFragment gameNewsFragment;
    public static final int GAME_NEWS_FRAGMENT = 2;
    private ChatFragment chatFragment;
    public static final int CHAT_FRAGMENT = 3;
    private MyCodeFragment myCodeFragment;
    public static final int MY_CODE_FRAGMENT = 4;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    public int curTab;

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

        // Open the initial fragment
        int fragmentToOpen;
        if (savedInstanceState != null) {
            fragmentToOpen = savedInstanceState.getInt(ARG_FRAGMENT_NAME);
        } else if (getIntent() != null && getIntent().getExtras() != null) {
            fragmentToOpen = getIntent().getExtras().getInt(ARG_FRAGMENT_NAME);
        } else {
            fragmentToOpen = MOD_UPDATES_FRAGMENT;
        }
        switch (fragmentToOpen) {
            case CHAT_FRAGMENT:
                switchToTab(R.id.nav_chat);
                break;
            case MOD_UPDATES_FRAGMENT:
                switchToTab(R.id.nav_mod_updates);
                break;
            case GAME_NEWS_FRAGMENT:
                switchToTab(R.id.nav_game_news);
                break;
            case MY_CODE_FRAGMENT:
                switchToTab(R.id.nav_my_code);
                break;
            default:
                switchToTab(R.id.nav_mod_updates);
                break;
        }


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(GCMRegistationPrefs.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.i(TAG, "Token retrieved and sent to server!");
                } else {
                    Log.i(TAG, "An error occurred while either fetching the InstanceID token, sending the fetched token to the server or subscribing to the PubSub topic.");
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            updateNotificationSubscriptions();
        }
    }

    public void updateNotificationSubscriptions() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        SharedPreferences prefs = getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        if (gameId == -1) {
            onLogout();
            return;
        }
        boolean isHuman = prefs.getBoolean(GamePrefs.PREFS_IS_HUMAN, false);
        boolean isAdmin = prefs.getBoolean(GamePrefs.PREFS_IS_HUMAN, false);

        if (isAdmin) {
            String[] topics = {
                    String.format("games_%d_chat_human", gameId),
                    String.format("games_%d_chat_zombie", gameId)
            };
            intent.putExtra(RegistrationIntentService.ARG_TO_SUBRCRIBE, topics);
        } else {
            String[] toSubscribe = {
                    String.format(
                            "games_%d_chat_%s",
                            gameId,
                            isHuman ? "human" : "zombie"
                    ),
            };
            intent.putExtra(RegistrationIntentService.ARG_TO_SUBRCRIBE, toSubscribe);

            // Make sure to unsubscribe from the other side's chat
            String[] toUnsubscribe = {
                    String.format(
                            "games_%d_chat_%s",
                            gameId,
                            isHuman ? "zombie" : "human"
                    ),
            };
            intent.putExtra(RegistrationIntentService.ARG_TO_UNSUBRCRIBE, toUnsubscribe);
        }


        // Start IntentService to register this application with GCM.
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(GCMRegistationPrefs.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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
        Intent i;
        Fragment toSwitch = null;
        switch (id) {
            case R.id.nav_mod_updates:
                if (modUpdatesFragment == null) {
                    modUpdatesFragment = ModUpdatesFragment.newInstance();
                }
                toSwitch = modUpdatesFragment;
                curTab = MOD_UPDATES_FRAGMENT;
                break;
            case R.id.nav_game_news:
                if (gameNewsFragment == null) {
                    gameNewsFragment = GameNewsFragment.newInstance();
                }
                toSwitch = gameNewsFragment;
                curTab = GAME_NEWS_FRAGMENT;
                break;
            case R.id.nav_chat:
                if (chatFragment == null) {
                    chatFragment = ChatFragment.newInstance();
                }
                toSwitch = chatFragment;
                curTab = CHAT_FRAGMENT;
                break;
            case R.id.nav_report_tag:
                break;
            case R.id.nav_heatmap:
                i = new Intent(this, HeatMapActivity.class);
                startActivity(i);
                break;
            case R.id.nav_my_code:
                if (myCodeFragment == null) {
                    myCodeFragment = MyCodeFragment.newInstance();
                }
                toSwitch = myCodeFragment;
                curTab = MY_CODE_FRAGMENT;
                break;
            case R.id.nav_settings:
                i = new Intent(GameActivity.this, SettingsActivity.class);
                i.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.NotificationPreferenceFragment.class.getName());
                i.putExtra(SettingsActivity.EXTRA_NO_HEADERS, true);
                startActivity(i);
                break;
            case R.id.nav_logout:
                onLogout();
                break;
        }
        if (toSwitch != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, toSwitch)
                    .addToBackStack(null)
                    .commit();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onLogout() {
        // Clear *all* GamePrefs
        SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

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
        DB.getInstance(this).wipeDatabase();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_FRAGMENT_NAME, curTab);
        super.onSaveInstanceState(outState);
    }
}
