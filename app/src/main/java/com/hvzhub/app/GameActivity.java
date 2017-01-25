package com.hvzhub.app;

import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
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
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
        OnRefreshIsHumanListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "GameActivity";
    public static final String ARG_FRAGMENT_NAME = "fragmentName";

    public static final String TAG_MOD_UPDATES_FRAGMENT = "modUpdates";
    public static final String TAG_GAME_NEWS_FRAGMENT = "gameNews";
    public static final String TAG_CHAT_FRAGMENT = "chat";
    public static final String TAG_MY_CODE_FRAGMENT = "myCode";
    private static final String TAG_REPORT_TAG_FRAGMENT = "reportTag";
    public static final String TAG_HOME_FRAGMENT = "home";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private SharedPreferences.OnSharedPreferenceChangeListener gamePrefsListener;
    private boolean isReceiverRegistered;

    public String curTab;
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Open the initial fragment
        String fragmentToOpen;
        if (savedInstanceState != null) {
            fragmentToOpen = savedInstanceState.getString(ARG_FRAGMENT_NAME);
        } else if (getIntent() != null && getIntent().getExtras() != null) {
            fragmentToOpen = getIntent().getExtras().getString(ARG_FRAGMENT_NAME);
        } else {
            fragmentToOpen = TAG_HOME_FRAGMENT; // default tab
        }
        switchToTab(getNavIdFromArgument(fragmentToOpen));


        // Keep curTab up to date
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                curTab = f.getTag();
                navigationView.setCheckedItem(getNavIdFromArgument(curTab));
            }
        });


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                sharedPreferences
                        .getBoolean(GCMRegistationPrefs.SENT_TOKEN_TO_SERVER, false);
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
        SharedPreferences prefs = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        boolean isHuman = prefs.getBoolean(GamePrefs.PREFS_IS_HUMAN, false);
        boolean isAdmin = prefs.getBoolean(GamePrefs.PREFS_IS_ADMIN, false);
        intent.putExtra(GCMRegIntentService.ARGS_GAME_ID, gameId);
        intent.putExtra(GCMRegIntentService.ARG_IS_HUMAN, isHuman);
        intent.putExtra(GCMRegIntentService.ARG_IS_ADMIN, isAdmin);

        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancelAll();
        registerReceiver();

        if (gamePrefsListener == null) {
            gamePrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(GamePrefs.PREFS_IS_HUMAN)) {
                        // TODO: Actually notify the user that this has happened
                        updateNotificationSubscriptions();

                        // Force a reload of all affected fragments
                        Fragment chatFragment = getSupportFragmentManager().findFragmentByTag(TAG_CHAT_FRAGMENT);
                        if (chatFragment != null) {
                            getSupportFragmentManager().beginTransaction().remove(chatFragment).commit();
                            // If the user was in this tab, their UI is now displaying a blank screen
                            // Explain to them what happened and reload the UI
                            if (curTab.equals(TAG_CHAT_FRAGMENT)) {
                                Toast t = Toast.makeText(GameActivity.this, getString(R.string.you_were_just_turned_reloading_chat), Toast.LENGTH_LONG);
                                t.show();
                                switchToTab(R.id.nav_chat);
                            }
                        }
                    }
                }
            };
        }

        getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(gamePrefsListener);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE)
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
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle(getString(R.string.quit))
                    .setMessage(getString(R.string.are_you_sure_you_want_to_quit))
                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        return switchToTab(item.getItemId());
    }

    public int getNavIdFromArgument(String tabArgument) {
        switch (tabArgument) {
            case TAG_HOME_FRAGMENT:
                return R.id.nav_home;
            case TAG_CHAT_FRAGMENT:
                return R.id.nav_chat;
            case TAG_MOD_UPDATES_FRAGMENT:
                return R.id.nav_mod_updates;
            case TAG_GAME_NEWS_FRAGMENT:
                return R.id.nav_game_news;
            case TAG_MY_CODE_FRAGMENT:
                return R.id.nav_my_code;
            case TAG_REPORT_TAG_FRAGMENT:
                return R.id.nav_report_tag;
            default:
                throw new RuntimeException("Invalid tab found");
        }
    }

    public boolean switchToTab(int id) {
        Intent i;
        Fragment toSwitch = null;
        switch (id) {
            case R.id.nav_home:
                toSwitch = getSupportFragmentManager().findFragmentByTag(TAG_HOME_FRAGMENT);
                if (toSwitch == null) {
                    toSwitch = HomeFragment.newInstance();
                }
                curTab = TAG_HOME_FRAGMENT;
                break;
            case R.id.nav_mod_updates:
                toSwitch = getSupportFragmentManager().findFragmentByTag(TAG_MOD_UPDATES_FRAGMENT);
                if (toSwitch == null) {
                    toSwitch = ModUpdatesFragment.newInstance();
                }
                curTab = TAG_MOD_UPDATES_FRAGMENT;
                break;
            case R.id.nav_game_news:
                toSwitch = getSupportFragmentManager().findFragmentByTag(TAG_GAME_NEWS_FRAGMENT);
                if (toSwitch == null) {
                    toSwitch = GameNewsFragment.newInstance();
                }
                curTab = TAG_GAME_NEWS_FRAGMENT;
                break;
            case R.id.nav_chat:
                toSwitch = getSupportFragmentManager().findFragmentByTag(TAG_CHAT_FRAGMENT);
                if (toSwitch == null) {
                    toSwitch = ChatFragment.newInstance();
                }
                curTab = TAG_CHAT_FRAGMENT;
                break;
            case R.id.nav_report_tag:
                toSwitch = getSupportFragmentManager().findFragmentByTag(TAG_REPORT_TAG_FRAGMENT);
                if (toSwitch == null) {
                    toSwitch = ReportTagFragment.newInstance();
                }
                curTab = TAG_REPORT_TAG_FRAGMENT;
                break;
            case R.id.nav_my_code:
                toSwitch = getSupportFragmentManager().findFragmentByTag(TAG_MY_CODE_FRAGMENT);
                if (toSwitch == null) {
                    toSwitch = MyCodeFragment.newInstance();
                }
                curTab = TAG_MY_CODE_FRAGMENT;
                break;
            case R.id.nav_heatmap:
                i = new Intent(GameActivity.this, HeatmapActivity.class);
                startActivity(i);
                break;
            case R.id.nav_logout:
                SessionManager.getInstance().logout();
                break;
            case R.id.nav_settings:
                i = new Intent(GameActivity.this, SettingsActivity.class);
                i.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.NotificationPreferenceFragment.class.getName());
                i.putExtra(SettingsActivity.EXTRA_NO_HEADERS, true);
                startActivity(i);
                break;
            case R.id.nav_feedback:
                i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", FeedbackConfig.DEV_EMAIL, null));
                i.putExtra(Intent.EXTRA_SUBJECT, "HvZHub App Feedback");
                startActivity(Intent.createChooser(i, "Send Email Using: "));
                break;


        }
        if (toSwitch != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, toSwitch, curTab)
                    .addToBackStack(null)
                    .commit();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else {
            return false; // If a fragment wasn't opened, don't highlight the navigation item.
        }
    }

    /**
     * Check if the player is a human or zombie, and update GamePrefs.PREFS_IS_HUMAN
     */
    @Override
    public void OnRefreshIsHuman(final OnIsHumanRefreshedListener listener) {
        HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
        SharedPreferences prefs = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        Call<RecordContainer> call = client.getMyRecord(
                SessionManager.getInstance().getSessionUUID(),
                gameId
        );
        call.enqueue(new Callback<RecordContainer>() {
            @Override
            public void onResponse(Call<RecordContainer> call, Response<RecordContainer> response) {
                if (response.isSuccessful()) {
                    Record r = response.body().record;
                    SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).edit();
                    editor.putBoolean(GamePrefs.PREFS_IS_HUMAN, r.status == Record.HUMAN);
                    editor.apply();
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
        outState.putString(ARG_FRAGMENT_NAME, curTab);
        super.onSaveInstanceState(outState);
    }
}
