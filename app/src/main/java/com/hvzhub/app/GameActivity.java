package com.hvzhub.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.hvzhub.app.API.API;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.Games.Record;
import com.hvzhub.app.API.model.Games.RecordContainer;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Config.FeedbackConfig;
import com.hvzhub.app.DB.DB;
import com.hvzhub.app.Prefs.GCMRegistationPrefs;
import com.hvzhub.app.Prefs.GamePrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnLogoutListener,
        OnRefreshIsHumanListener {

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
    private ReportTagFragment reportTagFragment;
    private static final int REPORT_TAG_FRAGMENT = 5;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private SharedPreferences.OnSharedPreferenceChangeListener gamePrefsListener;
    private boolean isReceiverRegistered;

    public int curTab;
    public TextView zedNum;
    public TextView humanNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        OnRefreshIsHuman(new OnIsHumanRefreshedListener() {
            @Override
            public void OnIsHumanRefreshed() {
                // Do nothing
            }
        });

        setContentView(R.layout.activity_game);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navView= (NavigationView) drawer.findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0);
        zedNum = (TextView) header.findViewById(R.id.zedcount);
        humanNum = (TextView) header.findViewById(R.id.humancount);

        humanNum.setText("Humans: 50");
        zedNum.setText("Zombies: 6");

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
            fragmentToOpen = MOD_UPDATES_FRAGMENT; // default tab
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
            case REPORT_TAG_FRAGMENT:
                switchToTab(R.id.nav_report_tag);
                break;
            default:
                throw new RuntimeException("Invalid tab found");
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
        Intent intent = new Intent(this, GCMRegIntentService.class);

        // Configure the service to be in update mode
        intent.putExtra(GCMRegIntentService.CHAT_UPDATE_SUBSCRIPTIONS, true);

        // Add arguments
        SharedPreferences prefs = getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        boolean isHuman = prefs.getBoolean(GamePrefs.PREFS_IS_HUMAN, false);
        boolean isAdmin = prefs.getBoolean(GamePrefs.PREFS_IS_ADMIN, false);
        intent.putExtra(GCMRegIntentService.ARGS_GAME_ID, gameId);
        intent.putExtra(GCMRegIntentService.ARG_IS_HUMAN, isHuman);
        intent.putExtra(GCMRegIntentService.ARG_IS_ADMIN, isAdmin);

        startService(intent);
    }

    public void unsubscribeAll() {
        Intent intent = new Intent(this, GCMRegIntentService.class);
        intent.putExtra(GCMRegIntentService.CHAT_UNSUBSCRIBE_ALL, true);

        SharedPreferences prefs = getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        intent.putExtra(GCMRegIntentService.ARGS_GAME_ID, gameId);

        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();

        if (gamePrefsListener == null) {
            gamePrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(GamePrefs.PREFS_IS_HUMAN)) {
                        // TODO: Actually notify the user that this has happened
                        updateNotificationSubscriptions();

                        // Force a reload of all affected fragments
                        if (chatFragment != null) {
                            getSupportFragmentManager().beginTransaction().remove(chatFragment).commit();
                            chatFragment = null;
                            if (curTab == CHAT_FRAGMENT) {
                                Toast t = Toast.makeText(GameActivity.this, getString(R.string.you_were_just_turned_reloading_chat), Toast.LENGTH_LONG);
                                t.show();
                                switchToTab(R.id.nav_chat);
                            }
                        }
                    }
                }
            };
        }

        getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(gamePrefsListener);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(gamePrefsListener);
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
        return switchToTab(item.getItemId());
    }

    public boolean switchToTab(int id) {
        Intent i;
        Fragment toSwitch = null;
        boolean activityWasOpened = true;
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
                if (reportTagFragment == null) {
                    reportTagFragment = ReportTagFragment.newInstance();
                }
                toSwitch = reportTagFragment;
                curTab = REPORT_TAG_FRAGMENT;
                break;
            case R.id.nav_my_code:
                if (myCodeFragment == null) {
                    myCodeFragment = MyCodeFragment.newInstance();
                }
                toSwitch = myCodeFragment;
                curTab = MY_CODE_FRAGMENT;
                break;
            case R.id.nav_logout:
                onLogout();
                break;
            case R.id.nav_settings:
                i = new Intent(GameActivity.this, SettingsActivity.class);
                i.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.NotificationPreferenceFragment.class.getName());
                i.putExtra(SettingsActivity.EXTRA_NO_HEADERS, true);
                startActivity(i);
                break;
            case R.id.nav_feedback:
                String devEmail = FeedbackConfig.DEV_EMAIL;
                i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", devEmail, null));
                i.putExtra(Intent.EXTRA_SUBJECT, "HvZHub App Feedback");
                startActivity(Intent.createChooser(i, "Send Email Using: "));
                break;

        }
        if (toSwitch != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, toSwitch)
                    .addToBackStack(null)
                    .commit();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else {
            return false; // If a fragment wasn't opened, don't highlight the navigation item.
        }
    }

    @Override
    public void onLogout() {
        // Clear notification subscriptions
        unsubscribeAll();

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

    /**
     * Check if the player is a human or zombie, and update GamePrefs.PREFS_IS_HUMAN
     */
    @Override
    public void OnRefreshIsHuman(final OnIsHumanRefreshedListener listener) {
        HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
        SharedPreferences prefs = getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE);
        String uuid = prefs.getString(GamePrefs.PREFS_SESSION_ID, null);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        Call<RecordContainer> call = client.getMyRecord(new Uuid(uuid), gameId);
        call.enqueue(new Callback<RecordContainer>() {
            @Override
            public void onResponse(Call<RecordContainer> call, Response<RecordContainer> response) {
                if (response.isSuccessful()) {
                    Record r = response.body().record;
                    SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE).edit();
                    editor.putBoolean(GamePrefs.PREFS_IS_HUMAN, r.status == Record.HUMAN);
                    editor.apply();
                    Log.d(TAG, String.format("Status updated: status = %d", r.status));
                    listener.OnIsHumanRefreshed();
                } else {
                    AlertDialog.Builder b = new AlertDialog.Builder(GameActivity.this);
                    b.setTitle(getString(R.string.unexpected_response))
                            .setMessage(getString(R.string.unexpected_response_hint))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(Call<RecordContainer> call, Throwable t) {
                AlertDialog.Builder b = new AlertDialog.Builder(GameActivity.this);
                b.setTitle(getString(R.string.generic_connection_error))
                        .setMessage(getString(R.string.generic_connection_error_hint))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_FRAGMENT_NAME, curTab);
        super.onSaveInstanceState(outState);
    }
}
